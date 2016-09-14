package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.HLogAgentConst;
import com.asiainfo.hlog.agent.jvm.HLogJvmReport;
import com.asiainfo.hlog.agent.runtime.dto.TranCostDto;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.LogData;
import com.asiainfo.hlog.org.objectweb.asm.Type;
import java.io.File;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.asiainfo.hlog.agent.runtime.RuntimeContext.enable;
import static com.asiainfo.hlog.agent.runtime.RuntimeContext.writeEvent;

/**
 * <p>运行时日志采集监控工具类：</p>
 * <p>1、采集运行过程耗时日志 start+end,包括异常;</p>
 * <p>2、采集第三方日志框架输出日志 logger;</p>
 * <p>3、sql执行耗时日志;</p>
 * <p>4、拦截方法入参日志;</p>
 * Created by chenfeng on 2016/4/20.
 */
public class HLogMonitor {

    private static Set<String> excludeParamTypes = new HashSet<String>();
    private static Set<String> excludeParamTypePaths = new HashSet<String>();
    private static WeakHashMap<Object,String> loopMonitorMap = new WeakHashMap<Object,String>();
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private static long configFileModifyTime = -1;
    private static long extConfigFileModifyTime = -1;


    static {
        excludeParamTypes.add("javax.servlet.http.HttpServletResponse");
        excludeParamTypes.add("javax.servlet.http.HttpServletRequest");
        excludeParamTypes.add("javax.servlet.http.HttpSession");
        excludeParamTypePaths.add("javax.");
        excludeParamTypePaths.add("org.springframework");
        excludeParamTypePaths.add("org.apache");
        excludeParamTypePaths.add("org.jdom");
        excludeParamTypePaths.add("com.fasterxml");
        excludeParamTypePaths.add("org.mybatis");
        excludeParamTypePaths.add("com.fasterxml");

        //jvm信息监控
        String enableMonitor = HLogConfig.getInstance().getProperty(Constants.KEY_ENABLE_MONITOR_JVM, "true");
        if ("true".equals(enableMonitor.toLowerCase())) {
            HLogJvmReport.getInstance().start();
        }
        //循环监控
        String enableLoopMonitor = HLogConfig.getInstance().getProperty(Constants.KEY_ENABLE_MONITOR_LOOP, "true");
        if ("true".equals(enableLoopMonitor.toLowerCase())) {
            startLoopMonitor();
        }
        startConfigReloadTask();
    }

    static final class Node{
        private String logId;
        private String logPid;
        private String logGid;
        private final String className;
        private final String methodName;
        private final String description;
        private final String[] paramNames;
        private final SoftReference<Object>[]  params;
        //private final Object[] params;
        private final long beginTime;
        private long speed = 0;
        private int isError = 0;
        private boolean enableProcess;
        private boolean enableError ;
        private boolean leaf = true;
        private boolean sql = false;

        private SoftReference<Throwable> rootThrowable ;

        public Node(){
            logId = null;
            logPid = null;
            logGid = null;
            className = null;
            methodName = null;
            description = null;
            paramNames = null;
            params = null;
            beginTime = 0;
        }

        public Node(String logId,String logPid,String className, String methodName, String description, String[] paramNames, SoftReference[] params) {
            this.logId = logId;
            this.logPid = logPid;
            this.className = className;
            this.methodName = methodName;
            this.description = description;
            this.paramNames = paramNames;
            this.params = params;
            beginTime = System.currentTimeMillis();
        }
        public boolean isEmpty(){
            return className == null;
        }
    }
    private static ThreadLocal<Stack<Node>> local = new ThreadLocal<Stack<Node>>();
    private static ThreadLocal<Stack<Node>> subNodes = new ThreadLocal<Stack<Node>>();

    private static HLogConfig config = HLogConfig.getInstance();

    public static void start(boolean enableProcess,boolean enableError,String className,String methodName,String desc,String[] paramNames,Object[] params){

        //enable(HLogAgentConst.MV_CODE_PROCESS, className,methodName);
        //enable(HLogAgentConst.MV_CODE_ERROR, className,methodName);

        if(!enableProcess && !enableError){
            Node node = new Node();
            node.enableProcess = enableProcess;
            node.enableError = enableError;
            pushNode(node);
            return ;
        }

        SoftReference[] softReferences = null;
        if(params!=null){
            softReferences = new SoftReference[params.length];
            for (int i = 0; i < params.length; i++) {
                softReferences[i] =  new SoftReference(params[i]);
            }
        }
        String id = null;
        String pid;
        if(enableProcess){
            id = LogUtil.logId();
            pid = RuntimeContext.buildLogPId(id);
        }else{
            pid = RuntimeContext.getLogId();
        }
        Node node = new Node(id,pid,className,methodName,desc,paramNames,softReferences);
        node.enableProcess = enableProcess;
        node.enableError = enableError;
        pushNode(node);
    }

    private static void pushNode(Node node) {
        Stack<Node> stack = local.get();
        if(stack == null){
            stack = new Stack<Node>();
            local.set(stack);
        }
        if(!stack.isEmpty()){
            stack.lastElement().leaf = false;
        }

        String gId = LogAgentContext.getThreadLogGroupId();
        if(gId==null){
            gId=node.logPid!=null?node.logPid:node.logId;
        }
        node.logGid = gId;
        stack.push(node);
        //添加循环监控
        addLoopMonitor(node);
    }
    private static void checkNodeId(Node node){
        if(node!=null && node.logId==null){
            synchronized (node){
                if(node.logId==null){
                    node.logId = LogUtil.logId();
                }
            }
        }
    }
    private static void pushSubNode(Node node,long ptime){
        Stack<Node> stack = subNodes.get();
        if(stack == null){
            stack = new Stack<Node>();
            subNodes.set(stack);
        }
        //避免一些死循环大量堆积子节点数据不处理
        //这里折中处理,如果耗时设置400,估算一个子节点大概耗时4,那么到达100个子节点会被先处理
        if(stack.size()>(ptime>>2)){
            doWriteSubNode();
        }
        stack.push(node);
    }

    public static void end(String mcode,Object returnObj,boolean isError){
        //local.get().pop();
        Stack<Node> stack = null;
        String pid = null;
        boolean enableProcess = false;
        try{
            stack = local.get();
            if(stack.isEmpty()){
                clear();
                return ;
            }
            Node node = stack.pop();

            //移除循环监控
            removeLoopMonitor(node);

            if(node.isEmpty()){
                return ;
            }
            node.speed = System.currentTimeMillis() - node.beginTime;
            //如果产生多个日志,这里的logId保持一致
            //String id = node.logId ;
            pid = node.logPid;
            node.isError = isError?1:0;
            boolean havWriteLog = false;
            boolean enableSaveWithoutSubs = config.isEnableSaveWithoutSubs();
            boolean isWriteErrLog = false;

            enableProcess = node.enableProcess;

            //发生异常时记录,在异常源头保存异常数据,同时将上级node设为非源头
            if(isError && node.enableError){
                Node pnode = null;
                if(!stack.isEmpty()){
                    pnode = stack.peek();
                }
                if(pnode!=null){
                    pnode.rootThrowable=new SoftReference(returnObj);
                }
                if(node.rootThrowable==null ||
                    !returnObj.equals(node.rootThrowable.get())){

                    checkNodeId(node);

                    doSendErrorLog(node, node.logId, pid, (Throwable) returnObj);
                    //如果没有开启记录该方法时,又发生异常了,就记录该方法
                    if(!enableProcess){
                        enableProcess = true;
                    }
                    havWriteLog = true;
                    isWriteErrLog = true;
                }
            }

            long ptime = config.getProcessTime();
            //耗时达到某值是记录
            if(enableProcess && node.speed>ptime){
                //记录运行耗时超过
                doSendProcessLog(node,node.logId,pid,node.isError,stack.isEmpty(),isWriteErrLog);
                //如果本节点超过预警值,追加写已经执行过的子过程节点
                if(enableSaveWithoutSubs){
                    doWriteSubNode();
                }
                if(node.speed>config.getProcessTimeWithout()){
                    havWriteLog = true;
                }
            }else if(enableSaveWithoutSubs && enableProcess && ptime>0){
                pushSubNode(node,ptime);
            }

            //保存入参
            if(havWriteLog && config.isEnableSaveWithoutParams()){
                doWriteMethodParams(node);
            }
        }catch (Throwable t){
            Logger.error("HLogMonitor end异常",t);
        }finally {
            if("nvl".equals(pid)){
                clear();
                stack.clear();
            }else{
                if(enableProcess){
                    LogAgentContext.setThreadCurrentLogId(pid);
                }
            }
            //如果上级id为null时,清了node =
            if(stack!=null && stack.isEmpty()){
                if(subNodes.get()!=null){
                    subNodes.get().clear();
                }
            }
        }

    }

    private static void clear(){
        if(!LogAgentContext.isKeepContext()){
            LogAgentContext.clear();
        }
    }

    private static void doWriteSubNode() {
        Stack<Node> subs = subNodes.get();
        if(subs!=null){
            while(!subs.isEmpty()){
                Node sub = subs.pop();
                doSendProcessLog(sub,sub.logId,sub.logPid,sub.isError,false,false);
            }
        }
    }


    private static void doWriteMethodParams(Node node){
        if(node==null){
            return;
        }
        LogData logData = createLogData(HLogAgentConst.MV_CODE_PARAMS,node.logId,node.logPid);
        if(node.paramNames==null || node.paramNames.length==0 ){
            logData.put("params","{}");
        }else{
            Type[] paramsDesc = Type.getArgumentTypes(node.description);
            String params = null;
            try{
                String[] pns = node.paramNames;
                int ilen = pns.length;
                Map<String,Object> jsonMap = new HashMap<String, Object>();
                for (int i = 0; i < ilen; i++) {
                    SoftReference<Object> p = node.params[i];
                    try{
                        //是否是排除的参数类型
                        boolean isExclude = isExcludeParamType(paramsDesc[i].getClassName());
                        if(!isExclude){
                            jsonMap.put(pns[i],p);
                        }else{
                            jsonMap.put(pns[i],"--");
                        }
                    }catch (Exception e){
                        jsonMap.put(pns[i],"ERR:"+e.getMessage());
                    }
                }
                params = RuntimeContext.toJson(jsonMap);
            }catch (Exception e){
                params = "{\"ERR\":\""+e.getMessage()+",param";
                for (SoftReference<Object> obj : node.params) {
                    if(obj!=null){
                        params = params + ":" + obj.toString();
                    }
                }
                params = params + "\"}";
            }
            logData.put("params",params);
        }

        writeEvent(node.className,node.methodName,logData);
    }

    private static boolean isExcludeParamType(String clazz) {
        for (String excludeParamType : excludeParamTypes) {
            if (excludeParamType.equals(clazz)) {
                return true;
            }
        }

        for (String excludeParamTypePath : excludeParamTypePaths) {
            if (clazz.startsWith(excludeParamTypePath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 组织和发送异常的日志数据
     * @param node
     * @param id
     * @param pid
     * @param err
     */
    private static void doSendErrorLog(Node node, String id, String pid, Throwable err) {
        LogData logData = createLogData(HLogAgentConst.MV_CODE_ERROR,id,pid);
        String errMsg = RuntimeContext.error(err);
        logData.setDesc(errMsg);
        logData.put("errCode",RuntimeContext.errorCode(err));
        writeEvent(node.className,node.methodName,logData);
    }

    /**
     * 组织和发送花费过高的日志数据
     * @param node
     * @param id
     * @param pid
     * @param status
     */
    private static void doSendProcessLog(Node node ,String id,String pid,int status,boolean isTop,boolean isWriteErrLog){
        LogData logData = createLogData(HLogAgentConst.MV_CODE_PROCESS,id,pid);
        logData.put("status",status);
        logData.put("clazz",node.className+"."+node.methodName);
        logData.put("method",node.methodName);
        logData.put("spend",node.speed);
        if(isTop || "nvl".equals(pid)){
            logData.put("isTop",1);
        }
        if(isWriteErrLog){
            logData.put("havErr",1);
        }
        //logData.put("havErr",isWriteErrLog?1:0);
        //logData.put("leaf",node.leaf?1:0);
        if(node.sql){
            logData.put("sql",1);
        }
        writeEvent(node.className,node.methodName,logData);
    }

    /**
     * 获取配置的sql耗时数据
     * @return
     */
    public static long getConfigSqlSpeed(){
        long time = config.getSqlTime();
        return time;
    }


    /**
     * 监控sql执行耗时,如果耗时超过预设的值,记录执行的sql和入参
     * @param start
     * @param className
     * @param sql
     * @param params
     */
    public static void sqlMonitor(long start,String className,String sql,String params,Object resObj) {
        if(!config.isEnableSqlTrack()){
            return ;
        }
        try{
            Node node = getCurrentNode();
            String clsName ;
            String methodName = null;
            String id = RuntimeContext.logId();
            String pid;
            if(node==null){
                pid = LogAgentContext.getThreadCurrentLogId();
                clsName = className;
            }else{
                pid = node.logId;
                clsName = node.className;
                methodName = node.methodName;
                node.sql=true;
            }
            int size = 0 ;
            LogData logData = createLogData(HLogAgentConst.MV_CODE_SQL,id,pid);
            logData.put("spend",System.currentTimeMillis()-start);
            logData.put("sql",sql);
            logData.put("params",params);
            if(resObj!=null){
                try{
                    if(resObj instanceof List){
                        size = ((List) resObj).size();
                    }else{
                        ((Integer)resObj).intValue();
                    }
                }catch (Exception e){
                    size = -1;
                }
            }
            logData.put("size",size);

            TranCostDto tc = LogAgentContext.getTranCost();
            if(tc!=null){
                tc.sqlCountIncrement();
                logData.put("tId",tc.getId());
            }

            writeEvent(clsName,methodName,logData);
        }catch (Throwable t){
            Logger.error("sqlMonitor",t);
        }

    }

    /**
     * 获取当前记录的方法node
     * @return
     */
    private static Node getCurrentNode(){
        Stack<Node> stack = local.get();
        if(stack!=null && !stack.isEmpty()){
            Node node = stack.peek();
            if(node.isEmpty()){
                return null;
            }
            checkNodeId(node);
            return node;
        }
        return null;
    }

    /**
     * 用于开源的第三方日志框架的开关增加判断,植入的开关判断会优于第三方日志框架的开关
     * @param className
     * @param level
     * @return
     */
    public static boolean isLoggerEnabled(String className,String level){
        try{
            if(!config.isEnableLoggerTrack()){
                return false;
            }
            Node node = getCurrentNode();
            String methodName = null;
            if(node!=null && node.className.equals(className)){
                methodName = node.methodName;
            }
            return enable(HLogAgentConst.MV_CODE_LOGGER,className,methodName,level);
        }catch (Throwable t){
            Logger.error("isLoggerEnabled",t);
        }
        return false;
    }

    /**
     * 采集第三方日志框架输出的内容
     * @param className
     * @param level
     * @param objects
     */
    public static void logger(String mcode,String className,String level,Object[] objects){
        if(!config.isEnableLoggerTrack()){
            return;
        }
        try{
            Node node = getCurrentNode();
            String methodName = null;
            if(node!=null && node.className.equals(className)){
                methodName = node.methodName;
            }
            String id = RuntimeContext.logId();
            String pid = LogAgentContext.getThreadCurrentLogId();
            StringBuilder buff = new StringBuilder();
            int ilen = objects.length;
            for (int i = 0; i < ilen; i++) {
                Object object = objects[i];
                if(i>0){
                    buff.append(" | ");
                }
                if(object==null){
                    continue;
                }
                if(object instanceof Throwable){
                    buff.append(RuntimeContext.error((Throwable)object));
                }else if(object.getClass().isArray()){
                    buff.append(Arrays.toString((Object[])object));
                }else{
                    buff.append(object);
                }
            }
            LogData logData = createLogData(mcode,id,pid);
            logData.setDesc(buff.toString());
            String point = className;
            if(methodName!=null){
                point = point +"."+methodName ;
            }
            logData.put("point",point);
            logData.put("level",level);
            writeEvent(className, methodName, logData);
        }catch (Throwable t){
            Logger.error("构建logger日志异常,主要入参:{0},{1}",t,className,mcode);
        }
    }

    public static void intercept(String mcode,String className,String methodName,String desc,String[] paramNames,Object[] params){
        try{
            if(enable(HLogAgentConst.MV_CODE_INTERCEPT, className, methodName)){
                doIntercept(mcode,className, methodName, paramNames, params);
            }
        }catch (Throwable t){
            Logger.error("构建拦截入参[intercept]日志异常,主要入参:{0},{1},{2},{3}",t,className,methodName,desc,params);
        }
    }
    public static void interceptRet(String mocde,String className,String methodName,String desc,String[] paramNames,Object[] params){
        try{
            if(enable(HLogAgentConst.MV_CODE_INTERCEPT_RET, className, methodName)){
                doIntercept(mocde,className, methodName, paramNames, params);
            }
        }catch (Throwable t){
            Logger.error("构建拦截入参并返回[interceptRet]日志异常,主要入参:{0},{1},{2},{3}",t,className,methodName,desc,params);
        }
    }
    private static void doIntercept(String mcode,String className, String methodName, String[] paramNames, Object[] params) {
        if(paramNames!=null && paramNames.length>0){
            String id = RuntimeContext.logId();
            String pid = LogAgentContext.getThreadCurrentLogId();
            LogData logData = createLogData(mcode, id, pid);
            logData.put("amc","ipt");
            for (int i=0;i<paramNames.length;i++) {
                String paramName = paramNames[i];
                if(logData.containsKey(paramName)){
                    paramName = paramName+"0";
                }
                logData.put(paramName,params[i]);
            }
            RuntimeContext.writeEvent(className, methodName, logData);
        }
    }

    /**
     * 创建一个LogData实例,并设置一些公共信息
     * @param mcode
     * @param id
     * @param pid
     * @return
     */
    public static LogData createLogData(String mcode, String id, String pid) {
        LogData logData = new LogData();
        logData.setMc(mcode);
        logData.setId(id);
        logData.setPId(pid);
        String gId = LogAgentContext.getThreadLogGroupId();
        if(gId==null){
            gId=pid!=null?pid:id;
        }
        logData.setGId(gId);
        logData.setTime(System.currentTimeMillis());
        return logData;
    }

    /**
     * 监控数据库事务耗时
     */
    public static void transactionCostMonitor() {
        try{
            if(!config.isEnableTransaction()){
                return;
            }
            TranCostDto dto = LogAgentContext.popTranCost();
            LogAgentContext.clearTranCostContext();
            if(dto == null){
                return ;
            }
            int index = dto.getMethodName().lastIndexOf(".");
            String clsName = dto.getMethodName().substring(0,index);
            String method = dto.getMethodName().substring(index+1);
            String pid = LogAgentContext.getThreadCurrentLogId();

            LogData logData = createLogData(HLogAgentConst.MV_CODE_TRANSACTION,dto.getId(),pid);
            logData.put("cost",dto.getCost());
            logData.put("method",method);
            logData.put("clazz",dto.getMethodName());
            logData.put("sqlCount",dto.getSqlCount());

            writeEvent(clsName,method,logData);
        }catch (Throwable t){
            Logger.error("transactionCostMonitor",t);
        }
    }

    /**
     * 添加循环监控
     * @param node
     */
    private static void addLoopMonitor(Node node){
        synchronized (loopMonitorMap){
            loopMonitorMap.put(node,null);
        }
    }

    /**
     * 移除循环监控
     * @param node
     */
    private static void removeLoopMonitor(Node node){
        synchronized (loopMonitorMap){
            loopMonitorMap.remove(node);
        }
    }

    /**
     * 循环监控
     */
    private static void loopMonitor(){
        try {
            String enableMonitor = HLogConfig.getInstance().getProperty(Constants.KEY_ENABLE_MONITOR_LOOP, "true");
            if (!"true".equals(enableMonitor.toLowerCase())) {
                return;
            }
            if (loopMonitorMap == null || loopMonitorMap.isEmpty()) {
                return;
            }
            int timeout = Integer.parseInt(HLogConfig.getInstance().getProperty(Constants.KEY_MONITOR_LOOP_TIMEOUT, "300")) * 1000;
            synchronized (loopMonitorMap) {
                Iterator<Map.Entry<Object, String>> iterator = loopMonitorMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Object, String> entry = iterator.next();
                    Node node = (Node) entry.getKey();
                    node.speed = System.currentTimeMillis() - node.beginTime;
                    if (node.speed > timeout) {
                        LogData logData = new LogData();
                        logData.setMc(HLogAgentConst.MV_CODE_LOOP);
                        logData.setId(node.logId);
                        logData.setPId(node.logPid);
                        logData.setGId(node.logGid);
                        logData.setTime(System.currentTimeMillis());
                        logData.put("clazz", node.className + "." + node.methodName);
                        logData.put("method", node.methodName);
                        logData.put("spend", node.speed);
                        if(node.logPid == null || "nvl".equals(node.logPid)){
                            logData.put("isTop",1);
                        }
                        writeEvent(node.className, node.methodName, logData);
                        iterator.remove();
                    }
                }
            }
        }catch (Throwable t){
            Logger.error("循环监控出错",t);
        }
    }

    public static void startLoopMonitor(){
        Runnable task = new Runnable() {
            public void run() {
                loopMonitor();
            }
        };

        try {
            String interval = HLogConfig.getInstance().getProperty(Constants.KEY_MONITOR_LOOP_INTERVAL_TIME, "30");
            //每隔interval秒执行task任务
            scheduledExecutorService.scheduleAtFixedRate(
                    task,
                    0,
                    Integer.parseInt(interval),
                    TimeUnit.SECONDS);

        }catch (Throwable t){
            Logger.error("执行循环监控任务出错",t);
        }

    }

    /**
     * 配置文件重新加载任务
     */
    public static void startConfigReloadTask(){
       Runnable task = new Runnable() {
            public void run() {
                try {
                    String filePath = HLogConfig.getInstance().getHLogAgentDir()+Constants.FIEL_NAME_HLOG_CONFS;
                    boolean isModify = false;
                    //判断hlog-confs.properties是否修改
                    File file = new File(filePath);
                    long modifyTime = file.lastModified();
                    if(configFileModifyTime != modifyTime){
                        isModify = true;
                        configFileModifyTime = modifyTime;
                    }
                    //判断hlog-{0}-confs.properties是否修改
                    if(HLogConfig.hlogCfgName!=null){
                        String extFilePath = HLogConfig.getInstance().getHLogAgentDir() + MessageFormat.format(Constants.FILE_NAME_HLOG_EXT_CONFS, HLogConfig.hlogCfgName);
                        File extFile = new File(extFilePath);
                        long extModifyTime = extFile.lastModified();
                        if(extConfigFileModifyTime != extModifyTime){
                            isModify = true;
                            extConfigFileModifyTime = extModifyTime;
                        }
                    }

                    if (isModify) {
                        Logger.info("刷新配置文件");
                        HLogConfig.getInstance().localProperties();
                    }
                }catch (Exception e){
                    Logger.error("定时任务刷新配置文件失败",e);
                }
            }
        };

        try {
            int interval = 15;
            //每隔interval秒执行task任务
            scheduledExecutorService.scheduleWithFixedDelay(
                    task,
                    0,
                    interval,
                    TimeUnit.SECONDS);

        }catch (Throwable t){
            Logger.error("启动刷新配置文件定时任务失败",t);
        }

    }

}

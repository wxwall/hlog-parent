package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.CollectRateKit;
import com.asiainfo.hlog.agent.HLogAgentConst;
import com.asiainfo.hlog.agent.jvm.HLogJvmReport;
import com.asiainfo.hlog.agent.runtime.dto.SqlInfoDto;
import com.asiainfo.hlog.agent.runtime.dto.TranCostDto;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.IdHepler;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.LogData;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
        excludeParamTypes.add("com.ailk.eaap.op2.serviceagent.common.MessageBO");

        excludeParamTypePaths.add("org.apache.velocity.");
        excludeParamTypePaths.add("javax.");
        excludeParamTypePaths.add("org.w3c");
        excludeParamTypePaths.add("org.springframework");
        excludeParamTypePaths.add("org.apache");
        excludeParamTypePaths.add("org.jdom");
        excludeParamTypePaths.add("com.fasterxml");
        excludeParamTypePaths.add("org.mybatis");
        excludeParamTypePaths.add("com.fasterxml");
        excludeParamTypePaths.add("net.sf.json");
        excludeParamTypePaths.add("org.codehaus");
        excludeParamTypePaths.add("java.lang.reflect");
        excludeParamTypePaths.add("java.lang.Object");
        excludeParamTypePaths.add("java.io");
        excludeParamTypePaths.add("java.nio");
        excludeParamTypePaths.add("sun.nio");

        excludeParamType();
        //jvm信息监控
        if (HLogConfig.getInstance().isEnableJVMMonitor()) {
            HLogJvmReport.getInstance().start();
        }
        //循环监控
        if (HLogConfig.getInstance().isEnableLoopMonitor()) {
            startLoopMonitor();
        }
        IdHepler.init();
        startConfigReloadTask();
        sendAgentVersionInfo();
    }

    private static void excludeParamType(){
        String type = HLogConfig.getInstance().getProperty(Constants.KEY_HLOG_EXCLUDE_PARAM_TYPES);
        Logger.debug("CONFIG EXCLUDE_PARAM_TYPES = {0}",type);
        if(type != null && type.length() > 0){
            String[] arr = type.split(",");
            for(int i = 0; i < arr.length; i++){
                excludeParamTypes.add(arr[i]);
            }
        }
        String paths = HLogConfig.getInstance().getProperty(Constants.KEY_HLOG_EXCLUDE_PARAM_TYPE_PATHS);
        Logger.debug("CONFIG EXCLUDE_PARAM_TYPE_PATHS = {0}",paths);
        if(paths != null && paths.length() > 0){
            String[] arr = paths.split(",");
            for(int i = 0; i < arr.length; i++){
                excludeParamTypePaths.add(arr[i]);
            }
        }
    }

    private static void sendAgentVersionInfo(){
        LogData logData = new LogData();
        logData.setMc("hlogver");
        logData.setId(RuntimeContext.logId());
        logData.setTime(System.currentTimeMillis());
        logData.put("ver",HLogConfig.VERSION);
        logData.put("vernum",HLogConfig.VER_NUM);
        logData.put("verdt",HLogConfig.VER_START_DT);
        writeEvent("agent.version",null,logData);
    }

    static final public class Node{
        public String logId;
        public String logPid;
        public String logGid;
        public final String className;
        public final String methodName;
        public final String description;
        public final String[] paramNames;
        public final SoftReference<Object>[]  params;
        //private final Object[] params;
        public final long beginTime;
        public long speed = 0;
        private int isError = 0;
        private boolean enableProcess;
        private boolean enableError ;
        private boolean leaf = true;
        private boolean sql = false;
        public String type;
        public String requestUrl;

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

        public Node(String logId,String logPid,String className, String methodName, Long beginTime) {
            this.logId = logId;
            this.logPid = logPid;
            this.className = className;
            this.methodName = methodName;
            this.beginTime = beginTime;

            this.description = null;
            this.paramNames = null;
            this.params = null;
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

    public static void start(boolean isProcess,boolean isError,String className,String methodName,String desc,String[] paramNames,Object[] params){

        boolean enableProcess = isProcess;
        boolean enableError = isError;
        if(config.isEnableDynamicProcessSwitch()){
            enableProcess = enable(HLogAgentConst.MV_CODE_PROCESS, className,methodName);
            enableError = enable(HLogAgentConst.MV_CODE_ERROR, className,methodName);
        }

        if(!enableProcess && !enableError){
            Node node = new Node();
            node.enableProcess = enableProcess;
            node.enableError = enableError;
            pushNode(node);
            return ;
        }

        //采集率判断
        boolean isCollect = CollectRateKit.isCollect();
        CollectRateKit.incrTotal();
        if(!isCollect){
            return;
        }
        CollectRateKit.incrCurrNum();


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

        int size = 0;
        Stack s = local.get();
        if(s!=null){
            size = s.size();
        }

        int limitStackSize = Integer.parseInt(HLogConfig.getInstance().getProperty(Constants.KEY_HLOG_LIMIT_STACK_SIZE,"-1"));
        if(limitStackSize > 0 && size >= limitStackSize){
            paramNames = null;
            softReferences = null;
            desc = "stack out";
        }

        Node node = new Node(id,pid,className,methodName,desc,paramNames,softReferences);
        node.enableProcess = enableProcess;
        node.enableError = enableError;
        node.type = HLogAgentConst.LOOP_TYPE_METHOD;
        pushNode(node);
        //System.out.println(Thread.currentThread().getId()+","+className+"."+methodName+"-----------------------------start 4 id="+id+" _pid="+pid+",size="+size);
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
            int size = stack.size();
            Node node = stack.pop();
            //移除循环监控
            removeLoopMonitor(node);

            if(node.isEmpty()){
                return ;
            }
            //System.out.println(Thread.currentThread().getId()+","+node.className+"."+node.methodName+"-----------------------------end 5.1 _pid="+node.logPid+",size="+size);
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
            //耗时达到某值时或出现异常时记录
            if(enableProcess && (node.speed>=ptime||isError)){
                //记录运行耗时超过
                doSendProcessLog(node,node.logId,pid,node.isError,stack.isEmpty(),isWriteErrLog);
                //如果本节点超过预警值,追加写已经执行过的子过程节点
                if(enableSaveWithoutSubs){
                    doWriteSubNode();
                }
                if(node.speed>config.getProcessTimeWithout()){
                    havWriteLog = true;
                }
            }else if(enableSaveWithoutSubs && enableProcess && ptime>=0){
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
                        String cls = paramsDesc[i].getClassName();

                        //是否是排除的参数类型
                        boolean isExclude = isExcludeParamType(cls,p);

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

    private static boolean isExcludeParamType(String clazz,SoftReference<Object> p) {
        Logger.debug("ParamType = {0}",clazz);
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

        Object o = p.get();
        if(o instanceof OutputStream || o instanceof InputStream){
            return true;
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
        if(err==null){
            return;
        }
        LogData logData = createLogData(HLogAgentConst.MV_CODE_ERROR,id,pid);
        String errMsg = RuntimeContext.error(err);
        logData.setDesc(errMsg);
        logData.put("msg",err.getMessage());
        logData.put("clazz",node.className+"."+node.methodName);
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
    public static void doSendProcessLog(Node node ,String id,String pid,int status,boolean isTop,boolean isWriteErrLog){
        LogData logData = createLogData(HLogAgentConst.MV_CODE_PROCESS,id,pid);
        logData.put("status",status);
        logData.put("clazz",node.className+"."+node.methodName);
        logData.put("method",node.methodName);
        logData.put("spend",node.speed);

        if(pid == null || pid.equals("nvl") || id.equals(pid) || pid.equals(logData.getGId())){
            logData.put("isTop",1);
        }
//
//        if(isTop || "nvl".equals(pid)){
//            logData.put("isTop",1);
//        }
        if(isWriteErrLog){
            logData.put("havErr",1);
        }
        //logData.put("havErr",isWriteErrLog?1:0);
        //logData.put("leaf",node.leaf?1:0);
        if(node.sql){
            logData.put("sql",1);
        }
        if(node.requestUrl!=null){
            logData.put("url",node.requestUrl);
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
        if(!config.isEnableSqlTrack() || sql==null){
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
            logData.put("sqlc","s"+sql.hashCode());
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
            logData.put("clazz",className+"."+methodName);
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
            TranCostDto dto = LogAgentContext.popTranCost();
            LogAgentContext.clearTranCostContext();
            if(!config.isEnableTransaction()){
                return;
            }
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

    public static void startTranCost(String methodName){
        try{
            if(!config.isEnableTransaction()){
                return;
            }
            Stack<TranCostDto> stack = LogAgentContext.getTranCostContext();
            TranCostDto dto = new TranCostDto();
            dto.setId(RuntimeContext.logId());
            dto.setMethodName(methodName);
            dto.setStartTime(System.currentTimeMillis());
            stack.push(dto);
        }catch (Throwable t){
            Logger.error("setTranCost error",t);
        }
    }

    /**
     * 添加循环监控
     * @param node
     */
    public static void addLoopMonitor(Node node){
        if (!config.isEnableLoopMonitor()) {
            return;
        }
        if(node == null){
            return;
        }
        synchronized (loopMonitorMap){
            loopMonitorMap.put(node,null);
        }
    }

    /**
     * 移除循环监控
     * @param node
     */
    public static void removeLoopMonitor(Node node){
        if (!config.isEnableLoopMonitor()) {
            return;
        }
        if(node == null){
            return;
        }
        synchronized (loopMonitorMap){
            loopMonitorMap.remove(node);
        }
    }

    /**
     * 循环监控
     */
    private static void loopMonitor(){
        try {
            if (!config.isEnableLoopMonitor()) {
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
                        logData.put("ltype",node.type);
                        if(node.requestUrl!=null){
                            logData.put("requestUrl",node.requestUrl);
                        }
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
                    String filePath = HLogConfig.getHLogAgentDir()+Constants.FIEL_NAME_HLOG_CONFS;
                    //System.out.println("load hlog agent config file = "+filePath);
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
                        String extFilePath = HLogConfig.getHLogAgentDir() + MessageFormat.format(Constants.FILE_NAME_HLOG_EXT_CONFS, HLogConfig.hlogCfgName);
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

    public static void setHibernateSql(String sql){
        if(config.isEnableHibernateSql()) {
            LogAgentContext.clearHibernateSql();
            SqlInfoDto d = new SqlInfoDto();
            d.setSql(sql);
            d.setStartTime(Calendar.getInstance().getTimeInMillis());
            LogAgentContext.setHibernateSql(d);
        }
    }

    public static void addHibernateParam(Object val){
        SqlInfoDto d = LogAgentContext.getHibernateSql();
        if(d != null){
            d.getParams().add(val);
        }
    }

    public static void sendHibernateSql(){
        SqlInfoDto d = LogAgentContext.getHibernateSql();
        if(d != null){
            try {
                String pId = LogAgentContext.getThreadCurrentLogId();
                LogData data = createLogData(HLogAgentConst.MV_CODE_SQL, LogUtil.logId(), pId);
                data.put("sql", d.getSql());
                data.put("params", RuntimeContext.toJson(d.getParams()));
                data.put("sqlc", "s" + d.getSql().hashCode());
                data.put("spend",Calendar.getInstance().getTimeInMillis()-d.getStartTime());
                writeEvent("hibernate.sql",null,data);
            }catch (Exception e){
                Logger.error("sendHibernateSql",e);
            }finally {
                LogAgentContext.clearHibernateSql();
            }

        }
    }

    public static void csfService(String srvCode, Map sysMap, Map busiMap){
        String pId = LogAgentContext.getThreadCurrentLogId();
        if(pId != null){
            busiMap.put("_pId",pId);
        }
    }

}

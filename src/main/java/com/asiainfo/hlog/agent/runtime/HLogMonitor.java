package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.HLogAgentConst;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.LogData;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import java.lang.ref.SoftReference;
import java.util.*;

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
    }

    static final class Node{
        private String logId;
        private String logPid;
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
        stack.push(node);
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
        logData.put("clazz",node.className);
        logData.put("method",node.methodName);
        logData.put("spend",node.speed);
        if(isTop){
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
     * @param speed
     * @param className
     * @param sql
     * @param params
     */
    public static void sqlMonitor(long speed,String className,String sql,String params) {
        if(!config.isEnableSqlTrack()){
            return ;
        }
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
        LogData logData = createLogData(HLogAgentConst.MV_CODE_SQL,id,pid);
        logData.put("spend",speed);
        logData.put("sql",sql);
        logData.put("params",params);
        writeEvent(clsName,methodName,logData);
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
        if(!config.isEnableLoggerTrack()){
            return false;
        }
        Node node = getCurrentNode();
        String methodName = null;
        if(node!=null && node.className.equals(className)){
            methodName = node.methodName;
        }
        return enable(HLogAgentConst.MV_CODE_LOGGER,className,methodName,level);
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
}

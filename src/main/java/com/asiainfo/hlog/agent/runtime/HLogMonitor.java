package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.HLogAgentConst;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.LogData;

import java.lang.ref.SoftReference;
import java.util.Stack;

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
    static final class Node{
        private final String logId;
        private final String logPid;
        private final String className;
        private final String methodName;
        private final SoftReference<Object>[]  params;
        //private final Object[] params;
        private final long beginTime;
        private long speed = 0;
        private boolean enableProcess;
        private boolean enableError ;

        public Node(){
            logId = null;
            logPid = null;
            className = null;
            methodName = null;
            //description = null;
            //paramNames = null;
            params = null;
            beginTime = 0;
        }

        public Node(String logId,String logPid,String className, String methodName, String description, String[] paramNames, SoftReference[] params) {
            this.logId = logId;
            this.logPid = logPid;
            this.className = className;
            this.methodName = methodName;
            //this.description = description;
            //this.paramNames = paramNames;
            this.params = params;
            beginTime = System.currentTimeMillis();
        }
        public boolean isEmpty(){
            return className == null;
        }
    }
    private static ThreadLocal<Stack<Node>> local = new ThreadLocal();

    public static void start(String className,String methodName,String desc,String[] paramNames,Object[] params){

        boolean enableProcess = enable(HLogAgentConst.MV_CODE_PROCESS, className,methodName);
        boolean enableError = enable(HLogAgentConst.MV_CODE_ERROR, className,methodName);

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
        String id = RuntimeContext.logId();
        String pid = RuntimeContext.buildLogPId(id);
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
        stack.push(node);
    }

    public static void end(String mcode,Object returnObj,boolean isError){

        Stack<Node> stack = null;
        try{
            stack = local.get();
            if(stack.isEmpty()){
                LogAgentContext.clear();
                return ;
            }
            Node node = stack.pop();
            if(node.isEmpty()){
                return ;
            }
            node.speed = System.currentTimeMillis() - node.beginTime;
            //如果产生多个日志,这里的logId保持一致
            String id = node.logId ;
            String pid = node.logPid;
            //boolean isUsed = false;
            //耗时达到某值是记录
            if(node.enableProcess && node.speed>RuntimeContext.processTime){
                //记录运行耗时超过
                doSendProcessLog(node,id,pid,isError?1:0);
            }
            //发生异常时记录
            if(isError && stack.isEmpty() && node.enableError){
                doSendErrorLog(node, id, pid, (Throwable) returnObj);
            }

            if(pid!=null){
                LogAgentContext.setThreadCurrentLogId(pid);
            }
        }finally {
            //如果上级id为null时,清了node =
            if(stack!=null && stack.isEmpty()){
                LogAgentContext.clear();
            }
        }
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
        writeEvent(node.className,node.methodName,logData);
    }

    /**
     * 组织和发送花费过高的日志数据
     * @param node
     * @param id
     * @param pid
     * @param status
     */
    private static void doSendProcessLog(Node node ,String id,String pid,int status){
        LogData logData = createLogData(HLogAgentConst.MV_CODE_PROCESS,id,pid);
        logData.put("status",status);
        logData.put("clazz",node.className);
        logData.put("method",node.methodName);
        logData.put("spend",node.speed);
        writeEvent(node.className,node.methodName,logData);
    }



    /**
     * 获取配置的sql耗时数据
     * @return
     */
    public static long getConfigSqlSpeed(){
        return -1;
    }


    /**
     * 监控sql执行耗时,如果耗时超过预设的值,记录执行的sql和入参
     * @param speed
     * @param className
     * @param sql
     * @param params
     */
    public static void sqlMonitor(long speed,String className,String sql,String params) {

        Node node = getCurrentNode();
        String clsName ;
        String methodName = null;
        String id = RuntimeContext.logId();
        String pid;
        if(node==null){
            pid = LogAgentContext.getThreadCurrentLogId();
            clsName = className;
        }else{
            pid = node.logPid;
            clsName = node.className;
            methodName = node.methodName;
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
                if(object instanceof Throwable){
                    buff.append(RuntimeContext.error((Throwable)object));
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
            for (int i=0;i<paramNames.length;i++) {
                String paramName = paramNames[i];
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
        logData.setGId(LogAgentContext.getThreadLogGroupId());
        logData.setTime(System.currentTimeMillis());
        return logData;
    }
}

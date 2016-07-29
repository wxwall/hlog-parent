package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.client.HLogReflex;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.helper.ClassHelper;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.helper.MethodCaller;
import com.asiainfo.hlog.client.model.Event;
import com.asiainfo.hlog.client.model.LogData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 运行状态下的各种上下文判断
 * Created by chenfeng on 2015/4/17.
 */
public class RuntimeContext {

    private static MethodCaller enableMethodCaller = null;

    private static MethodCaller logIdMehtodClass = null;

    private static MethodCaller toJson = null;

    private static Field messageField = null;

    private static MethodCaller processTime  ;

    private static MethodCaller processTimeWithout  ;

    private static MethodCaller sqlTime ;

    private static MethodCaller enableRequest ;

    private static MethodCaller enableSaveWithoutParams ;

    private static MethodCaller enableSaveWithoutSubs ;

    private static MethodCaller enableSqlTrack ;

    private static MethodCaller enableLoggerTrack ;

    private static Object hlogConfigInstance ;

    private static MethodCaller propertyMethodCaller;

    private static Object hlogConfigInstanceInvoke(MethodCaller caller){
        if(caller==null){
            return null;
        }
        return caller.invoke();
    }

    public static long getProcessTime() {
        return (Long)hlogConfigInstanceInvoke(processTime);
    }
    public static long getProcessTimeWithout() {
        return (Long)hlogConfigInstanceInvoke(processTimeWithout);
    }

    public static long getSqlTime() {
        return (Long)hlogConfigInstanceInvoke(sqlTime);
    }

    public static boolean isEnableRequest() {
        return (Boolean)hlogConfigInstanceInvoke(enableRequest);
    }

    public static boolean isEnableSaveWithoutParams() {
        return (Boolean)hlogConfigInstanceInvoke(enableSaveWithoutParams);
    }

    public static String[] getErrorCodeTypes(){
        if(propertyMethodCaller !=null){
            String values = (String)propertyMethodCaller.invoke(Constants.KEY_ERROR_CODE_TYPES,"code");
            return values.split(",");
        }
        return new String[]{"code"};
    }

    public static boolean isEnableSaveWithoutSubs() {
        return (Boolean)hlogConfigInstanceInvoke(enableSaveWithoutSubs);
    }

    public static boolean isEnableSqlTrack() {
        return (Boolean)hlogConfigInstanceInvoke(enableSqlTrack);
    }

    public static boolean isEnableLoggerTrack() {
        return (Boolean)hlogConfigInstanceInvoke(enableLoggerTrack);
    }
    static {

        Class clazz = ClassHelper.loadClass("com.asiainfo.hlog.client.config.HLogConfig");
        try {
            hlogConfigInstance = ClassHelper.getMethod(clazz,"getInstance").invoke(null,null);

            processTime = new MethodCaller(ClassHelper.getMethod(clazz,"getProcessTime"),hlogConfigInstance);

            processTimeWithout = new MethodCaller(ClassHelper.getMethod(clazz,"getProcessTimeWithout"),hlogConfigInstance);

            sqlTime = new MethodCaller(ClassHelper.getMethod(clazz,"getSqlTime"),hlogConfigInstance);

            enableRequest = new MethodCaller(ClassHelper.getMethod(clazz,"isEnableRequest"),hlogConfigInstance);

            enableSaveWithoutParams = new MethodCaller(ClassHelper.getMethod(clazz,"isEnableSaveWithoutParams"),hlogConfigInstance);

            enableSaveWithoutSubs = new MethodCaller(ClassHelper.getMethod(clazz,"isEnableSaveWithoutSubs"),hlogConfigInstance);
            enableSqlTrack = new MethodCaller(ClassHelper.getMethod(clazz,"isEnableSqlTrack"),hlogConfigInstance);
            enableLoggerTrack = new MethodCaller(ClassHelper.getMethod(clazz,"isEnableLoggerTrack"),hlogConfigInstance);

            propertyMethodCaller = new MethodCaller(ClassHelper.getMethod(clazz,"getProperty",String.class,String.class),hlogConfigInstance);
        } catch (Exception e) {
            Logger.error("读取运行时配置数据异常",e);
        }
    }


    /**
     * <p>给异常一个编号,并返回一个异常栈的字符串.</p>
     * @param t
     * @return
     */
    public static String error(Throwable t){
        StringBuilder builder = new StringBuilder();

        StackTraceElement[] stes = t.getStackTrace();

        boolean enable = false;
        for(StackTraceElement ste : stes){
            enable = enable("error",ste.getClassName(),ste.getMethodName());
            if(enable){
                break;
            }
        }
        if(!enable){
            return null;
        }
        int realLineNum = 0 ;
        builder.append(t);
        for(StackTraceElement ste : stes){
            builder.append("\nat ").append(ste.getClassName()).append(".")
                    .append(ste.getMethodName()).append("(").append(realLineNum>0?realLineNum:ste.getLineNumber()).append(")");
            realLineNum = 0 ;
        }
        //将日志ID写入到异常对象中
        if(messageField==null){
            try{
                messageField = Throwable.class.getDeclaredField("detailMessage");
                messageField.setAccessible(true);
            }catch (Throwable dd){
                dd.printStackTrace();
            }
        }
        String detailMessage = t.getMessage();
        if(messageField!=null){
            try{
                messageField.set(t,"["+LogAgentContext.getThreadLogGroupId()+"]"+detailMessage);
            }catch (Exception e){
                if(Logger.isError()){
                    Logger.error("在给异常对象写入日志ID时异常,logId={0}",e,LogAgentContext.getThreadCurrentLogId());
                }
            }
        }
        return builder.toString();
    }

    public static boolean enable(String weaveName ,String clazz,String method) {
        return enable(weaveName,clazz,method,null);
    }
    public static boolean enable(String weaveName ,String clazz,String method,String level) {
        if (enableMethodCaller == null) {
            synchronized (RuntimeContext.class) {
                Object obj = ClassHelper.newInstance(RuntimeCall.class.getName());
                Method m = ClassHelper.getMethod(obj.getClass(),"enable",String.class,String.class,String.class,String.class);
                enableMethodCaller = new MethodCaller(m,obj);
            }
        }

        boolean b = (Boolean)enableMethodCaller.invoke(weaveName,clazz,method,level);

        return b;
    }

    public static String logId(){
        if(logIdMehtodClass==null){
            Class logUtil = ClassHelper.loadClass(LogUtil.class.getName());
            if(logUtil==null){
                return "";
            }
            Method method = ClassHelper.getMethod(logUtil,"logId");
            logIdMehtodClass = new MethodCaller(method,null);
        }

        return (String)logIdMehtodClass.invoke();
    }
    public static String buildLogPId(String _agent_Log_Id_){
        //获取上级日志id
        String _agent_Log_pId = LogAgentContext.getThreadCurrentLogId();
        if(_agent_Log_pId==null){
            LogAgentContext.clear();
            LogAgentContext.setThreadLogGroupId(_agent_Log_Id_);
            _agent_Log_pId = "nvl";
        }
        LogAgentContext.setThreadCurrentLogId(_agent_Log_Id_);
        return _agent_Log_pId;
    }

    public static String toJson(Object obj){
        if(toJson==null){
            Class runtimeCallClass = ClassHelper.loadClass(RuntimeCall.class.getName());
            if(runtimeCallClass==null){
                return "";
            }
            Method method = ClassHelper.getMethod(runtimeCallClass,"toJson",Object.class);
            toJson = new MethodCaller(method,null);
        }

        return (String)toJson.invokeStatic(obj);
    }

    public static void writeEvent(String clazz,String method,LogData logData){
        try{
            Event event = new Event();
            event.setClassName(clazz);
            event.setMethodName(method);
            event.setData(logData);
            HLogReflex.reveice(event);
        }catch (Throwable t){
            Logger.error("将日志写入队列时出错,参数：{0},{1},{2}",t,clazz,method,logData);
        }
    }

    /**
     * 获取异常编码，如果属性不存在返回字符串“undefined”
     * @param t
     * @return
     */
    public static String errorCode(Throwable t){
        String[] codeTypeArray = getErrorCodeTypes();
        for(String codeType : codeTypeArray){
            try {
                Field field = t.getClass().getDeclaredField(codeType);
                field.setAccessible(true);
                Object value = field.get(t);
                if(value!=null){
                    return value.toString();
                }
                return t.getClass().getSimpleName();
            } catch (Throwable e) {
                Logger.debug("{1}类ERROR_CODE_TYPE={0}不存在",codeType,t.getClass());
            }
        }
        return t.getClass().getSimpleName();
    }

}



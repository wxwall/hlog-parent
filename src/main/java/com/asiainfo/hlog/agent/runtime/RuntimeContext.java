package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.client.HLogReflex;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.Event;
import com.asiainfo.hlog.client.model.LogData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 运行状态下的各种上下文判断
 * Created by chenfeng on 2015/4/17.
 */
public class RuntimeContext {

    private static RuntimeEnable runtimeCall = null;

    private static Field messageField = null;

    private static IRutimeCall rutimeCall = RutimeCallFactory.getRutimeCall();


    private static String[] errCodeMethodNames = null;

    public static String[] getErrorCodeTypes(){
        if(errCodeMethodNames!=null){
            return errCodeMethodNames;
        }

        String values = HLogConfig.getInstance().getProperty(Constants.KEY_ERROR_CODE_TYPES,"code");
        String[] names = values.split(",");
        errCodeMethodNames = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            errCodeMethodNames[i]= "get"+name.substring(0,1).toUpperCase()+name.substring(1);
        }
        return errCodeMethodNames;
    }

    private static StackTraceElement[] getTargetStackTrace(Throwable t){
        if(t==null){
            return null;
        }
        StackTraceElement[] stes = t.getStackTrace();

        if((stes==null || stes.length==0) && t.getCause()!=null){
            return getTargetStackTrace(t.getCause());
        }

        return stes;
    }

    /**
     * <p>给异常一个编号,并返回一个异常栈的字符串.</p>
     * @param t
     * @return
     */
    public static String error(Throwable t){

        StringBuilder builder = new StringBuilder();

        StackTraceElement[] stes = getTargetStackTrace(t);

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
        if (runtimeCall == null) {
            synchronized (RuntimeContext.class) {
                runtimeCall = new RuntimeEnable();
            }
        }
        return runtimeCall.enable(weaveName,clazz,method,level);
    }

    public static String logId(){
        return LogUtil.logId();
    }
    public static String getLogId(){
        return LogAgentContext.getThreadCurrentLogId();
    }

    public static String buildLogPId(String _agent_Log_Id_){
        //获取上级日志id
        String _agent_Log_pId = LogAgentContext.getThreadCurrentLogId();
        if(_agent_Log_pId==null){
            //LogAgentContext.clear();
            if(LogAgentContext.getThreadLogGroupId()==null){
                LogAgentContext.setThreadLogGroupId(_agent_Log_Id_);
            }
            _agent_Log_pId = "nvl";
        }
        LogAgentContext.setThreadCurrentLogId(_agent_Log_Id_);
        return _agent_Log_pId;
    }

    public static String toJson(Object obj){
        return rutimeCall.toJson(obj);
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
                Method method = t.getClass().getMethod(codeType,null);
                method.setAccessible(true);
                Object value = method.invoke(t,null);
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



package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.client.HLogReflex;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.Event;
import com.asiainfo.hlog.client.model.LogData;

import java.lang.reflect.Field;

/**
 * 运行状态下的各种上下文判断
 * Created by chenfeng on 2015/4/17.
 */
public class RuntimeContext {

    private static RuntimeCall runtimeCall = null;

    private static Field messageField = null;

    public static long getProcessTime() {
        return HLogConfig.getInstance().getProcessTime();
    }
    public static long getProcessTimeWithout() {
        return HLogConfig.getInstance().getProcessTimeWithout();
    }

    public static long getSqlTime() {
        return HLogConfig.getInstance().getSqlTime();
    }

    public static boolean isEnableRequest() {
        return HLogConfig.getInstance().isEnableRequest();
    }

    public static boolean isEnableSaveWithoutParams() {
        return HLogConfig.getInstance().isEnableSaveWithoutParams();
    }

    public static String[] getErrorCodeTypes(){
        String values = HLogConfig.getInstance().getProperty(Constants.KEY_ERROR_CODE_TYPES,"code");
        if(values!=null){
            return values.split(",");
        }
        return new String[]{"code"};
    }

    public static boolean isEnableSaveWithoutSubs() {
        return HLogConfig.getInstance().isEnableSaveWithoutSubs();
    }

    public static boolean isEnableSqlTrack() {
        return HLogConfig.getInstance().isEnableSqlTrack();
    }

    public static boolean isEnableLoggerTrack() {
        return HLogConfig.getInstance().isEnableLoggerTrack();
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
                runtimeCall = new RuntimeCall();
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
            LogAgentContext.clear();
            LogAgentContext.setThreadLogGroupId(_agent_Log_Id_);
            _agent_Log_pId = "nvl";
        }
        LogAgentContext.setThreadCurrentLogId(_agent_Log_Id_);
        return _agent_Log_pId;
    }

    public static String toJson(Object obj){
        return RuntimeCall.toJson(obj);
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



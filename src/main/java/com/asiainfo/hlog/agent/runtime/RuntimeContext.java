package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.bytecode.javassist.ErrorLogWeave;
import com.asiainfo.hlog.client.HLogReflex;
import com.asiainfo.hlog.client.helper.ClassHelper;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.helper.MethodCaller;
import com.asiainfo.hlog.client.model.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 运行状态下的各种上下文判断
 * Created by chenfeng on 2015/4/17.
 */
public class RuntimeContext {

    private static MethodCaller enableMethodCaller = null;

    private static MethodCaller logIdMehtodClass = null;

    private static Field messageField = null;


    public static String error(Throwable t){
        StringBuilder builder = new StringBuilder();

        StackTraceElement[] stes = t.getStackTrace();

        boolean enable = false;
        for(StackTraceElement ste : stes){
            enable = enable(ErrorLogWeave.ID,ste.getClassName(),ste.getMethodName());
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
            String methodName = ste.getMethodName();
            if(methodName.indexOf("$$")!=-1){
                realLineNum = ste.getLineNumber();
                continue;
            }
            //enable = enable(ErrorLogWeave.ID,ste.getClassName(),ste.getMethodName());
            builder.append("\nat ").append(ste.getClassName()).append(".")
                    .append(ste.getMethodName()).append("(").append(realLineNum>0?realLineNum:ste.getLineNumber()).append(")");
            /*
            if(enable){
                //builder.insert(0,t);
                //return builder.toString();
            }*/
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
        }
        LogAgentContext.setThreadCurrentLogId(_agent_Log_Id_);
        return _agent_Log_pId;
    }


    public static void writeEvent(String clazz,String method,LogData logData){
        Event event = new Event();
        event.setClassName(clazz);
        event.setMethodName(method);
        event.setData(logData);
        HLogReflex.reveice(event);
    }

    //写入参数据
    public static void writeInterceptParam(String mcode,String id,String pId,
                                           String clazz,String method,ParamObjs paramObjs){
        try{
            if(enable("interceptParam", clazz, method)){
                //如果有参数信息的话
                if(paramObjs!=null && paramObjs.getParamNames()!=null && paramObjs.getParamNames().length>0){
                    LogData logData = new LogData();
                    logData.setMc(mcode);
                    logData.setId(id);
                    logData.setPId(pId);
                    logData.setGId(LogAgentContext.getThreadLogGroupId());
                    long time = System.currentTimeMillis();
                    logData.setTime(time);
                    Map map = LogUtil.paramToJson(paramObjs);
                    logData.putAll(map);
                    writeEvent(clazz, method, logData);
                }
            }
        }catch (Throwable t){
            Logger.error("构建日志异常,主要入参:{0},{1},{2},{3}",t,mcode,id,clazz,method);
        }
    }
    //写入参数据
    public static void writeInterceptParam2(String mcode,String id,String pId,
                                           String clazz,String method,ParamObjs paramObjs){

        try{
            if(enable("interceptParam2", clazz, method)){
                //如果有参数信息的话
                if(paramObjs!=null && paramObjs.getParamNames()!=null && paramObjs.getParamNames().length>0){
                    LogData logData = new LogData();
                    logData.setMc(mcode);
                    logData.setId(id);
                    logData.setPId(pId);
                    logData.setGId(LogAgentContext.getThreadLogGroupId());
                    long time = System.currentTimeMillis();
                    logData.setTime(time);
                    Map map = LogUtil.paramToJson(paramObjs);
                    logData.putAll(map);
                    writeEvent(clazz, method, logData);
                }
            }
        }catch (Throwable t){
            Logger.error("构建日志异常,主要入参:{0},{1},{2},{3}",t,mcode,id,clazz,method);
        }
    }

    //写第三方日志
    public static void writeLogger(String mcode,String id,String pId,
                                   String clazz,String method,String desc,String level){
        try{
            if(enable("logger", clazz, method,level)){
                LoggerLogData logData = new LoggerLogData();
                logData.setMc(mcode);
                logData.setId(id);
                logData.setPId(pId);
                logData.setGId(LogAgentContext.getThreadLogGroupId());
                long time = System.currentTimeMillis();
                logData.setTime(time);
                logData.setDesc(desc);
                logData.setLevel(level);
                writeEvent(clazz, method, logData);
            }
        }catch (Throwable t){
            Logger.error("构建日志异常,主要入参:{0},{1},{2},{3}",t,mcode,id,clazz,method);
        }
    }

    //写process日志
    public static void writeProcessLog(String mcode,String id,String pId,
                                       String clazz,String method,int status,long startTime){
        try{
            if(enable("process", clazz, method)){
                RPLogData logData = new RPLogData();
                logData.setMc(mcode);
                logData.setId(id);
                logData.setPId(pId);
                logData.setGId(LogAgentContext.getThreadLogGroupId());
                long time = System.currentTimeMillis();
                logData.setTime(time);
                logData.setStatus(status);
                logData.setClazz(clazz);
                logData.setMethod(method);
                logData.setSpend(time - startTime);
                writeEvent(clazz,method,logData);
            }
        }catch (Throwable t){
            Logger.error("构建日志异常,主要入参:{0},{1},{2},{3}",t,mcode,id,clazz,method);
        }
    }
    //写error日志
    public static void writeErrorLog(String mcode,String id,String pId,String clazz,String method,Throwable t){
        try{
            if(pId==null && enable("error", clazz, method)){
                ErrorLogData logData = new ErrorLogData();
                logData.setMc(mcode);
                logData.setId(id);
                logData.setPId(pId);
                logData.setGId(LogAgentContext.getThreadLogGroupId());
                long time = System.currentTimeMillis();
                logData.setTime(time);
                String errMsg = RuntimeContext.error(t);
                logData.setDesc(errMsg);
                writeEvent(clazz,method,logData);
            }
        }catch (Throwable tt){
            Logger.error("构建日志异常,主要入参:{0},{1},{2},{3}",tt,mcode,id,clazz,method);
        }
    }
}



package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.bytecode.javassist.ErrorLogWeave;
import com.asiainfo.hlog.client.helper.ClassHelper;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.helper.MethodCaller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
}



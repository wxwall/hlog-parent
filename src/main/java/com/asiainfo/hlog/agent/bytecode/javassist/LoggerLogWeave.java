package com.asiainfo.hlog.agent.bytecode.javassist;

import java.util.HashSet;
import java.util.Set;

/**
 * 采集第三方日志框架输出的日志,支持：</br>
 * 1、log4j </br>
 * 2、
 * Created by c on 2015/3/16.
 */
public class LoggerLogWeave extends AbstractLogWeave {

    public static final String ID = new String("logger");

    private Set<String> methods = new HashSet<String>();

    private String[] depends ;

    public LoggerLogWeave(){
        methods.add("debug");
        methods.add("info");
        methods.add("warn");
        methods.add("error");
        //depends = new String[]{DefinitionInParamLogWeave.ID};
    }

    public String[] getDependLogWeave() {
        return depends;
    }


    public String getName() {
        return ID;
    }

    @Override
    protected String getMcode() {
        return "h03";
    }

    public int getOrder() {
        return 10005;
    }


    public String beforWeave(LogWeaveContext logWeaveContext) {
        StringBuilder codeBuffer = new StringBuilder();
        String methodName = logWeaveContext.getMethodName();
        if(methodName.equals("isTraceEnabled") || methodName.equals("isDebugEnabled")
                || methodName.equals("isInfoEnabled") || methodName.equals("isWarnEnabled")
                || methodName.equals("isErrorEnabled")){
            String level = methodName.equals("isTraceEnabled")?"trace":
                    methodName.equals("isDebugEnabled")?"debug":
                            methodName.equals("isInfoEnabled")?"info":
                                    methodName.equals("isWarnEnabled")?"warn":
                                            methodName.equals("isErrorEnabled")?"error":"none";

            codeBuffer.append("StackTraceElement ste = Thread.currentThread().getStackTrace()[2];");
            codeBuffer.append("if(com.asiainfo.hlog.agent.runtime.RuntimeContext.enable")
                    .append("(\"").append(ID).append("\",ste.getClassName(),ste.getMethodName(),\""+level+"\")){");
            codeBuffer.append("return true;}");
            return codeBuffer.toString();
        }else if(methods.contains(methodName)){
            codeBuffer.append("StackTraceElement ste = Thread.currentThread().getStackTrace()[2];");
            codeBuffer.append("if(com.asiainfo.hlog.agent.runtime.RuntimeContext.enable")
                    .append("(\"").append(ID).append("\",ste.getClassName(),ste.getMethodName(),\"" + methodName + "\")){");
            codeBuffer.append("String _agent_Log_pId_ = com.asiainfo.hlog.agent.runtime.LogAgentContext.getThreadCurrentLogId();");
            codeBuffer.append("String _agent_Log_Id_ = com.asiainfo.hlog.agent.runtime.RuntimeContext.logId();");

            codeBuffer.append("com.asiainfo.hlog.client.model.LoggerLogData data = new com.asiainfo.hlog.client.model.LoggerLogData();");
            codeBuffer.append("data.setLevel(\"" + methodName + "\");");
            codeBuffer.append("com.asiainfo.hlog.client.model.ParamObjs _inParams = null;");
            getInParams(logWeaveContext,codeBuffer,false);
            //StackTraceElement ste;
            codeBuffer.append("String desc = new StringBuilder(ste.getClassName()).append(\"(\").append(ste.getLineNumber()).append(\")\").append(_inParams.toString()).toString();");
            //codeBuffer.append("String desc = ste.getClassName()+\"(\"+ste.getLineNumber()+\")\"+params.toString();");
            buildBaseLogData(logWeaveContext, getMcode(logWeaveContext), "desc",codeBuffer);
            buildReveiceEvent(codeBuffer);
            codeBuffer.append("}");
            return codeBuffer.toString();
        }
        return null;
    }


    public String tryWeave(LogWeaveContext logWeaveContext) {
        return null;
    }


    public String exceptionWeave(LogWeaveContext logWeaveContext) {
        return null;
    }


    public String afterWeave(LogWeaveContext logWeaveContext) {
        return null;
    }


    public String finallyWeave(LogWeaveContext logWeaveContext) {
        return null;
    }
}

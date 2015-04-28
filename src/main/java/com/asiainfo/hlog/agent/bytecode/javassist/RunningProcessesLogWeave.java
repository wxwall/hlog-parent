package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

/**
 * Created by c on 2015/3/17.
 */
public class RunningProcessesLogWeave implements ILogWeave {

    private String[] depends = new String[]{"logId"};


    public String[] getDependLogWeave() {
        return depends;
    }


    public String getName() {
        return "process";
    }


    public int getOrder() {
        return 10001;
    }


    public String beforWeave(LogWeaveContext logWeaveContext) {
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append("int happenErr = 1;");
        codeBuffer.append("long time1 = System.currentTimeMillis();");
        return codeBuffer.toString();
    }


    public String tryWeave(LogWeaveContext logWeaveContext) {
        return null;
    }


    public String exceptionWeave(LogWeaveContext logWeaveContext) {
        return null;
    }


    public String afterWeave(LogWeaveContext logWeaveContext) {
        return "happenErr=0;";
    }


    public String finallyWeave(LogWeaveContext logWeaveContext) {
        StringBuffer codeBuffer = new StringBuffer();
        //增加开关配置信息
        codeBuffer.append("if(com.asiainfo.hlog.agent.runtime.RuntimeContext.enable" +
                "(\""+getName()+"\",\""+logWeaveContext.getClassName()+"\",\""+logWeaveContext.getMethodName()+"\")){");
            codeBuffer.append("com.asiainfo.hlog.client.model.RPLogData data = new com.asiainfo.hlog.client.model.RPLogData();");
            codeBuffer.append("data.setStatus(happenErr);");
            codeBuffer.append("data.setId("+ LogAgentContext.S_AGENT_LOG_ID+");");
            codeBuffer.append("data.setPid("+ LogAgentContext.S_AGENT_LOG_PID+");");
            codeBuffer.append("data.setStatus(happenErr);");
            codeBuffer.append("data.setClazz(\""+logWeaveContext.getClassName()+"\");");
            codeBuffer.append("data.setMethod(\""+logWeaveContext.getMethodName()+"\");");
            codeBuffer.append("data.setGId(com.asiainfo.hlog.agent.runtime.LogAgentContext.getThreadLogGroupId());");
            codeBuffer.append("data.setTime(System.currentTimeMillis());");
            codeBuffer.append("data.setSpend(System.currentTimeMillis()-time1);");
            //codeBuffer.append("System.out.println(data);");
            //codeBuffer.append("com.asiainfo.hlog.client.queue.QueueHolder.getHolder().add(data);");
            codeBuffer.append("com.asiainfo.hlog.client.model.Event event = new com.asiainfo.hlog.client.model.Event();");
            codeBuffer.append("event.setClassName(\"" + logWeaveContext.getClassName() + "\");");
            codeBuffer.append("event.setMethodName(\"" + logWeaveContext.getMethodName() + "\");");
            codeBuffer.append("event.setData(data);");
            codeBuffer.append("com.asiainfo.hlog.client.HLogReflex.reveice(event);");
        codeBuffer.append("}");

        return codeBuffer.toString();
    }


}

package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

/**
 * 收集方法运行过程的代码
 * Created by chenfeng on 2015/3/17.
 */
public class RunningProcessesLogWeave extends AbstractLogWeave {

    private String[] depends = new String[]{LogIdWeave.ID,ClassMethodNameWeave.ID};


    public String[] getDependLogWeave() {
        return depends;
    }


    public String getName() {
        return "process";
    }

    @Override
    protected String getMcode() {
        return "h02";
    }

    public int getOrder() {
        return 10001;
    }


    public String beforWeave(LogWeaveContext logWeaveContext) {
        StringBuilder codeBuffer = new StringBuilder();
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
        StringBuilder codeBuffer = new StringBuilder();
        codeBuffer.append(METHOD_WriteProcessLog).append(BL);
        codeBuffer.append(Q).append(getMcode(logWeaveContext)).append(Q).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_LOG_ID).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_LOG_PID).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_CLASS_NAME).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_METHOD_NAME).append(D);
        codeBuffer.append("happenErr").append(D);
        codeBuffer.append("time1").append(");");
        return codeBuffer.toString();
    }


}

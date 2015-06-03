package com.asiainfo.hlog.agent.bytecode.javassist;

/**
 * Created by c on 2015/3/17.
 */
public class LogIdWeave implements ILogWeave {


    public static final String ID = new String("logId");


    public String toString() {
        return "LogIdWeave :" + getOrder();
    }


    public String[] getDependLogWeave() {
        return null;
    }


    public String getName() {
        return ID;
    }


    public int getOrder() {
        return -1;
    }



    public String beforWeave(LogWeaveContext logWeaveContext) {
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append("String _agent_Log_pId_ = com.asiainfo.hlog.agent.runtime.LogAgentContext.getThreadCurrentLogId();");
        codeBuffer.append("String _agent_Log_Id_ = com.asiainfo.hlog.agent.runtime.RuntimeContext.logId();");
        codeBuffer.append("if(_agent_Log_pId_==null){");
        codeBuffer.append("com.asiainfo.hlog.agent.runtime.LogAgentContext.clear();");
        codeBuffer.append("com.asiainfo.hlog.agent.runtime.LogAgentContext.setThreadLogGroupId(_agent_Log_Id_);");
        codeBuffer.append("}");
        codeBuffer.append("com.asiainfo.hlog.agent.runtime.LogAgentContext.setThreadCurrentLogId(_agent_Log_Id_);");
        return codeBuffer.toString();
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
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append("com.asiainfo.hlog.agent.runtime.LogAgentContext.setThreadCurrentLogId(_agent_Log_pId_);");
        return codeBuffer.toString();
    }
}

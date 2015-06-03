package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

/**
 * 收集异常信息
 * Created by chenfeng on 2015/4/29.
 */
public class ErrorLogWeave extends AbstractLogWeave {

    public static final String ID = "error";

    @Override
    public String[] getDependLogWeave() {
        return new String[]{LogIdWeave.ID};
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public int getOrder() {
        return 10002;
    }

    @Override
    protected String getMcode() {
        return "h01";
    }

    @Override
    public String beforWeave(LogWeaveContext logWeaveContext) {
        return null;
    }

    @Override
    public String tryWeave(LogWeaveContext logWeaveContext) {
        return null;
    }

    @Override
    public String exceptionWeave(LogWeaveContext logWeaveContext) {
        StringBuilder code  = new StringBuilder();
        code.append("if(_agent_Log_pId_==null){");
        code.append("String errorMsg = com.asiainfo.hlog.agent.runtime.RuntimeContext.error("+LogAgentContext.S_AGENT_ERR_PARAM_NAME+");");
        buildIfEnable(ID,logWeaveContext,code).append("{");
        code.append("com.asiainfo.hlog.client.model.ErrorLogData data = new com.asiainfo.hlog.client.model.ErrorLogData();");
        buildBaseLogData(logWeaveContext,getMcode(logWeaveContext),"errorMsg",code);
        buildReveiceEvent(logWeaveContext,code);
        code.append("}}");
        return code.toString();
    }

    @Override
    public String afterWeave(LogWeaveContext logWeaveContext) {
        return null;
    }

    @Override
    public String finallyWeave(LogWeaveContext logWeaveContext) {
        return null;
    }
}

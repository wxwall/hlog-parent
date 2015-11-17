package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

/**
 * 拦截记录入参,并直接返回
 * Created by chenfeng on 2015/5/31.
 */
public class InterceptParam2LogWeave extends AbstractLogWeave{

    public static final String ID = "interceptParam2";

    private final String DEF_MC = "h04";

    private String[] depends = new String[]{LogIdWeave.ID,DefinitionInParamLogWeave.ID,ClassMethodNameWeave.ID};

    @Override
    public String[] getDependLogWeave() {
        return depends;
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public int getOrder() {
        return 10000;
    }

    @Override
    public String beforWeave(LogWeaveContext logWeaveContext) {
        StringBuilder codeBuffer = new StringBuilder();
        getInParams(logWeaveContext, codeBuffer, true);
        codeBuffer.append(METHOD_WriteInterceptParam2).append(BL);
        codeBuffer.append(Q).append(getMcode(logWeaveContext)).append(Q).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_LOG_ID).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_LOG_PID).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_CLASS_NAME).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_METHOD_NAME).append(D);
        codeBuffer.append("_inParams);");

        return codeBuffer.toString();
    }

    @Override
    public String tryWeave(LogWeaveContext logWeaveContext) {
        return null;
    }

    @Override
    public String exceptionWeave(LogWeaveContext logWeaveContext) {
        return null;
    }

    @Override
    public String afterWeave(LogWeaveContext logWeaveContext) {
        return null;
    }

    @Override
    public String finallyWeave(LogWeaveContext logWeaveContext) {
        return null;
    }

    @Override
    protected String getMcode() {
        return DEF_MC;
    }

    @Override
    public boolean interrupt() {
        return true;
    }
}

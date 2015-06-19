package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

/**
 * 拦截记录入参
 * Created by chenfeng on 2015/5/31.
 */
public class InterceptParamLogWeave extends AbstractLogWeave{

    public static final String ID = "interceptParam";

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
        codeBuffer.append("com.asiainfo.hlog.agent.runtime.RuntimeContext.writeInterceptParam(");
        codeBuffer.append(Q).append(getMcode(logWeaveContext)).append(Q).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_LOG_ID).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_LOG_PID).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_CLASS_NAME).append(D);
        codeBuffer.append(LogAgentContext.S_AGENT_METHOD_NAME).append(D);
        codeBuffer.append("_inParams);");
        /*
        StringBuilder nameBuilder = new StringBuilder("new String[]{");
        String[] names = logWeaveContext.getParamNames();
        for (int i=0;i<names.length;i++){
            String name = names[i];
            if(i>0){
                nameBuilder.append(D);
            }
            nameBuilder.append(Q).append(name).append(Q);
        }
        nameBuilder.append("}");
        codeBuffer.append(nameBuilder);
        for(int i=1;i<=logWeaveContext.getParamNumber(); i++) {
            codeBuffer.append(D).append("(Object)$").append(i);
        }
        codeBuffer.append(");");
        */


        /*
        //增加开关配置信息
        buildIfEnable(this.getName(), logWeaveContext, codeBuffer).append("{");
        getInParams(logWeaveContext, codeBuffer, true);
        //将入参转JSON
        codeBuffer.append("java.util.Map map = com.asiainfo.hlog.client.helper.LogUtil.paramToJson(_inParams);");
        codeBuffer.append("com.asiainfo.hlog.client.model.LogData data = new com.asiainfo.hlog.client.model.LogData();");
        codeBuffer.append("data.putAll(map);");
        buildBaseLogData(logWeaveContext, getMcode(logWeaveContext), null, codeBuffer);
        buildReveiceEvent(logWeaveContext,codeBuffer);
        codeBuffer.append("}");
        */
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
        return "h04";
    }
}

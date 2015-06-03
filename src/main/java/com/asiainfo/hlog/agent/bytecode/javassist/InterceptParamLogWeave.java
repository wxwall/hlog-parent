package com.asiainfo.hlog.agent.bytecode.javassist;

/**
 * 拦截记录入参
 * Created by chenfeng on 2015/5/31.
 */
public class InterceptParamLogWeave extends AbstractLogWeave{

    public static final String ID = "interceptParam";

    private String[] depends = new String[]{LogIdWeave.ID,DefinitionInParamLogWeave.ID};

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
        //增加开关配置信息
        buildIfEnable(this.getName(),logWeaveContext,codeBuffer).append("{");
        getInParams(logWeaveContext, codeBuffer, true);
        //将入参转JSON
        codeBuffer.append("java.util.Map map = com.asiainfo.hlog.client.helper.LogUtil.paramToJson(_inParams);");
        codeBuffer.append("com.asiainfo.hlog.client.model.LogData data = new com.asiainfo.hlog.client.model.LogData();");
        codeBuffer.append("data.putAll(map);");
        buildBaseLogData(logWeaveContext, getMcode(logWeaveContext), null, codeBuffer);
        buildReveiceEvent(logWeaveContext,codeBuffer);
        codeBuffer.append("}");
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

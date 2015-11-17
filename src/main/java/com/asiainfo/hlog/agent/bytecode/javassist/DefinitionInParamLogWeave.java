package com.asiainfo.hlog.agent.bytecode.javassist;

/**
 * Created by chenfeng on 2015/5/31.
 */
public class DefinitionInParamLogWeave implements ILogWeave {

    public static final String ID = "defInParam";

    @Override
    public String[] getDependLogWeave() {
        return null;
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String beforWeave(LogWeaveContext logWeaveContext) {
        StringBuilder codeBuffer = new StringBuilder();
        codeBuffer.append("com.asiainfo.hlog.client.model.ParamObjs _inParams = null;");
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
    public boolean interrupt() {
        return false;
    }
}

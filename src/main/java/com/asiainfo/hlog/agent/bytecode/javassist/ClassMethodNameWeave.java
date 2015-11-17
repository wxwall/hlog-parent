package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

/**
 * 声音类和方法的变量
 * Created by c on 2015/3/17.
 */
public class ClassMethodNameWeave implements ILogWeave {


    public static final String ID = new String("classMethodName");


    public String toString() {
        return "ClassMethodNameWeave :" + getOrder();
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
        codeBuffer.append("String ").append(LogAgentContext.S_AGENT_CLASS_NAME).append(AbstractLogWeave.E)
                .append(AbstractLogWeave.Q).append(logWeaveContext.getClassName()).append(AbstractLogWeave.Q).append(AbstractLogWeave.S);
        codeBuffer.append("String ").append(LogAgentContext.S_AGENT_METHOD_NAME).append(AbstractLogWeave.E)
                .append(AbstractLogWeave.Q).append(logWeaveContext.getMethodName()).append(AbstractLogWeave.Q).append(AbstractLogWeave.S);
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
        return null;
    }

    @Override
    public boolean interrupt() {
        return false;
    }
}

package com.asiainfo.hlog.agent.bytecode.javassist;

/**
 * Created by c on 2015/3/16.
 */
public class LogForJWeave implements ILogWeave {

    public String[] getDependLogWeave() {
        return new String[0];
    }


    public String getName() {
        return "log4j";
    }


    public int getOrder() {
        return 0;
    }


    public String beforWeave(LogWeaveContext logWeaveContext) {
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

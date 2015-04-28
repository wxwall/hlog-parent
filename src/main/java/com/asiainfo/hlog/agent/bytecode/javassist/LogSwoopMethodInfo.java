package com.asiainfo.hlog.agent.bytecode.javassist;

/**
 * Created by c on 2015/3/16.
 */
public class LogSwoopMethodInfo {

    private String name;

    private String[] paramTypes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(String[] paramTypes) {
        this.paramTypes = paramTypes;
    }
}

package com.asiainfo.hlog.agent.bytecode.javassist;

import java.util.List;

/**
 * Created by c on 2015/3/16.
 */
public class LogSwoopRule {

    private String[] className ;
    private List<LogSwoopMethodInfo> methodInfoList ;

    private List<ILogWeave> weaves ;

    public String[] getClassName() {
        return className;
    }

    public void setClassName(String[] className) {
        this.className = className;
    }

    public List<LogSwoopMethodInfo> getMethodInfoList() {
        return methodInfoList;
    }

    public void setMethodInfoList(List<LogSwoopMethodInfo> methodInfoList) {
        this.methodInfoList = methodInfoList;
    }

    public List<ILogWeave> getWeaves() {
        return weaves;
    }

    public void setWeaves(List<ILogWeave> weaves) {
        this.weaves = weaves;
    }
}

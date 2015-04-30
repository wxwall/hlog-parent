package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.client.config.Path;

import java.util.List;

/**
 * Created by c on 2015/3/16.
 */
public class LogSwoopRule {

    private Path path ;
    private List<LogSwoopMethodInfo> methodInfoList ;

    private List<ILogWeave> weaves ;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
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

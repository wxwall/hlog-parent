package com.asiainfo.hlog.agent.bytecode.javassist;

/**
 * Created by chenfeng on 2015/3/17.
 */
public class LogWeaveContext {

    private String hashCode ;
    private LogSwoopRule rule;
    private String className;
    private String methodName;
    private String[] paramNames;

    private boolean isCreateInParams;
    /**
     * 第三方日志框架的采集栈深度
     */
    private int loggerStackDepth = 2;

    private int paramNumber = 0;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public int getParamNumber() {
        return paramNumber;
    }

    public void setParamNumber(int paramNumber) {
        this.paramNumber = paramNumber;
    }

    public LogSwoopRule getRule() {
        return rule;
    }

    public void setRule(LogSwoopRule rule) {
        this.rule = rule;
    }

    public boolean isCreateInParams() {
        return isCreateInParams;
    }

    public void setCreateInParams(boolean isCreateInParams) {
        this.isCreateInParams = isCreateInParams;
    }

    public int getLoggerStackDepth() {
        return loggerStackDepth;
    }

    public void setLoggerStackDepth(int loggerStackDepth) {
        this.loggerStackDepth = loggerStackDepth;
    }

    public String getHashCode(){
        if(hashCode==null){
            hashCode = Integer.toString(Math.abs(className.hashCode()),32);
        }
        return hashCode;
    }



}

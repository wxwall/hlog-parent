package com.asiainfo.hlog.agent.bytecode.javassist;

/**
 * Created by c on 2015/3/17.
 */
public class LogWeaveContext {

    private String hashCode ;

    private String className;
    private String methodName;
    private String[] paramNames;

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

    public String getHashCode(){
        if(hashCode==null){
            hashCode = Integer.toString(Math.abs(className.hashCode()),32);
        }
        return hashCode;
    }

}

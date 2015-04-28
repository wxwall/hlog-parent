package com.asiainfo.hlog.client.model;

/**
 * 日志事件,当收集到一条日志数据时便生产一个event对象
 * Created by chenfeng on 2015/4/22.
 */
public class Event<T> {

    private String className;

    private String methodName;

    private T data;

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

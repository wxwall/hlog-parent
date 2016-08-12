package com.asiainfo.hlog.agent.runtime.dto;

/**
 * Created by chenYuanlong on 2016/8/11.
 * 事务耗时
 */
public class TranElapsedTimeDto {
    private long startTime;
    private String methodName;
    private long elapsedTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getElapsedTime () {
        return elapsedTime;
    }

    public void setElapsed (long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}

package com.asiainfo.hlog.agent.runtime.dto;

/**
 * Created by chenYuanlong on 2016/8/11.
 * 事务耗时
 */
public class TranCostDto {
    private long startTime;
    private String methodName;
    private long cost;
    private String id;
    private int sqlCount = 0;

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

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSqlCount(){
        return  this.sqlCount;
    }

    public void sqlCountIncrement(){
        this.sqlCount += 1;
    }
}

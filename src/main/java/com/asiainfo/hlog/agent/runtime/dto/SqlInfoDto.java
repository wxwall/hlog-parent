package com.asiainfo.hlog.agent.runtime.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuan on 2017/7/20.
 */
public class SqlInfoDto {
    private String sql;
    private List params = new ArrayList();
    private int paramCount = 0;
    private long startTime = 0;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List getParams() {
        return params;
    }

    public void setParams(List params) {
        this.params = params;
    }

    public int getParamCount() {
        return paramCount;
    }

    public void setParamCount(int paramsCount) {
        this.paramCount = paramsCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}

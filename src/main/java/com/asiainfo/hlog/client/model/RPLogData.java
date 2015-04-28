package com.asiainfo.hlog.client.model;

/**
 * Created by c on 2015/3/17.
 */
public class RPLogData extends LogData {

    private String clazz;
    private String method;
    private String[] params;
    private long spend ;
    private int status ;

    public RPLogData(){
        setType(2);
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public long getSpend() {
        return spend;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

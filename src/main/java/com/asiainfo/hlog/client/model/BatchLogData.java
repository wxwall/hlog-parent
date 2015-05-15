package com.asiainfo.hlog.client.model;

import java.util.List;

/**
 * Created by chenfeng on 2015/5/12.
 */
public class BatchLogData {
    private String server;
    private String ip;
    private List<LogData> data ;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<LogData> getData() {
        return data;
    }

    public void setData(List<LogData> data) {
        this.data = data;
    }
}

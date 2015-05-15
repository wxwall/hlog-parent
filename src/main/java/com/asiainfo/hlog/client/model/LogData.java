package com.asiainfo.hlog.client.model;

/**
 * type:0异常日志,1Logger,2运行过程
 * Created by c on 2015/3/17.
 */
public class LogData {
    private String ip;
    private String server;
    private String mc ;
    private long time;
    private String gId;
    private String id;
    private String pId;
    //private int seq ;

    private String desc ;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getMc() {
        return mc;
    }

    public void setMc(String type) {
        this.mc = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getGId() {
        return gId;
    }

    public void setGId(String groupId) {
        this.gId = groupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPId() {
        return pId;
    }

    public void setPId(String pId) {
        this.pId = pId;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public String toString() {

        return "LogData{" +
                "mc=" + mc +
                ", time=" + time +
                ", gId='" + gId + '\'' +
                ", id='" + id + '\'' +
                ", pid='" + pId + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}

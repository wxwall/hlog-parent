package com.asiainfo.hlog.client.model;

/**
 * type:0异常日志,1Logger,2运行过程
 * Created by c on 2015/3/17.
 */
public class LogData {
    private String type ;
    private long time;
    private String gId;
    private String id;
    private String pid;
    //private int seq ;

    private String desc ;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public String toString() {

        return "LogData{" +
                "type=" + type +
                ", time=" + time +
                ", groupId='" + gId + '\'' +
                ", id='" + id + '\'' +
                ", pid='" + pid + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}

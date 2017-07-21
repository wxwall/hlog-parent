package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.agent.runtime.HLogMonitor;

import java.sql.PreparedStatement;

/**
 * Created by yuan on 2017/7/20.
 */
public class Twst {
    public static void main(String[] args) {
        HLogMonitor.sendHibernateSql();
    }

    public static void bind(PreparedStatement st, Object value){
        HLogMonitor.addHibernateParam(value);
    }
    
    public static void log(String sql){
        HLogMonitor.setHibernateSql(sql);
    }
}

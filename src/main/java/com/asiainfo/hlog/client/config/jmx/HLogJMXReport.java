package com.asiainfo.hlog.client.config.jmx;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.comm.jmx.MBeanServerAgent;

import java.io.IOException;
import java.net.BindException;

/**
 * Created by chenfeng on 2015/5/12.
 */
public class HLogJMXReport {

    private static HLogJMXReport report = null;

    private MBeanServerAgent agent = null;

    private PropMbean propMbean = null;

    private RunStatusMBean runStatusMBean = null;

    private boolean isRunServer = false;

    private HLogJMXReport(){
        //注册JMX观察
        runStatusMBean = new RunStatusMBean();

        if(HLogConfig.jmxEnable && agent==null) {
            agent = new MBeanServerAgent("HLogAgentReport");
            propMbean = new PropMbean();
            agent.registerBean(propMbean,new PropMbeanExportBuilder());
            agent.registerBean(runStatusMBean);
        }
    }

    public static HLogJMXReport getHLogJMXReport(){
        if(report==null) {
            synchronized (HLogJMXReport.class) {
                if (report == null) {
                    report = new HLogJMXReport();
                }
            }
        }
        return report;
    }

    public void start(){
        synchronized (report){
            if(HLogConfig.jmxEnable && isRunServer==false) {
                int jmxPost = Integer.parseInt(HLogConfig.jmxPost);
                while (!isRunServer){
                    try{
                        String url = agent.startServer(jmxPost);
                        Logger.debug("开启JMX服务:{0}", url);
                        isRunServer = true;
                    }catch (IOException e){
                        //Logger.error(e);
                        Throwable cause = e.getCause();
                        if(cause instanceof BindException){
                            System.out.println("JMX服务端["+jmxPost+"]被占用,自动启用["+(jmxPost+1)+"]端口");
                            jmxPost = jmxPost+1;
                        }
                    }
                }
            }
        }
    }

    public RunStatusMBean getRunStatusMBean(){
        return runStatusMBean;
    }


}

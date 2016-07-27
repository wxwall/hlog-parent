package com.asiainfo.hlog.agent.jvm;


import com.asiainfo.hlog.agent.runtime.LogAgentContext;
import com.asiainfo.hlog.agent.runtime.RuntimeContext;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.LogData;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.*;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lenovo on 2016/7/26.
 */
public class HLogJvmReport {
    private static Object pLock = new Object();
    private static HLogJvmReport p_instance = null;
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public static HLogJvmReport getInstance(){
        synchronized(pLock){
            if(null == p_instance){
                p_instance = new HLogJvmReport();
            }
        }
        return p_instance;
    }
    private HLogJvmReport() {

    }

    public void acquireJvmInfo(){
        Logger.debug("采集jvm信息");
        //堆内存
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        LogData logData = createLogData();
        MemoryUsage heap = mbean.getHeapMemoryUsage();
        logData.put("heapInit",heap.getInit());
        logData.put("heapMax",heap.getMax());
        logData.put("heapUsed",heap.getUsed());
        logData.put("heapCommitted",heap.getCommitted());
        logData.put("heapRate",(heap.getUsed()*100)/heap.getMax());

        List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean:list){
            //永久区或jkd8+元空间
            if("Perm Gen".equals(bean.getName()) || "Metaspace".equals(bean.getName())){
                MemoryUsage usage = bean.getUsage();
                logData.put("permGenInit",usage.getInit());
                logData.put("permGenMax",usage.getMax());
                logData.put("permGenUsed",usage.getUsed());
                logData.put("permGenRate",(usage.getUsed()*100)/usage.getMax());
            }
        }
        //操作系统物理内存
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        logData.put("phyMemFree",osmxb.getFreePhysicalMemorySize());
        logData.put("phyMemTotal",osmxb.getTotalPhysicalMemorySize());
        logData.put("phyMemRate",(osmxb.getFreePhysicalMemorySize()*100)/osmxb.getTotalPhysicalMemorySize());
        //logData.put("cpuLoad",osmxb.getSystemCpuLoad());//jdk7+
        //线程
        ThreadMXBean thread = ManagementFactory.getThreadMXBean();
        logData.put("threadPeak",thread.getPeakThreadCount());
        logData.put("threadLive",thread.getThreadCount());
        RuntimeContext.writeEvent("jvm.log",null,logData);
    }


    public LogData createLogData() {
        LogData logData = new LogData();
        logData.setMc("jvm");
        logData.setId(RuntimeContext.logId());
        logData.setPId(null);
        logData.setGId(null);
        logData.setTime(System.currentTimeMillis());
        return logData;
    }

    private Runnable task = new Runnable() {
        public void run() {
            try {
                String enableMonitor = HLogConfig.getInstance().getProperty(Constants.KEY_ENABLE_MONITOR_JVM, "true");
                if ("true".equals(enableMonitor.toLowerCase())) {
                    acquireJvmInfo();
                }
            }catch (Throwable t){
                Logger.error("获取JVM信息出错",t);
            }
        }
    };
    public void start(){
        try {
            String interval = HLogConfig.getInstance().getProperty(Constants.KEY_MONITOR_JVM_INTERVAL_TIME, "2");
            int second = Calendar.getInstance().get(Calendar.SECOND);
            int delay = 60 - second;
            //延迟delay秒后以每隔interval秒执行异常task任务
            scheduledExecutorService.scheduleAtFixedRate(
                    task,
                    delay,
                    Integer.parseInt(interval),
                    TimeUnit.SECONDS);

        }catch (Throwable t){
            Logger.error("打开JVM监控失败",t);
        }

    }
}

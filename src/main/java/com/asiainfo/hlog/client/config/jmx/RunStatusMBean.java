package com.asiainfo.hlog.client.config.jmx;

import com.asiainfo.hlog.comm.jmx.AnnotationMBean;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hlog的运行状态
 * Created by chenfeng on 2015/5/12.
 */
@AnnotationMBean(write = false)
public class RunStatusMBean {

    private String startDate = null;

    /**
     * 被植入代码的class个数
     */
    private AtomicLong weaveClassNum = new AtomicLong(0);
    /**
     * 被植入代码异常class个数
     */
    private AtomicLong weaveErrClassNum = new AtomicLong(0);
    /**
     * 收集的hlog总个数
     */
    private AtomicLong hlogTotalNum = new AtomicLong(0);

    /**
     * 正常运行的天数
     */
    private AtomicLong hlogDayNum = new AtomicLong(1);

    /**
     * 当前收集的hlog个数
     */
    private AtomicLong hlogNum = new AtomicLong(0);

    /**
     * hlog处理个数
     */
    private AtomicLong hlogHandleTotalNum = new AtomicLong(0);

    /**
     * hlog今天处理个数
     */
    private AtomicLong hlogHandleNum = new AtomicLong(0);


    public RunStatusMBean(){
        startDate = new Date().toString();
        /**
         * 每天0点都执行一次
         */
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23); // 控制时
        calendar.set(Calendar.MINUTE, 59); // 控制分
        calendar.set(Calendar.SECOND, 59); // 控制秒

        // 得出执行任务的时间,此处为今天的12：00：00
        Date time = calendar.getTime();
        //如果第一次执行定时任务的时间 小于当前的时间
        //此时要在 第一次执行定时任务的时间加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
        if (time.before(new Date())) {
            Calendar startDT = Calendar.getInstance();
            startDT.setTime(time);
            startDT.add(Calendar.DAY_OF_MONTH, 1);
            time = startDT.getTime();

        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                incrementHlogDayNum();
                repeatNum();
            }
        }, time, 1000 * 60 * 60 * 24);
    }

    public void incrementWeaveClassNum(){
        weaveClassNum.incrementAndGet();
    }

    public void incrementweaveErrClassNum(){
        weaveErrClassNum.incrementAndGet();
    }

    public void incrementHlogTotalNum(){
        hlogTotalNum.incrementAndGet();
    }

    public void incrementHlogDayNum(){
        hlogDayNum.incrementAndGet();
    }

    public void repeatNum(){
        hlogNum.getAndSet(0);
        hlogHandleNum.getAndSet(0);
    }

    public void incrementHlogNum(){
        hlogNum.incrementAndGet();
    }
    public void incrementHlogHandleTotalNum(int handNum){
        hlogHandleTotalNum.getAndAdd(handNum);
    }
    public void incrementHlogHandleNum(int handNum){
        hlogHandleNum.getAndAdd(handNum);
    }
}

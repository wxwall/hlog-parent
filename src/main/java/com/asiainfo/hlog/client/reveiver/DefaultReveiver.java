package com.asiainfo.hlog.client.reveiver;

import com.asiainfo.hlog.client.IHLogReveiver;
import com.asiainfo.hlog.client.ITransmitter;
import com.asiainfo.hlog.client.TransmitterFactory;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.jmx.HLogJMXReport;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.Event;
import com.asiainfo.hlog.client.model.LogData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 默认的日志接收处理,所有的监控到的日志都将传送到reveice方法进行下一步处理</br>
 *
 * Created by chenfeng on 2015/4/22.
 */
public class DefaultReveiver implements IHLogReveiver {

    private Map<String,BlockingQueue<LogData>> mapQueue = new ConcurrentHashMap<String,BlockingQueue<LogData>>();

    /**
     * 内存队列大小
     */
    private int memQueueSize = 2000 ;

    /**
     * 每次处理量
     */
    private int eachQuantity = 200;

    /**
     * 处理频率
     */
    private int frequency = 100;


    private ExecutorService service = null;

    private Runnable task = null;

    private boolean start = false;

    public DefaultReveiver(){
        String queueSize = HLogConfig.getInstance()
                .getProperty(Constants.KEY_DEF_REV_MEM_QUEUE_SIZE, "2000");
        String eachQuantityS = HLogConfig.getInstance()
                .getProperty(Constants.KEY_DEF_REV_EACH_QUANTITY, "200");
        String frequencyS = HLogConfig.getInstance()
                .getProperty(Constants.KEY_DEF_REV_FREQUENCY, "100");

        memQueueSize = Integer.parseInt(queueSize);
        eachQuantity = Integer.parseInt(eachQuantityS);
        frequency = Integer.parseInt(frequencyS);

        service = Executors.newSingleThreadExecutor();
        task = new QueueConsumer();
    }

    private Set<String> getHandlerByEvent(Event<LogData> event){

        HLogConfig config = HLogConfig.getInstance();

        Set<String> handlers = LogUtil.suitableConfig(event.getClassName(), event.getMethodName()
                , config.getRuntimeHandlerCofnig());

        return handlers;
    }

    private BlockingQueue<LogData> getQueue(String handlerName){
        BlockingQueue<LogData> queue ;
        if(mapQueue.containsKey(handlerName)){
            return mapQueue.get(handlerName);
        }else{
            synchronized (mapQueue){
                queue = mapQueue.get(handlerName);
                if(queue==null){
                    queue = new LinkedBlockingQueue<LogData>(memQueueSize);
                    mapQueue.put(handlerName,queue);
                }
            }
        }

        return queue;
    }

    @Override
    public void reveice(Event<LogData> event) {
        //获取关注该事件的处理器
        Set<String> handlerNames = getHandlerByEvent(event);

        if(Logger.isTrace()){
            Logger.trace("[{0}.{1}]配置的处理器为:{2}",event.getClassName(),event.getMethodName(),handlerNames);
        }

        if(handlerNames==null){
            return;
        }

        for (String handlerName : handlerNames){
            BlockingQueue<LogData> queue = getQueue(handlerName);
            boolean isSuccess = queue.offer(event.getData());
            if (!isSuccess) {
                //当队列满时可以选择阻塞完成或者直接将数据提交消费者
                //再次阻塞完成
                try {
                    queue.put(event.getData());
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        }
        HLogJMXReport.getHLogJMXReport().getRunStatusMBean().incrementHlogTotalNum();
        HLogJMXReport.getHLogJMXReport().getRunStatusMBean().incrementHlogNum();
        if (!start) {
            start();
        }
    }


    /**
     * 默认的日志内存队列消费者
     * Created by chenfeng on 2015/4/14.
     */
    class QueueConsumer implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                if (!Thread.currentThread().isInterrupted()) {
                    try {
                        //TODO 做成配置
                        Thread.sleep(frequency);
                    } catch (InterruptedException e) {
                    }
                }

                Set<ConcurrentHashMap.Entry<String,BlockingQueue<LogData>>> entries
                        = mapQueue.entrySet();

                int handNum = 0 ;
                for (ConcurrentHashMap.Entry<String,BlockingQueue<LogData>> entry : entries){
                    BlockingQueue<LogData> queue = entry.getValue();
                    if(queue.isEmpty()){
                        continue;
                    }
                    //TODO 做成可配置 500
                    List<LogData> dataList = new ArrayList<LogData>(eachQuantity);
                    queue.drainTo(dataList, eachQuantity);
                    handNum = dataList.size();
                    if (!dataList.isEmpty()) {
                        ITransmitter transmitter = null;
                        try{
                            transmitter = TransmitterFactory
                                    .getTransmitter(entry.getKey());
                            if(transmitter!=null){
                                transmitter.transition(dataList);
                            }
                        }catch (Throwable e){
                            //TODO 发生异常了,需要考虑日志数据的重发问题
                            if(transmitter!=null){
                                //如果处理器不空时
                            }
                            //e.printStackTrace();
                            Logger.error("在[{0}]日志处理时出现异常",e,entry.getKey());
                        }
                    }
                }
                HLogJMXReport.getHLogJMXReport().getRunStatusMBean().incrementHlogHandleTotalNum(handNum);
                HLogJMXReport.getHLogJMXReport().getRunStatusMBean().incrementHlogHandleNum(handNum);
            }
        }
    }


    public void start() {
        start = true;
        service.execute(task);
    }

    /**
     * 任务结束函数.
     */
    //@PreDestroy
    public void stop(){
        try {
            service.shutdownNow();
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Pool did not terminate");
            }
            mapQueue.clear();
            mapQueue = null;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}

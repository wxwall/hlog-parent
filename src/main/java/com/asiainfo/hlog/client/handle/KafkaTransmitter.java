package com.asiainfo.hlog.client.handle;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.BatchLogData;
import com.asiainfo.hlog.client.model.LogData;
import kafka.common.FailedToSendMessageException;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import scala.Int;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by chenfeng on 2015/4/15.
 */
public class KafkaTransmitter extends AbstractTransmitter {

    private Producer producer = null;

    private String topic = "hlog-data-q";

    private final String def_serializer = "kafka.serializer.StringEncoder";

    private String kafka_server_list = "";

    private final String def_request_timeout_ms = "600";
    private final String key_topic_name = "topic.name";
    private final String key_metadata_broker_list = "metadata.broker.list";
    private final String key_serializer_class = "serializer.class";

    private final String key_request_timeout_ms = "request.timeout.ms";

    private final String key_keyed_message_size = "keyed.message.size";

    private final int defKeyedMessageSize = 20;

    private int keyedMessageSize = defKeyedMessageSize;

    public KafkaTransmitter(){
        name = "kafka";
    }

    @Override
    public void doInitialize() {

        HLogConfig config = HLogConfig.getInstance();
        //获取话题配置
        topic = config.getProperty(getWrapKey(key_topic_name),topic);

        Properties properties = new Properties();

        String wrapKey = getWrapKey(key_metadata_broker_list);
        String val = config.getProperty(wrapKey);
        if(val==null){
            throw new IllegalArgumentException("缺少["+wrapKey+"]配置信息.");
        }
        kafka_server_list = val;
        properties.put(key_metadata_broker_list,val);

        wrapKey = getWrapKey(key_serializer_class);
        val = config.getProperty(wrapKey);
        if(val==null){
            val = def_serializer;
        }
        properties.put(key_serializer_class,val);

        wrapKey = getWrapKey(key_request_timeout_ms);
        val = config.getProperty(wrapKey);
        if(val==null){
            val = def_request_timeout_ms;
        }
        properties.put(key_request_timeout_ms,val);

        //
        wrapKey = getWrapKey(key_keyed_message_size);
        val = config.getProperty(wrapKey);
        if(val!=null){
            keyedMessageSize = Integer.parseInt(val);
        }

        properties.put("partitioner.class", "com.asiainfo.hlog.client.handle.KafkaPartitioner");

        //构造生产者属性
        ProducerConfig producerconfig = new ProducerConfig(properties);
        //构造实例
        producer = new Producer<String, String>(producerconfig);
    }

    @Override
    public void stop() {
        if(producer!=null){
            producer.close();
        }
    }

    @Override
    public void transition(List<LogData> datas) {
        if (producer==null){
            Logger.warn("无法通过kafka发送日志数据,请确认[{0}]服务地址是否通畅.",kafka_server_list);
            //TODO 将日志数据持久层本地.
            //TODO 考虑建立重连
            return;
        }

        //将datas拆分成多个KeyedMessage来发送,默认是20条一个KeyedMessage
        int size = datas.size();

        if(size==0){
            return;
        }
        int batchNum = size/keyedMessageSize + (size%keyedMessageSize==0?0:1);
        List messageList = new ArrayList<KeyedMessage<String, String>>(batchNum);
        LogData[] batchDatas = datas.toArray(new LogData[size]);
        LogData[] batchList = new LogData[keyedMessageSize];
        for (int num=0;num<batchNum;num++){
            int bsize = size - (num*keyedMessageSize);
            if(bsize>keyedMessageSize){
                bsize = keyedMessageSize;
                batchList = new LogData[bsize];
            }
            System.arraycopy(batchDatas,num*keyedMessageSize,batchList,0,bsize);
            String msg = messageConver.convert(batchList);
            //构造消息体
            KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic,String.valueOf(num+1) ,msg);
            messageList.add(data);
        }
        try{
            if(Logger.isTrace()){
                Logger.trace("kafka list size={0} send data：{1}",messageList.size(),messageList);
            }
            //消息发送
            producer.send(messageList);
    }catch (FailedToSendMessageException ex){
        Logger.error("kafka发送消息到[{0}]服务异常",ex,kafka_server_list);
    }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(15000);
        List<LogData> list =  new ArrayList<LogData>(1000000);
        for (int i=0;i<1000000;i++){
            LogData data = new LogData();
            data.setIp("------"+i);
            data.setServer("======="+i);
            data.setId("-=-==-=-=-=-=-=-=-="+i);
            data.setGId("00000000000000000000"+i);
            data.setPId("99999999999999999999"+i);
            data.setDesc("7777777777777777777777777777"+i);
            list.add(data);
        }
        System.out.println("--add finished");
        Thread.sleep(5000);
        /*
        LogData[] datas = list.toArray(new LogData[1000000]);
        LogData[] ds = new LogData[10];
        for (int i=0;i<1000;i++){
            System.out.println(i);
            for(int j=1;j<=10;j++){
                System.arraycopy(datas,(j-1)*10,ds,0,10);

            }
            Thread.sleep(1000);
        }*/

        Thread.sleep(5000*1000);
    }
}

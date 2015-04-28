package com.asiainfo.hlog.client.handle;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.LogData;

import java.util.List;
import java.util.Properties;

import kafka.common.FailedToSendMessageException;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

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
        //消息
        String msg = messageConver.convert(datas);
        //构造消息体
        KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, msg);
        //消息发送
        try{
            producer.send(data);
        }catch (FailedToSendMessageException ex){
            Logger.error("kafka发送消息到[{0}]服务异常",ex,kafka_server_list);
        }
    }
}

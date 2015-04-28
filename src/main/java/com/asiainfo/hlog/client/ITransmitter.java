package com.asiainfo.hlog.client;

import com.asiainfo.hlog.client.model.LogData;

import java.util.List;
import java.util.Properties;

/**
 * 日志处理器</br>
 * 即将应用端的日志队列数据消费掉:</br>
 * 1、直接发送到日志服务(http)</br>
 * 2、将日志数据存储到消息队列(kafka)中</br>
 * Created by chenfeng on 2015/4/14.
 */
public interface ITransmitter {

    /**
     * 定义的名称
     */
    void setName(String name);

    /**
     * 处理队列传递过来的日志
     * @param datas
     */
    void transition(List<LogData> datas);

    /**
     * 初始化
     */
    void init();

    /**
     * 停止调用,可用于释放资源
     */
    void stop();
}

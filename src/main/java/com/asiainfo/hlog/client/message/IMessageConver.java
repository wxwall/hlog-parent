package com.asiainfo.hlog.client.message;

import java.util.List;

/**
 * 日志消息转化器</br>
 * LogData->JSON</br>
 * LogData->txt</br>
 * LogData->xml</br>
 * Created by chenfeng on 2015/4/16.
 */
public interface IMessageConver {

    /**
     * 数据转化
     * @param dataList
     * @return
     */
    String convert(List<Object> dataList);

    String convert(Object logData);
}

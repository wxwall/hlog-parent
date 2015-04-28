package com.asiainfo.hlog.client;

import com.asiainfo.hlog.client.model.Event;
import com.asiainfo.hlog.client.model.LogData;

/**
 * 日志接收器</br>
 * 接收各类日志数据
 * Created by chenfeng on 2015/4/22.
 */
public interface IHLogReveiver {

    /**
     * 接收包装后日志的入口
     * @param event
     */
    void reveice(Event<LogData> event);

}

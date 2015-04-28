package com.asiainfo.hlog.client.handle;

import com.asiainfo.hlog.client.model.LogData;

import java.util.List;
import java.util.Properties;

/**
 * Created by chenfeng on 2015/4/18.
 */
public class ConsoleTransmitter extends AbstractTransmitter {

    public ConsoleTransmitter(){
        name = "console";
    }

    @Override
    public void transition(List<LogData> datas) {
        for (Object logData : datas){
            String log = messageConver.convert(logData);
            System.out.println(log);
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void doInitialize() {
    }

}

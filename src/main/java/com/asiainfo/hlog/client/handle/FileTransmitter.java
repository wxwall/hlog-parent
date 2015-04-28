package com.asiainfo.hlog.client.handle;

import com.asiainfo.hlog.client.ITransmitter;
import com.asiainfo.hlog.client.model.LogData;

import java.util.List;

/**
 * Created by chenfeng on 2015/4/15.
 */
public class FileTransmitter extends AbstractTransmitter {

    public FileTransmitter(){
        name = "file";
    }

    @Override
    public void doInitialize() {

    }

    @Override
    public void stop() {

    }

    public void transition(List<LogData> datas) {
        //TODO 写文件
    }
}

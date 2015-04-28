package com.asiainfo.hlog.client.handle;

import com.asiainfo.hlog.client.model.LogData;

import java.util.List;

/**
 * Created by chenfeng on 2015/4/15.
 */
public class FtpTransmitter extends AbstractTransmitter {

    public FtpTransmitter(){
        name = "ftp";
    }
    @Override
    public void doInitialize() {

    }

    @Override
    public void transition(List<LogData> datas) {

    }

    @Override
    public void stop() {

    }
}

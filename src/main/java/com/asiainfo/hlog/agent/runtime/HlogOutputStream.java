package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.client.model.LogData;

import java.io.IOException;

/**
 * 用于采集System.out或System.err的输出日志
 * Created by chenfeng on 2015/6/18.
 */
public class HLogOutputStream extends java.io.OutputStream {

    private String mcode ;
    private StringBuffer buffer = new StringBuffer();
    public HLogOutputStream(String mcode){
        this.mcode = mcode;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void write(int b) throws IOException {
        char c = (char)b;
        //得到数据
        //String msg = new String(b,off,len);
        buffer.append(c);
        if(c != '\n'){
            return;
        }
        String msg = buffer.toString();
        buffer = new StringBuffer();
        if(msg.startsWith("-hlog:")){
            return;
        }
        String _agent_Log_Id_ = com.asiainfo.hlog.agent.runtime.RuntimeContext.logId();
        String _agent_Log_pId_ = com.asiainfo.hlog.agent.runtime.RuntimeContext.buildLogPId(_agent_Log_Id_);
        LogData logData = new LogData();
        logData.setMc(mcode);
        logData.setId(_agent_Log_Id_);
        logData.setPId(_agent_Log_pId_);
        logData.setGId(LogAgentContext.getThreadLogGroupId());
        long time = System.currentTimeMillis();
        logData.setTime(time);
        logData.setDesc(msg);
        RuntimeContext.writeEvent(System.class.getName(),mcode,logData);
    }

    @Override
    public void flush() throws IOException {
    }
}

package com.asiainfo.hlog.client.handle;

import com.asiainfo.hlog.client.ITransmitter;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.ClassHelper;
import com.asiainfo.hlog.client.message.IMessageConver;
import com.asiainfo.hlog.client.message.JsonMessageConver;

/**
 * Created by chenfeng on 2015/4/17.
 */
public abstract class AbstractTransmitter implements ITransmitter {
    /**
     * 插象传递器公共的信息</br>
     * 1、消息转处理器
     * @param properties
     */
    protected IMessageConver messageConver = null;

    /**
     * 定义这个处理器的别名
     */
    protected String name ;

    /**
     * 定义处理器对处的日志消息转换器
     */
    protected final String key_msg_conver = "msg.conver";

    @Override
    public void init() {
        //通过配置获取消息转化器,如果为空直接采用默认JSON
        String msgConverKey = getWrapKey(key_msg_conver);
        String msgConverCls = HLogConfig.getInstance().getProperty(msgConverKey);
        if(msgConverCls==null){
            messageConver = new JsonMessageConver();
        }else{
            messageConver = (IMessageConver)ClassHelper.newInstance(msgConverCls);
        }
        doInitialize();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param key
     * @return
     */
    protected String getWrapKey(String key){
        return new StringBuffer(Constants.KEY_HLOG_HANDLER)
                .append(name).append(".")
                .append(key).toString();
    }

    /**
     * 留给具体的转送器做具体的初始化工作
     */
    abstract public void doInitialize();
}

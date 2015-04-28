package com.asiainfo.hlog.client;

import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.handle.ConsoleTransmitter;
import com.asiainfo.hlog.client.handle.FileTransmitter;
import com.asiainfo.hlog.client.handle.HttpTransmitter;
import com.asiainfo.hlog.client.handle.KafkaTransmitter;
import com.asiainfo.hlog.client.helper.LoaderHelper;
import com.asiainfo.hlog.client.helper.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志处理器工厂类
 * Created by chenfeng on 2015/4/22.
 */
public class TransmitterFactory {

    private static List<String> classNotFoundList = new ArrayList<String>();

    /**
     * 已经实例化过的处理器
     */
    private static Map<String,ITransmitter> transmitterMap = new ConcurrentHashMap<String,ITransmitter>();

    /**
     * 默认的别名与处理器class的对应关系
     */
    private static Map<String,Class<? extends ITransmitter>> defTransmitterCls = new ConcurrentHashMap<String,Class<? extends ITransmitter>>();
    static {
        defTransmitterCls.put("kafka",KafkaTransmitter.class);
        defTransmitterCls.put("file",FileTransmitter.class);
        defTransmitterCls.put("console",ConsoleTransmitter.class);
        defTransmitterCls.put("http", HttpTransmitter.class);
    }


    /**
     * 根据别名取得收送处理器的实例
     * @param name
     * @return
     */
    public static ITransmitter getTransmitter(String name){

        //看是否已经有实例化对象了
        if(transmitterMap.containsKey(name)){
            return transmitterMap.get(name);
        }

        ITransmitter transmitter = null;
        synchronized (transmitterMap){
            //看是否已经有实例化对象了
            if(transmitterMap.containsKey(name)){
                return transmitterMap.get(name);
            }
            //看是否已经实例过，但是失败了.
            if(classNotFoundList.contains(name)){
               return null;
            }
            //根据name从配置信息里看是否有读取到对应的配置类
            String handlerClass = HLogConfig.getInstance().getProperty(Constants.KEY_HLOG_HANDLER+name);
            Class<?> clazz = null;
            try{
                if(handlerClass==null){
                    if(defTransmitterCls.containsKey(name)){
                        clazz = defTransmitterCls.get(name);
                    }
                }else{
                    if(defTransmitterCls.containsKey(handlerClass)){
                        clazz = defTransmitterCls.get(handlerClass);
                    }else{
                        clazz = LoaderHelper.loadClass(handlerClass);
                    }
                }
                transmitter = (ITransmitter)clazz.newInstance();
                transmitter.setName(name);
                transmitter.init();
                if(Logger.isDebug()){
                    Logger.debug("构建[{0}]对应的类[{1}]实例完成",name,handlerClass!=null?handlerClass:clazz);
                }
            }catch (Exception cfe){
                Logger.error("构建[{0}]对应的类[{1}]实例时异常",
                        cfe,name,handlerClass!=null?handlerClass:clazz);
            }
            if(transmitter!=null){
                transmitterMap.put(name,transmitter);
            }else if(!classNotFoundList.contains(name)){
                classNotFoundList.add(name);
            }
        }

        return transmitter;
    }

}

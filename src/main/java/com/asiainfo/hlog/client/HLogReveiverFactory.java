package com.asiainfo.hlog.client;

import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.LoaderHelper;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.reveiver.DefaultReveiver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志接收器工厂类
 * Created by chenfeng on 2015/4/22.
 */
public class HLogReveiverFactory {


    private static IHLogReveiver defReveiver = null;


    public static IHLogReveiver getReveiver(){

        if(defReveiver!=null){
            return defReveiver;
        }

        synchronized (HLogReveiverFactory.class){
            if(defReveiver==null){
                String clz = HLogConfig.getInstance().getProperty(Constants.KEY_DEF_REVEIVER_CLASS);
                if(clz==null){
                    defReveiver = new DefaultReveiver();
                }else{
                    try{
                        defReveiver = (IHLogReveiver)LoaderHelper
                                .loadClass(clz).newInstance();
                    }catch (Exception e){
                        Logger.error("构建[{0}]实例异常",e,clz);
                    }
                }
            }
        }
        return defReveiver;
    }


}

package com.asiainfo.hlog.client.config;

import com.al.uniconfig.spring.UniConfigPropertyLoader;
import com.al.uniconfig.util.ObservablePropertyHolder;
import com.al.uniconfig.util.PropertyObserver;
import com.asiainfo.hlog.client.helper.Logger;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * 从统一配置平台上获取配置信息
 * Created by chenfeng on 2015/4/27.
 */
public abstract class UniConfig {

    private static UniConfigPropertyLoader uniConfigPropertyLoader ;

    private static Properties uniProps = null;

    public synchronized static Properties init(Properties prop) throws IOException {
        if(uniProps!=null){
            return uniProps;
        }
        ObservablePropertyHolder.addObserver(new PropertyObserver() {
            public void handleProperty(Properties props) {
                System.out.println(props);
            }
        });

        uniConfigPropertyLoader = new UniConfigPropertyLoader();

        if(!prop.containsKey("constantclass.basepackage")){
            prop.setProperty("constantclass.basepackage","com.asiainfo.hlog");
        }
        if(!prop.containsKey("uniconfig.localdir")){
            prop.setProperty("uniconfig.localdir","hlog_uniconfig");
        }
        if(!prop.containsKey("constantclass.pattern")){
            prop.setProperty("constantclass.pattern","MDA*.class");
        }

        //是否配置了系统域信息
        if(!prop.containsKey("uniconfig.domain")){
            prop.setProperty("uniconfig.domain",HLogConfig.hlogDomain);
        }

        if(Logger.isDebug()){
            Logger.debug("连接统一配置平台的初始化信息:{0}",prop);
        }


        uniConfigPropertyLoader.init(prop);

        Properties localProps = uniConfigPropertyLoader.loadProperties();

        if(Logger.isDebug()){
            Logger.debug("从统一配置平台获取到的配置信息:{0}",localProps);
        }
        uniProps = new Properties();
        if(localProps!=null){
            Set<Object> keys = localProps.keySet();
            int start = HLogConfig.hlogDomain.length() + 1;
            for(Object key : keys){
                String tmpkey = key.toString();
                String k  = tmpkey.substring(start);
                uniProps.put(k,localProps.getProperty(tmpkey));
            }
        }

        return uniProps;
    }

}

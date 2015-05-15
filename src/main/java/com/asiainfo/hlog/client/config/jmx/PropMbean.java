package com.asiainfo.hlog.client.config.jmx;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.comm.jmx.AnnotationMBean;
import com.asiainfo.hlog.comm.jmx.AnnotationMBeanEvent;

/**
 * 将hlog的porperties信息展示出来
 * Created by chenfeng on 2015/5/11.
 */
@AnnotationMBean(describle="hlog properties configuration information.")
public class PropMbean {

    @AnnotationMBeanEvent
    public void firePropertiesChanged(String key){
        String tmpKey = key;
        if(HLogConfig.hlogCfgName!=null) {
            if (key.startsWith(HLogConfig.hlogCfgNamePix)){
                tmpKey = key.substring(HLogConfig.hlogCfgNamePix.length());
            }
        }
        HLogConfig config = HLogConfig.getInstance();
        //
        if(tmpKey.equals("hlog.enable")){
            config.setEnable("true".equals(config.getProperty(tmpKey)));
        }

        //System.out.println(key);
    }
}

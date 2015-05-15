package com.asiainfo.hlog.client.config.jmx;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.comm.jmx.MBeanExportBuilder;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import java.util.*;

/**
 * Created by chenfeng on 2015/5/11.
 */
public class PropMbeanExportBuilder implements MBeanExportBuilder {

    @Override
    public MBeanConstructorInfo[] exportCons(Object bean) {
        return new MBeanConstructorInfo[0];
    }

    @Override
    public MBeanAttributeInfo[] exportAttrs(Object bean) {

        Properties prop = HLogConfig.getInstance().getProperties();

        if(prop==null){
            return new MBeanAttributeInfo[0];
        }

        Set<Map.Entry<Object, Object>> entries =  prop.entrySet();

        List<MBeanAttributeInfo> infoList = new ArrayList<MBeanAttributeInfo>(entries.size());
        for(Map.Entry<Object, Object> entry : entries){
            String key = entry.getKey().toString();
            boolean isWrite = key.indexOf("hlog.base.path.")==-1;
            MBeanAttributeInfo info = new MBeanAttributeInfo(entry.getKey().toString(),
                    "java.lang.String","",
                    true, isWrite,false);
            infoList.add(info);
        }

        return infoList.toArray(new MBeanAttributeInfo[infoList.size()]);
    }

    @Override
    public Object getAttribute(Object bean, String attribute) {
        Properties prop = HLogConfig.getInstance().getProperties();

        return prop.get(attribute);
    }

    @Override
    public void setAttribute(Object bean, String attribute, Object value) {
        Properties prop = HLogConfig.getInstance().getProperties();
        prop.put(attribute,value);
    }
}

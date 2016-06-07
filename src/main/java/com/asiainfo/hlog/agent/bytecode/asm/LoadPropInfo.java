package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by chenfeng on 2016/5/5.
 */
public class LoadPropInfo {

    public static void loadPropAndExt(Map<String,Class> classMap,String prop,String extProp) throws IOException, ClassNotFoundException {

        //读取默认配置
        InputStream inputStream = LoadPropInfo.class.getResourceAsStream(prop);
        loadMethocVisitorConfig(inputStream,classMap);

        //读取扩展配置
        String extDir = HLogConfig.getInstance().getHLogAgentDir();
        InputStream in = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(extDir+extProp);
            in = new BufferedInputStream(fis);
            loadMethocVisitorConfig(in,classMap);
        } catch (FileNotFoundException e) {
            Logger.warn("没有读取到本地配置文件[{0}],程序继续执行.",extDir+extProp);
        } catch (IOException e) {
            Logger.warn("没有读取到本地配置文件错误[{0}],程序继续执行.",extDir+extProp);
        }finally {
            try {
                if (in!=null){
                    in.close();
                }
            } catch (IOException e) {
            }
            if(fis!=null){
                fis.close();
            }
        }
    }



    private static void loadMethocVisitorConfig(InputStream inputStream,Map<String,Class> classMap) throws IOException, ClassNotFoundException {
        Properties prop = new Properties();
        if(inputStream==null){
            return ;
        }
        prop.load(inputStream);
        Set<Object> keys = prop.keySet();
        for (Object key : keys) {
            String strKey = key.toString();
            String value = prop.getProperty(strKey);
            classMap.put(strKey,Class.forName(value));
        }
    }
}

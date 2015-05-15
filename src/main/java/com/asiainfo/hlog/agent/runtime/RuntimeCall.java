package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.HLogConfigRule;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenfeng on 2015/4/17.
 */
public class RuntimeCall{
    /**
     * 排序
     * com.asiainfo.test.TestApp
     * com.asiainfo.test
     * com.asiainfo
     *
     * com.asiainfo.test.TestApp=[{name:process,enable:true},{name:logger,enable:false}]
     *
     */
    //private static Map<String,List<Weave>> runtimeConfigSwitch = new HashMap<String,List<Weave>>();

    private static Map<String,Boolean> runtimeSwitchMap = new HashMap<String,Boolean>(200);



    /**
     * 判断某个类某个方法是否启用某个采集日志数据
     * @param weaveName
     * @param clazz
     * @param method
     * @param level
     * @return
     */
    public boolean enable(String weaveName ,String clazz,String method,String level){

        HLogConfig config = HLogConfig.getInstance();

        if(!config.isEnable()){
            return false;
        }

        //查收到是有直接是方法级的
        String classKey = clazz + "-" + weaveName;
        if("logger".equals(weaveName) && level!=null){
            classKey = classKey + "-" + level;
        }
        if(runtimeSwitchMap.containsKey(classKey)){
            boolean b = runtimeSwitchMap.get(classKey);
            if(Logger.isTrace()){
                Logger.trace("判断[{0}]是否在收集日志数据范围:{1}",classKey,b);
            }
            return b;
        }else{
            //寻找最合适的配置
            HLogConfigRule rule = LogUtil.suitableConfig(clazz, method, config.getRuntimeCaptureCofnigRule());
            if(rule!=null){
                List<String> captureWeaves = rule.getCaptureWeaves();
                for (String weave : captureWeaves){
                    if(weave.equals(weaveName)){
                        boolean enable = false;
                        if(("logger".equals(weaveName))
                                && level!=null && rule.getLevel()!=null){
                            if(Logger.canOutprint(level,rule.getLevel())){
                                enable = true;
                            }
                        }else{
                            enable = true;
                        }

                        runtimeSwitchMap.put(classKey, enable);
                        if(Logger.isTrace()){
                            Logger.trace("判断[{0}]是否在收集日志数据范围:{1}",classKey,enable);
                        }
                        return enable;
                    }
                }
            }
        }

        /*
        hlog.level.com.asiainfo.test.TestApp=debug
        capture.enable.com.asiainfo.test.TestApp=log4j,process
         */
        runtimeSwitchMap.put(classKey,false);
        if(Logger.isTrace()){
            Logger.trace("判断[{0}]是否在收集日志数据范围:{1}",classKey,true);
        }
        return false;
    }
}
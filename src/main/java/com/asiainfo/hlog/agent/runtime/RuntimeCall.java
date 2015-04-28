package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.HLogConfigRule;
import com.asiainfo.hlog.client.config.Path;
import com.asiainfo.hlog.client.config.Weave;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;

import java.util.*;

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
    private static Map<String,List<Weave>> runtimeConfigSwitch = new HashMap<String,List<Weave>>();

    private static Map<String,Boolean> runtimeSwitchMap = new HashMap<String,Boolean>(200);

    public void sort(){
        /*
        List<Map.Entry<String,List<Weave>>> mappingList =
                new ArrayList<>(runtimeConfigSwitch.entrySet());
        Collections.sort(mappingList, new Comparator<Map.Entry<String,List<Weave>>>() {
            public int compare(Map.Entry<String,List<Weave>> entry1,
                               Map.Entry<String,List<Weave>> entry2) {
                return entry1.getKey().compareTo(entry2.getKey());
            }
        });*/
    }


    /**
     * 判断某个类某个方法是否启用某个采集日志数据
     * @param weaveName
     * @param clazz
     * @param method
     * @return
     */
    public boolean enable(String weaveName ,String clazz,String method){

        HLogConfig config = HLogConfig.getInstance();

        if(!config.isEnable()){
            return false;
        }

        //查收到是有直接是方法级的
        String classKey = clazz + "-" + weaveName;
        if(runtimeSwitchMap.containsKey(classKey)){
            boolean b = runtimeSwitchMap.get(classKey);
            if(Logger.isTrace()){
                Logger.trace("判断[{0}]是否是d在收集日志数据范围:{1}",classKey,b);
            }
            return b;
        }else{
            //寻找最合适的配置
            HLogConfigRule rule = LogUtil.suitableConfig(clazz, method, config.getRuntimeCaptureCofnigRule());
            if(rule!=null){
                List<String> captureWeaves = rule.getCaptureWeaves();
                for (String weave : captureWeaves){
                    if(weave.equals(weaveName)){
                        runtimeSwitchMap.put(classKey, true);
                        if(Logger.isTrace()){
                            Logger.trace("判断[{0}]是否是d在收集日志数据范围:{1}",classKey,true);
                        }
                        return true;
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
            Logger.trace("判断[{0}]是否是d在收集日志数据范围:{1}",classKey,true);
        }
        return false;
    }
}
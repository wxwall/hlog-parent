package com.asiainfo.hlog.agent.runtime;

import com.al.common.context.IPropertyListener;
import com.al.common.context.PropertyEvent;
import com.al.common.context.PropertyHolder;
import com.alibaba.fastjson.JSON;
import com.asiainfo.hlog.agent.ExcludeRuleUtils;
import com.asiainfo.hlog.client.config.Constants;
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

    public RuntimeCall(){
        PropertyHolder.addListener(new IPropertyListener() {
            public void changed(PropertyEvent event) {
                String key = event.getKey();
                if(key.startsWith(Constants.KEY_HLOG_CAPTURE_ENABLE)){
                    runtimeSwitchMap.clear();
                }
            }
        });
    }

    /**
     * 判断某个类某个方法是否启用某个采集日志数据
     * @param weaveName
     * @param clazz
     * @param method
     * @param level
     * @return
     */
    public boolean enable(String weaveName ,String clazz,String method,String level){

        try{
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
                    if(captureWeaves!=null){
                        for (String weave : captureWeaves){
                            if(weave.equals(weaveName)){
                                boolean enable = false;
                                if(("logger".equals(weaveName))
                                        && level!=null && rule.getLevel()!=null){
                                    //看是否是被排除的类
                                    if(ExcludeRuleUtils.isExcludePath(clazz)){
                                        enable = false;
                                    }else if(Logger.canOutprint(level,rule.getLevel())){
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
            }

            runtimeSwitchMap.put(classKey,false);
            if(Logger.isTrace()){
                Logger.trace("判断[{0}]是否在收集日志数据范围:{1}",classKey,false);
            }
            return false;
        }catch (Throwable t){
            Logger.error("判断{0}的{1}的{2}方法是否启用收集日志时异常",t,weaveName ,clazz,method);
            return false;
        }

    }

    public static String toJson(Object obj){
        return JSON.toJSONString(obj);
    }
}
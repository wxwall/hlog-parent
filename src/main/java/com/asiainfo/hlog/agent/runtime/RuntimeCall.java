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
    //private static Map<String,List<Weave>> runtimeConfigSwitch = new HashMap<String,List<Weave>>();

    //private static Map<String,Boolean> runtimeSwitchMap = new HashMap<String,Boolean>(200);

    private static Map<String,RuntimeSwitch> mcodeRuntimeSwitchMap = new HashMap<String, RuntimeSwitch>();

    private static Object nullObject = new Object();

    class SwitchLRUCache<K,V> extends LinkedHashMap<K, V>{
        private static final long serialVersionUID = 1L;
        private final int         maxSize;

        public SwitchLRUCache(int maxSize){
            this(maxSize, 16, 0.75f, true);
        }

        public SwitchLRUCache(int maxSize, int initialCapacity, float loadFactor, boolean accessOrder){
            super(initialCapacity, loadFactor, accessOrder);
            this.maxSize = maxSize;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return this.size() > this.maxSize;
        }
    }

    class RuntimeSwitch{
        private Map<String,Object> ON = new SwitchLRUCache<String,Object>(2000);
        private Map<String,Object> OFF = new SwitchLRUCache<String,Object>(2000);

        public int getSwitch(String key){
            if(ON.get(key)!=null){
                return 1;
            }else if(OFF.get(key)!=null){
                return 0;
            }
            return -1;
        }

        public void onSwitch(String key){
            ON.put(key,nullObject);
        }
        public void offSwitch(String key){
            OFF.put(key,nullObject);
        }
        public void clear(){
            ON.clear();
            OFF.clear();
        }
    }

    public RuntimeCall(){
        PropertyHolder.addListener(new IPropertyListener() {
            public void changed(PropertyEvent event) {
                String key = event.getKey();
                if(key.startsWith(Constants.KEY_HLOG_CAPTURE_ENABLE)
                        || key.startsWith(Constants.KEY_HLOG_LEVLE)){
                    for (RuntimeSwitch runtimeSwitch : mcodeRuntimeSwitchMap.values()) {
                        runtimeSwitch.clear();
                    }
                    mcodeRuntimeSwitchMap.clear();
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

            String switchKey = clazz + "-" + method;
            int flag = -1;
            RuntimeSwitch runtimeSwitch = mcodeRuntimeSwitchMap.get(weaveName);
            if (runtimeSwitch==null) {
                runtimeSwitch = new RuntimeSwitch();
                mcodeRuntimeSwitchMap.put(weaveName,runtimeSwitch);
            }else if((flag=runtimeSwitch.getSwitch(switchKey))!=-1){
                return flag==1?true:false;
            }

            //寻找最合适的配置
            HLogConfigRule rule = LogUtil.suitableConfig(clazz, method, config.getRuntimeCaptureCofnigRule());
            if(rule==null){
                runtimeSwitch.offSwitch(switchKey);
                return false;
            }

            List<String> captureWeaves = rule.getCaptureWeaves();
            if(captureWeaves==null || captureWeaves.size()==0){
                runtimeSwitch.offSwitch(switchKey);
                return false;
            }


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
                    if(enable){
                        runtimeSwitch.onSwitch(switchKey);
                    }else{
                        runtimeSwitch.offSwitch(switchKey);
                    }

                    if(Logger.isTrace()){
                        Logger.trace("判断[{0}]是否在收集日志数据范围:{1}",switchKey,enable);
                    }
                    return enable;
                }
            }

            /*

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
            */
        }catch (Throwable t){
            Logger.error("判断{0}的{1}的{2}方法是否启用收集日志时异常",t,weaveName ,clazz,method);
        }
        return false;

    }

    public static String toJson(Object obj){
        return JSON.toJSONString(obj);
    }
}
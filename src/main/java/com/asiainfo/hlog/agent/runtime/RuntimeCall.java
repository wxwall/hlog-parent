package com.asiainfo.hlog.agent.runtime;

import com.alibaba.fastjson.JSON;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.HLogConfigRule;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.comm.context.IPropertyListener;
import com.asiainfo.hlog.comm.context.PropertyEvent;
import com.asiainfo.hlog.comm.context.PropertyHolder;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    class SwitchKey {

        private String clazz;

        private int flag ;

        public SwitchKey(String clazz,int flag){
            this.clazz = clazz;
            this.flag = flag;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SwitchKey switchKey = (SwitchKey) o;

            return clazz.equals(switchKey.clazz);

        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }
    }

    class RuntimeSwitch{
        private Map<SwitchKey,Object> ON = new SwitchLRUCache<SwitchKey,Object>(10000);
        private Map<SwitchKey,Object> OFF = new SwitchLRUCache<SwitchKey,Object>(10000);
        //private Map<String,Object> ON = new LinkedHashMap<String,Object>(1000, 0.8f,false);
        //private Map<String,Object> OFF = new LinkedHashMap<String,Object>(1000, 0.8f,false);

        public int getSwitch(SwitchKey key){
            try{
                if(ON.get(key)!=null){
                    return 1;
                }else if(OFF.get(key)!=null){
                    return 0;
                }
            }catch (Throwable t){
                return 0;
            }
            return -1;
        }

        public void onSwitch(SwitchKey key){
            if(key==null){
                return ;
            }
            ON.put(key,nullObject);
        }
        public void offSwitch(SwitchKey key){
            if(key==null){
                return ;
            }
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

    private static Map<String,Integer> CAPTURE_ENABLE_CACHE = new HashMap<String,Integer>(2000);

    public boolean enable(String weaveName ,String clazz,String method,String level){

        HLogConfig config = HLogConfig.getInstance();
        if(!config.isEnable()){
            return false;
        }

        Integer temp = config.CAPTURE_ENABLE_FLAG.get(weaveName);
        if(temp==null){
            if("logger".equals(weaveName)){
                String tmp = weaveName + "-" + level;
                temp = config.CAPTURE_ENABLE_FLAG.get(tmp);
            }
            return false;
        }
        if(temp==null){
            return false;
        }

        int captureFlag = temp.intValue();

        Integer cacheFlag = CAPTURE_ENABLE_CACHE.get(clazz);

        int flag = 0;
        if(cacheFlag!=null){
            flag = cacheFlag.intValue();
            return ckeckFlag(captureFlag, flag);
        }

        HLogConfigRule rule = LogUtil.suitableConfig(clazz, null, config.getRuntimeCaptureCofnigRule());

        if(rule==null){
            rule = config.getDefRuntimeCaptureCofnigRule();
        }

        if(rule!=null){
            flag = rule.getFlag();
        }
        CAPTURE_ENABLE_CACHE.put(clazz,flag);

        return ckeckFlag(captureFlag, flag);


    }

    private boolean ckeckFlag(int captureFlag, int flag) {
        if(flag==-1){
            return true;
        }
        return (flag & captureFlag) == captureFlag;
    }


    public static String toJson(Object obj){
        return JSON.toJSONString(obj);
    }


    public static void main(String[] args) {


        System.out.println(0x0100);

        System.out.println(Integer.valueOf(0x0100));

        int H_PROCESS = 0x0001;
        int H_ERROR = 0x0002;
        int H_INTECEPT = 0x0004;
        int H_INTECEPTRET = 0x0008;
        int H_SQL = 0x0010;
        int H_LOG_DEBUG = 0x0020;
        int H_LOG_INFO = 0x0040;
        int H_LOG_WARN = 0x0080;
        int H_LOG_ERROR = 0x0100;

        int flag = H_ERROR+H_INTECEPTRET;

        if((flag & H_SQL) == H_SQL){
            System.out.println("true");
        }else{
            System.out.println("false");
        }

    }
}
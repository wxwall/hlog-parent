package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.HLogConfigRule;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.comm.context.IPropertyListener;
import com.asiainfo.hlog.comm.context.PropertyEvent;
import com.asiainfo.hlog.comm.context.PropHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenfeng on 2015/4/17.
 */
public class RuntimeEnable {
    /**
     * 排序
     * com.asiainfo.test.TestApp
     * com.asiainfo.test
     * com.asiainfo
     *
     * com.asiainfo.test.TestApp=[{name:process,enable:true},{name:logger,enable:false}]
     *
     */

    public RuntimeEnable(){
        PropHolder.addListener(new IPropertyListener() {
            public void changed(PropertyEvent event) {
                String key = event.getKey();
                if(key.startsWith(Constants.KEY_HLOG_CAPTURE_ENABLE)
                        || key.startsWith(Constants.KEY_HLOG_CAPTURE_LOGGER_ENABLE_PATHS)){
                    CAPTURE_ENABLE_CACHE.clear();
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

    private static final Map<String,String> loggerWeaveMap  = new HashMap();

    static {
        loggerWeaveMap.put("debug","logger.debug");
        loggerWeaveMap.put("info","logger.info");
        loggerWeaveMap.put("warn","logger.warn");
        loggerWeaveMap.put("error","logger.error");
    }

    public boolean enable(String weaveName ,String clazz,String method,String level){

        HLogConfig config = HLogConfig.getInstance();
        if(!config.isEnable()){
            return false;
        }
        Integer temp ;
        if(level!=null){
            temp = config.CAPTURE_ENABLE_FLAG.get(loggerWeaveMap.get(level));
        }else{
            temp = config.CAPTURE_ENABLE_FLAG.get(weaveName);
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
            if(rule!=null && level!=null){
                boolean havCfgLogger = false;
                String[] loggerPaths = rule.getLoggerPaths();
                if(loggerPaths!=null){
                    for (String loggerPath : loggerPaths) {
                        if(clazz.startsWith(loggerPath)){
                            havCfgLogger = true;
                            //返回正常配置
                            break;
                        }
                    }
                }
                if(!havCfgLogger){
                    flag = rule.getNoLoggerFlag();
                    CAPTURE_ENABLE_CACHE.put(clazz,flag);
                    return ckeckFlag(captureFlag, flag);
                }

            }
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

}
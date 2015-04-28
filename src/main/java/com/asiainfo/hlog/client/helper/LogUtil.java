package com.asiainfo.hlog.client.helper;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.Path;
import com.asiainfo.hlog.client.config.PathType;

import java.util.Map;
import java.util.Set;

/**
 * Created by c on 2015/3/17.
 */
public abstract class LogUtil {

    public static String uuid() {
        StringBuilder sb = new StringBuilder();
        sb.append(HLogConfig.hlogCode);
        sb.append(Thread.currentThread().getId())
                .append(Long.toString(System.nanoTime()>>32,32))
                .append(Long.toString(Math.round(Math.random() * 8999999 + 1000000), 32));
        return sb.toString();
    }

    /**
     * 寻找最适合自己的它
     * @param clazz
     * @param method
     * @param configs
     * @param <T>
     * @return
     */
    public static <T> T suitableConfig(String clazz,String method,Map<Path,T> configs){

        T config = null;
        Set<Path> paths = configs.keySet();
        String packageName = "";
        String className = "";
        int last = clazz.lastIndexOf(".");
        if(last>-1){
            packageName = clazz.substring(0,last);
            className = clazz.substring(last+1);
        }else{
            className = clazz;
        }
        for(Path path : paths){
            switch (path.getType()){
                case METHOD:
                    if(method!=null && method.equals(path.getMethodName())
                            && className.equals(path.getClassName())
                            && packageName.equals(path.getPackageName())
                            ){
                        config =  configs.get(path);
                    }
                    break;
                case CLASS:
                    if(className.equals(path.getClassName())
                            && packageName.equals(path.getPackageName())
                            ){
                        config =  configs.get(path);
                    }
                    break;
                default:
                    if(packageName.startsWith(path.getPackageName())
                            ){
                        config =  configs.get(path);
                    }
                    break;
            }
            if(config!=null){
                break;
            }
        }

        if(Logger.isTrace()){
            Logger.trace("{0}.{1}对就应最优配置是{2}",clazz,method,config);
        }

        return config;
    }


}


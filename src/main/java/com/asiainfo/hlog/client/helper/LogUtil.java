package com.asiainfo.hlog.client.helper;

import com.asiainfo.hlog.client.config.Path;
import com.asiainfo.hlog.comm.uuid.UUIDGen;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by c on 2015/3/17.
 */
public abstract class LogUtil {

    private static long clockSeqAndNode = -1;
    private static String sClockSeqAndNode = null;
    private static AtomicLong incrementId = new AtomicLong(0);

    public static String logId(){
        if(sClockSeqAndNode==null){
            createClockSeqAndNode();
        }
        StringBuilder sb = new StringBuilder(sClockSeqAndNode);
        long incrId = incrementId.incrementAndGet();
        //这里没有999999999是预留足够的多线程情况下容错空间
        if(incrId>989999999){
            createClockSeqAndNode();
        }
        String sIncrId = UUIDGen.toUnsignedString(incrId,6);
        int repairSize = 5 - sIncrId.length();
        for(int i=0;i<repairSize;i++){
            sb.append("0");
        }
        sb.append(sIncrId);
        return sb.toString();
    }

    private static synchronized void createClockSeqAndNode(){
        clockSeqAndNode = UUIDGen.getClockSeqAndNode();
        sClockSeqAndNode = UUIDGen.toUnsignedString(clockSeqAndNode,6);
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


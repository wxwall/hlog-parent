package com.asiainfo.hlog.client;

import com.asiainfo.hlog.client.helper.ClassHelper;
import com.asiainfo.hlog.client.helper.LoaderHelper;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.helper.MethodCaller;
import com.asiainfo.hlog.client.model.Event;
import com.asiainfo.hlog.client.model.LogData;

import java.lang.reflect.Method;

/**
 * 日志处理的反射
 * Created by chenfeng on 2015/4/23.
 */
public abstract class HLogReflex {

    /**
     * 这个是指向 {@link IHLogReveiver} 的reveice方法
     */
    private static MethodCaller reveiverCaller = null;

    /**
     * 初始工作,用{@link HLogReveiverFactory}工厂生产了一个指定的IHLogReveiver实现
     */
    private static void init(){
        if(reveiverCaller!=null){
            return;
        }
        try {
            Class factory = LoaderHelper.loadClass("com.asiainfo.hlog.client.HLogReveiverFactory");
            Method method = factory.getMethod("getReveiver");

            MethodCaller reveiverFactoryCaller = new MethodCaller(method,null);

            Object reveiverObj = reveiverFactoryCaller.invokeStatic(null);

            Method reveiceMethod = ClassHelper.getMethod(reveiverObj.getClass(),
                    "reveice", LoaderHelper.loadClass(Event.class.getName()));

            reveiverCaller = new MethodCaller(reveiceMethod,reveiverObj);


        } catch (Exception e) {
            Logger.error("HLogReflex初始化失败.",e);
        }
    }

    public static void reveice(Event<LogData> event){
        if (reveiverCaller==null){
            if(Logger.isDebug()){
                Logger.debug("HLogReflex的reveiverCaller为空,现进行初始化...");
            }
            synchronized (HLogReflex.class){
                init();
            }
            if(Logger.isDebug()){
                Logger.debug("HLogReflex的reveiverCaller初始化完成.");
            }
        }
        if(reveiverCaller!=null){
            reveiverCaller.invoke(event);
        }

    }
}

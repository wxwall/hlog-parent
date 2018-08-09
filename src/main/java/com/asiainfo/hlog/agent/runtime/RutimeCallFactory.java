package com.asiainfo.hlog.agent.runtime;

/**
 * Created by chenfeng on 2016/8/7.
 */
public class RutimeCallFactory {

    private static IRutimeCall rutimeCall = null;

    public static IRutimeCall getRutimeCall(){
//
//        if(rutimeCall==null){
//            synchronized (IRutimeCall.class){
//                if(rutimeCall==null){
//                    try {
//                        Class clazz = ClassLoaderHolder.getInstance()
//                                .loadClass("com.asiainfo.hlog.agent.runtime.DefRutimeCall");
//
//                        rutimeCall = (IRutimeCall)clazz.newInstance();
//                    } catch (Throwable e) {
//                    }
//                }
//            }
//        }
        return  new DefRutimeCall();
    }

}

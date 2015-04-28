package com.asiainfo.hlog.client.helper;

/**
 * Created by chenfeng on 2015/4/15.
 */
public abstract class LoaderHelper {

    private static ClassLoader loader = null;

    public static void setLoader(ClassLoader classLoader){
        if(loader==null)
            loader = classLoader;
    }

    public static ClassLoader getLoader(){
        if(loader!=null){
            return loader;
        }
        return Thread.currentThread().getContextClassLoader();
    }

    public static Class<?> loadClass(String name) throws ClassNotFoundException{
        Class<?> clazz = null;
        try {
            clazz = getLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(name);
            } catch (ClassNotFoundException e1) {
                clazz = Class.forName(name);
            }
        }
        return clazz;
    }


}

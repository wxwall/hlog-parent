package com.asiainfo.hlog.client.helper;

import java.lang.reflect.Method;

/**
 * Created by chenfeng on 2015/4/15.
 */
public abstract class ClassHelper {
    public static Class<?> loadClass(String name){
        try{
            return LoaderHelper.loadClass(name);
        }catch (ClassNotFoundException cc){
        }
        return null;
    }
    public static Object newInstance(String clazz){
        try{
            return LoaderHelper.getLoader().loadClass(clazz).newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Method getMethod(Class clazz,String name,Class<?> ... types){
        try {
            return clazz.getMethod(name,types);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}

package com.asiainfo.hlog.agent.classloader;

/**
 * Created by yuan on 2018/8/9.
 */
public class ClassLoaderHolder {
    private static ClassLoader classLoader;
    public static void setClassLoader(ClassLoader loader){
        classLoader = loader;
    }

    public static ClassLoader getClassLoader(){
        return classLoader;
    }
}

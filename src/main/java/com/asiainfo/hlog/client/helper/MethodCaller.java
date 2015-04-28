package com.asiainfo.hlog.client.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by chenfeng on 2015/4/15.
 */
public class MethodCaller {

    private Method method;

    private Object instance ;

    private Class<?> aClass;

    public MethodCaller(Method method, Object instance) {
        this.method = method;
        this.instance = instance;

        this.aClass = method.getDeclaringClass();
    }

    public Object invoke(Object ... args){
        try {
            return this.method.invoke(this.instance,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object invokeStatic(Object ... args){
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}

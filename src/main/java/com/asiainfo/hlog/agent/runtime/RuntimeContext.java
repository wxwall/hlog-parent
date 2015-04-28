package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.client.helper.ClassHelper;
import com.asiainfo.hlog.client.helper.MethodCaller;

import java.lang.reflect.Method;

/**
 * 运行状态下的各种上下文判断
 * Created by chenfeng on 2015/4/17.
 */
public class RuntimeContext {

    private static MethodCaller enableMethodCaller = null;

    public static boolean enable(String weaveName ,String clazz,String method) {

        if (enableMethodCaller == null) {
            synchronized (RuntimeContext.class) {
                Object obj = ClassHelper.newInstance(RuntimeCall.class.getName());
                Method methed = ClassHelper.getMethod(obj.getClass(),"enable",String.class,String.class,String.class);
                enableMethodCaller = new MethodCaller(methed,obj);
            }
        }

        boolean b = (Boolean)enableMethodCaller.invoke(weaveName,clazz,method);

        return b;
    }

}



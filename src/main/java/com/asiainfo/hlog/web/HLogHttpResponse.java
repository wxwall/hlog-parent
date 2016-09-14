package com.asiainfo.hlog.web;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by lenovo on 2016/9/14.
 */
public class HLogHttpResponse  {

    private static HashMap<String,Method> methodMap = new HashMap<String, Method>();
    private Object resp;
    public HLogHttpResponse(Object resp){
        this.resp = resp;
    }

    private Method getMethod(String name) throws NoSuchMethodException {
        if(methodMap.containsKey(name)){
            return methodMap.get(name);
        }
        Method[] methods = resp.getClass().getMethods();
        for(Method method : methods){
            if(method.getName().equals(name)){
                methodMap.put(name,method);
                return method;
            }
        }
        throw new NoSuchMethodException(String.format("方法%s不存在",name));
    }

    public void setContentType(String type) throws  Exception{
        getMethod("setContentType").invoke(resp,type);
    }


    public OutputStream getOutputStream() throws Exception{
        return (OutputStream)getMethod("getOutputStream").invoke(resp);
    }

    public PrintWriter getWriter() throws Exception{
        return (PrintWriter)getMethod("getWriter").invoke(resp);
    }

}

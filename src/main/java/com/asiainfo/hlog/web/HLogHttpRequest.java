package com.asiainfo.hlog.web;

import com.asiainfo.hlog.client.helper.Logger;

import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lenovo on 2016/9/14.
 */
public class HLogHttpRequest {
    private static HashMap<String,Method> methodMap = new HashMap<String, Method>();
    private static Method methodSessionGetAttribute = null;
    private Object req;
    public HLogHttpRequest(Object req){
        this.req = req;
    }

    private Method getMethod(String name) throws NoSuchMethodException {
        if(methodMap.containsKey(name)){
            return methodMap.get(name);
        }
        Method[] methods = req.getClass().getMethods();
        for(Method method : methods){
            if(method.getName().equals(name)){
                //methodMap.put(name,method);
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(String.format("方法%s不存在",name));
    }
    public String getRequestURI() throws Exception{
        return (String)getMethod("getRequestURI").invoke(req);
    }
    public String getParameter(String name) throws Exception{
        return (String)getMethod("getParameter").invoke(req,name);
    }
    public Map<String, String[]> getParameterMap() throws Exception{
        return (Map<String, String[]> )getMethod("getParameterMap").invoke(req);
    }
    public String getContextPath() throws Exception{
        return (String)getMethod("getContextPath").invoke(req);
    }
    public BufferedReader getReader() throws Exception{
        return (BufferedReader)getMethod("getReader").invoke(req);
    }
    public String getRequestURL() throws Exception{
        Method method = getMethod("getRequestURL");
        Object url = method.invoke(req);
        if(url != null){
            return url.toString();
        }
        return null;
    }
    public Object getSessionAttribute(String key) throws Exception{
        Object session = getSession();
        if(methodSessionGetAttribute == null){
            methodSessionGetAttribute = session.getClass().getMethod("getAttribute",String.class);
            methodSessionGetAttribute.setAccessible(true);
        }
        return methodSessionGetAttribute.invoke(session,key);
    }

    public Object getSession()  throws Exception{
        Method method = req.getClass().getMethod("getSession");
        method.setAccessible(true);
        return method.invoke(req);
    }

    public String getHeader(String key){
        if(key == null){
            return null;
        }
        try {
            return (String) getMethod("getHeader").invoke(req, key);
        }catch (Exception e){
            Logger.error(e);
        }
        return null;
    }
}

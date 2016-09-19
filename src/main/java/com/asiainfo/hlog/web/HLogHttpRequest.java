package com.asiainfo.hlog.web;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.HashMap;

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
                methodMap.put(name,method);
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

    public String getContextPath() throws Exception{
        return (String)getMethod("getContextPath").invoke(req);
    }

    public BufferedReader getReader() throws Exception{
        return (BufferedReader)getMethod("getReader").invoke(req);
    }

    public StringBuffer getRequestURL() throws Exception{
        return (StringBuffer)getMethod("getRequestURL").invoke(req);
    }

    public Object getSessionAttribute(String key) throws Exception{
        Object session = getSession();
        if(methodSessionGetAttribute == null){
            methodSessionGetAttribute = session.getClass().getMethod("getAttribute",String.class);
        }
        return methodSessionGetAttribute.invoke(session,key);
    }

    public Object getSession()  throws Exception{
        if(!methodMap.containsKey("getSession")){
            methodMap.put("getSession",req.getClass().getMethod("getSession"));
        }
        return methodMap.get("getSession").invoke(req);
    }
}

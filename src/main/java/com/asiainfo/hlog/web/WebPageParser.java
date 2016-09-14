package com.asiainfo.hlog.web;

import com.asiainfo.hlog.comm.ResourceUtils;

/**
 * Created by lenovo on 2016/9/9.
 */
public class WebPageParser {

    public static boolean pageRedirect(Object req0, Object resp0) throws Exception{
        HLogHttpResponse resp = new HLogHttpResponse(resp0);
        HLogHttpRequest req = new HLogHttpRequest(req0);
        String uri = req.getRequestURI();
        if(uri.contains("/hlogweb/pages/")){
            uri = uri.substring(uri.indexOf("/hlogweb/pages/"));
            WebPageParser.returnResourceFile(uri+".html",req,resp);
            return true;
        }else if(uri.contains("/hlogweb/statics/")){
            uri = uri.substring(uri.indexOf("/hlogweb/statics/"));
            WebPageParser.returnResourceFile(uri,req,resp);
            return true;
        }else if(uri.contains("/hlogweb/action/")){
            uri = uri.substring(uri.indexOf("/hlogweb/action/"));
            WebRequestParser.parserRequest(req,resp);
            return true;
        }
        return  false;
    }

    public static void returnResourceFile(String fileName, HLogHttpRequest req, HLogHttpResponse response)
            throws Exception {

        if (fileName.endsWith(".jpg")||fileName.endsWith(".png")
                ||fileName.endsWith(".gif")||fileName.endsWith(".ico")) {
            byte[] bytes = ResourceUtils.readByteArrayFromResource(fileName);
            if (bytes != null) {
                response.getOutputStream().write(bytes);
            }
            return;
        }

        if(fileName.endsWith(".eot")) {
            response.setContentType("application/octet-stream");
            response.getOutputStream().write(ResourceUtils.readByteArrayFromResource(fileName));
            return;
        }else if(fileName.endsWith(".ttf")) {
            response.setContentType("application/octet-stream");
            response.getOutputStream().write(ResourceUtils.readByteArrayFromResource(fileName));
            return;
        }else if(fileName.endsWith(".woff")) {
            response.setContentType("application/font-woff");
            response.getOutputStream().write(ResourceUtils.readByteArrayFromResource(fileName));
            return;
        }else if(fileName.endsWith(".woff2")) {
            response.setContentType("application/font-woff2");
            response.getOutputStream().write(ResourceUtils.readByteArrayFromResource(fileName));
            return;
        }

        String text = ResourceUtils.readFromResource(fileName);
        if (text == null) {
            //response.sendRedirect(uri + "/index.html");
            return;
        }
        if(fileName.endsWith(".html")) {
            response.setContentType("text/html; charset=utf-8");
            text = text.replace("${baseRoot}",req.getContextPath());
        }else if (fileName.endsWith(".css")) {
            response.setContentType("text/css;charset=utf-8");
        } else if (fileName.endsWith(".js")) {
            response.setContentType("text/javascript;charset=utf-8");
        }else if(fileName.endsWith(".svg")) {
            response.setContentType("text/xml;charset=utf-8");
        }
        response.getWriter().write(text);
    }
}

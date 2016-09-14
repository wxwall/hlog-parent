package com.asiainfo.hlog.agent.runtime.http;

import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.agent.runtime.LogAgentContext;
import com.asiainfo.hlog.agent.runtime.RuntimeContext;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.ClassHelper;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.LogData;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>用于监控各种http方式的请求处理:</p>
 * <p>1、转递上游的日志组ID和上线ID;</p>
 * <p>2、记录资源请求日志;</p>
 * Created by chenfeng on 2016/5/8.
 */
public class HttpMonitor {

    private static Set<String> excludeExpands = new HashSet<String>();

    static {
        excludeExpands.add("js");
        excludeExpands.add("css");
        //jpg,bmp,tga,vst,pcd,pct,gif,ai,fpx,img,cal,wi,png,jpeg
        excludeExpands.add("jpg");
        excludeExpands.add("bmp");
        excludeExpands.add("gif");
        excludeExpands.add("png");
        excludeExpands.add("img");
        excludeExpands.add("jpeg");
        excludeExpands.add("tiff");
        excludeExpands.add("swf");
        excludeExpands.add("svg");
        excludeExpands.add("zip");
        excludeExpands.add("rar");
        excludeExpands.add("doc");
        excludeExpands.add("xsl");
        //TODO 增加可配置
    }

    private static HLogConfig config = HLogConfig.getInstance();

    public static void receiveHlogId(String _gid,String _pid){
        //LogAgentContext.clear();
        //如果没有上游系统传递gId的话,从当前线程中获取
        if(_gid==null){
            _gid = LogAgentContext.getThreadLogGroupId();
        }

        //如果当前线程也是空的,产生一个gId
        if(_gid==null){
            _gid = RuntimeContext.logId();
            _pid = RuntimeContext.buildLogPId(_gid);
        }else if(_pid==null){
            _pid = RuntimeContext.buildLogPId(_gid);
        }

        LogAgentContext.setThreadLogGroupId(_gid);
        LogAgentContext.setThreadCurrentLogId(_pid);
        LogAgentContext.setKeepContext(true);
    }

    public static void clearReceiveHlogId(){
        LogAgentContext.clear();
    }

    public static void request(StringBuffer requestUrl,String addr,long start,int status){
        //判断是否开启收集
        if(!config.isEnableRequest()){
            return;
        }

        //排除一些资源的请求
        String expand = getExpand(requestUrl);

        if(excludeExpands.contains(expand)){
            return ;
        }
        String pid = RuntimeContext.getLogId();
        String id = RuntimeContext.logId();
        LogData logData = HLogMonitor.createLogData("request",id,pid);
        logData.put("url",requestUrl.toString());
        logData.put("remoteAddr",addr);
        logData.put("spend",System.currentTimeMillis()-start);
        logData.put("status",status);
        RuntimeContext.writeEvent("request.log",null,logData);

        LogAgentContext.clear();
    }

    private static String getExpand(StringBuffer requestUrl){
        int index = 0;
        int length = requestUrl.length();
        while (index++<8){
            int newIndex = length-index;
            if(newIndex<0){
                break ;
            }
            char c = requestUrl.charAt(newIndex);
            if(c=='.'){
                index=index-1;
                break;
            }
        }
        return requestUrl.substring(length-index,length);
    }

    private static Method  pageRedirectMethod = null;
    public static boolean pageRedirect(Object req, Object resp){
        try {
            if(pageRedirectMethod == null){
                synchronized (HttpMonitor.class){
                    Class webPageParserClass = ClassHelper.loadClass("com.asiainfo.hlog.web.WebPageParser");
                    Method[] methods = webPageParserClass.getMethods();
                    for (Method method : methods){
                        if(method.getName().equals("pageRedirect")){
                            pageRedirectMethod = method;
                            break;
                        }
                    }
                }
            }
            if(pageRedirectMethod != null){
                return (Boolean) pageRedirectMethod.invoke(null,req,resp);
            }
            return false;
        }catch (Exception e){
            Logger.error("HLogWeb页面出错",e);
        }
        return  false;
    }

}

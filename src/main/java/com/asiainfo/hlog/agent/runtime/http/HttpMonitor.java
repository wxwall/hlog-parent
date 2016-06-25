package com.asiainfo.hlog.agent.runtime.http;

import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.agent.runtime.LogAgentContext;
import com.asiainfo.hlog.agent.runtime.RuntimeContext;
import com.asiainfo.hlog.client.model.LogData;

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
        //TODO 增加可配置
    }

    public static void receiveHlogId(String _gid,String _pid){
        LogAgentContext.setThreadLogGroupId(_gid);
        LogAgentContext.setThreadCurrentLogId(_pid);
    }

    public static void request(StringBuffer requestUrl,String addr,long start,int status){

        //判断是否开启收集
        if(!RuntimeContext.isEnableRequest()){
            return;
        }

        //排除一些资源的请求
        String expand = getExpand(requestUrl);

        if(excludeExpands.contains(expand)){
            return ;
        }
        String id = RuntimeContext.logId();
        String pid = RuntimeContext.buildLogPId(id);
        LogData logData = HLogMonitor.createLogData("request",id,pid);
        logData.put("url",requestUrl.toString());
        logData.put("remoteAddr",addr);
        logData.put("spend",System.currentTimeMillis()-start);
        logData.put("status",status);
        RuntimeContext.writeEvent("request.log",null,logData);
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

}

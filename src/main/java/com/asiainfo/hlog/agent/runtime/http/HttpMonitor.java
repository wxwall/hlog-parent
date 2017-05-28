package com.asiainfo.hlog.agent.runtime.http;

import com.asiainfo.hlog.agent.HLogAgentConst;
import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.agent.runtime.LogAgentContext;
import com.asiainfo.hlog.agent.runtime.RuntimeContext;
import com.asiainfo.hlog.agent.runtime.RutimeCallFactory;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.ClassHelper;
import com.asiainfo.hlog.client.helper.LogUtil;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.client.model.LogData;
import com.asiainfo.hlog.web.HLogHttpRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>用于监控各种http方式的请求处理:</p>
 * <p>1、转递上游的日志组ID和上线ID;</p>
 * <p>2、记录资源请求日志;</p>
 * Created by chenfeng on 2016/5/8.
 */
public class HttpMonitor {

    private static Set<String> excludeExpands = new HashSet<String>();
    private  static Map<String,Object> sessionKeyExpr = new HashMap<String,Object>();

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

        Map<String,String> keyPaths = HLogConfig.getInstance().getSessionKeyPath();
        if(keyPaths!=null){
            for(String keypath : keyPaths.keySet()){
                Object complied = RutimeCallFactory.getRutimeCall().compileExpression(keyPaths.get(keypath));
                sessionKeyExpr.put(keypath,complied);
            }
        }
        //TODO 增加可配置
    }

    private static HLogConfig config = HLogConfig.getInstance();

    public static void receiveHlogId(String _gid,String _pid){
        HttpMonitor.clearReceiveHlogId();
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

    public static HLogMonitor.Node requestBegin(StringBuffer requestUrl,long beginTime,String className, String methodName){
        //String logId,String logPid,String className, String methodName, Long beginTime
        try {
            String id = LogUtil.logId();
            HLogMonitor.Node node = new HLogMonitor.Node(id,RuntimeContext.buildLogPId(id),className,methodName,beginTime);
            node.requestUrl = requestUrl.toString();
            node.type = HLogAgentConst.LOOP_TYPE_REQUEST;
            String gId = LogAgentContext.getThreadLogGroupId();
            if(gId==null){
                gId=node.logPid!=null?node.logPid:node.logId;
                LogAgentContext.setThreadLogGroupId(gId);
            }
            node.logGid = gId;
            String enableMonitor = HLogConfig.getInstance().getProperty(Constants.KEY_ENABLE_MONITOR_LOOP, "true");
            if (!"true".equals(enableMonitor.toLowerCase())) {
                HLogMonitor.addLoopMonitor(node);
            }
            return node;
        }catch (Exception e){
            Logger.error("请求超时监控异常",e);
        }
        return  null;
    }

    public static void request(StringBuffer requestUrl, String addr, long start, int status, HLogMonitor.Node node){
        HLogMonitor.removeLoopMonitor(node);
        //判断是否开启收集
        if(!config.isEnableRequest()){
            return;
        }

        //排除一些资源的请求
        String expand = getExpand(requestUrl);

        if(excludeExpands.contains(expand)){
            return ;
        }
        String pid = null;
        String id = null;
        if(node!=null){
            pid = node.logPid;
            id = node.logId;
            //System.out.println("1-----url="+requestUrl+",id="+id+",pid="+pid);
        }else{
            pid = RuntimeContext.getLogId();
            id = RuntimeContext.logId();
            //System.out.println("2-----url="+requestUrl+",id="+id+",pid="+pid);
        }

        //String pid = RuntimeContext.getLogId();
        //String id = RuntimeContext.logId();
        LogData logData = HLogMonitor.createLogData("request",id,pid);
        logData.put("url",requestUrl.toString());
        logData.put("remoteAddr",addr);
        long spend = System.currentTimeMillis()-start;
        logData.put("spend",spend);
        logData.put("status",status);
        if(config.isEnableSession()){
           Map session = LogAgentContext.getThreadSession();
            if(session != null && !session.isEmpty()){
                logData.put("sesinfo",session);
            }
        }
        RuntimeContext.writeEvent("request.log",null,logData);

        //将url也当作process写入
        node.speed=spend;
        HLogMonitor.doSendProcessLog(node,id,pid,status,"nvl".equals(pid),false);

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

    public static void sessionInfo(Object req0){
        if(!config.isEnableSession()){
            return;
        }
        try{
            //排除一些资源的请求
            HLogHttpRequest req = new HLogHttpRequest(req0);
            String expand = getExpand(req.getRequestURL());
            if(excludeExpands.contains(expand)){
                return ;
            }
            Object session = req.getSession();
            if(session == null){
                return;
            }

            Map<String,Object> sessionMap = new HashMap<String, Object>();
            for(String keypath : sessionKeyExpr.keySet()){
                try{
                    String[] keypathArray = keypath.split(":");
                    String sessionKey = keypathArray[0].trim().replace(".","_");
                    if(keypathArray.length == 2){
                        sessionKey = keypathArray[1];
                    }
                    Object val = RutimeCallFactory.getRutimeCall().executeExpression(sessionKeyExpr.get(keypath),session);
                    if(val != null){
                        sessionMap.put(sessionKey, val);
                    }
                }catch (Throwable t){
                    //sessionMap.put(keypath,t.getMessage());
                    Logger.warn("获取session信息[{0}]异常:{1}",keypath,t.getMessage());
                }
            }
            LogAgentContext.setThreadSession(sessionMap);
        }catch (Exception e){
            Logger.error("session采集失败",e);
        }
    }

}

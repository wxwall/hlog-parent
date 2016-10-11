package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.runtime.http.HttpMonitor;

/**
 * Created by lenovo on 2016/10/8.
 */
public class MyTest {
    public void testASM(){
        String logId = null;
        String logPid = null;
        String className = null;
        String methodName = null;
        Long beginTime = 1L;
        StringBuffer requestUrl = null;
        HLogMonitor.Node node = HttpMonitor.requestBegin(requestUrl,beginTime,logId,logPid,className,methodName);

        HLogMonitor.addLoopMonitor(node);

        String addr = null;long start=1;int status=1;

        HttpMonitor.request(requestUrl,addr,start,status,node);

    }

    public void test2(){


    }
}

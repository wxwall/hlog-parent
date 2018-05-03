package com.asiainfo.hlog.agent.bytecode.asm.socket;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

import java.util.Map;

/**
 * Created by yuan on 2018/5/3.
 */
public class SocketUtils {

    public static String buildHeader(){
        StringBuilder head = new StringBuilder();
        String gid = LogAgentContext.getThreadLogGroupId();
        String pid = LogAgentContext.getThreadCurrentLogId();
        String ctag = LogAgentContext.getCollectTag();
        if(gid != null){
            head.append("hloggid:").append(gid).append("\r\n");
        }
        if(pid != null){
            head.append("hloggid:").append(pid).append("\r\n");
        }
        if(ctag != null){
            head.append("hlogctag:").append(ctag).append("\r\n");
        }

        Map<String,Object> session = LogAgentContext.getThreadSession();
        if(session != null) {
            String deviceId = (String) session.get("deviceId");
            if(deviceId != null){
                head.append("deviceId:").append(deviceId).append("\r\n");
            }
            String staffCode = (String) session.get("staffCode");
            if(staffCode != null){
                head.append("staffCode:").append(staffCode).append("\r\n");
            }
        }
        return head.toString();
    }
}

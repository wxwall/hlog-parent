package com.asiainfo.hlog.agent.runtime;

/**
 * Created by c on 2015/3/17.
 */
public class LogAgentContext {

    public static final String S_AGENT_LOG_PID = "_agent_Log_pId_";
    public static final String S_AGENT_LOG_ID = "_agent_Log_Id_";

    //public static final String S_AGENT_ERR_PARAM_NAME = "_agent_Log_Err";


    private static final ThreadLocal<String> threadLogGroupId = new ThreadLocal<String>();
    private static final ThreadLocal<String> threadCurrentLogId = new ThreadLocal<String>();

    private static final ThreadLocal<Integer> threadCurrentIndex = new ThreadLocal<Integer>();

    public static void setThreadLogGroupId(String logGroupId){
        threadLogGroupId.set(logGroupId);
    }
    public static String getThreadLogGroupId(){
        return threadLogGroupId.get();
    }
    public static void clearLogGroupId(){
        threadLogGroupId.remove();
    }

    public static void setThreadCurrentLogId(String currentLogId){
        threadCurrentLogId.set(currentLogId);
    }

    public static String getThreadCurrentLogId(){
        return threadCurrentLogId.get();
    }
    public static void clearCurrentLogId(){
        threadCurrentLogId.remove();
    }

    public static void clear(){
        threadLogGroupId.remove();
        threadCurrentLogId.remove();
        threadCurrentIndex.set(new Integer(0));
    }

    public static int getIndex(){
        Integer index = threadCurrentIndex.get();
        if(index==null){
            index = new Integer(0);
        }
        index = index + 1;
        threadCurrentIndex.set(index);
        return index;
    }
}


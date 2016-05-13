package com.asiainfo.hlog.agent.runtime;

/**
 * 运行时的各类变量值
 * Created by c on 2015/3/17.
 */
public class LogAgentContext {


    /**
     * 日志组ID
     */
    private static final ThreadLocal<String> threadLogGroupId = new ThreadLocal<String>();
    /**
     * 当前日志ID
     */
    private static final ThreadLocal<String> threadCurrentLogId = new ThreadLocal<String>();
    /**
     * 当前日志序列
     */
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


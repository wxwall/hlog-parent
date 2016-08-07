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

    private static final ThreadLocal<Boolean> keepContext = new ThreadLocal<Boolean>();

    private static final ThreadLocal<Boolean> writeHeaderLocked = new ThreadLocal<Boolean>();

    public static void setKeepContext(boolean keep){
        keepContext.set(keep);
    }

    public static boolean isKeepContext(){
        if(keepContext.get()==null){
            return false;
        }else{
            return keepContext.get();
        }
    }

    public static void writeHeaderLocked(){
        writeHeaderLocked.set(true);
    }
    public static boolean isWriteHeaderLocked(){
        if(writeHeaderLocked.get()==null){
            return false;
        }else{
            return writeHeaderLocked.get();
        }
    }
    public static void cleanWriteHeaderLock(){
        writeHeaderLocked.set(false);
    }

    public static void setThreadLogGroupId(String logGroupId){
        threadLogGroupId.set(logGroupId);
    }
    public static String getThreadLogGroupId(){
        String gId = threadLogGroupId.get();
        if(gId==null){
            gId = threadCurrentLogId.get();
        }
        return gId;
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
        /*
        System.out.println("-------------------public static void clear()");
        StackTraceElement[] tt = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : tt) {
            System.out.println(stackTraceElement);
        }
        */
        threadLogGroupId.remove();
        threadCurrentLogId.remove();
        threadCurrentIndex.set(new Integer(0));
        keepContext.set(false);
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


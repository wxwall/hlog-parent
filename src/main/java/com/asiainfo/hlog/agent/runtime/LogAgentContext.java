package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.runtime.dto.TranCostDto;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;

import java.util.Map;
import java.util.Stack;

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

    /**
     * 事务耗时
     */
    private static final ThreadLocal<Stack<TranCostDto>> tranCostContext = new ThreadLocal<Stack<TranCostDto>>();

    private static final ThreadLocal<Map<String,Object>> threadSession = new ThreadLocal<Map<String,Object>>();


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
        threadSession.remove();
        threadCurrentIndex.set(new Integer(0));
        keepContext.set(false);
        clearThreadSession();
        tranCostContext.remove();
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

    public static Stack<TranCostDto> getTranCostContext(){
        Stack<TranCostDto> stack = tranCostContext.get();
        if(stack == null){
            stack = new Stack<TranCostDto>();
            tranCostContext.set(stack);
        }
        return stack;
    }

    public static TranCostDto getTranCost(){
        Stack<TranCostDto> stack = tranCostContext.get();
        if(stack!=null){
            return stack.peek();
        }
        return null;
    }

    public static TranCostDto popTranCost(){
        Stack<TranCostDto> stack = tranCostContext.get();
        if(stack == null || stack.isEmpty()){
            return null;
        }
        TranCostDto dto = stack.pop();
        dto.setCost(System.currentTimeMillis() - dto.getStartTime());
        return dto;
    }

    public static void clearTranCostContext(){
        Stack<TranCostDto> stack = tranCostContext.get();
        if(stack == null || stack.isEmpty()){
            tranCostContext.remove();
        }
    }

    public static Map<String, Object> getThreadSession() {
        return threadSession.get();
    }

    public static void setThreadSession(Map<String, Object> map){
        threadSession.set(map);
    }

    public static void clearThreadSession(){
        threadSession.remove();
    }
}


package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.agent.runtime.dto.SqlInfoDto;
import com.asiainfo.hlog.agent.runtime.dto.TranCostDto;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.model.LogData;
import sun.java2d.SurfaceDataProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 运行时的各类变量值
 * Created by c on 2015/3/17.
 */
public class LogAgentContext {

    /**
     * 采样标识
     */
    private static final ThreadLocal<String> collectTag = new ThreadLocal<String>();

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

    private static final ThreadLocal<SqlInfoDto> hibernateSqlContext = new ThreadLocal<SqlInfoDto>();

    private static final ThreadLocal<Boolean> isHttp = new ThreadLocal<Boolean>();

    /**
     * 同一个方法下的子方法被循环次数计数
     */
    public static final ThreadLocal<HashMap<String,Integer>> loopCounter = new ThreadLocal<HashMap<String,Integer>>();

    public static void setIsHttp(boolean keep){
        isHttp.set(keep);
    }

    public static boolean isHttp(){
        Boolean flag = isHttp.get();
        if(flag == null || !flag){
            return false;
        }
        return true;
    }


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
        if(HLogConfig.hlogGidTrace) {
            System.out.println("setThreadLogGroupId------------------:" + Thread.currentThread().getId() + ",gid=" + logGroupId);
            StackTraceElement[] sts = Thread.currentThread().getStackTrace();
            for (StackTraceElement st : sts) {
                System.out.println("--" + st.getClassName() + "." + st.getMethodName());
            }
        }
        if(logGroupId == null || logGroupId.isEmpty()){
            return;
        }
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
        if(HLogConfig.hlogGidTrace) {
            System.out.println("clearLogGroupId------------------:" + Thread.currentThread().getId() + ",gid=" + threadLogGroupId.get());
            StackTraceElement[] sts = Thread.currentThread().getStackTrace();
            for (StackTraceElement st : sts) {
                System.out.println("--" + st.getClassName() + "." + st.getMethodName());
            }
        }
        threadLogGroupId.remove();
    }

    public static void setThreadCurrentLogId(String currentLogId){
        if(currentLogId == null || currentLogId.isEmpty()){
            return;
        }
        threadCurrentLogId.set(currentLogId);
    }

    public static String getThreadCurrentLogId(){
        return threadCurrentLogId.get();
    }

    public static void clear(){
        threadLogGroupId.remove();
        threadCurrentLogId.remove();
        clearThreadSession();
        threadCurrentIndex.set(new Integer(0));
        keepContext.set(false);
        clearThreadSession();
        tranCostContext.remove();
        clearLoopCounter();
        if(!isHttp()){//http的由http请求入口进行clear
            clearCollectTag();
        }
    }

    public static void clearCollectTag(){
        collectTag.remove();
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

    public static void addThreadSession(String key,Object val){
        if(key == null || val == null){
            return;
        }
        Map<String, Object> map = threadSession.get();
        if(map == null){
            map = new HashMap<String, Object>();
        }
        map.put(key,val);
        setThreadSession(map);
    }

    public static void setThreadSession(Map<String, Object> map){
        threadSession.set(map);
    }

    public static void clearThreadSession(){
        threadSession.remove();
    }

    public static SqlInfoDto getHibernateSql(){
        return hibernateSqlContext.get();
    }
    public static void setHibernateSql(SqlInfoDto d){
        hibernateSqlContext.set(d);
    }

    public static void clearHibernateSql(){
        hibernateSqlContext.remove();
    }

    public static String getCollectTag(){
        return collectTag.get();
    }

    public static void setCollectTag(String tag){
        if(tag == null || tag.isEmpty()){
            return;
        }
        collectTag.set(tag);
    }

    public static String getLogGroupIdOrNull(){
        String tag = getCollectTag();
        String gId = threadLogGroupId.get();
        if("Y".equals(tag) && gId != null){
            return gId;
        }
        return "nvl";
    }

    public static void clearLoopCounter(){
        loopCounter.remove();
    }
}


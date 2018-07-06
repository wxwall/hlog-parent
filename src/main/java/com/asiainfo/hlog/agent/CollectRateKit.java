package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yuan on 2018/4/21.
 */
public class CollectRateKit {
    private static AtomicLong total = new AtomicLong(0);
    private static AtomicLong currNum = new AtomicLong(0);

    private static AtomicLong totalCrmSrv = new AtomicLong(0);
    private static AtomicLong currNumCrmSrv = new AtomicLong(0);

    public static long incrTotal(){
        return total.incrementAndGet();
    }

    public static long incrTotalCrmSrv(){
        return totalCrmSrv.incrementAndGet();
    }

    public static long incrTotal(long num){
        return total.addAndGet(num);
    }

    public static long getTotal(){
        return total.get();
    }

    public static long getTotalCrmSrv(){
        return totalCrmSrv.get();
    }

    public static long incrCurrNum(){
        return currNum.incrementAndGet();
    }

    public static long incrCurrNumCrmSrv(){
        return currNumCrmSrv.incrementAndGet();
    }

    public static long incrCurrNum(long num){
        return currNum.addAndGet(num);
    }

    public static long getCurrNum(){
        return currNum.get();
    }

    public static long getCurrNumCrmSrv(){
        return currNumCrmSrv.get();
    }


    public static long getSqlTime(long sqlTime){
        Map<String,Object> session = LogAgentContext.getThreadSession();
        String staffArray = HLogConfig.getInstance().getProperty("hlog.collect.staffCode","");
        if(session != null && !session.isEmpty() && !staffArray.isEmpty()){
            String staffCode = (String) session.get("staffCode");
            List<String> staffList = Arrays.asList(staffArray.split(","));
            if(staffList.contains(staffCode)){
                sqlTime = Integer.parseInt(HLogConfig.getInstance().getProperty(Constants.KEY_HLOG_COLLECT_STAFFCODE_SQL_TIME,""+sqlTime));
            }
        }
        return sqlTime;
    }

    public static long getProcessTime(long processTime){
        Map<String,Object> session = LogAgentContext.getThreadSession();
        String staffArray = HLogConfig.getInstance().getProperty("hlog.collect.staffCode","");
        if(session != null && !session.isEmpty() && !staffArray.isEmpty()){
            String staffCode = (String) session.get("staffCode");
            List<String> staffList = Arrays.asList(staffArray.split(","));
            if(staffList.contains(staffCode)){
                processTime = Integer.parseInt(HLogConfig.getInstance().getProperty(Constants.KEY_HLOG_COLLECT_STAFFCODE_PROCESS_TIME,""+processTime));
            }
        }
        return processTime;
    }

    public static boolean isCollect(){
        //配置全量的，全部采集，不受标识限制
        int rate = Integer.parseInt(HLogConfig.getInstance().getProperty("hlog.rate","100"));
        if(rate >= 100){
            LogAgentContext.setCollectTag("Y");
            return true;
        }

        //配置必采集的工号，不受采样影响，全采集
        Map<String,Object> session = LogAgentContext.getThreadSession();
        if(session != null && !session.isEmpty()){
            String staffCode = (String) session.get("staffCode");
            String staffArray = HLogConfig.getInstance().getProperty("hlog.collect.staffCode","");
            List<String> staffList = Arrays.asList(staffArray.split(","));
            if(staffList.contains(staffCode)){
                LogAgentContext.setCollectTag("Y");
                return true;
            }
        }

        String ctag = LogAgentContext.getCollectTag();
        if("Y".equals(ctag)){
            return true;
        }else if("N".equals(ctag)){
            return false;
        }


        boolean isCollect = true;

        Long _currNum = getCurrNum() + 1;
        Long _total = incrTotal();
        Double r = (_currNum/(_total * 1.0)) * 100;
        if(_currNum == 1 || r.intValue() <= rate){
            isCollect = true;
            incrCurrNum();
            LogAgentContext.setCollectTag("Y");
        }else {
            isCollect = false;
            LogAgentContext.setCollectTag("N");
        }
        return isCollect;
    }

    public static boolean isCollectCrmSrv(){
        int rate = Integer.parseInt(HLogConfig.getInstance().getProperty("hlog.rate.crmsrv","100"));
        if(rate >= 100){
            return true;
        }
        Long _currNum = getCurrNumCrmSrv() + 1;
        Long _total = incrTotalCrmSrv();
        Double r = (_currNum/(_total * 1.0)) * 100;
        if(_currNum == 1 || r.intValue() <= rate){
            incrCurrNumCrmSrv();
            return true;
        }
        return false;
    }
}

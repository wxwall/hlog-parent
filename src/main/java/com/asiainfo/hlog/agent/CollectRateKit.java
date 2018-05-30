package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;
import com.asiainfo.hlog.client.config.HLogConfig;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yuan on 2018/4/21.
 */
public class CollectRateKit {
    private static AtomicLong total = new AtomicLong(0);
    private static AtomicLong currNum = new AtomicLong(0);

    public static long incrTotal(){
        return total.incrementAndGet();
    }

    public static long incrTotal(long num){
        return total.addAndGet(num);
    }

    public static long getTotal(){
        return total.get();
    }

    public static long incrCurrNum(){
        return currNum.incrementAndGet();
    }

    public static long incrCurrNum(long num){
        return currNum.addAndGet(num);
    }

    public static long getCurrNum(){
        return currNum.get();
    }

    public static boolean isCollect(){
        //配置全量的，全部采集，不受标识限制
        int rate = Integer.parseInt(HLogConfig.getInstance().getProperty("hlog.rate","100"));
        if(rate >= 100){
            LogAgentContext.setCollectTag("Y");
            return true;
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
}

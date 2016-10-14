package com.asiainfo.hlog.agent.runtime;

import com.alibaba.fastjson.JSON;
import org.mvel2.MVEL;

/**
 * Created by chenfeng on 2016/8/7.
 */
public class DefRutimeCall implements IRutimeCall {
    public String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    public Object eval(String expr, Object object){
        return MVEL.eval(expr, object);
    }

    public Object executeExpression(Object expr, Object object){
        return MVEL.executeExpression(expr,object);
    }

    public Object compileExpression(String expr){
        return MVEL.compileExpression(expr);
    }
}

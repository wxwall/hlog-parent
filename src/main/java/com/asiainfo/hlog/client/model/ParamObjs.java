package com.asiainfo.hlog.client.model;

import java.util.Arrays;

/**
 * 入参对象
 * Created by chenfeng on 2015/4/30.
 */
public class ParamObjs {

    private Object[] params ;

    public ParamObjs(int num){
        params = new Object[num];
    }

    public void addParam(int offset,Object obj){
        if(params==null || params.length<=offset){
            return;
        }
        params[offset] = obj;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        if(params!=null){
            boolean b = false;
            for(Object param : params){
                if(b){
                    buff.append(",");
                }
                buff.append(param);
                b = true;
            }
        }
        return buff.toString();
    }
}

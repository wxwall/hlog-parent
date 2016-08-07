package com.asiainfo.hlog.agent.runtime;

import com.alibaba.fastjson.JSON;

/**
 * Created by chenfeng on 2016/8/7.
 */
public class DefRutimeCall implements IRutimeCall {
    public String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }
}

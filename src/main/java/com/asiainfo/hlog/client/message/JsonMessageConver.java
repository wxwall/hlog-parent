package com.asiainfo.hlog.client.message;

import com.google.gson.Gson;

import java.util.List;

/**
 * 默认的日志转JSON串
 * Created by chenfeng on 2015/4/16.
 */
public class JsonMessageConver implements IMessageConver {

    private final Gson gson = new Gson();


    public String convert(List<Object> dataList) {
        return gson.toJson(dataList);
    }

    @Override
    public String convert(Object logData) {
        return gson.toJson(logData);
    }

}

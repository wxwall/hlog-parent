package com.asiainfo.hlog.agent.runtime;

/**
 * 在应用运行时需要用到自定义类加载器中的类时,需要通过该接口定义
 * Created by chenfeng on 2016/8/7.
 */
public interface IRutimeCall {

    /**
     * 将对象转成字符串
     * @param obj
     * @return
     */
    String toJson(Object obj);

    /**
     * 运行表达式
     * @param expr
     * @param object
     * @return
     */
    Object eval(String expr, Object object);
}

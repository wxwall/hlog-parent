package com.asiainfo.hlog.agent;

/**
 * 代理层代码增强常量
 * Created by chenfeng on 2016/5/3.
 */
public abstract class HLogAgentConst {

    /**
     * 表示是一个运行过程的编码
     */
    public static final String MV_CODE_PROCESS = "process";
    /**
     * 表示是一个采集第三方日志框架的编码
     */
    public static final String MV_CODE_LOGGER = "logger";
    /**
     * 表示是一个采集SQL的编码
     */
    public static final String MV_CODE_SQL = "sql";
    /**
     * 表示是一个采集异常的编码
     */
    public static final String MV_CODE_ERROR = "error";

    /**
     * 表示是一个方法入参的编码
     */
    public static final String MV_CODE_PARAMS = "params";
    /**
     * 表示一个拦截入参的编码
     */
    public static final String MV_CODE_INTERCEPT = "intercept";
    /**
     * 表示一个拦截入参并且不继续执行后续代码
     */
    public static final String MV_CODE_INTERCEPT_RET = "interceptRet";
    /**
     * 表示一个拦截事务的编码
     */
    public static final String MV_CODE_TRANSACTION = "tran";

    /**
     * 表示一个循环监控的编码
     */
    public static final String MV_CODE_LOOP = "loop";

    /**
    * 表示一个循环监控的类型，方法
    */
    public static final String LOOP_TYPE_METHOD = "method";
    /**
     * 表示一个循环监控的类型，请求
     */
    public static final String LOOP_TYPE_REQUEST = "request";

}

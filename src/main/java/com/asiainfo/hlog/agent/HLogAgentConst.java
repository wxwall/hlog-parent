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
     * 表示一个拦截入参的编码
     */
    public static final String MV_CODE_INTERCEPT = "intercept";
    /**
     * 表示一个拦截入参并且不继续执行后续代码
     */
    public static final String MV_CODE_INTERCEPT_RET = "interceptRet";

    public static final String HEADER_HLOG_AGENT_GID = "Hlog-Agent-Gid";
    public static final String HEADER_HLOG_AGENT_PID = "Hlog-Agent-Pid";
}

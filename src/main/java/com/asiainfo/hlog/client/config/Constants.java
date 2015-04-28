package com.asiainfo.hlog.client.config;

/**
 * 常量定义
 * Created by chenfeng on 2015/4/27.
 */
public class Constants {
    //--------------------------配置文件名称-------------------------------------

    /**
     * 默认配置文件名称
     */
    public static final String FIEL_NAME_HLOG_CONFS = "hlog-confs.properties";

    /**
     * 默认扩展配置文件名称,{0}为hlogCfgName的系统属性值
     */
    public static final String FILE_NAME_HLOG_EXT_CONFS = "hlog-{0}-confs.properties";

    //--------------------------系统属性key名称-----------------------------------

    public static final String SYS_KEY_HLOG_DOMAIN = "hlogDomain";

    public static final String SYS_KEY_HLOG_CFG_NAME = "hlogCfgName";

    //--------------------------配置属性key名称-----------------------------------

    /**
     *全局开关
     */
    public static final String KEY_HLOG_ENABLE = "hlog.enable";
    /**
     *植入代码的基础路径前缀，如hlog.base.path.com.asiainfo
     */
    public static final String KEY_BASE_PATH_PREFIX = "hlog.base.path.";

    /**
     * 排除路径
     */
    public static final String KEY_HLOG_EXCLUDE_PATHS = "hlog.exclude.paths";

    /**
     * 收集Log输出的级别,如debug,info,warm,error
     */
    public static final String KEY_HLOG_LEVLE = "hlog.level.";
    /**
     *开启哪些路径需要收集日志
     */
    public static final String KEY_HLOG_CAPTURE_ENABLE = "hlog.capture.enable.";

    /**
     *对收集到日志数据的处理器
     */
    public static final String KEY_HLOG_CAPTURE_HANDLER = "hlog.capture.handler.";

    /**
     * 处理器前缀
     */
    public static final String KEY_HLOG_HANDLER = "hlog.handler.";

    /**
     * 设置默认的日志接收器
     */
    public static final String KEY_DEF_REVEIVER_CLASS = new String("hlog.reveriver.class");


    //--------------------------配置平台属性key名称---------------------------------

    public static final String KEY_HLOG_UNICONFIG_ENABLED = "uniconfig.enabled";


}

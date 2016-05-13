package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.Type;

/**
 * Created by chenfeng on 2016/4/20.
 */
public class ASMConsts {
    public static final String JAVA_LANG_STRING = Type.getInternalName(String.class);
    public static final String JAVA_LANG_OBJECT = Type.getInternalName(Object.class);

    //--String相关的一些描述符
    public static final String LJAVA_LANG_STRING = "Ljava/lang/String;";
    public static final String NONPARAM_LJAVA_LANG_STRING = "()Ljava/lang/String;";
    public static final String LJAVA_LANG_STRING_V = "(Ljava/lang/String;)V";

    public static final String CURRENT_TIME_MILLIS = "currentTimeMillis";

    //--Mybatis相关的
    public static final String MY_BATIS_BASE_JDBC_LOGGER = "org/apache/ibatis/logging/jdbc/BaseJdbcLogger";
    public static final String PARAMS = "params";
    public static final String MY_BATIS_ERROR_CONTEXT = "org/apache/ibatis/executor/ErrorContext";
    public static final String MY_BATIS_JDBC_PREPARED_STATEMENT_LOGGER = "org/apache/ibatis/logging/jdbc/PreparedStatementLogger";
    //public static final String HLOG_MONITOR = "com/asiainfo/hlog/agent/bytecode/asm/HLogMonitor";
    public static final String MY_BATIS_JDBC_PREPARED_STATEMENT_LOGGER_CLS = "org.apache.ibatis.logging.jdbc.PreparedStatementLogger";

    //---HlogMonitor类的方法
    public static final String HLOG_MONITOR_GET_CONFIG_SQL_SPEED = "getConfigSqlSpeed";
    public static final String HLOG_MONITOR_SQL_MONITOR = "sqlMonitor";
    public static final String JAVA_LANG_STRING_BUILDER = "java/lang/StringBuilder";
    public static final String LJAVA_LANG_STRING_LJAVA_LANG_STRING_BUILDER = "(Ljava/lang/String;)Ljava/lang/StringBuilder;";
    public static final String STRINGBUILDER_APPEND = "append";
}

package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.bytecode.asm.mybatis.BaseJdbcLoggerNewMethods;
import com.asiainfo.hlog.agent.bytecode.asm.mybatis.ErrorContextNewMethods;
import com.asiainfo.hlog.agent.bytecode.asm.socket.SocketOutputStreamNewMethods;
import com.asiainfo.hlog.client.config.LogSwoopRule;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 新增方法的创建器
 * Created by chenfeng on 2016/4/25.Sts
 */
public abstract class HLogMethodCreator {

    private static Map<String,Class> codeClassMap = new ConcurrentHashMap<String,Class>();

    private static Map<String,IHLogNewMethods> codeIntesMap = new ConcurrentHashMap<String,IHLogNewMethods>();

    private static final String HLOG_EXT_METHOD_CREATE_PROPERTIES = "hlog-method-creator.properties";

    private static final String HLOG_METHOD_CREATE_PROPERTIES = "/META-INF/hlog-method-creator.properties";
    static {
        try {
            LoadPropInfo.loadPropAndExt(codeClassMap,HLOG_METHOD_CREATE_PROPERTIES,HLOG_EXT_METHOD_CREATE_PROPERTIES);
        } catch (Throwable t) {
            Logger.error("加载hlog-method-creator.properties异常",t);
            codeClassMap.put(ErrorContextNewMethods.CODE,ErrorContextNewMethods.class);
            codeClassMap.put(BaseJdbcLoggerNewMethods.CODE,BaseJdbcLoggerNewMethods.class);
            codeClassMap.put(SocketOutputStreamNewMethods.CODE,SocketOutputStreamNewMethods.class);
        }
    }

    private static IHLogNewMethods getIHlogNewMethodsByCode(String code,String className){
        IHLogNewMethods hlogNewMethods = null;

        if(codeIntesMap.containsKey(code)){
            return codeIntesMap.get(code);
        }
        synchronized (codeClassMap){
            try{
                Class cls = codeClassMap.get(code);
                Constructor c = cls.getConstructor(String.class);
                hlogNewMethods = ((IHLogNewMethods) c.newInstance(className));
                codeIntesMap.put(code,hlogNewMethods);
            }catch (Throwable t){
                Logger.error(t);
            }
        }


        return hlogNewMethods;
    }

    public static void create(LogSwoopRule classRule, ClassWriter classWriter,String className){
        List<String> codes = classRule.getNewMethodCodes();
        for (String code : codes){
            IHLogNewMethods newMethods = getIHlogNewMethodsByCode(code,className);
            if(newMethods!=null){
                newMethods.createNewMethods(classWriter);
            }
        }
    }
}

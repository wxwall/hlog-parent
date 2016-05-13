package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.bytecode.asm.mybatis.BaseJdbcLoggerMethodVisitor;
import com.asiainfo.hlog.agent.bytecode.asm.mybatis.MyBatisSQLMethodVisitor;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据编码取出方法指令的观察者
 * Created by chenfeng on 2016/4/26.
 */
public abstract class HLogMethodVisitorFactory {

    private static Map<String,Class> mvClassMap = new ConcurrentHashMap<String,Class>();

    private static final String HLOG_EXT_VISITOR_PROPERTIES = "hlog-method-visitor.properties";

    private static final String HLOG_METHOD_VISITOR_PROPERTIES = "/META-INF/hlog-method-visitor.properties";

    static {
        try{
            LoadPropInfo.loadPropAndExt(mvClassMap,HLOG_METHOD_VISITOR_PROPERTIES,HLOG_EXT_VISITOR_PROPERTIES);
        }catch (Throwable t){
            Logger.error("加载hlog-method-visitor.properties异常",t);
            //如果读取失败,增加默认配置
            mvClassMap.put(ProcessMethodVisitor.CODE,ProcessMethodVisitor.class);
            mvClassMap.put(LoggerMethodVisitor.CODE,LoggerMethodVisitor.class);
            mvClassMap.put(MyBatisSQLMethodVisitor.CODE,MyBatisSQLMethodVisitor.class);
            mvClassMap.put(BaseJdbcLoggerMethodVisitor.CODE,BaseJdbcLoggerMethodVisitor.class);
            mvClassMap.put(InterceptMethodVisitor.CODE,InterceptMethodVisitor.class);
            mvClassMap.put(InterceptRetMethodVisitor.CODE,InterceptRetMethodVisitor.class);
        }
    }


    public static MethodVisitor newMethodVisitor(String className,MethodVisitor mv,int access, String name, String desc, byte[] datas,String code,String mcode){

        Class clazz = mvClassMap.get(code);

        if(clazz==null){
            return mv;
        }
        try{
            return (MethodVisitor)clazz.getConstructor(int.class,String.class,String.class,String.class,MethodVisitor.class,byte[].class,String.class)
                    .newInstance(access, className, name, desc, mv, datas,mcode);
        }catch (Throwable t){
            Logger.error(t);
            return mv;
        }

    }
}

package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.agent.classloader.ClassLoaderHolder;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.LoaderHelper;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * 日志代理,将负责应用jvm的agent操作</br>
 * 一切从此开始....
 * Created by chenfeng on 2015/3/16.
 */
public class HLogAgent {

    /**
     * 默认的ClassFileTransformer实现类
     */
    private static final String DEF_CLASS_PRE_AGENT_ADAPTER = "com.asiainfo.hlog.agent.ClassPreProcessorAgentAdapter";

    static ClassFileTransformer classPreProcessorAgentAdapter;
    static Instrumentation inst;

    private static void domain(Instrumentation inst){

        System.out.println("========== Asiainfo HLog Agent ["+HLogConfig.VERSION+"] ==============\n");

        if (HLogAgent.inst == null) {
            HLogAgent.inst = inst;
        }

        if (classPreProcessorAgentAdapter == null) {
            //获取配置实例
            HLogConfig config = HLogConfig.getInstance();
            //初始化配置信息,后需要从properties文件或服务端来获取
            config.initConfig(true);
            classPreProcessorAgentAdapter = createClassFileTransformer();
        }

        inst.addTransformer(classPreProcessorAgentAdapter);

        LoaderHelper.setLoader(ClassLoaderHolder.getInstance().getClassLoader());

    }

    public static void premain(String agentArgs, Instrumentation inst)
            throws ClassNotFoundException, UnmodifiableClassException {
        domain(inst);
    }

    public static void agentmain(String options, Instrumentation inst) {
        domain(inst);
    }


    private static ClassFileTransformer createClassFileTransformer() {
        try{
            Class c = ClassLoaderHolder.getInstance()
                    .loadClass(DEF_CLASS_PRE_AGENT_ADAPTER);
            ClassFileTransformer classFileTransformer = (ClassFileTransformer) c.newInstance();
            return classFileTransformer;
        }catch (Throwable e){
            e.printStackTrace(System.err);
        }
        return null;
    }


}

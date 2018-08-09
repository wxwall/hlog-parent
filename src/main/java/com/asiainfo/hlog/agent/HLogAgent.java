package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.client.config.HLogConfig;
import com.google.common.base.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

        System.out.println("========== Asiainfo HLog Agent ["+HLogConfig.VERSION+"] ==============");

        printAsciiImage();
        printVersionInfo();

        if (HLogAgent.inst == null) {
            HLogAgent.inst = inst;
        }

        if (classPreProcessorAgentAdapter == null) {
            //获取配置实例
            //HLogConfig config = HLogConfig.getInstance();
            //初始化配置信息,后需要从properties文件或服务端来获取
            //config.initConfig(true);
            classPreProcessorAgentAdapter = createClassFileTransformer();
        }

        inst.addTransformer(classPreProcessorAgentAdapter);
    }

    private static void printVersionInfo(){
        StringBuilder sb = new StringBuilder();
        sb.append("VERSION=").append(HLogConfig.VERSION).append(";");
        sb.append("VER_NUM=").append(HLogConfig.VER_NUM).append(";");
        sb.append("VER_START_DT=").append(HLogConfig.VER_START_DT).append(";");
        System.err.println(sb.toString());
    }

    public static void premain(String agentArgs, Instrumentation inst)
            throws ClassNotFoundException, UnmodifiableClassException {
        domain(inst);
    }

    public static void agentmain(String options, Instrumentation inst) {
        domain(inst);
    }


    private static ClassFileTransformer createClassFileTransformer() {
//        try{
//            Class c = ClassLoaderHolder.getInstance()
//                    .loadClass(DEF_CLASS_PRE_AGENT_ADAPTER);
//            ClassFileTransformer classFileTransformer = (ClassFileTransformer) c.newInstance();
//            return classFileTransformer;
//        }catch (Throwable e){
//            e.printStackTrace(System.err);
//        }
        return new ClassPreProcessorAgentAdapter();
    }

    private static void printAsciiImage(){
        InputStream fis = null;
        try{
            fis = HLogAgent.class.getResourceAsStream("/ascii.txt");
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String str = result.toString(Charsets.UTF_8.name());
            System.out.println(str);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}

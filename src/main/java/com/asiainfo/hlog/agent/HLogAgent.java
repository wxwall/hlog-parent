package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.agent.classloader.ClassLoaderHolder;
import com.asiainfo.hlog.client.helper.LoaderHelper;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * 日志代理,将负责应用jvm的agent操作</br>
 * 一切从此开始....
 * Created by c on 2015/3/16.
 */
public class HLogAgent {
    static ClassFileTransformer classPreProcessorAgentAdapter;
    static Instrumentation inst;

    public static void premain(String agentArgs, Instrumentation inst)
            throws ClassNotFoundException, UnmodifiableClassException {

        if (HLogAgent.inst == null) {
            HLogAgent.inst = inst;
        }

        if (classPreProcessorAgentAdapter == null) {
            classPreProcessorAgentAdapter = createClassFileTransformer();
        }
        inst.addTransformer(classPreProcessorAgentAdapter);

        LoaderHelper.setLoader(ClassLoaderHolder.getInstance().getClassLoader());

        System.out.println("Asiainfo HLog Agent start!!");
    }

    public static void agentmain(String options, Instrumentation inst) {
        if (HLogAgent.inst == null) {
            HLogAgent.inst = inst;
        }

        if (classPreProcessorAgentAdapter == null) {
            classPreProcessorAgentAdapter = createClassFileTransformer();
        }
        inst.addTransformer(classPreProcessorAgentAdapter);

        LoaderHelper.setLoader(ClassLoaderHolder.getInstance().getClassLoader());

        System.out.println("Asiainfo HLog Agent start!!");
    }


    private static ClassFileTransformer createClassFileTransformer() {
        try{
            Class c = ClassLoaderHolder.getInstance()
                    .loadClass("com.asiainfo.hlog.agent.ClassPreProcessorAgentAdapter");
            ClassFileTransformer classFileTransformer = (ClassFileTransformer) c.newInstance();
            return classFileTransformer;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}

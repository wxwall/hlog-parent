package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.agent.bytecode.javassist.HLogPreProcessor;
import com.asiainfo.hlog.agent.classloader.ClassLoaderHolder;
import com.asiainfo.hlog.agent.runtime.HLogOutputStream;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.jmx.HLogJMXReport;
import com.asiainfo.hlog.client.helper.LoaderHelper;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 *  对类定义可动态改变和操作,在类的字节码载入jvm前会</br>
 *  调用ClassFileTransformer的transform方法，从而实现修改原类方法的功能</br>
 * > 通过HLogConfig初始化整个运行需要的参数,模块名称,</br>
 * > 实例化IHLogPreProcessor的实现
 * Created by chenfeng on 2015/4/9.
 */
public class ClassPreProcessorAgentAdapter implements ClassFileTransformer {

    private IHLogPreProcessor preProcessor;

    public ClassPreProcessorAgentAdapter(){

        LoaderHelper.setLoader(this.getClass().getClassLoader());

        //获取配置实例
        HLogConfig config = HLogConfig.getInstance();
        //初始化配置信息,后需要从properties文件或服务端来获取
        config.initConfig();

        //注册JMX观察
        HLogJMXReport.getHLogJMXReport().start();

        //是否开启System.out/System.err
        if(HLogConfig.getInstance().isSystemOutEnable() ){
            HLogOutputStream hLogOutputStream = new HLogOutputStream(Constants.MCODE_SYSTEM_OUT);
            PrintStream printStream = new PrintStream(hLogOutputStream);
            System.setOut(printStream);
        }
        if(HLogConfig.getInstance().isSystemErrEnable()){
            HLogOutputStream hLogOutputStream = new HLogOutputStream(Constants.MCODE_SYSTEM_ERR);
            PrintStream printStream = new PrintStream(hLogOutputStream);
            System.setErr(printStream);
        }

        //TODO 可根据配置来创建不同的实现
        preProcessor = new HLogPreProcessor();
        preProcessor.initialize();
    }

    /**
     * 字节码加载到虚拟机前会进入这个方法
     * @param loader
     * @param className
     * @param classBeingRedefined
     * @param protectionDomain
     * @param classfileBuffer
     * @return
     * @throws IllegalClassFormatException
     */
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return preProcessor.preProcess(loader,className,protectionDomain,classfileBuffer);
    }
}

package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.agent.bytecode.asm.HLogPreProcessor;
import com.asiainfo.hlog.agent.jvm.HLogJvmReport;
import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.jmx.HLogJMXReport;
import com.asiainfo.hlog.client.helper.LoaderHelper;
import com.asiainfo.hlog.client.helper.Logger;

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
        HLogConfig config = HLogConfig.getInstance(true);
        //初始化配置信息,后需要从properties文件或服务端来获取
        //config.initConfig();

        //注册JMX观察
        try{
            HLogJMXReport.getHLogJMXReport().start();
        }catch (Throwable t){
            Logger.warn("Hlog注册JMX服务失败.",t);
        }
        //jvm信息监控
        String enableMonitor = HLogConfig.getInstance().getProperty(Constants.KEY_ENABLE_MONITOR_JVM, "true");
        if ("true".equals(enableMonitor.toLowerCase())) {
            HLogJvmReport.getInstance().start();
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

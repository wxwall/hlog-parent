package com.asiainfo.hlog.agent;

import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * 对类二进制字节流进行预处理</br>
 * 所有被classloader加载的class二进制都将经过preProcess方法</br>
 * 对类字节码操作可以有多种实现比如javassist、asm等
 * Created by c on 2015/4/10.
 */
public interface IHLogPreProcessor {

    /**
     * 初始化方法,在这里可以加载配置信息或以配置平台通信
     */
    void initialize();

    /**
     * 类字节码预处理,主要功能是将需要监测的代码织入到字节流中
     * @param classLoader
     * @param className
     * @param protectionDomain
     * @param bytes
     * @return 返回加工后的类字节码
     */
    byte[] preProcess(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] bytes);
}


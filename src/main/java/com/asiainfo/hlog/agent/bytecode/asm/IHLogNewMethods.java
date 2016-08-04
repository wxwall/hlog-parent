package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.ClassWriter;

/**
 * 用于创建新的方法
 * Created by chenfeng on 2016/4/25.
 */
public interface IHLogNewMethods {
    /**
     * 创建新的方法,可以创建多个新方法
     * @param classWriter
     */
    void createNewMethods(ClassWriter classWriter);
}

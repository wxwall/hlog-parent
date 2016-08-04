package com.asiainfo.hlog.agent.bytecode.asm.mybatis;

import com.asiainfo.hlog.agent.bytecode.asm.ASMConsts;
import com.asiainfo.hlog.agent.bytecode.asm.IHLogNewMethods;
import com.asiainfo.hlog.org.objectweb.asm.ClassWriter;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

/**
 * 给ErrorContext类增加getSql方法
 * Created by chenfeng on 2016/4/25.
 */
public class ErrorContextNewMethods implements IHLogNewMethods, Opcodes{

    public static final String CODE = "+mybatis.ErrorContext.newMethods";

    public ErrorContextNewMethods(String className){
    }

    public void createNewMethods(ClassWriter classWriter) {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "getSql", ASMConsts.NONPARAM_LJAVA_LANG_STRING, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ASMConsts.MY_BATIS_ERROR_CONTEXT, "sql", ASMConsts.LJAVA_LANG_STRING);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}

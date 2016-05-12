package com.asiainfo.hlog.agent.bytecode.asm.mybatis;

import com.asiainfo.hlog.agent.bytecode.asm.ASMConsts;
import com.asiainfo.hlog.agent.bytecode.asm.IHLogNewMethods;
import com.asiainfo.hlog.org.objectweb.asm.ClassWriter;
import com.asiainfo.hlog.org.objectweb.asm.FieldVisitor;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

/**
 * <p>给BaseJdbcLogger类增加</p>
 * <p> params字段;</p>
 * <p> params的get和set方法;</p>
 * Created by chenfeng on 2016/4/26.
 */
public class BaseJdbcLoggerNewMethods implements IHLogNewMethods, Opcodes {

    public static final String CODE = "+mybatis.BaseJdbcLogger.newMethods";

    public void createNewMethods(ClassWriter cw) {

        FieldVisitor fv = cw.visitField(ACC_PRIVATE, ASMConsts.PARAMS, ASMConsts.LJAVA_LANG_STRING, null, null);
        fv.visitEnd();
        MethodVisitor mv = null;
        //定义getParams方法
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getParams", ASMConsts.NONPARAM_LJAVA_LANG_STRING, null, null);
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, ASMConsts.MY_BATIS_BASE_JDBC_LOGGER, ASMConsts.PARAMS, ASMConsts.LJAVA_LANG_STRING);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        //定义setParams方法
        {
            mv = cw.visitMethod(ACC_PUBLIC, "setParams", ASMConsts.LJAVA_LANG_STRING_V, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, ASMConsts.MY_BATIS_BASE_JDBC_LOGGER, ASMConsts.PARAMS, ASMConsts.LJAVA_LANG_STRING);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

    }
}

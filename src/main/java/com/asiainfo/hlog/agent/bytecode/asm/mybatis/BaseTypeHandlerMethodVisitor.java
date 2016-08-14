package com.asiainfo.hlog.agent.bytecode.asm.mybatis;

import com.asiainfo.hlog.agent.bytecode.asm.ASMConsts;
import com.asiainfo.hlog.agent.bytecode.asm.AbstractMethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

/**
 * Created by chenfeng on 2016/8/14.
 */
public class BaseTypeHandlerMethodVisitor extends AbstractMethodVisitor {

    public static final String CODE = "mybatis.setParameters";
    public BaseTypeHandlerMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
    }

    public void visitInsn(int opcode) {

        boolean isReturn = opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
        if (isReturn) {
            visitMethodInsn(Opcodes.INVOKESTATIC, ASMConsts.MY_BATIS_ERROR_CONTEXT, "instance", "()Lorg/apache/ibatis/executor/ErrorContext;", false);
            visitIntInsn(Opcodes.ILOAD,2);
            visitIntInsn(Opcodes.ALOAD,3);
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMConsts.MY_BATIS_ERROR_CONTEXT, "addParamVal", "(ILjava/lang/Object;)V", false);
        }
        super.visitInsn(opcode);
    }
}

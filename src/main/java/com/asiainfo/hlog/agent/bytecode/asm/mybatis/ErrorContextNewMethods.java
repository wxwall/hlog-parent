package com.asiainfo.hlog.agent.bytecode.asm.mybatis;

import com.asiainfo.hlog.agent.bytecode.asm.ASMConsts;
import com.asiainfo.hlog.agent.bytecode.asm.IHLogNewMethods;
import com.asiainfo.hlog.org.objectweb.asm.*;

/**
 * 给ErrorContext类增加getSql方法
 * Created by chenfeng on 2016/4/25.
 */
public class ErrorContextNewMethods implements IHLogNewMethods, Opcodes{

    public static final String CODE = "+mybatis.ErrorContext.newMethods";

    public ErrorContextNewMethods(String className){
    }

    public void createNewMethods(ClassWriter classWriter) {
        FieldVisitor fv = classWriter.visitField(ACC_PRIVATE, "_paramVals", "Ljava/util/List;", null, null);
        fv.visitEnd();

        createGetSqlMethod(classWriter);

        createAddParamVal(classWriter);

        createGetParamVals(classWriter);
    }

    private void createGetSqlMethod(ClassWriter classWriter) {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "getSql", ASMConsts.NONPARAM_LJAVA_LANG_STRING, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ASMConsts.MY_BATIS_ERROR_CONTEXT, "sql", ASMConsts.LJAVA_LANG_STRING);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createAddParamVal(ClassWriter classWriter){
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "addParamVal", "(ILjava/lang/Object;)V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/apache/ibatis/executor/ErrorContext", "_paramVals", "Ljava/util/List;");
        Label l1 = new Label();
        mv.visitJumpInsn(IFNONNULL, l1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
        mv.visitFieldInsn(PUTFIELD, "org/apache/ibatis/executor/ErrorContext", "_paramVals", "Ljava/util/List;");
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/apache/ibatis/executor/ErrorContext", "_paramVals", "Ljava/util/List;");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
        Label l4 = new Label();
        mv.visitJumpInsn(IF_ICMPGE, l4);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/apache/ibatis/executor/ErrorContext", "_paramVals", "Ljava/util/List;");
        mv.visitVarInsn(ILOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "remove", "(I)Ljava/lang/Object;", true);
        mv.visitInsn(POP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/apache/ibatis/executor/ErrorContext", "_paramVals", "Ljava/util/List;");
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(ILjava/lang/Object;)V", true);
        Label l7 = new Label();
        mv.visitJumpInsn(GOTO, l7);
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/apache/ibatis/executor/ErrorContext", "_paramVals", "Ljava/util/List;");
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
        mv.visitInsn(POP);
        mv.visitLabel(l7);
        mv.visitInsn(RETURN);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitLocalVariable("this", "Lorg/apache/ibatis/executor/ErrorContext;", null, l0, l8, 0);
        mv.visitLocalVariable("index", "I", null, l0, l8, 1);
        mv.visitLocalVariable("val", "Ljava/lang/Object;", null, l0, l8, 2);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    private void createGetParamVals(ClassWriter classWriter){
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "getParamVals", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/apache/ibatis/executor/ErrorContext", "_paramVals", "Ljava/util/List;");
        Label l1 = new Label();
        mv.visitJumpInsn(IFNONNULL, l1);
        mv.visitLdcInsn("");
        mv.visitInsn(ARETURN);
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/apache/ibatis/executor/ErrorContext", "_paramVals", "Ljava/util/List;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
        mv.visitInsn(ARETURN);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLocalVariable("this", "Lorg/apache/ibatis/executor/ErrorContext;", null, l0, l3, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}

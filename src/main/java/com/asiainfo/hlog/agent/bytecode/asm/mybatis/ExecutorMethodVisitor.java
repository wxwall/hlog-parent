package com.asiainfo.hlog.agent.bytecode.asm.mybatis;

import com.asiainfo.hlog.agent.bytecode.asm.ASMConsts;
import com.asiainfo.hlog.agent.bytecode.asm.ASMUtils;
import com.asiainfo.hlog.agent.bytecode.asm.AbstractMethodVisitor;
import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * Created by chenfeng on 2016/8/14.
 */
public class ExecutorMethodVisitor extends AbstractMethodVisitor {

    public static final String CODE = "mybatis.executor.sql";

    public ExecutorMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
    }

    public void visitInsn(int opcode) {

        boolean isReturn = opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
        if (isReturn) {

            Label label = new Label();
            int retLVSlot;
            boolean isIntRet = false;
            if(methodName.equals("doUpdate")){
                retLVSlot = defineLocalVariable("_ret",int.class,label,null);
                visitIntInsn(Opcodes.ISTORE,retLVSlot);
                isIntRet = true;
            }else{
                retLVSlot = defineLocalVariable("_ret",List.class,label,null);
                visitIntInsn(Opcodes.ASTORE,retLVSlot);
            }


            //visitVarInsn(Opcodes.LLOAD, startLVSlot);
            ASMUtils.visitStaticMethod(mv,System.class, ASMConsts.CURRENT_TIME_MILLIS,null);
            visitVarInsn(Opcodes.ALOAD, 1);
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/ibatis/mapping/MappedStatement", "getId", "()Ljava/lang/String;", false);
            visitMethodInsn(Opcodes.INVOKESTATIC, ASMConsts.MY_BATIS_ERROR_CONTEXT, "instance", "()Lorg/apache/ibatis/executor/ErrorContext;", false);
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMConsts.MY_BATIS_ERROR_CONTEXT, "getSql", ASMConsts.NONPARAM_LJAVA_LANG_STRING, false);
            visitMethodInsn(Opcodes.INVOKESTATIC, ASMConsts.MY_BATIS_ERROR_CONTEXT, "instance", "()Lorg/apache/ibatis/executor/ErrorContext;", false);
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMConsts.MY_BATIS_ERROR_CONTEXT, "getParamVals", "()Ljava/lang/String;", false);
            if(isIntRet){
                visitVarInsn(Opcodes.ILOAD, retLVSlot);
                visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            }else{
                visitIntInsn(Opcodes.ALOAD,retLVSlot);
            }

            ASMUtils.visitStaticMethod(mv,HLogMonitor.class, ASMConsts.HLOG_MONITOR_SQL_MONITOR,long.class,String.class,String.class,String.class,Object.class);

            if(isIntRet){
                visitIntInsn(Opcodes.ILOAD,retLVSlot);
            }else{
                visitIntInsn(Opcodes.ALOAD,retLVSlot);
            }
        }

        super.visitInsn(opcode);
    }


}

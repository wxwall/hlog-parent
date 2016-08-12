package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;


/**
 * Created by chenfeng on 2016/5/5.
 */
public class TransactionMethodVisitor extends AbstractMethodVisitor {

    private final String className;
    private final String methodName;

    public TransactionMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public void visitInsn(int opcode) {
        boolean isRetInsn = IRETURN <= opcode && opcode <= RETURN;
        if(isRetInsn && "doBegin".equals(methodName)){
            doTransactionBegin();
        }else if(isRetInsn && "doCleanupAfterCompletion".equals(methodName)){
            doTransactionEnd();
        }
        super.visitInsn(opcode);
    }

    private void doTransactionEnd(){
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/HLogMonitor", "transactionElapsedTimeMonitor", "()V", false);
    }

    private void doTransactionBegin(){
        Label start = new Label();
        visitLabel(start);
        int startSlot = defineLocalVariable("_start",long.class,start,null);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LSTORE, startSlot);

        Label method = new Label();
        mv.visitLabel(method);
        int methodSlot = defineLocalVariable("_method",String.class,method,null);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/springframework/transaction/TransactionDefinition", "getName", "()Ljava/lang/String;", true);
        mv.visitVarInsn(ASTORE, methodSlot);

        mv.visitVarInsn(LLOAD, startSlot);
        mv.visitVarInsn(ALOAD, methodSlot);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "setTranElapsedTimeContext", "(JLjava/lang/String;Z)V", false);
    }
}

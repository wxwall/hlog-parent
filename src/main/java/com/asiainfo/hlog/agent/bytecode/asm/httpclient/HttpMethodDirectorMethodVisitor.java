package com.asiainfo.hlog.agent.bytecode.asm.httpclient;

import com.asiainfo.hlog.agent.bytecode.asm.AbstractTryCatchMethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * Created by chenfeng on 2016/7/25.
 */
public class HttpMethodDirectorMethodVisitor extends AbstractTryCatchMethodVisitor {

    public static final String CODE  = "receive.id.httpclient3";

    private int lockIndx;

    public HttpMethodDirectorMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pmv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pmv, datas, mcode);
    }


    @Override
    public void visitCode() {
        //defineThrowable();

        Label start = new Label();
        visitLabel(start);
        lockIndx = defineLocalVariable("lock",boolean.class,start,null);
        int gIdIndx = defineLocalVariable("gId",String.class,start,null);
        int pIdIndx = defineLocalVariable("pId",String.class,start,null);

        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, lockIndx);

        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "isWriteHeaderLocked", "()Z", false);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNE, l1);

        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "writeHeaderLocked", "()V", false);

        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, lockIndx);

        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "getThreadLogGroupId", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, gIdIndx);

        mv.visitVarInsn(ALOAD, gIdIndx);
        Label l4 = new Label();
        mv.visitJumpInsn(IFNULL, l4);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitLdcInsn("HlogGid");
        mv.visitVarInsn(ALOAD, gIdIndx);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/apache/commons/httpclient/HttpMethod", "addRequestHeader", "(Ljava/lang/String;Ljava/lang/String;)V", true);
        mv.visitLabel(l4);

        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "getThreadCurrentLogId", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, pIdIndx);

        mv.visitVarInsn(ALOAD, pIdIndx);
        mv.visitJumpInsn(IFNULL, l1);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitLdcInsn("HlogPid");
        mv.visitVarInsn(ALOAD, pIdIndx);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/apache/commons/httpclient/HttpMethod", "addRequestHeader", "(Ljava/lang/String;Ljava/lang/String;)V", true);
        mv.visitLabel(l1);

        super.visitCode();
    }

    protected void beforeReturn(boolean isVoid) {
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "cleanWriteHeaderLock", "()V", false);
    }

    protected void beforeThrow(int flag) {
        if(flag==0){
            mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "cleanWriteHeaderLock", "()V", false);
        }
    }

    private void doClean(){
        mv.visitVarInsn(ILOAD, lockIndx);
        Label l11 = new Label();
        mv.visitJumpInsn(IFEQ, l11);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "cleanWriteHeaderLock", "()V", false);
        mv.visitLabel(l11);
    }
}

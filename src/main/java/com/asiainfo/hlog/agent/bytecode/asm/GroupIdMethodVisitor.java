package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

public class GroupIdMethodVisitor extends AbstractMethodVisitor {


    public GroupIdMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
    }

    @Override
    public void visitCode() {
        Label start = new Label();
        mv.visitLabel(start);
        int idSlot = defineLocalVariable("_id",String.class,start,null);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "getThreadLogGroupId", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, idSlot);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, idSlot);
        Label l2 = new Label();
        mv.visitJumpInsn(IFNULL, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, idSlot);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l2);
        super.visitCode();
    }
}

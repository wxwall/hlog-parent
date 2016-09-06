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
        Logger.debug("GroupIdMethodVisitor#[{0}.{1}]",className,methodName);
        Label label = new Label();
        visitLabel(label);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "getThreadLogGroupId", "()Ljava/lang/String;", false);
        mv.visitInsn(ARETURN);
        super.visitCode();
    }
}

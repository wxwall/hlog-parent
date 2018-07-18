package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

/**
 * Created by chenfeng on 2017/4/19.
 */
public class HLogGidPidMethodVistor extends AbstractMethodVisitor {

    public HLogGidPidMethodVistor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
    }

    public void visitCode() {
        if ("putGid".equals(methodName)) {
            visitVarInsn(Opcodes.ALOAD, 0);
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"setThreadLogGroupId",String.class);

        }
        if ("fetchGid".equals(methodName)) {
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"getThreadLogGroupId");
            visitInsn(Opcodes.ARETURN);
        }
        if ("putPid".equals(methodName)) {
            visitVarInsn(Opcodes.ALOAD, 0);
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"setThreadCurrentLogId",String.class);
        }
        if ("fetchPid".equals(methodName)) {
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"getThreadCurrentLogId");
            visitInsn(Opcodes.ARETURN);
        }

        if ("putCollectTag".equals(methodName)) {
            visitVarInsn(Opcodes.ALOAD, 0);
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"setCollectTag",String.class);
        }

        if ("fetchCollectTag".equals(methodName)) {
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"getCollectTag");
            visitInsn(Opcodes.ARETURN);
        }

        if ("addSession".equals(methodName)) {
            visitVarInsn(Opcodes.ALOAD, 0);
            visitVarInsn(Opcodes.ALOAD, 1);
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"addThreadSession",String.class,Object.class);
        }

        if ("fetchSession".equals(methodName)) {
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"getThreadSession");
            visitInsn(Opcodes.ARETURN);
        }

        if ("clearThreadSession".equals(methodName)) {
            ASMUtils.visitStaticMethod(mv, LogAgentContext.class,"clearThreadSession");
        }

        super.visitCode();
    }

}

package com.asiainfo.hlog.agent.bytecode.asm.web;

import com.asiainfo.hlog.agent.bytecode.asm.AbstractMethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.ALOAD;
import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * <p>按业务条件（工号，渠道）采集、统计</p>
 * <p>要求被处理的方法入参必须要有{@link javax.servlet.http.HttpServletRequest}类型
 */
public class SessionMethodVisitor extends AbstractMethodVisitor {

    private int reqIdx = -1;
    private final String ServletRequest = "javax/servlet/http/HttpServletRequest";

    public SessionMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (ServletRequest.equals(type.getInternalName())) {
                reqIdx = i+1;
            }
        }
    }

    @Override
    public void visitCode() {
        //Label start = new Label();
        //mv.visitLabel(start);
        mv.visitVarInsn(ALOAD, reqIdx);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/http/HttpMonitor", "sessionInfo", "(Ljava/lang/Object;)V", false);
        super.visitCode();
    }
}

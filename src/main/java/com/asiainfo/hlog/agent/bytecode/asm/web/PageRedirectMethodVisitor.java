package com.asiainfo.hlog.agent.bytecode.asm.web;

import com.asiainfo.hlog.agent.bytecode.asm.AbstractMethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Type;
import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * <p>处理HLog页面跳转</p>
 * <p>所以要求被处理的方法入参必须要有{@link javax.servlet.http.HttpServletRequest}类型
 * 和{@link javax.servlet.http.HttpServletResponse}类型</p>
 */
public class PageRedirectMethodVisitor extends AbstractMethodVisitor {

    private int reqIdx = -1;
    private int respIdx = -1;
    private final String ServletRequest = "javax/servlet/http/HttpServletRequest";
    private final String ServletResponse = "javax/servlet/http/HttpServletResponse";

    public PageRedirectMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (ServletRequest.equals(type.getInternalName())) {
                reqIdx = i+1;
            }
            if (ServletResponse.equals(type.getInternalName())) {
                respIdx = i+1;
            }
        }
    }

    @Override
    public void visitCode() {
        Label start = new Label();
        mv.visitLabel(start);
        mv.visitVarInsn(ALOAD, reqIdx);
        mv.visitVarInsn(ALOAD, respIdx);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/http/HttpMonitor", "pageRedirect", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
        Label ifLabel = new Label();
        mv.visitJumpInsn(IFEQ, ifLabel);
        mv.visitInsn(RETURN);
        mv.visitLabel(ifLabel);
        super.visitCode();
    }
}

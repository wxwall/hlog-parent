package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;

/**
 * 拦截方法的入参，将入参发送到日志平台
 * Created by chenfeng on 2016/4/30.
 */
public class InterceptMethodVisitor extends AbstractMethodVisitor {

    public static final String CODE  = "intercept";

    public InterceptMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas,String mcode) {
        super(access, className, methodName, desc, pnv, datas,mcode);
    }

    public void visitCode() {

        //调用监控intercept方法
        visitLdcInsn(mcode);
        callMonitorMethod("intercept",String.class,String.class,String.class,String.class,String[].class, Object[].class);

        super.visitCode();
    }
}

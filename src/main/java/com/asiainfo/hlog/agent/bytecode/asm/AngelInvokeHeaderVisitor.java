package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.ALOAD;
import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.INVOKESTATIC;


/**
 * Created by yuan on 2018/7/18.
 */
public class AngelInvokeHeaderVisitor extends AbstractMethodVisitor {
    public static final String CODE = "angel.header";

    private final String className;
    private final String methodName;

    public AngelInvokeHeaderVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
        this.className = className;
        this.methodName = methodName;
    }

    public void visitCode(){
        if("sendRequest".equals(methodName) && paramTypes != null && paramTypes.length == 6){
            mv.visitVarInsn(ALOAD,2);
            mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/HLogMonitor", "angelInvokeHeader", "(Ljava/util/Map;)V", false);
        }
        super.visitCode();
    }



}

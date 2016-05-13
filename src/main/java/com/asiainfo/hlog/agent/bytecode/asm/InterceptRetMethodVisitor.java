package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.RETURN;

/**
 * 拦截方法的入参，将入参发送到日志平台
 * Created by chenfeng on 2016/4/30.
 */
public class InterceptRetMethodVisitor extends AbstractMethodVisitor {

    public static final String CODE  = "interceptRet";

    public InterceptRetMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas,String mcode) {
        super(access, className, methodName, desc, pnv, datas,mcode);
    }

    public void visitCode() {

        //调用监控intercept方法
        visitLdcInsn(mcode);
        callMonitorMethod("interceptRet",String.class,String.class,String.class,String.class,String[].class, Object[].class);

        //返回
        if(returnType.getSort()!= Type.VOID){
            int[] retCodeAndVal = ASMUtils.getReturnCodeAndDefValue(returnType.getSort());
            visitInsn(retCodeAndVal[1]);
            visitInsn(retCodeAndVal[0]);
        }else{
            visitInsn(RETURN);
        }

        super.visitCode();
    }
}

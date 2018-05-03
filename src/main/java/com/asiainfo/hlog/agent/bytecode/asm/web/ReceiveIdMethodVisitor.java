package com.asiainfo.hlog.agent.bytecode.asm.web;

import com.asiainfo.hlog.agent.bytecode.asm.ASMUtils;
import com.asiainfo.hlog.agent.bytecode.asm.AbstractMethodVisitor;
import com.asiainfo.hlog.agent.runtime.http.HttpMonitor;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.ALOAD;
import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.INVOKEINTERFACE;

/**
 * <p>接收上流传递过来的方法</p>
 * <p>这里只处理http的请求,并且从head里读取</p>
 * <p>所以要求被处理的方法入参必须要有{@link javax.servlet.http.HttpServletRequest}类型</p>
 * Created by chenfeng on 2016/5/8.
 */
public class ReceiveIdMethodVisitor extends AbstractMethodVisitor {

    public static final String CODE  = "receive.id";

    private final String CLA_SERVLETREQUEST = "javax/servlet/http/HttpServletRequest";

    private int paramIndex = -1;

    private int startIndex = 1;

    public ReceiveIdMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pmv, byte[] datas, String mcode) {
        super(access,className,methodName,desc, pmv,datas,mcode);

        Type[] types = Type.getArgumentTypes(desc);
        startIndex = types.length+1;
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (CLA_SERVLETREQUEST.equals(type.getInternalName())) {
                paramIndex = i+1;
                break;
            }
        }
    }

    public void visitCode() {

        //defineThrowable();

        if(paramIndex==-1){
            super.visitCode();
            return ;
        }
        mv.visitIntInsn(ALOAD,paramIndex);
        mv.visitLdcInsn("hloggid");
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getHeader", "(Ljava/lang/String;)Ljava/lang/String;", true);

        mv.visitIntInsn(ALOAD,paramIndex);
        mv.visitLdcInsn("hlogpid");
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getHeader", "(Ljava/lang/String;)Ljava/lang/String;", true);

        mv.visitIntInsn(ALOAD,paramIndex);
        mv.visitLdcInsn("hlogctag");
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getHeader", "(Ljava/lang/String;)Ljava/lang/String;", true);

        visitMethodInsn(Opcodes.INVOKESTATIC,"com/asiainfo/hlog/agent/runtime/http/HttpMonitor","receiveHlogId",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
        super.visitCode();
    }


    private void doClean(){
        ASMUtils.visitStaticMethod(mv, HttpMonitor.class,"clearReceiveHlogId",null);
    }

}

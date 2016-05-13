package com.asiainfo.hlog.agent.bytecode.asm.web;

import com.asiainfo.hlog.agent.bytecode.asm.AbstractTryCatchMethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;


/**
 * <p>处理接收的请求信息</p>
 * <p>记录url、耗时、和状态</p>
 * <p>所以要求被处理的方法入参必须要有{@link javax.servlet.http.HttpServletRequest}类型</p>
 * Created by chenfeng on 2016/5/8.
 */
public class HttpRequestMethodVisitor extends AbstractTryCatchMethodVisitor {

    public static final String CODE  = "http.request";

    private final String ServletRequest = "javax/servlet/http/HttpServletRequest";

    private int paramIndex = -1;

    private int startIndex = 1;

    public HttpRequestMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas,mcode);

        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (ServletRequest.equals(type.getInternalName())) {
                paramIndex = i+1;
                break;
            }
        }
    }

    public void visitCode() {
        if(paramIndex==-1){
            super.visitCode();
            return ;
        }
        Label start = new Label();
        visitLabel(start);

        //如果有返回结果的话
        //如果需要有返回则定义
        defineReturnObject();
        if(returnType != Type.VOID_TYPE){
            startIndex = idxReturn + 1;
        }else{
            startIndex = idxReturn;
        }
        visitLocalVariable("_start",Type.getDescriptor(long.class),null,start,start,startIndex);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LSTORE, startIndex);

        super.visitCode();
    }

    @Override
    protected void beforeReturn(boolean isVoid) {
        doEnd(0);
    }
    protected void beforeThrow() {
        doEnd(1);
    }
    private void doEnd(int status){
        visitVarInsn(ALOAD,paramIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getRequestURL", "()Ljava/lang/StringBuffer;", true);
        visitVarInsn(ALOAD,paramIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getRemoteAddr", "()Ljava/lang/String;", true);
        visitVarInsn(LLOAD,startIndex);
        mv.visitIntInsn(BIPUSH, status);
        visitMethodInsn(Opcodes.INVOKESTATIC,"com/asiainfo/hlog/agent/runtime/http/HttpMonitor","request",
                "(Ljava/lang/StringBuffer;Ljava/lang/String;JI)V", false);
    }
}

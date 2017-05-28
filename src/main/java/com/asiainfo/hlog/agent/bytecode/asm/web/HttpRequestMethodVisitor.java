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

    private int _startLVSlot;
    private int _nodeSlot;

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
        /*
        if(returnType != Type.VOID_TYPE){
            startIndex = idxReturn + 1;
        }else{
            startIndex = idxReturn;
        }*/
        //visitLocalVariable("_start",Type.getDescriptor(long.class),null,start,start,startIndex);
        _startLVSlot = defineLocalVariable("_start",long.class,start,null);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LSTORE, _startLVSlot);

        Label nodeLabel = new Label();
        mv.visitLabel(nodeLabel);
        _nodeSlot = defineLocalVariable("_node",Object.class,nodeLabel,null);

        mv.visitVarInsn(ALOAD,paramIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getRequestURL", "()Ljava/lang/StringBuffer;", true);
        mv.visitVarInsn(LLOAD, _startLVSlot);
        //mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/client/helper/LogUtil", "logId", "()Ljava/lang/String;", false);
        //mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/RuntimeContext", "getLogId", "()Ljava/lang/String;", false);
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        //,StringBuffer requestUrl,long start,String logId,String pId,String className, String methodName
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/http/HttpMonitor", "requestBegin", "(Ljava/lang/StringBuffer;JLjava/lang/String;Ljava/lang/String;)Lcom/asiainfo/hlog/agent/runtime/HLogMonitor$Node;", false);
        mv.visitVarInsn(ASTORE, _nodeSlot);

        //mv.visitVarInsn(ALOAD, _nodeSlot);
        //mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/HLogMonitor", "addLoopMonitor", "(Lcom/asiainfo/hlog/agent/runtime/HLogMonitor$Node;)V", false);

        super.visitCode();
    }


    protected void beforeReturn(boolean isVoid) {
        doEnd(0);
    }
    protected void beforeThrow(int flag) {
        if(flag==1){
            doEnd(1);
        }
    }
    private void doEnd(int status){
        mv.visitVarInsn(ALOAD,paramIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getRequestURL", "()Ljava/lang/StringBuffer;", true);
        mv.visitVarInsn(ALOAD,paramIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getRemoteAddr", "()Ljava/lang/String;", true);
        mv.visitVarInsn(LLOAD,_startLVSlot);
        //mv.visitInsn(LCONST_1);
        mv.visitIntInsn(BIPUSH, status);
        mv.visitVarInsn(ALOAD,_nodeSlot);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,"com/asiainfo/hlog/agent/runtime/http/HttpMonitor","request",
                "(Ljava/lang/StringBuffer;Ljava/lang/String;JILcom/asiainfo/hlog/agent/runtime/HLogMonitor$Node;)V", false);
    }
}

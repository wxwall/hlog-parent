package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * 采集第三方日志框架
 * Created by chenfeng on 2016/4/22.
 */
public class LoggerMethodVisitor extends MethodVisitor {

    public static final String CODE = "logger";

    private final static Map<String,String> methods = new HashMap<String,String>();

    static {
        methods.put("trace","trace");
        methods.put("debug","debug");
        methods.put("info","info");
        methods.put("warn","warn");
        methods.put("error","error");
        methods.put("isTraceEnabled","trace");
        methods.put("isDebugEnabled","debug");
        methods.put("isInfoEnabled","info");
        methods.put("isWarnEnabled","warn");
        methods.put("isErrorEnabled","error");
    }

    private final String className;
    private final String methodName;
    private final String desc;
    private final byte[] datas;
    private final int access;
    private final String mcode;


    public LoggerMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pmv, byte[] datas,String mcode) {
        super(ASM5, pmv);
        this.access = access;
        this.className = className;
        this.methodName = methodName;
        this.datas = datas;
        this.desc = desc;
        this.mcode = mcode;
    }


    public void visitCode() {
        if(!methods.keySet().contains(methodName)){
            super.visitCode();
            return ;
        }
        //增加判断代码
        visitVarInsn(ALOAD, 0);
        visitFieldInsn(GETFIELD, className.replaceAll("\\.","/"), "name", Type.getDescriptor(String.class));
        visitLdcInsn(methods.get(methodName));
        ASMUtils.visitStaticMethod(mv, HLogMonitor.class,"isLoggerEnabled",String.class,String.class);

        Label endIfLable = new Label();
        visitJumpInsn(IFEQ, endIfLable);

        boolean isSwitch = methodName.startsWith("is");

        //如果是开关的判断返回ture
        if(isSwitch){
            visitInsn(ICONST_1);
            visitInsn(IRETURN);
        }else{
            visitLdcInsn(mcode);
            visitVarInsn(ALOAD, 0);
            visitFieldInsn(GETFIELD, className.replaceAll("\\.","/"), "name", Type.getDescriptor(String.class));
            visitLdcInsn(methods.get(methodName));
            //获取方法的参数名称
            String[] argumentNames = ParameterNameHelper.getMethodParameterName(datas,className,methodName,desc);
            boolean isStatic = ASMUtils.isStatic(access);
            int index = isStatic ? 0:1;
            int argumentLength = argumentNames.length;
            visitIntInsn(BIPUSH, argumentLength);
            visitTypeInsn(ANEWARRAY, ASMConsts.JAVA_LANG_OBJECT);
            for (int i = 0; i < argumentLength; i++) {
                visitInsn(DUP);
                visitIntInsn(BIPUSH,i);
                visitVarInsn(ALOAD, index++);
                visitInsn(AASTORE);
            }
            ASMUtils.visitStaticMethod(mv, HLogMonitor.class,"logger",String.class,String.class,String.class, Object[].class);
        }
        visitLabel(endIfLable);

        super.visitCode();
    }

}

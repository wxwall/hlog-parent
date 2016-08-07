package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.HLogAgentConst;
import com.asiainfo.hlog.agent.runtime.RuntimeContext;
import com.asiainfo.hlog.org.objectweb.asm.ClassWriter;
import com.asiainfo.hlog.org.objectweb.asm.FieldVisitor;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

import static com.asiainfo.hlog.agent.runtime.RuntimeContext.enable;

public class ProcessNewMethods implements IHLogNewMethods, Opcodes{

    public static final String CODE = "+process";

    private String className ;

    public ProcessNewMethods(String className){
        this.className = className;
    }

    public void createNewMethods(ClassWriter classWriter) {
        {
            FieldVisitor fv = classWriter.visitField(ACC_PUBLIC + ACC_STATIC, "_hlog_process", "Z", null, null);
            fv.visitEnd();
        }
        {
            FieldVisitor fv = classWriter.visitField(ACC_PUBLIC + ACC_STATIC, "_hlog_error", "Z", null, null);
            fv.visitEnd();
        }


        MethodVisitor mv = null;
        {
            boolean f = RuntimeContext.enable(HLogAgentConst.MV_CODE_PROCESS,className,null);

            mv = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            if(f){
                mv.visitInsn(ICONST_1);
            }else{
                mv.visitInsn(ICONST_0);
            }
            mv.visitFieldInsn(PUTSTATIC, className.replaceAll("\\.","/"), "_hlog_process", "Z");


            f = enable(HLogAgentConst.MV_CODE_ERROR,className,null);
            if(f){
                mv.visitInsn(ICONST_1);
            }else{
                mv.visitInsn(ICONST_0);
            }
            mv.visitInsn(ICONST_1);
            mv.visitFieldInsn(PUTSTATIC, className.replaceAll("\\.","/"), "_hlog_error", "Z");
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 0);
            mv.visitEnd();
        }
    }
}

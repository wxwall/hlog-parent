package com.asiainfo.hlog.agent.bytecode.asm.hibernate;

import com.asiainfo.hlog.agent.bytecode.asm.AbstractMethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;


/**
 * Created by ChenYuanlong on 2016/5/5.
 */
public class Hibernate3MethodVisitor extends AbstractMethodVisitor {
    public static final String CODE = "hibernate.sql";

    private final String className;
    private final String methodName;

    public Hibernate3MethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        super(access, className, methodName, desc, pnv, datas, mcode);
        this.className = className;
        this.methodName = methodName;
    }

    public void visitCode(){
        //org.hibernate.type.descriptor.sql.BasicBinder.bind(PreparedStatement st, J value, int index, WrapperOptions options)
        if("bind".equals(methodName)){
            mv.visitVarInsn(ALOAD,2);
            mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/HLogMonitor", "addHibernateParam", "(Ljava/lang/Object;)V", false);
        }else if("logStatement".equals(methodName)){
            //org.hibernate.jdbc.util.SQLStatementLogger.logStatement(String statement, FormatStyle style)
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/HLogMonitor", "setHibernateSql", "(Ljava/lang/String;)V", false);
        }else if("doReleaseConnection".equals(methodName)||"doCloseConnection".equals(methodName)){
            mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/HLogMonitor", "sendHibernateSql", "()V", false);
        }
        super.visitCode();
    }



}

package com.asiainfo.hlog.agent.bytecode.asm.mybatis;

import com.asiainfo.hlog.agent.bytecode.asm.ASMConsts;
import com.asiainfo.hlog.agent.bytecode.asm.AbstractMethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

import static com.asiainfo.hlog.agent.bytecode.asm.ASMConsts.MY_BATIS_BASE_JDBC_LOGGER;

/**
 * 给BaseJdbcLogger类的getParameterValueString方法在返回之前将数据赋值给param字段
 * Created by chenfeng on 2016/4/26.
 */
public class BaseJdbcLoggerMethodVisitor extends AbstractMethodVisitor {

    public static final String CODE = "mybatis.BaseJdbcLogger";

    private int _psIndex ;

    public BaseJdbcLoggerMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas,String mcode) {
        super(access, className, methodName, desc, pnv, datas,mcode);
        //创建方法
    }


    public void visitCode() {
        Label start = new Label();
        visitLabel(start);
        //visitLocalVariable("_ps", Type.getDescriptor(String.class),null,start,start,1);
        _psIndex = defineLocalVariable("_ps",String.class,start,null);
        super.visitCode();
    }

    public void visitInsn(int opcode) {
        boolean isReturn = opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
        if (isReturn) {
            visitVarInsn(Opcodes.ASTORE, _psIndex);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, _psIndex);
            mv.visitFieldInsn(Opcodes.PUTFIELD, MY_BATIS_BASE_JDBC_LOGGER, ASMConsts.PARAMS, ASMConsts.LJAVA_LANG_STRING);
            visitVarInsn(Opcodes.ALOAD,_psIndex);
        }
        super.visitInsn(opcode);
    }
}

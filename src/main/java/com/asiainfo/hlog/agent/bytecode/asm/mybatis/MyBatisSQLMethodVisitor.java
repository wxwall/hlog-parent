package com.asiainfo.hlog.agent.bytecode.asm.mybatis;

import com.asiainfo.hlog.agent.bytecode.asm.ASMConsts;
import com.asiainfo.hlog.agent.bytecode.asm.ASMUtils;
import com.asiainfo.hlog.agent.bytecode.asm.AbstractMethodVisitor;
import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

/**
 * <p>捕获mybatis的执行sql性能日志</p>
 * <p>通过{@link BaseJdbcLoggerMethodVisitor}和{@link BaseJdbcLoggerNewMethods}两个类增强了BaseJdbcLogger提供了getParams的能力</p>
 * <p>通过{@link ErrorContextNewMethods}增加了ErrorContext的getSql方法,用来获取运行的sql</p>
 * Created by chenfeng on 2016/4/25.
 */
public class MyBatisSQLMethodVisitor extends AbstractMethodVisitor {

    public static final String CODE = "mybatis.sql";

    private static final String FIELD_EXECUTE_METHODS = "EXECUTE_METHODS" ;

    private Label endIfLabel = null;

    private boolean isEndIf = true;

    private int go = 0;

    private int startLVSlot ;
    private int _psLVSlot ;

    public MyBatisSQLMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas,String mcode) {
        super(access, className, methodName, desc, pnv, datas,mcode);
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if(FIELD_EXECUTE_METHODS.equals(name)){
            go = 1;
        }
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if(go==1 && "contains".equals(name)){
            go = 2;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode,label);
        if(go==2 && opcode == Opcodes.IFEQ){
            go = -1;
            //当执行耗时超过预设的值时记录sql和入参
            endIfLabel = label;
            Label start = new Label();
            visitLabel(start);
            //visitLocalVariable("start", Type.getDescriptor(long.class),null,start,start,4);
            startLVSlot = defineLocalVariable("start",long.class,start,null);

            ASMUtils.visitStaticMethod(mv,System.class, ASMConsts.CURRENT_TIME_MILLIS,null);
            visitVarInsn(Opcodes.LSTORE, startLVSlot);

            start = new Label();
            visitLabel(start);
            //visitLocalVariable("_ps", Type.getDescriptor(String.class),null,start,start,5);
            _psLVSlot = defineLocalVariable("_ps",String.class,start,null);
            isEndIf = false;
        }
    }

    public void visitLabel(Label label) {
        if(label.equals(endIfLabel)){
            isEndIf = true;
        }
        super.visitLabel(label);
    }


    public void visitInsn(int opcode) {
        boolean isReturn = opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
        if(isReturn && !isEndIf){

            Label l7 = new Label();
            visitVarInsn(Opcodes.ALOAD, 0);
            visitMethodInsn(Opcodes.INVOKESPECIAL, ASMConsts.MY_BATIS_JDBC_PREPARED_STATEMENT_LOGGER, "getParams", ASMConsts.NONPARAM_LJAVA_LANG_STRING, false);
            visitVarInsn(Opcodes.ASTORE, _psLVSlot);
            visitVarInsn(Opcodes.ALOAD, _psLVSlot);
            visitJumpInsn(Opcodes.IFNONNULL, l7);
            visitVarInsn(Opcodes.ALOAD, 0);
            visitMethodInsn(Opcodes.INVOKESPECIAL, ASMConsts.MY_BATIS_JDBC_PREPARED_STATEMENT_LOGGER, "getParameterValueString", ASMConsts.NONPARAM_LJAVA_LANG_STRING, false);
            visitVarInsn(Opcodes.ASTORE, _psLVSlot);
            visitLabel(l7);

            ASMUtils.visitStaticMethod(mv,System.class, ASMConsts.CURRENT_TIME_MILLIS,null);
            visitVarInsn(Opcodes.LLOAD, startLVSlot);
            visitInsn(Opcodes.LSUB);
            visitVarInsn(Opcodes.LSTORE, startLVSlot);

            Label ifLab = new Label();
            visitVarInsn(Opcodes.LLOAD, startLVSlot);
            //visitMethodInsn(Opcodes.INVOKESTATIC, ASMConsts.HLOG_MONITOR, ASMConsts.HLOG_MONITOR_GET_CONFIG_SQL_SPEED, "()J", false);
            ASMUtils.visitStaticMethod(mv,HLogMonitor.class, ASMConsts.HLOG_MONITOR_GET_CONFIG_SQL_SPEED,null);
            visitInsn(Opcodes.LCMP);
            visitJumpInsn(Opcodes.IFLT, ifLab);

            visitVarInsn(Opcodes.LLOAD, startLVSlot);
            mv.visitLdcInsn(ASMConsts.MY_BATIS_JDBC_PREPARED_STATEMENT_LOGGER_CLS);
            visitMethodInsn(Opcodes.INVOKESTATIC, ASMConsts.MY_BATIS_ERROR_CONTEXT, "instance", "()Lorg/apache/ibatis/executor/ErrorContext;", false);
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMConsts.MY_BATIS_ERROR_CONTEXT, "getSql", ASMConsts.NONPARAM_LJAVA_LANG_STRING, false);
            visitVarInsn(Opcodes.ALOAD, _psLVSlot);
            ASMUtils.visitStaticMethod(mv,HLogMonitor.class, ASMConsts.HLOG_MONITOR_SQL_MONITOR,long.class,String.class,String.class,String.class);
            visitLabel(ifLab);
        }
        super.visitInsn(opcode);
    }
}

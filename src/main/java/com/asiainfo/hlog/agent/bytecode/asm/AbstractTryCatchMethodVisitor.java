package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * 构建一个带有try catch块的方法观察者
 * Created by chenfeng on 2016/4/20.
 */
public abstract class AbstractTryCatchMethodVisitor extends AbstractEndTodoMethodVisitor {

    /**
     * try块开始位
     */
    private Label lTryBlockStart;
    /**
     * try块终止位
     */
    protected Label lTryBlockEnd;
    /**
     *catch块开始位
     */
    private Label lCatchBlockStart;
    /**
     * catch块终止位
     */
    protected Label lCatchBlockEnd;


    public AbstractTryCatchMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pmv, byte[] datas, String mcode) {

        super(access,className,methodName,desc, pmv,datas,mcode);

        //初始化trycatch块的起止位置
        lTryBlockStart = new Label();
        lTryBlockEnd = new Label();
        lCatchBlockStart = new Label();
        lCatchBlockEnd = new Label();
    }



    /**
     * 每个方法开始遍历字节码的入码
     */
    public void visitCode() {
        defineThrowable();
        //加入try块开始标记
        visitLabel(lTryBlockStart);

        super.visitCode();
    }

    /**
     * 在这里面处理catch发生时的指令
     * @param maxStack
     * @param maxLocals
     */
    public void visitMaxs(int maxStack, int maxLocals) {
        // 结束阶段
        setGoToLastVisit();
        // 加入trycatch块,获取ang异常
        visitTryCatchBlock(lTryBlockStart, lTryBlockEnd,
                lCatchBlockStart, null);
        // 打上catch块开始标记
        visitLabel(lCatchBlockStart);
        // 当异常发生时,将异常的实例先存储到我们事先声明的局部变量中
        visitVarInsn(ASTORE, getIdxExce());
        beforeThrow(1);

        boolean superIsTryCatch = mv instanceof AbstractTryCatchMethodVisitor;

        visitVarInsn(ALOAD, getIdxExce());
        if(!superIsTryCatch){
            //return ;
            // 抛出异常
            visitInsn(ATHROW);
        }

        // catch块结果
        visitLabel(lCatchBlockEnd);

        super.visitMaxs(maxStack, maxLocals);
    }

    /**
     * 遍历所有的指令,关注return 和 throw
     */
    @Override
    protected void visitInsnFinish() {
        lTryBlockEnd = new Label();
        visitLabel(lTryBlockEnd);
    }
}

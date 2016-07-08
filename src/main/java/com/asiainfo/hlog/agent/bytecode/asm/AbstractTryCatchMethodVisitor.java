package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * 构建一个带有try catch块的方法观察者
 * Created by chenfeng on 2016/4/20.
 */
public abstract class AbstractTryCatchMethodVisitor extends AbstractMethodVisitor {

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

    /**
     * 遍历是否已经到达过 visitEnd 或 visitMaxs
     */
    private boolean isGoToLastVisit= false;

    /**
     * 定义用于临时存储返回对象的局部变量表中的位置
     */
    protected int idxReturn;

    protected int idxEx = -1;

    /**
     * 定义用于保存返回对象的指令,不同类型的返回指令不同
     */
    protected int RES_STORE_CODE ;
    /**
     * 定义用于加载局部变量的返回对象的指令,同上.
     */
    protected int RES_LOAD_CODE ;

    public AbstractTryCatchMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pmv, byte[] datas, String mcode) {

        super(access,className,methodName,desc, pmv,datas,mcode);

        //初始化trycatch块的起止位置
        lTryBlockStart = new Label();
        lTryBlockEnd = new Label();
        lCatchBlockStart = new Label();
        lCatchBlockEnd = new Label();

        //计算局部变量从第几个开始
        //int argumentLength = argumentNames==null ? 0 : argumentNames.length;
        //idxReturn = paramSlot+1;
    }

    protected void defineThrowable(){
        Label start = new Label();
        visitLabel(start);
        idxEx = defineLocalVariable("_ex",Throwable.class,start,start);
    }

    /**
     * 如果有返回类型的话,定义一个该类型的返回对象
     */
    protected void defineReturnObject(){
        if(returnType.getSort()!= Type.VOID){
            //先打上我们自己的位置标签,方便后面定义变量时确定作用范围
            Label start = new Label();
            visitLabel(start);

            int sort = returnType.getSort();

            int [] loadStoreAndDefVal = ASMUtils.getLoadStoreAndDefValue(sort);
            RES_STORE_CODE = loadStoreAndDefVal[1];
            RES_LOAD_CODE = loadStoreAndDefVal[0];
            //visitLocalVariable(RETURN_OBJ,returnType.getDescriptor(),null,start,start,idxReturn);
            idxReturn = defineLocalVariable(RETURN_OBJ,returnType,start,start);
            visitInsn(loadStoreAndDefVal[2]);
            visitIntInsn(RES_STORE_CODE,idxReturn);
        }
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
     * 设置方法指令遍历进入结束阶段
     */
    protected void setGoToLastVisit(){
        isGoToLastVisit = true;
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
        visitVarInsn(ASTORE, idxEx);
        beforeThrow(1);
        // 抛出异常
        visitVarInsn(ALOAD, idxEx);
        visitInsn(ATHROW);

        if(end ==null){
            end = new Label();
            mv.visitLabel(end);
        }
        // catch块结果
        visitLabel(lCatchBlockEnd);

        super.visitMaxs(maxStack, maxLocals);
    }

    protected abstract  void beforeReturn(boolean isVoid);

    /**
     * 在throw之前
     * @param flag 0应用代码throw,1hlog的throw
     */
    protected abstract void beforeThrow(int flag) ;

    public void visitEnd(){
        // 结束阶段
        setGoToLastVisit();

        //定义参数

        super.visitEnd();
    }

    /**
     * 遍历所有的指令,关注return 和 throw
     * @param opcode
     */
    public void visitInsn(int opcode) {
        if(isGoToLastVisit){
            super.visitInsn(opcode);
            return;
        }
        //此方法可以获取方法中每一条指令的操作类型，被访问多次
        //如应在方法结尾处添加新指令，则应判断：
        boolean isReturn = opcode >= IRETURN && opcode <= RETURN;
        if(isReturn){
            boolean isVoid = returnType.getSort()==Type.VOID;
            if(!isVoid) {
                visitIntInsn(RES_STORE_CODE, idxReturn);
            }
            beforeReturn(isVoid);
            if(!isVoid) {
                visitIntInsn(RES_LOAD_CODE, idxReturn);
            }
        }else if(opcode == ATHROW){
            // 当异常发生时,将异常的实例先存储到我们事先声明的局部变量中
            visitVarInsn(ASTORE, idxEx);
            beforeThrow(0);
            // 抛出异常
            visitVarInsn(ALOAD, idxEx);
        }
        //
        if(isReturn || opcode == ATHROW){
            if(!isGoToLastVisit){
                lTryBlockEnd = new Label();
                visitLabel(lTryBlockEnd);
            }
        }
        super.visitInsn(opcode);
    }

}

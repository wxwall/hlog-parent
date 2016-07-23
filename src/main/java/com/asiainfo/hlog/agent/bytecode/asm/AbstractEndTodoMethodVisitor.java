package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * 构建一个在方法返回之前(return/throw)做些事情
 *
 * //TODO 最大的问题就是前一个mv定义的参数范围会有问题,后期改进为局部范围0~最后
 * Created by chenfeng on 2016/7/23.
 */
public abstract class AbstractEndTodoMethodVisitor extends AbstractMethodVisitor {


    /**
     * 遍历是否已经到达过 visitEnd 或 visitMaxs
     */
    private boolean isGoToLastVisit= false;

    /**
     * 定义用于临时存储返回对象的局部变量表中的位置
     */
    protected int idxReturn;

    private int idxExce = -1;

    /**
     * 定义用于保存返回对象的指令,不同类型的返回指令不同
     */
    protected int RES_STORE_CODE ;
    /**
     * 定义用于加载局部变量的返回对象的指令,同上.
     */
    protected int RES_LOAD_CODE ;

    public AbstractEndTodoMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pmv, byte[] datas, String mcode) {

        super(access,className,methodName,desc, pmv,datas,mcode);

    }

    public int getIdxExce(){
        if(mv instanceof  AbstractEndTodoMethodVisitor){
            return ((AbstractEndTodoMethodVisitor)mv).getIdxExce();
        }
        return idxExce;
    }

    protected void defineThrowable(){

        if(mv instanceof  AbstractEndTodoMethodVisitor){
            return ;
        }

        Label start = new Label();
        visitLabel(start);
        idxExce = defineLocalVariable("_ex",Throwable.class,start,null);
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
            idxReturn = defineLocalVariable(RETURN_OBJ,returnType,start,null);
            visitInsn(loadStoreAndDefVal[2]);
            visitIntInsn(RES_STORE_CODE,idxReturn);
        }
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

        super.visitMaxs(maxStack, maxLocals);
    }

    /**
     * 在return之前
     * @param isVoid
     */
    protected abstract  void beforeReturn(boolean isVoid);

    /**
     * 在throw之前
     * @param flag 0应用代码throw,1hlog的throw
     */
    protected abstract void beforeThrow(int flag) ;

    /**
     * 观察完return 和 throw 指令
     */
    protected void visitInsnFinish(){

    }

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
            visitVarInsn(ASTORE, getIdxExce());
            beforeThrow(0);
            // 抛出异常
            visitVarInsn(ALOAD, getIdxExce());
        }
        //
        if(isReturn || opcode == ATHROW){
            if(!isGoToLastVisit){
                visitInsnFinish();
            }
        }

        super.visitInsn(opcode);
    }

}

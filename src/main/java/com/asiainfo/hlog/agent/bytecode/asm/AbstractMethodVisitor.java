package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.org.objectweb.asm.Label;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * 监控方法的抽象
 * Created by chenfeng on 2016/4/25.
 */
public abstract class AbstractMethodVisitor extends MethodVisitor {

    public static final String RETURN_OBJ = "_returnObj";

    protected final String className;
    protected final String methodName;
    protected final String desc;
    protected final Type[] paramTypes;

    protected final Type returnType;
    protected final byte[] datas;
    protected final String mcode;

    /**
     * 是否是一个静态方法
     */
    protected final boolean isStatic ;

    protected final String[] argumentNames;

    protected final int paramSlot ;

    protected List<Variable> variables ;

    private int localVarSlot ;

    private boolean isEnd = false;


    public AbstractMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas,String mcode) {

        super(ASM5,pnv);
        this.datas = datas;
        this.mcode = mcode;
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
        this.paramTypes = Type.getArgumentTypes(desc);
        this.returnType = Type.getReturnType(desc);

        this.isStatic = ASMUtils.isStatic(access);

        MethodInfoHelper.MethodInfo methodInfo = MethodInfoHelper.getMethodInfo(datas,className,methodName,desc);

        argumentNames = methodInfo.getParameterName();

        paramSlot = isStatic?ASMUtils.getSlotLength(paramTypes):ASMUtils.getSlotLength(paramTypes)+1;

        localVarSlot = methodInfo.getMaxLocal();

        this.variables = new ArrayList<Variable>();
    }


    protected int defineLocalVariable(String lvName, Type lvType, Label start, Label end){
        int index = localVarSlot ;

        //visitLocalVariable(lvName,lvType.getDescriptor(),null,start,end,index);
        variables.add(new Variable(index,lvName,lvType,start,end));

        if(lvType==Type.LONG_TYPE || lvType==Type.DOUBLE_TYPE ){
            localVarSlot = localVarSlot + 2;
        }else {
            localVarSlot = localVarSlot + 1;
        }
        return index;
    }
    protected int defineLocalVariable(String lvName, Class clazz, Label start, Label end){
        Type lvType = Type.getType(clazz);
        return defineLocalVariable(lvName,lvType,start,end);
    }

    protected void callMonitorMethod(String callMethodName,Class ... classes){

        buildMethodBaseParameters();

        //此方法在访问方法的头部时被访问到，仅被访问一次
        ASMUtils.visitStaticMethod(mv,HLogMonitor.class,callMethodName,
                classes);

    }

    private void buildMethodBaseParameters() {
        // 调用ldc将下面三个字符串常量推送到栈顶
        visitLdcInsn(className);
        visitLdcInsn(methodName);
        visitLdcInsn(desc);

        int argumentLength = argumentNames.length;
        // 如果方法有入参
        if(argumentLength>0){
            // 创建一个参数名称字符串数组
            visitIntInsn(BIPUSH, argumentLength);
            visitTypeInsn(ANEWARRAY, ASMConsts.JAVA_LANG_STRING);
            // 将参数名称压入到数组中
            for (int i = 0; i < argumentLength; i++) {
                String argumentName = argumentNames[i];
                visitInsn(DUP);
                visitIntInsn(BIPUSH,i);
                if(className.endsWith("BusiOrder")){
                    System.out.println(methodName+"-argumentName="+argumentName);
                }
                if(argumentName==null){
                    visitLdcInsn("arg"+i);
                }else{
                    visitLdcInsn(argumentName);
                }
                visitInsn(AASTORE);
            }

            // 创建一个Object[]用于保存入对对象
            visitIntInsn(BIPUSH, argumentLength);
            visitTypeInsn(ANEWARRAY, ASMConsts.JAVA_LANG_OBJECT);
            int index = isStatic ? 0:1;
            for (int i = 0; i < argumentLength; i++) {
                int sort = paramTypes[i].getSort();
                Class baseClass = ASMUtils.getBaseWareClass(sort);
                visitInsn(DUP);
                visitIntInsn(BIPUSH,i);
                //如果入参是一个基本类型的话,统一将入参值传成String对象
                if(baseClass!=null){
                    int[] loadStoreAndDefVal = ASMUtils.getLoadStoreAndDefValue(sort);
                    visitVarInsn(loadStoreAndDefVal[0], index);
                    Class paramType = ASMUtils.getBaseClass(sort);
                    ASMUtils.visitStaticMethod(mv,baseClass,"valueOf",paramType);
                }else{
                    visitVarInsn(ALOAD, index);
                }
                visitInsn(AASTORE);
                if(sort == Type.LONG || sort == Type.DOUBLE){
                    index = index + 2;
                }else {
                    index = index + 1;
                }
            }

        }else{
            //如果没有入参的话,设置成空
            visitInsn(ACONST_NULL);
            visitInsn(ACONST_NULL);
        }
    }

    /**
     * 编写调用HlogMonitor.start方法的字节码指令
     */
    protected void callMonitorStart(){
        callMonitorMethod("start",String.class,String.class,String.class,String[].class, Object[].class);
    }

    //protected Label end ;

    public void visitEnd(){
        if(!variables.isEmpty()){
            Label end = null;
            for (Variable variable : variables) {
                if(variable.end==null){
                    if(end == null){
                        end = new Label();
                        mv.visitLabel(end);
                    }
                    visitLocalVariable(variable.lvName,variable.lvType.getDescriptor(),null,variable.start,end,variable.index);
                }else{
                    visitLocalVariable(variable.lvName,variable.lvType.getDescriptor(),null,variable.start,variable.end,variable.index);
                }
            }
        }
        super.visitEnd();
    }

    //Variable(String lvName, Type lvType, Label start, Label end)
    class Variable{
        private final int index;
        private final String lvName;
        private final Type lvType;
        private final Label start;
        private final Label end;

        public Variable(int index , String lvName, Type lvType, Label start,Label end){
            this.index = index;
            this.lvName = lvName;
            this.lvType = lvType;
            this.start = start;
            this.end = end;
        }

        public int getIndex() {
            return index;
        }

        public String getLvName() {
            return lvName;
        }

        public Type getLvType() {
            return lvType;
        }

        public Label getStart() {
            return start;
        }

        public Label getEnd() {
            return end;
        }
    }
}

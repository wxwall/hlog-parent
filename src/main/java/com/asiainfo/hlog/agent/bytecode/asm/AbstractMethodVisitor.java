package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Type;
import com.asiainfo.hlog.org.objectweb.asm.commons.LocalVariablesSorter;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * 监控方法的抽象
 * Created by chenfeng on 2016/4/25.
 */
public abstract class AbstractMethodVisitor extends LocalVariablesSorter {

    public static final String RETURN_OBJ = "_returnObj";

    protected final String className;
    protected final String methodName;
    protected final String desc;

    protected final Type returnType;
    protected final byte[] datas;
    protected final String mcode;

    /**
     * 是否是一个静态方法
     */
    protected final boolean isStatic ;

    protected final String[] argumentNames;


    public AbstractMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas,String mcode) {

        super(ASM5,access,desc, pnv);

        this.datas = datas;
        this.mcode = mcode;
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
        this.returnType = Type.getReturnType(desc);

        this.isStatic = ASMUtils.isStatic(access);

        argumentNames = ParameterNameHelper.getMethodParameterName(datas,className,methodName,desc);

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
                visitLdcInsn(argumentName);
                visitInsn(AASTORE);
            }

            // 创建一个Object[]用于保存入对对象
            visitIntInsn(BIPUSH, argumentLength);
            visitTypeInsn(ANEWARRAY, ASMConsts.JAVA_LANG_OBJECT);
            int index = isStatic ? 0:1;
            Type[] paramTypes = Type.getArgumentTypes(desc);
            for (int i = 0; i < argumentLength; i++) {
                int sort = paramTypes[i].getSort();
                boolean isBaseType = ASMUtils.isBaseType(sort);
                visitInsn(DUP);
                visitIntInsn(BIPUSH,i);
                //如果入参是一个基本类型的话,统一将入参值传成String对象
                if(isBaseType){
                    int[] loadStoreAndDefVal = ASMUtils.getLoadStoreAndDefValue(sort);
                    visitVarInsn(loadStoreAndDefVal[0], index++);
                    Class paramType = ASMUtils.getBaseClass(sort);
                    ASMUtils.visitStaticMethod(mv,String.class,"valueOf",paramType);
                }else{
                    visitVarInsn(ALOAD, index++);
                }
                visitInsn(AASTORE);
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

}

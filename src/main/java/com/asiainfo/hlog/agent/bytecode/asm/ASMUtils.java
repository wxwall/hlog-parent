package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.*;

import java.lang.reflect.Method;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * ASM工具类
 * Created by chenfeng on 2016/4/20.
 */
public abstract class ASMUtils {

    public static boolean isStatic(int access) {
        return ((access & Opcodes.ACC_STATIC) > 0);
    }

    public static boolean isBaseType(int sort){
        switch (sort){
            case Type.INT:
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.DOUBLE:
            case Type.LONG:
            case Type.FLOAT:
                return true;
        }
        return false;
    }
    public static Class getClassByStringValueOf(int sort) {
        if (sort == Type.INT || sort == Type.BYTE || sort == Type.SHORT) {
            return int.class;
        } else if (sort == Type.BOOLEAN) {
            return boolean.class;
        } else if (sort == Type.CHAR) {
            return char.class;
        } else if (sort == Type.FLOAT) {
            return float.class;
        }  else if (sort == Type.DOUBLE) {
            return double.class;
        } else if (sort == Type.LONG) {
            return long.class;
        }
        return null;
    }
    public static Class getBaseClass(int sort) {
        if (sort == Type.INT) {
            return int.class;
        } else if (sort == Type.BOOLEAN) {
            return boolean.class;
        } else if (sort == Type.BYTE) {
            return byte.class;
        } else if (sort == Type.CHAR) {
            return char.class;
        } else if (sort == Type.SHORT) {
            return short.class;
        } else if (sort == Type.DOUBLE) {
            return double.class;
        } else if (sort == Type.LONG) {
            return long.class;
        } else if (sort == Type.FLOAT) {
            return float.class;
        }
        return null;
    }
    public static Class getBaseWareClass(int sort) {
        if (sort == Type.INT) {
            return Integer.class;
        } else if (sort == Type.BOOLEAN) {
            return Boolean.class;
        } else if (sort == Type.BYTE) {
            return Byte.class;
        } else if (sort == Type.CHAR) {
            return Character.class;
        } else if (sort == Type.SHORT) {
            return Short.class;
        } else if (sort == Type.DOUBLE) {
            return Double.class;
        } else if (sort == Type.LONG) {
            return Long.class;
        } else if (sort == Type.FLOAT) {
            return Float.class;
        }
        return null;
    }
    public static int getSlotLength(Type[] paramTypes){
        if(paramTypes==null || paramTypes.length==0){
            return 0;
        }
        int slot = 0;
        for (Type paramType : paramTypes) {
            if(paramType.getSort() == Type.LONG || paramType.getSort() == Type.DOUBLE){
                slot = slot + 2;
            }else{
                slot ++ ;
            }
        }
        return slot;
    }
    public static char getBaseTypeSort(int sort) {
        char c = '0';
        if (sort == Type.INT) {
            c = 'I';
        } else if (sort == Type.BOOLEAN) {
            c = 'Z';
        } else if (sort == Type.BYTE) {
            c = 'B';
        } else if (sort == Type.CHAR) {
            c = 'C';
        } else if (sort == Type.SHORT) {
            c = 'S';
        } else if (sort == Type.DOUBLE) {
            c = 'D';
        } else if (sort == Type.LONG) {
            c = 'J';
        } else if (sort == Type.FLOAT) {
            c = 'F';
        }
        return c;
    }
    /**
     * 根据sort返回该类型的LOAD、STORE指令，及它的可用默认值指令。
     * @param sort
     * @return
     */
    public static int[] getReturnCodeAndDefValue(int sort){
        int defVal ;
        int ret ;
        switch (sort){
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
            case Type.BOOLEAN:
                defVal = Opcodes.ICONST_0 ;
                ret = Opcodes.IRETURN;
                break;
            case Type.DOUBLE:
                defVal = Opcodes.DCONST_0;
                ret = Opcodes.DRETURN;
                break;
            case Type.FLOAT:
                defVal = Opcodes.FCONST_0;
                ret = Opcodes.FRETURN;
                break;
            case Type.LONG:
                defVal = Opcodes.LCONST_0;
                ret = Opcodes.LRETURN;
                break;
            default:
                defVal = Opcodes.ACONST_NULL;
                ret = Opcodes.ARETURN;
        }
        return new int[]{ret,defVal};
    }
    /**
     * 根据sort返回该类型的LOAD、STORE指令，及它的可用默认值指令。
     * @param sort
     * @return
     */
    public static int[] getLoadStoreAndDefValue(int sort){
        int defVal ;
        int store ;
        int load ;
        switch (sort){
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
            case Type.BOOLEAN:
                defVal = Opcodes.ICONST_0 ;
                store = Opcodes.ISTORE;
                load = Opcodes.ILOAD;
                break;
            case Type.DOUBLE:
                defVal = Opcodes.DCONST_0;
                store = Opcodes.DSTORE;
                load = Opcodes.DLOAD;
                break;
            case Type.FLOAT:
                defVal = Opcodes.FCONST_0;
                store = Opcodes.FSTORE;
                load = Opcodes.FLOAD;
                break;
            case Type.LONG:
                defVal = Opcodes.LCONST_0;
                store = Opcodes.LSTORE;
                load = Opcodes.LLOAD;
                break;
            default:
                defVal = Opcodes.ACONST_NULL;
                store = Opcodes.ASTORE;
                load = Opcodes.ALOAD;
        }
        return new int[]{load,store,defVal};
    }

    /**
     * 编写调用某个静态方法的指令
     * @param mv
     * @param tClass
     * @param methodName
     * @param paramTypes
     */
    public static void visitStaticMethod(MethodVisitor mv,Class<?> tClass,String methodName,Class ... paramTypes){
        //查找出这个方式
        try{
            Method method = tClass.getMethod(methodName,paramTypes);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,Type.getInternalName(tClass),methodName, Type.getMethodDescriptor(method), false);
        }catch (NoSuchMethodException e){
            throw new HLogASMException(e);
        }
    }

    /**
     * 创建一个_getLogId的内部方法
     * @param classWriter
     */
    public static void createGetLogIdMethod(ClassWriter classWriter){
        MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE, "_getLogId", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
        mv.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
        Label l3 = new Label();
        Label l4 = new Label();
        Label l5 = new Label();
        mv.visitTryCatchBlock(l3, l4, l5, "java/lang/Throwable");
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitLdcInsn("getThreadCurrentLogId");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        Label l7 = new Label();
        mv.visitJumpInsn(IFEQ, l7);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "getThreadCurrentLogId", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l7);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "getThreadLogGroupId", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitLabel(l1);
        Label l9 = new Label();
        mv.visitJumpInsn(GOTO, l9);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitLabel(l3);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getContextClassLoader", "()Ljava/lang/ClassLoader;", false);
        mv.visitLdcInsn("com.asiainfo.hlog.agent.runtime.LogAgentContext");
        Label l10 = new Label();
        mv.visitLabel(l10);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_0);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ICONST_0);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        mv.visitVarInsn(ASTORE, 2);
        mv.visitLabel(l4);
        mv.visitJumpInsn(GOTO, l9);
        mv.visitLabel(l5);
        mv.visitVarInsn(ASTORE, 4);
        mv.visitLabel(l9);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ARETURN);
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitLocalVariable("t", "Ljava/lang/Throwable;", null, l3, l9, 3);
        mv.visitLocalVariable("this", "Lcom/asiainfo/hlog/agent/bytecode/asm/TestASM;", null, l6, l12, 0);
        mv.visitLocalVariable("method", "Ljava/lang/String;", null, l6, l12, 1);
        mv.visitLocalVariable("_id", "Ljava/lang/String;", null, l0, l12, 2);
        mv.visitMaxs(3, 5);
        mv.visitEnd();
    }
}

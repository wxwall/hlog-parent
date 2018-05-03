package com.asiainfo.hlog.agent.bytecode.asm.socket;

import com.asiainfo.hlog.agent.bytecode.asm.ASMUtils;
import com.asiainfo.hlog.agent.bytecode.asm.IHLogNewMethods;
import com.asiainfo.hlog.org.objectweb.asm.*;

/**
 * 对http协议的输出的流增加hlog的head信息
 * <p>对流分析,如果是http协议的,在头信息增加下面两个信息:</p>
 * <p>Hlog-Agent-Gid:日志组唯一标志</p>
 * <p>Hlog-Agent-Pid:日志的上级标志</p>
 * Created by chenfeng on 2016/5/6.
 */
public class SocketOutputStreamNewMethods implements IHLogNewMethods,Opcodes {

    public static final String CODE = "+java.net.SocketOutputStream.newMethods";

    private static final String JAVA_NET_SOCKET_OUTPUT_STREAM = "java/net/SocketOutputStream";

    private static final String FIELD_IS_WRITED = "isWrited";

    public SocketOutputStreamNewMethods(String className){
    }

    public void createNewMethods(ClassWriter classWriter) {
        //创建isWrited成员属性
        {
            FieldVisitor fv = classWriter.visitField(0, FIELD_IS_WRITED, "Z", null, null);
            fv.visitEnd();
        }

        ASMUtils.createGetLogIdMethod(classWriter);

        showLog(classWriter);
        createIsLockWriteHeader(classWriter);
        createSocketWrite(classWriter);
        isHttpProtocol(classWriter);
    }

    private void createIsLockWriteHeader(ClassWriter classWriter){
        MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE, "_isLockWriteHeader", "()Z", null, null);
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
        mv.visitLineNumber(20, l6);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitLineNumber(22, l0);
        mv.visitMethodInsn(INVOKESTATIC, "com/asiainfo/hlog/agent/runtime/LogAgentContext", "isWriteHeaderLocked", "()Z", false);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l1);
        mv.visitLineNumber(30, l1);
        Label l7 = new Label();
        mv.visitJumpInsn(GOTO, l7);
        mv.visitLabel(l2);
        mv.visitLineNumber(23, l2);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitLabel(l3);
        mv.visitLineNumber(25, l3);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getContextClassLoader", "()Ljava/lang/ClassLoader;", false);
        mv.visitLdcInsn("com.asiainfo.hlog.agent.runtime.LogAgentContext");
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitLineNumber(26, l8);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
        mv.visitLdcInsn("isWriteHeaderLocked");
        mv.visitInsn(ICONST_0);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        Label l9 = new Label();
        mv.visitLabel(l9);
        mv.visitLineNumber(27, l9);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ICONST_0);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        Label l10 = new Label();
        mv.visitLabel(l10);
        mv.visitLineNumber(25, l10);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l4);
        mv.visitLineNumber(29, l4);
        mv.visitJumpInsn(GOTO, l7);
        mv.visitLabel(l5);
        mv.visitLineNumber(28, l5);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitLabel(l7);
        mv.visitLineNumber(31, l7);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(IRETURN);
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitLocalVariable("t", "Ljava/lang/Throwable;", null, l3, l7, 2);
        mv.visitLocalVariable("this", "Ljava/net/SocketOutputStream;", null, l6, l11, 0);
        mv.visitLocalVariable("_lock", "Z", null, l0, l11, 1);
        mv.visitMaxs(3, 4);
        mv.visitEnd();

    }

    /**
     * <p>给SocketOutputStream类新增_socketWrite方法</p>
     * <p>入参为：FileDescriptor fd,byte b[], int off, int len</p>
     * 
     * @param classWriter
     */
    private void createSocketWrite(ClassWriter classWriter){
        MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE, "_socketWrite", "(Ljava/io/FileDescriptor;[BII)V", null, new String[]{"java/io/IOException"});
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(44, l0);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitIntInsn(BIPUSH, 6);
        Label l1 = new Label();
        mv.visitJumpInsn(IF_ICMPLE, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLineNumber(45, l2);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 5);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLineNumber(47, l3);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("getThreadLogGroupId");
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "_getLogId", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, 6);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitLineNumber(48, l4);
        mv.visitVarInsn(ALOAD, 6);
        Label l5 = new Label();
        mv.visitJumpInsn(IFNONNULL, l5);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitLineNumber(49, l6);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "socketWrite0", "(Ljava/io/FileDescriptor;[BII)V", false);
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitLineNumber(50, l7);
        mv.visitInsn(RETURN);
        mv.visitLabel(l5);
        mv.visitLineNumber(52, l5);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ISTORE, 7);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitJumpInsn(IF_ICMPGE, l1);
        Label l9 = new Label();
        mv.visitLabel(l9);
        mv.visitLineNumber(53, l9);
        mv.visitVarInsn(ILOAD, 5);
        Label l10 = new Label();
        mv.visitJumpInsn(IFNE, l10);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 32);
        mv.visitJumpInsn(IF_ICMPNE, l10);
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitLineNumber(54, l11);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "isHttpProtocol", "([BI)Z", false);
        mv.visitVarInsn(ISTORE, 5);
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitLineNumber(55, l12);
        mv.visitVarInsn(ILOAD, 5);
        Label l13 = new Label();
        mv.visitJumpInsn(IFNE, l13);
        Label l14 = new Label();
        mv.visitLabel(l14);
        mv.visitLineNumber(56, l14);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l10);
        mv.visitLineNumber(58, l10);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitJumpInsn(IFEQ, l13);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 10);
        mv.visitJumpInsn(IF_ICMPNE, l13);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 13);
        mv.visitJumpInsn(IF_ICMPNE, l13);
        Label l15 = new Label();
        mv.visitLabel(l15);
        mv.visitLineNumber(59, l15);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, 8);


        Label l16 = new Label();
        mv.visitLabel(l16);
        mv.visitLineNumber(61, l16);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("getThreadCurrentLogId");
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "_getLogId", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, 9);
        Label l17 = new Label();
        mv.visitLabel(l17);
        mv.visitLineNumber(63, l17);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 10);
        Label l18 = new Label();
        mv.visitLabel(l18);
        mv.visitLineNumber(64, l18);
        mv.visitVarInsn(ALOAD, 10);
        mv.visitLdcInsn("hloggid: ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ALOAD, 6);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn("\r\n");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitInsn(POP);


        Label l19 = new Label();
        mv.visitLabel(l19);
        mv.visitLineNumber(65, l19);
        mv.visitVarInsn(ALOAD, 9);
        Label l20 = new Label();
        mv.visitJumpInsn(IFNULL, l20);
        Label l21 = new Label();
        mv.visitLabel(l21);
        mv.visitLineNumber(66, l21);
        mv.visitVarInsn(ALOAD, 10);
        mv.visitLdcInsn("hlogpid: ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ALOAD, 9);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn("\r\n");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitInsn(POP);

        //------------------
        mv.visitLabel(l20);
        mv.visitLineNumber(68, l20);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("getCollectTag");
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "_getLogId", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, 11);
        Label l23 = new Label();
        mv.visitLabel(l23);
        mv.visitLineNumber(69, l23);
        mv.visitVarInsn(ALOAD, 11);
        Label l24 = new Label();
        mv.visitJumpInsn(IFNULL, l24);
        Label l25 = new Label();
        mv.visitLabel(l25);
        mv.visitLineNumber(70, l25);
        mv.visitVarInsn(ALOAD, 10);
        mv.visitLdcInsn("hlogctag: ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ALOAD, 11);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn("\r\n");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitInsn(POP);

//----
        mv.visitLabel(l24);
        mv.visitLineNumber(72, l24);
        mv.visitVarInsn(ALOAD, 10);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false);
        mv.visitVarInsn(ASTORE, 12);
        Label l26 = new Label();
        mv.visitLabel(l26);
        mv.visitLineNumber(75, l26);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitVarInsn(ALOAD, 12);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitInsn(IADD);
        mv.visitIntInsn(NEWARRAY, T_BYTE);
        mv.visitVarInsn(ASTORE, 13);
        Label l27 = new Label();
        mv.visitLabel(l27);
        mv.visitLineNumber(77, l27);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
        Label l28 = new Label();
        mv.visitLabel(l28);
        mv.visitLineNumber(78, l28);
        mv.visitVarInsn(ALOAD, 12);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitVarInsn(ALOAD, 12);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
        Label l29 = new Label();
        mv.visitLabel(l29);
        mv.visitLineNumber(79, l29);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitVarInsn(ALOAD, 12);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitInsn(ISUB);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
        Label l30 = new Label();
        mv.visitLabel(l30);
        mv.visitLineNumber(87, l30);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "showLog", "([BIII)V", false);
        Label l31 = new Label();
        mv.visitLabel(l31);
        mv.visitLineNumber(89, l31);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "socketWrite0", "(Ljava/io/FileDescriptor;[BII)V", false);
        Label l32 = new Label();
        mv.visitLabel(l32);
        mv.visitLineNumber(91, l32);
        mv.visitInsn(RETURN);
        mv.visitLabel(l13);
        mv.visitLineNumber(52, l13);
        mv.visitIincInsn(7, 1);
        mv.visitJumpInsn(GOTO, l8);
        mv.visitLabel(l1);
        mv.visitLineNumber(97, l1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "showLog", "([BIII)V", false);
        Label l33 = new Label();
        mv.visitLabel(l33);
        mv.visitLineNumber(98, l33);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "socketWrite0", "(Ljava/io/FileDescriptor;[BII)V", false);
        Label l34 = new Label();
        mv.visitLabel(l34);
        mv.visitLineNumber(99, l34);
        mv.visitInsn(RETURN);
        Label l35 = new Label();
        mv.visitLabel(l35);
        mv.visitLocalVariable("newIndex", "I", null, l16, l13, 8);
        mv.visitLocalVariable("pid", "Ljava/lang/String;", null, l17, l13, 9);
        mv.visitLocalVariable("head", "Ljava/lang/StringBuilder;", null, l18, l13, 10);
        mv.visitLocalVariable("tag", "Ljava/lang/String;", null, l23, l13, 11);
        mv.visitLocalVariable("headBytes", "[B", null, l26, l13, 12);
        mv.visitLocalVariable("newdatas", "[B", null, l27, l13, 13);
        mv.visitLocalVariable("index", "I", null, l8, l1, 7);
        mv.visitLocalVariable("isHttp", "Z", null, l3, l1, 5);
        mv.visitLocalVariable("gid", "Ljava/lang/String;", null, l4, l1, 6);
        mv.visitLocalVariable("this", "Lcom/asiainfo/hlog/agent/bytecode/asm/socket/TestASM;", null, l0, l35, 0);
        mv.visitLocalVariable("fd", "Ljava/io/FileDescriptor;", null, l0, l35, 1);
        mv.visitLocalVariable("b", "[B", null, l0, l35, 2);
        mv.visitLocalVariable("off", "I", null, l0, l35, 3);
        mv.visitLocalVariable("len", "I", null, l0, l35, 4);
        mv.visitMaxs(6, 14);
        mv.visitEnd();
    }


    private void showLog(ClassWriter classWriter){
        MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE, "showLog", "([BIII)V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLdcInsn("true");
        mv.visitLdcInsn("socketLog");
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        Label l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("-hlog:flag=");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn(",_socketWrite :\n[");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitTypeInsn(NEW, "java/lang/String");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([BII)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn("]");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitLabel(l1);
        mv.visitInsn(RETURN);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLocalVariable("this", "Ljava/net/SocketOutputStream;", null, l0, l3, 0);
        mv.visitLocalVariable("b", "[B", null, l0, l3, 1);
        mv.visitLocalVariable("off", "I", null, l0, l3, 2);
        mv.visitLocalVariable("len", "I", null, l0, l3, 3);
        mv.visitLocalVariable("flag", "I", null, l0, l3, 4);
        mv.visitMaxs(7, 5);
        mv.visitEnd();
    }

    private void isHttpProtocol(ClassWriter classWriter){
        MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE, "isHttpProtocol", "([BI)Z", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ILOAD, 2);
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        Label l4 = new Label();
        mv.visitTableSwitchInsn(3, 6, l3, new Label[]{l1, l2, l3, l4});
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 71);
        Label l5 = new Label();
        mv.visitJumpInsn(IF_ICMPNE, l5);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 69);
        mv.visitJumpInsn(IF_ICMPNE, l5);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_2);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 84);
        mv.visitJumpInsn(IF_ICMPNE, l5);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l5);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 80);
        mv.visitJumpInsn(IF_ICMPNE, l3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 85);
        mv.visitJumpInsn(IF_ICMPNE, l3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_2);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 84);
        mv.visitJumpInsn(IF_ICMPNE, l3);
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 80);
        Label l8 = new Label();
        mv.visitJumpInsn(IF_ICMPNE, l8);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 79);
        mv.visitJumpInsn(IF_ICMPNE, l8);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_2);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 83);
        mv.visitJumpInsn(IF_ICMPNE, l8);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_3);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 84);
        mv.visitJumpInsn(IF_ICMPNE, l8);
        mv.visitInsn(ICONST_1);
        Label l9 = new Label();
        mv.visitJumpInsn(GOTO, l9);
        mv.visitLabel(l8);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(l9);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 68);
        Label l10 = new Label();
        mv.visitJumpInsn(IF_ICMPNE, l10);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 69);
        mv.visitJumpInsn(IF_ICMPNE, l10);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_2);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 76);
        mv.visitJumpInsn(IF_ICMPNE, l10);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_3);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 69);
        mv.visitJumpInsn(IF_ICMPNE, l10);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_4);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 84);
        mv.visitJumpInsn(IF_ICMPNE, l10);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_5);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 69);
        mv.visitJumpInsn(IF_ICMPNE, l10);
        mv.visitInsn(ICONST_1);
        Label l11 = new Label();
        mv.visitJumpInsn(GOTO, l11);
        mv.visitLabel(l10);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(l11);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l3);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitLocalVariable("this", "Ljava/net/SocketOutputStream;", null, l0, l12, 0);
        mv.visitLocalVariable("b", "[B", null, l0, l12, 1);
        mv.visitLocalVariable("len", "I", null, l0, l12, 2);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }
}

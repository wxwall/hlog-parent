package com.asiainfo.hlog.agent.bytecode.asm.socket;

import com.asiainfo.hlog.agent.HLogAgentConst;
import com.asiainfo.hlog.agent.bytecode.asm.ASMConsts;
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


    public void createNewMethods(ClassWriter classWriter) {
        //创建isWrited成员属性
        {
            FieldVisitor fv = classWriter.visitField(0, FIELD_IS_WRITED, "Z", null, null);
            fv.visitEnd();
        }

        ASMUtils.createGetLogIdMethod(classWriter);

        showLog(classWriter);

        createSocketWrite(classWriter);

        isHttpProtocol(classWriter);
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
        /*
        mv.visitLdcInsn("true");
        mv.visitLdcInsn("socketLog");
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        Label ll1 = new Label();
        mv.visitJumpInsn(IFEQ, ll1);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("_socketWrite : ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitTypeInsn(NEW, "java/lang/String");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitLdcInsn("UTF-8");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([BIILjava/lang/String;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitLabel(ll1);
        */

        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, JAVA_NET_SOCKET_OUTPUT_STREAM, FIELD_IS_WRITED, "Z");
        Label l1 = new Label();
        mv.visitJumpInsn(IFNE, l1);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitIntInsn(BIPUSH, 6);
        mv.visitJumpInsn(IF_ICMPLE, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 5);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, JAVA_NET_SOCKET_OUTPUT_STREAM, FIELD_IS_WRITED, "Z");
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("getThreadLogGroupId");
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "_getLogId", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, 6);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitVarInsn(ALOAD, 6);
        Label l6 = new Label();
        mv.visitJumpInsn(IFNONNULL, l6);
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "socketWrite0", "(Ljava/io/FileDescriptor;[BII)V", false);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitInsn(RETURN);
        mv.visitLabel(l6);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ISTORE, 7);
        Label l9 = new Label();
        mv.visitLabel(l9);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitJumpInsn(IF_ICMPGE, l1);
        Label l10 = new Label();
        mv.visitLabel(l10);
        mv.visitVarInsn(ILOAD, 5);
        Label l11 = new Label();
        mv.visitJumpInsn(IFNE, l11);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 32);
        mv.visitJumpInsn(IF_ICMPNE, l11);
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "isHttpProtocol", "([BI)Z", false);
        mv.visitVarInsn(ISTORE, 5);
        Label l13 = new Label();
        mv.visitLabel(l13);
        mv.visitVarInsn(ILOAD, 5);
        Label l14 = new Label();
        mv.visitJumpInsn(IFNE, l14);
        Label l15 = new Label();
        mv.visitLabel(l15);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l11);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitJumpInsn(IFEQ, l14);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 10);
        mv.visitJumpInsn(IF_ICMPNE, l14);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
        mv.visitInsn(BALOAD);
        mv.visitIntInsn(BIPUSH, 13);
        mv.visitJumpInsn(IF_ICMPNE, l14);
        Label l16 = new Label();
        mv.visitLabel(l16);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, 8);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "showLog", "([BIII)V", false);


        Label l17 = new Label();
        mv.visitLabel(l17);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "socketWrite0", "(Ljava/io/FileDescriptor;[BII)V", false);
        Label l18 = new Label();
        mv.visitLabel(l18);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("getThreadCurrentLogId");
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "_getLogId", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, 9);
        Label l19 = new Label();
        mv.visitLabel(l19);
        mv.visitTypeInsn(NEW, ASMConsts.JAVA_LANG_STRING_BUILDER);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ASMConsts.JAVA_LANG_STRING_BUILDER, "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 10);
        Label l20 = new Label();
        mv.visitLabel(l20);
        mv.visitVarInsn(ALOAD, 10);
        mv.visitLdcInsn(HLogAgentConst.HEADER_HLOG_AGENT_GID+":");
        mv.visitMethodInsn(INVOKEVIRTUAL, ASMConsts.JAVA_LANG_STRING_BUILDER, ASMConsts.STRINGBUILDER_APPEND, ASMConsts.LJAVA_LANG_STRING_LJAVA_LANG_STRING_BUILDER, false);
        mv.visitVarInsn(ALOAD, 6);
        mv.visitMethodInsn(INVOKEVIRTUAL, ASMConsts.JAVA_LANG_STRING_BUILDER, ASMConsts.STRINGBUILDER_APPEND, ASMConsts.LJAVA_LANG_STRING_LJAVA_LANG_STRING_BUILDER, false);
        mv.visitLdcInsn("\r\n");
        mv.visitMethodInsn(INVOKEVIRTUAL, ASMConsts.JAVA_LANG_STRING_BUILDER, ASMConsts.STRINGBUILDER_APPEND, ASMConsts.LJAVA_LANG_STRING_LJAVA_LANG_STRING_BUILDER, false);
        mv.visitInsn(POP);
        Label l21 = new Label();
        mv.visitLabel(l21);
        mv.visitVarInsn(ALOAD, 9);
        Label l22 = new Label();
        mv.visitJumpInsn(IFNULL, l22);
        Label l23 = new Label();
        mv.visitLabel(l23);
        mv.visitVarInsn(ALOAD, 10);
        mv.visitLdcInsn(HLogAgentConst.HEADER_HLOG_AGENT_PID+":");
        mv.visitMethodInsn(INVOKEVIRTUAL, ASMConsts.JAVA_LANG_STRING_BUILDER, ASMConsts.STRINGBUILDER_APPEND, ASMConsts.LJAVA_LANG_STRING_LJAVA_LANG_STRING_BUILDER, false);
        mv.visitVarInsn(ALOAD, 9);
        mv.visitMethodInsn(INVOKEVIRTUAL, ASMConsts.JAVA_LANG_STRING_BUILDER, ASMConsts.STRINGBUILDER_APPEND, ASMConsts.LJAVA_LANG_STRING_LJAVA_LANG_STRING_BUILDER, false);
        mv.visitLdcInsn("\r\n");
        mv.visitMethodInsn(INVOKEVIRTUAL, ASMConsts.JAVA_LANG_STRING_BUILDER, ASMConsts.STRINGBUILDER_APPEND, ASMConsts.LJAVA_LANG_STRING_LJAVA_LANG_STRING_BUILDER, false);
        mv.visitInsn(POP);
        mv.visitLabel(l22);
        mv.visitVarInsn(ALOAD, 10);
        mv.visitMethodInsn(INVOKEVIRTUAL, ASMConsts.JAVA_LANG_STRING_BUILDER, "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false);
        mv.visitVarInsn(ASTORE, 11);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 11);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ALOAD, 11);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitInsn(ICONST_2);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "showLog", "([BIII)V", false);

        Label l24 = new Label();
        mv.visitLabel(l24);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 11);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ALOAD, 11);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "socketWrite0", "(Ljava/io/FileDescriptor;[BII)V", false);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitInsn(ISUB);
        mv.visitInsn(ICONST_3);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "showLog", "([BIII)V", false);

        Label l25 = new Label();
        mv.visitLabel(l25);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitVarInsn(ILOAD, 8);
        mv.visitInsn(ISUB);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "socketWrite0", "(Ljava/io/FileDescriptor;[BII)V", false);
        Label l26 = new Label();
        mv.visitLabel(l26);
        mv.visitInsn(RETURN);
        mv.visitLabel(l14);
        mv.visitIincInsn(7, 1);
        mv.visitJumpInsn(GOTO, l9);
        mv.visitLabel(l1);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "showLog", "([BIII)V", false);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NET_SOCKET_OUTPUT_STREAM, "socketWrite0", "(Ljava/io/FileDescriptor;[BII)V", false);
        Label l27 = new Label();
        mv.visitLabel(l27);
        mv.visitInsn(RETURN);
        Label l28 = new Label();
        mv.visitLabel(l28);
        mv.visitLocalVariable("newIndex", "I", null, l17, l14, 8);
        mv.visitLocalVariable("pid", "Ljava/lang/String;", null, l19, l14, 9);
        mv.visitLocalVariable("head", "Ljava/lang/StringBuilder;", null, l20, l14, 10);
        mv.visitLocalVariable("headBytes", "[B", null, l24, l14, 11);
        mv.visitLocalVariable("index", "I", null, l9, l1, 7);
        mv.visitLocalVariable("isHttp", "Z", null, l3, l1, 5);
        mv.visitLocalVariable("gid", "Ljava/lang/String;", null, l5, l1, 6);
        mv.visitLocalVariable("this", "Ljava/net/SocketOutputStream;", null, l0, l28, 0);
        mv.visitLocalVariable("fd", "Ljava/io/FileDescriptor;", null, l0, l28, 1);
        mv.visitLocalVariable("b", "[B", null, l0, l28, 2);
        mv.visitLocalVariable("off", "I", null, l0, l28, 3);
        mv.visitLocalVariable("len", "I", null, l0, l28, 4);
        mv.visitMaxs(6, 12);
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

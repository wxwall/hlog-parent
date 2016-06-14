package com.asiainfo.hlog.agent.bytecode.asm.socket;

import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.ASM5;

/**
 * Created by chenfeng on 2016/5/6.
 */
public class SocketOutputStreamMethodVisitor extends MethodVisitor {

    public static final String CODE  = "socket.http.track";

    public SocketOutputStreamMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pnv, byte[] datas, String mcode) {
        //super(access, className, methodName, desc, pnv, datas, mcode);
        super(ASM5,pnv);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if("socketWrite0".equals(name)){
            super.visitMethodInsn(opcode, owner, "_socketWrite", desc, itf);
        }else{
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}

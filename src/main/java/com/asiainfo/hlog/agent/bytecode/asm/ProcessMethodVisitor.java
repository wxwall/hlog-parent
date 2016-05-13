package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.runtime.HLogMonitor;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.commons.LocalVariablesSorter;

import static com.asiainfo.hlog.org.objectweb.asm.Opcodes.*;

/**
 * 观察一个方法是否需要进行日志监控.<p>
 * 当需要监控会创建一些局部变量,所以我们需要从{@link LocalVariablesSorter}继承下来.
 * <pre>
 *     public String test(String param1,String param2){
 *          String str = "hello world";
 *          System.out.println(str);
 *          return str;
 *     }
 *
 *     如果test我们监控的方法,那么它将会变成下面这样:
 *
 *     public String test(String param1,String param2){
 *         HLogMonitor.start("className","test","(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",new String[]{"param1","param2",new Object[]{param1,param2}});
 *         try{
 *             String str = "hello world";
 *             System.out.println(str);
 *             HLogMonitor.end(str);
 *             return str;
 *         }catch(Throwable t){
 *             HLogMonitor.end(t);
 *             throw t;
 *         }
 *     }
 * </pre>
 * Created by chenfeng on 2016/4/20.
 */
public class ProcessMethodVisitor extends AbstractTryCatchMethodVisitor {

    public static final String CODE  = "process";


    public ProcessMethodVisitor(int access, String className, String methodName, String desc, MethodVisitor pmv, byte[] datas,String mcode) {
        super(access,className,methodName,desc, pmv,datas,mcode);
    }



    /**
     * 每个方法开始遍历字节码的入码
     */
    public void visitCode() {
        //如果有返回结果的话
        //如果需要有返回则定义
        defineReturnObject();

        //调用监控开始方法
        callMonitorStart();

        super.visitCode();
    }

    protected void beforeReturn(boolean isVoid){
        if(!isVoid){
            visitLdcInsn(mcode);
            visitIntInsn(RES_LOAD_CODE,idxReturn);
            //如果是一个基本类型的数据转字符串
            if(RES_LOAD_CODE != ALOAD){
                ASMUtils.visitStaticMethod(mv,String.class,"valueOf",ASMUtils.getBaseClass(returnType.getSort()));
            }
        }else {
            visitInsn(ACONST_NULL);
            visitInsn(ACONST_NULL);
        }
        visitInsn(ICONST_0);
        ASMUtils.visitStaticMethod(mv,HLogMonitor.class,"end",String.class,Object.class,boolean.class);
    }

    protected void beforeThrow() {
        // 调用日志监控结束,并记录发生的异常
        visitLdcInsn(mcode);
        visitVarInsn(ALOAD, 2);
        visitInsn(ICONST_1);
        ASMUtils.visitStaticMethod(mv,HLogMonitor.class,"end",String.class,Object.class,boolean.class);
    }


}

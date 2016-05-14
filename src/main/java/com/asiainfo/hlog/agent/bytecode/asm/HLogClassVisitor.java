package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.ExcludeRuleUtils;
import com.asiainfo.hlog.client.config.LogSwoopRule;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.org.objectweb.asm.ClassVisitor;
import com.asiainfo.hlog.org.objectweb.asm.ClassWriter;
import com.asiainfo.hlog.org.objectweb.asm.MethodVisitor;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.Set;

/**
 * 当一个类来临时,我们开始遍历它,找出需要的东西
 * Created by chenfeng on 2016/4/20.
 */
public class HLogClassVisitor extends ClassVisitor {
    /**
     * 类名称
     */
    private final String className;
    /**
     * class的二进制流
     */
    private final byte[] datas;

    private final HLogPreProcessor processor;
    /**
     * 类的植入规则
     */
    private final LogSwoopRule classRule;

    public HLogClassVisitor(HLogPreProcessor processor, LogSwoopRule classRule, String className, byte[] datas, ClassWriter classVisitor) {
        super(Opcodes.ASM5, classVisitor);
        this.processor = processor;
        this.classRule = classRule;
        this.className = className;
        this.datas = datas;
    }

    private MethodVisitor getMethodVisitorByCode(MethodVisitor mv,int access, String name, String desc,String code,String mcode){
        if(code.charAt(0)=='+'){
            return mv;
        }
        return HLogMethodVisitorFactory.newMethodVisitor(className,mv,access,name,desc,datas,code,mcode);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // 排除不关注的方法
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (name.charAt(0) == '<' || ExcludeRuleUtils.isExcludeMethod(className,name)){
            return mv;
        }else if(ExcludeRuleUtils.isGetOrSetMethod(name,desc)){
            //如果是get和set方法不处理
            return mv;
        }
        //获取方法的规则
        LogSwoopRule methodRule = processor.getSupportRule(className,name);
        //如果方法没有规则,使用类的
        if(methodRule==null || methodRule.getMcodeMap().isEmpty()){
            if (classRule != null) {
                methodRule = classRule;
            } else {
                //没有规则返回原始方法
                return mv;
            }
        }
        //判断,如果规则是方法级的,需要判断方法名称是否一样
        if(methodRule.getPath().isMethod()){
            if(!methodRule.getPath().getMethodName().equals(name)){
                return mv;
            }
        }
        //访问需要修改的方法
        try {
            Set<Map.Entry<String, String>> entries =  methodRule.getMcodeMap().entrySet();
            MethodVisitor newMethod = mv;
            for (Map.Entry<String, String> entry : entries) {
                //根据不同的类型顺序执行字节码操作
                newMethod = getMethodVisitorByCode(newMethod,access,name,desc,entry.getKey(),entry.getValue());
            }
            if(newMethod==null){
                newMethod = mv;
            }
            return newMethod;
        }catch (HLogASMException e){
            // 通知发生异常了
            Logger.error("在植入{}类的{}方法异常",e,className,name);
            // 返回原始的方法,保存应用正常
            return  mv;
        }
    }

}

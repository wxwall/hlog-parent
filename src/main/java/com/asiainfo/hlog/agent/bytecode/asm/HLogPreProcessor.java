package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.AbstractPreProcessor;
import com.asiainfo.hlog.client.config.LogSwoopRule;
import com.asiainfo.hlog.client.config.jmx.HLogJMXReport;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.org.objectweb.asm.ClassReader;
import com.asiainfo.hlog.org.objectweb.asm.ClassWriter;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;

import java.security.ProtectionDomain;

/**
 * 1、拦截类加载器加载class到内存时的数据流;</br>
 * 2、判断拦截到的类是否是需要过滤处理的</br>
 * 3、如果有需要处理的,那么基于ASM字节码对业务代码进行过滤,主要目的如下:</br>
 *    》 遍历出方法,判断方法是否在关注范围;</br>
 *    》 如果在关注范围,并在该方法上根据配置植入下面类型的日志采集代码：</br>
 *           运行情况(耗时、出入参、异常)--用于分析运行性能;</br>
 *           运行拦截,并可返回指定结果;  </br>
 *           Log4j/Logback开关和数据; </br>
 * 4、生成新的字节码返回JVM加载; </br>
 *
 * Created by chenfeng on 2016/4/20.
 */
public class HLogPreProcessor extends AbstractPreProcessor {

    public void initialize() {
        super.initialize();
    }

    public byte[] preProcess(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] bytes) {
        // 如果类名为空,或者字节为空,跳过处理
        if(className==null || bytes == null || bytes.length==0){
            return null;
        }

        String clazz = className;
        // 将类名转换分割符  com/asiainfo/Test -> com.asiainfo.Test
        if (className.indexOf("/") != -1) {
            clazz = className.replaceAll("/", ".");
        }
        // 排除hlog的类
        if(clazz.startsWith(exclude_path)){
            return null;
        }
        // 排除指定规则的类
        if(isExcludePath(clazz) && !fullClass.contains(clazz)){
            return null;
        }

        // 获取类的采集规则
        LogSwoopRule classRule = getSupportRule(clazz,null);

        if(classRule==null && !isSupportMethodRule(clazz)){
            return null;
        }

        try{
            ClassReader classReader = new ClassReader(bytes);
            int flag = ClassWriter.COMPUTE_MAXS;
            int jdk_v = bytes[7];
            //jdk版本大于1.6
            if(jdk_v>Opcodes.V1_6){
                flag = flag + ClassWriter.COMPUTE_FRAMES;
            }
            ClassWriter classWriter = new ClassWriter(classReader, flag);
            //修改原来方法
            HLogClassVisitor classVisitor = new HLogClassVisitor(this, classRule, clazz, bytes, classWriter);
            //未发生改变
            if(!classVisitor.isToVisit()){
                return null;
            }
            flag = Opcodes.ASM5;
            if(jdk_v<=Opcodes.V1_6){
                flag = flag + ClassReader.EXPAND_FRAMES;
            }
            classReader.accept(classVisitor, flag);
            //增加新方法
            if(classRule!=null && classRule.getNewMethodCodes().size()>0){
                HLogMethodCreator.create(classRule,classWriter);
            }

            byte[] code = classWriter.toByteArray();
            saveWaveClassFile(className,code);

            HLogJMXReport.getHLogJMXReport().getRunStatusInfo().incrementWeaveClassNum();

            return code;
        }catch (Throwable t){
            HLogJMXReport.getHLogJMXReport().getRunStatusInfo().incrementweaveErrClassNum();
            Logger.error("操作["+clazz+"]类时出错",t);
        }

        return null;
    }
}

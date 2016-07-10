package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.AbstractPreProcessor;
import com.asiainfo.hlog.client.config.LogSwoopRule;
import com.asiainfo.hlog.client.config.jmx.HLogJMXReport;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.org.objectweb.asm.ClassReader;
import com.asiainfo.hlog.org.objectweb.asm.ClassWriter;
import com.asiainfo.hlog.org.objectweb.asm.Opcodes;
import com.asiainfo.hlog.org.objectweb.asm.Type;
import com.asiainfo.hlog.org.objectweb.asm.tree.ClassNode;
import com.asiainfo.hlog.org.objectweb.asm.tree.MethodNode;
import com.asiainfo.hlog.org.objectweb.asm.tree.analysis.Analyzer;
import com.asiainfo.hlog.org.objectweb.asm.tree.analysis.BasicValue;
import com.asiainfo.hlog.org.objectweb.asm.tree.analysis.SimpleVerifier;
import com.asiainfo.hlog.org.objectweb.asm.util.CheckClassAdapter;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        byte[] code = null;
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

            flag = Opcodes.ASM5;
            if(jdk_v<=Opcodes.V1_6){
                flag = flag + ClassReader.EXPAND_FRAMES;
            }

            classReader.accept(classVisitor, flag);
            //增加新方法
            if(classRule!=null && classRule.getNewMethodCodes().size()>0){
                HLogMethodCreator.create(classRule,classWriter);
            }

            //未发生改变
            if(!classVisitor.isToVisit()){
                return null;
            }
            //得到新的字节码
            code = classWriter.toByteArray();
            //对新的字节码进行检验
            //字节码检验不通过,抛出异常并原字节码
            verify(new ClassReader(code),classLoader,flag);

            saveWaveClassFile(className,code);

            HLogJMXReport.getHLogJMXReport().getRunStatusInfo().incrementWeaveClassNum();

            return code;
        }catch (Throwable t){
            HLogJMXReport.getHLogJMXReport().getRunStatusInfo().incrementweaveErrClassNum();
            Logger.error("解析["+clazz+"]类时遇到问题,放弃解析返回原类内容.(不影响程序运行):{0}",null,t.getMessage());
            if(code!=null){
                saveErrorWaveClassFile(className,code);
            }
        }

        return null;
    }

    private void verify(final ClassReader cr, final ClassLoader loader,final  int flag) throws Exception {
        ClassNode cn = new ClassNode();
        cr.accept(new CheckClassAdapter(cn, false), ClassReader.SKIP_DEBUG+flag);

        Type syperType = cn.superName == null ? null : Type
                .getObjectType(cn.superName);
        List<MethodNode> methods = cn.methods;

        List<Type> interfaces = new ArrayList<Type>();
        for (Iterator<String> i = cn.interfaces.iterator(); i.hasNext();) {
            interfaces.add(Type.getObjectType(i.next()));
        }

        for (int i = 0; i < methods.size(); ++i) {
            MethodNode method = methods.get(i);
            SimpleVerifier verifier = new SimpleVerifier(
                    Type.getObjectType(cn.name), syperType, interfaces,
                    (cn.access & Opcodes.ACC_INTERFACE) != 0);
            Analyzer<BasicValue> a = new Analyzer<BasicValue>(verifier);
            if (loader != null) {
                verifier.setClassLoader(loader);
            }
            try {
                a.analyze(cn.name, method);
            } catch (Exception e) {
                throw e;
            }
        }
    }
}

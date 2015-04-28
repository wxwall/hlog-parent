package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.AbstractPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.AfterPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.BeforeAfterPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.IMethodPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.RoundPreProcessor;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;
import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * 日志代理处理器:</br>
 * Created by c on 2015/4/9.
 */
public class HLogPreProcessor extends AbstractPreProcessor {

    private final String exclude_path = "com.asiainfo.hlog";
    //增加排序功能
    private List<LogSwoopRule> logSwoopRuleList = null;

    private ILogWeaveActuator logWeaveActuator = null;

    private IMethodPreProcessor beforePreProcessor = null;

    private IMethodPreProcessor afterPreProcessor = null;

    private IMethodPreProcessor roundPreProcessor = null;

    private IMethodPreProcessor beforeAfterPreProcessor = null;

    //private ClassPool pool = null;

    private void addDependLogWeave(List<ILogWeave> weaves,String[] depends){

        if(depends==null){
            return ;
        }
        for(String weaveName:depends){
            ILogWeave weave = LogWeaveFactory.getInstance().getLogWeave(weaveName);
            if(weave!=null){
                if(!weaves.contains(weave)){
                    weaves.add(weave);
                    addDependLogWeave(weaves, weave.getDependLogWeave());
                }
            }
        }
    }

    public void initialize(){


        HLogConfig config = HLogConfig.getInstance();

        logWeaveActuator = new DefaultLogWeaveActuator();
        logSwoopRuleList = new ArrayList<LogSwoopRule>();

        Set<String> basePaths = config.getBasePaths().keySet();

        for (String basePath : basePaths){

            LogSwoopRule logSwoopRule = new LogSwoopRule();
            logSwoopRule.setClassName(new String[]{basePath});

            String[] weaveNames = config.getBasePaths().get(basePath);

            List<ILogWeave> weaves = new ArrayList<ILogWeave>(weaveNames.length);
            addDependLogWeave(weaves,weaveNames);

            Collections.sort(weaves, new Comparator<ILogWeave>() {
                public int compare(ILogWeave weave1, ILogWeave weave2) {
                    return weave1.getOrder() > weave2.getOrder() ? 1 : -1;
                }
            });
            logSwoopRule.setWeaves(weaves);
            logSwoopRuleList.add(logSwoopRule);
        }


        beforePreProcessor = new BeforeAfterPreProcessor();
        afterPreProcessor = new AfterPreProcessor();
        roundPreProcessor = new RoundPreProcessor();
        beforeAfterPreProcessor = new BeforeAfterPreProcessor();

        //QueueHolder.getHolder().start();
    }
    private LogSwoopRule isSupportRule(String className){
        for(LogSwoopRule rule : logSwoopRuleList){
            String[] classNames = rule.getClassName();
            for (String name : classNames){
                if(className.startsWith(name)){
                    return rule;
                }
            }
        }
        return null;
    }
    private ClassPool pool;
    public byte[] preProcess(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        //判断 该className是否有符合的 LogSwoopRule
        if(className==null){
            return null;
        }

        if (className.indexOf("/") != -1) {
            className = className.replaceAll("/", ".");
        }

        if(className.startsWith(exclude_path)){
            return null;
        }

        if(isExcludePath(className)){
            return null;
        }
        //System.out.println("className="+className);
        LogSwoopRule rule = isSupportRule(className);
        if(rule==null || rule.getWeaves()==null || rule.getWeaves().size()==0){
            return null;
        }
        //构建 LogWeaveContext
        LogWeaveContext context = new LogWeaveContext();
        context.setClassName(className);
        ByteArrayInputStream inp = new ByteArrayInputStream(classfileBuffer);
        //通过包名获取类文件
        try {

            if(Logger.isTrace()){
                Logger.trace("正在处理[{0}]类字节码.当前Loader:{1},paramClassLoader:{2}", className, Thread.currentThread().getContextClassLoader().getClass(), classLoader);
            }

            //Thread.currentThread().getContextClassLoader().loadClass(className);

            if(pool==null){
                pool = ClassPool.getDefault();
                pool.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
            }

            CtClass ctClass = pool.makeClass(inp);

            //如果是接口直接返回
            if(ctClass.isInterface()){
                return null;
            }

            CtMethod[] ctMethods = ctClass.getDeclaredMethods();

            for (CtMethod ctMethod:ctMethods){
                String methodName = ctMethod.getName();
                if(ctMethod.isEmpty() || isExcludeMethod(methodName)){
                    continue;
                }
                try{

                    context.setMethodName(methodName);

                    //计算出该方法的代码块
                    LogWeaveCode logWeaveCode = logWeaveActuator.executeWeave(context, rule.getWeaves());

                    int type = logWeaveCode.getType();

                    switch (type){
                        case IMethodPreProcessor.BEFORE_TYPE:
                            beforePreProcessor.preProcessor(ctClass,ctMethod,context,logWeaveCode);
                            break;
                        case IMethodPreProcessor.OFTER_TYPE:
                            afterPreProcessor.preProcessor(ctClass,ctMethod,context,logWeaveCode);
                            break;
                        case IMethodPreProcessor.BEFORE_OFTER_TYPE:
                            beforeAfterPreProcessor.preProcessor(ctClass, ctMethod, context, logWeaveCode);
                            break;
                        case IMethodPreProcessor.ROUND_TYPE:
                            roundPreProcessor.preProcessor(ctClass, ctMethod, context, logWeaveCode);
                            break;
                    }

                }catch (Throwable e){
                    //e.printStackTrace();
                    //System.out.println("weave method["+methodName+"] error:"+e.getMessage());
                    Logger.error("weave class [{0}] method[{1}]",e,className,methodName);
                }
            }


            byte[] code = ctClass.toBytecode();
            /*
            FileOutputStream fos = new FileOutputStream("d:/tmp/"+ctClass.getSimpleName()+".class");
            fos.write(code);
            fos.close();
            */
            return code;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}

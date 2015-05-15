package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.AbstractPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.AfterPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.BeforeAfterPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.IMethodPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.RoundPreProcessor;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.config.Path;
import com.asiainfo.hlog.client.config.PathType;
import com.asiainfo.hlog.client.config.jmx.HLogJMXReport;
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

        Set<Path> basePaths = config.getBasePaths().keySet();

        for (Path basePath : basePaths){

            LogSwoopRule logSwoopRule = new LogSwoopRule();
            logSwoopRule.setPath(basePath);

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
            //String[] classNames = rule.getClassName();
            Path path = rule.getPath();
            if(path.getType()== PathType.PACKAGE){
                if(className.startsWith(path.toString())){
                    return rule;
                }
            }else if(path.getType() == PathType.CLASS){
                if(className.equals(path.getFullClassName())){
                    return rule;
                }
            }
        }
        return null;
    }
    private ClassPool pool;
    public byte[] preProcess(ClassLoader classLoader, String classFile, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        //判断 该className是否有符合的 LogSwoopRule
        if(classFile==null){
            return null;
        }

        String clazz = classFile;
        if (classFile.indexOf("/") != -1) {
            clazz = classFile.replaceAll("/", ".");
        }

        if(clazz.startsWith(exclude_path)){
            return null;
        }

        if(isExcludePath(clazz)){
            return null;
        }

        //System.out.println("className="+className);
        LogSwoopRule rule = isSupportRule(clazz);
        if(rule==null || rule.getWeaves()==null || rule.getWeaves().size()==0){
            return null;
        }
        //构建 LogWeaveContext
        LogWeaveContext context = new LogWeaveContext();
        context.setClassName(clazz);
        ByteArrayInputStream inp = new ByteArrayInputStream(classfileBuffer);
        boolean weaved = false;
        //通过包名获取类文件
        try {

            if(Logger.isTrace()){
                Logger.trace("正在处理[{0}]类字节码.当前Loader:{1},paramClassLoader:{2}", clazz,
                        Thread.currentThread().getContextClassLoader().getClass(), classLoader!=null?classLoader.getClass():"null");
            }

            //Thread.currentThread().getContextClassLoader().loadClass(className);

            if(pool==null){
                pool = ClassPool.getDefault();
                pool.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
            }
            CtClass ctClass = null;
            try{
                ctClass = pool.get(clazz);
            }catch (Exception e){}
            if(ctClass==null){
                ctClass = pool.makeClass(inp);
            }

            //如果是接口直接返回
            if(ctClass.isInterface() || ctClass.isFrozen()){
                return null;
            }

            CtMethod[] ctMethods = ctClass.getDeclaredMethods();
            HLogJMXReport.getHLogJMXReport().getRunStatusMBean().incrementWeaveClassNum();
            for (CtMethod ctMethod:ctMethods){
                String methodName = ctMethod.getName();
                if(isExcludeMethod(null,methodName)){
                    continue;
                }
                try{

                    context.setMethodName(methodName);
                    context.setParamNumber(ctMethod.getParameterTypes().length);

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
                    weaved = true;
                }catch (Throwable e){
                    //e.printStackTrace();
                    //System.out.println("weave method["+methodName+"] error:"+e.getMessage());
                    HLogJMXReport.getHLogJMXReport().getRunStatusMBean().incrementweaveErrClassNum();
                    Logger.error("weave class [{0}] method[{1}]",e,clazz,methodName);
                }
            }


            byte[] code = ctClass.toBytecode();
            //保存文件
            if(weaved){
                saveWaveClassFile(classFile,code);
            }

            return code;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}

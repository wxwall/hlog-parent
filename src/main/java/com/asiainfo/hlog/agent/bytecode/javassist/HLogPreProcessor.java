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
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * 日志代理处理器,Javassist实现:</br>
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

    private Map<String,String> addLogWeaveAndReturnMcode(List<ILogWeave> weaves,String[] depends){

        if(depends==null){
            return null;
        }
        Map<String,String> mcodeMap = new HashMap<String, String>();
        for(String weaveMcodeCfg:depends){
            String[] weaveMcode = weaveMcodeCfg.split(":");
            String weaveName = weaveMcode[0];
            ILogWeave weave = LogWeaveFactory.getInstance().getLogWeave(weaveName);
            if(weave!=null){
                if(!weaves.contains(weave)){
                    if(weaveMcode.length==2){
                        mcodeMap.put(weaveName,weaveMcode[1]);
                    }
                    weaves.add(weave);
                    addLogWeaveAndReturnMcode(weaves, weave.getDependLogWeave());
                }
            }
        }
        return mcodeMap;
    }

    public void initialize(){
        HLogConfig config = HLogConfig.getInstance();

        logSwoopRuleList = new ArrayList<LogSwoopRule>();
        Set<Path> basePaths = config.getBasePaths().keySet();
        for (Path basePath : basePaths){
            LogSwoopRule logSwoopRule = new LogSwoopRule();
            logSwoopRule.setPath(basePath);
            String[] weaveNames = config.getBasePaths().get(basePath);
            List<ILogWeave> weaves = new ArrayList<ILogWeave>(weaveNames.length);
            Map<String,String> mcodeMap = addLogWeaveAndReturnMcode(weaves, weaveNames);
            Collections.sort(weaves, new Comparator<ILogWeave>() {
                public int compare(ILogWeave weave1, ILogWeave weave2) {
                    return weave1.getOrder() > weave2.getOrder() ? 1 : -1;
                }
            });
            logSwoopRule.setWeaves(weaves);
            logSwoopRule.setMcodeMap(mcodeMap);
            logSwoopRuleList.add(logSwoopRule);
        }

        logWeaveActuator = new DefaultLogWeaveActuator();
        beforePreProcessor = new BeforeAfterPreProcessor();
        afterPreProcessor = new AfterPreProcessor();
        roundPreProcessor = new RoundPreProcessor();
        beforeAfterPreProcessor = new BeforeAfterPreProcessor();

        //QueueHolder.getHolder().start();
    }
    private boolean isSupportMethodRule(String className){
        for(LogSwoopRule rule : logSwoopRuleList){
            Path path = rule.getPath();
            if(path.getType() == PathType.METHOD){
                if(className.equals(path.getFullClassName())){
                    return true;
                }
            }
        }
        return false;
    }
    private LogSwoopRule isSupportRule(String className,String methodName){
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
            }else if(path.getType() == PathType.METHOD && methodName!=null){
                if(methodName.equals(path.getMethodName())){
                    if(className.equals(path.getFullClassName())){
                        return rule;
                    }
                }
            }
        }
        return null;
    }
    private ClassPool pool;
    private static String[] getMethodParamNames(CtMethod cm){
        if(cm==null)
            return null;
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

        String[] paramNames = null;
        try {
            paramNames = new String[cm.getParameterTypes().length];
        } catch (NotFoundException e) {
        }
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramNames.length; i++){
            String name = attr.variableName(i + pos);
            if("this".equals(name)){
                pos = pos + 1;
                name = attr.variableName(i + pos);
            }
            paramNames[i] = name;
        }
        return paramNames;
    }
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
        LogSwoopRule rule = isSupportRule(clazz,null);
        boolean havClassRule = false;
        if(rule!=null && rule.getWeaves()!=null || rule.getWeaves().size()>0){
            havClassRule = true;
        }else{
            //判断是否有该类的方法级配置
            if(!isSupportMethodRule(clazz)){
                return null;
            }
        }
        //构建 LogWeaveContext
        LogWeaveContext context = new LogWeaveContext();
        context.setClassName(clazz);
        ByteArrayInputStream inp = new ByteArrayInputStream(classfileBuffer);
        boolean weaved = false;
        //通过包名获取类文件
        try {

            Class classLoaderCls = classLoader!=null?classLoader.getClass():null;

            if(Logger.isTrace()){
                Logger.trace("正在处理[{0}]类字节码.当前Loader:{1},paramClassLoader:{2}", clazz,
                        Thread.currentThread().getContextClassLoader().getClass(), classLoaderCls);
            }
            if(classLoaderCls!=null && classLoaderCls.getName().startsWith("com.asiainfo.hlog.agent.classloader.ClassLoaderHolder")){
                if(Logger.isTrace()){
                    Logger.trace("{0}为代理自定义加载器里的类,不需要织入代码.");
                }
                return null;
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
            HLogJMXReport.getHLogJMXReport().getRunStatusInfo().incrementWeaveClassNum();
            for (CtMethod ctMethod:ctMethods){
                String methodName = ctMethod.getName();
                LogSwoopRule methodRule = isSupportRule(clazz,methodName);
                if(methodRule==null || methodRule.getWeaves()==null || methodRule.getWeaves().size()==0){
                    if(!havClassRule){
                        methodRule = rule;
                    }else{
                        return null;
                    }
                }
                context.setRule(methodRule);
                context.setCreateInParams(false);

                if(isExcludeMethod(null,methodName)){
                    continue;
                }
                try{

                    context.setMethodName(methodName);
                    context.setParamNumber(ctMethod.getParameterTypes().length);
                    String[] paramNames = getMethodParamNames(ctMethod);
                    context.setParamNames(paramNames);

                    //计算出该方法的代码块
                    LogWeaveCode logWeaveCode = logWeaveActuator.executeWeave(context, methodRule.getWeaves());

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
                    HLogJMXReport.getHLogJMXReport().getRunStatusInfo().incrementweaveErrClassNum();
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
            Logger.error("weave class [{0}]", e, clazz);
        }
        return null;
    }

}

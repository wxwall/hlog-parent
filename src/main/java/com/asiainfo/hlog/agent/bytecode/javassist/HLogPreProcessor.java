package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.AbstractPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.AfterPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.BeforeAfterPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.IMethodPreProcessor;
import com.asiainfo.hlog.agent.bytecode.javassist.process.RoundPreProcessor;
import com.asiainfo.hlog.client.config.Constants;
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
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志代理处理器,Javassist实现:</br>
 * Created by chenfeng on 2015/4/9.
 */
public class HLogPreProcessor extends AbstractPreProcessor {

    private final String exclude_path = "com.asiainfo.hlog";
    //增加排序功能
    private List<LogSwoopRule> logSwoopRuleList = null;

    private List<String> fullClass = new ArrayList<String>();

    private ILogWeaveActuator logWeaveActuator = null;

    private IMethodPreProcessor beforePreProcessor = null;

    private IMethodPreProcessor afterPreProcessor = null;

    private IMethodPreProcessor roundPreProcessor = null;

    private IMethodPreProcessor beforeAfterPreProcessor = null;
    /**
     * 第三方日志框架的采集栈深度
     */
    private int loggerStackDepth = 2;

    //private ClassPool pool = null;

    private Set<Integer> existsHashCode = new TreeSet<Integer>();

    /**
     * 存储被织入类的字节码
     */

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

        String depth=config.getProperty(Constants.KEY_LOGGER_STACK_DEPTH,"2");
        loggerStackDepth = Integer.parseInt(depth);

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
            if(basePath.getType()==PathType.METHOD || basePath.getType()==PathType.CLASS){
                fullClass.add(basePath.getFullClassName());
            }
        }

        logWeaveActuator = new DefaultLogWeaveActuator();
        beforePreProcessor = new BeforeAfterPreProcessor();
        afterPreProcessor = new AfterPreProcessor();
        roundPreProcessor = new RoundPreProcessor();
        beforeAfterPreProcessor = new BeforeAfterPreProcessor();

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
            }else if(path.getType() == PathType.METHOD){
                if(className.equals(path.getFullClassName())){
                    if(methodName!=null){
                        if(methodName.equals(path.getMethodName())){
                            return rule;
                        }
                    }else{
                        return rule;
                    }
                }
            }
        }
        return null;
    }
    private ClassPool pool;
    private String[] getMethodParamNames(CtMethod cm){
        if(cm==null)
            return null;
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

        if(attr==null){
            return new String[0];
        }

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
    private boolean isCanWeaveMethod(CtMethod ctMethod){
        int accessFlag = ctMethod.getMethodInfo().getAccessFlags();
        if(accessFlag>1000){
            return false;
        }
        return true;
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

        if(clazz.startsWith(exclude_path) || clazz.indexOf("$EnhancerByCGLIB$")!=-1){
            return null;
        }

        if(isExcludePath(clazz) && !fullClass.contains(clazz)){
            return null;
        }

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
        context.setLoggerStackDepth(loggerStackDepth);
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
            ClassLoader threaClassLoader = Thread.currentThread().getContextClassLoader();
            if(pool==null){
                pool = ClassPool.getDefault();
                //pool = new ClassPool(true);
            }
            int threadCLHash = threaClassLoader.hashCode();
            if(!existsHashCode.contains(threadCLHash)){
                //weakClassLoader.put(threadCLHash, threaClassLoader);
                existsHashCode.add(threadCLHash);
                pool.insertClassPath(new LoaderClassPath(threaClassLoader));
                if(Logger.isDebug()){
                    Logger.debug("当前线程中新的类加载器[{0}]:{1}",threadCLHash,threaClassLoader);
                }
            }
            int clHash = classLoader.hashCode();
            if(!existsHashCode.contains(clHash)){
                //weakClassLoader.put(clHash, classLoader);
                existsHashCode.add(clHash);
                pool.insertClassPath(new LoaderClassPath(classLoader));
                if(Logger.isDebug()){
                    Logger.debug("新的类加载器[{0}]:{1}",clHash,classLoader);
                }
            }

            CtClass ctClass = pool.makeClass(inp,false);


            //如果是接口直接返回
            if(ctClass==null || ctClass.isInterface() || ctClass.isAnnotation() || ctClass.isEnum() || ctClass.isFrozen() ){
                if(Logger.isDebug()){
                    Logger.debug("{0}类可能是接口、注释类、枚举或者被冻结{1}",clazz,ctClass.isFrozen());
                }
                return null;
            }

            CtMethod[] ctMethods = ctClass.getDeclaredMethods();
            HLogJMXReport.getHLogJMXReport().getRunStatusInfo().incrementWeaveClassNum();
            for (CtMethod ctMethod:ctMethods){
                String methodName = ctMethod.getName();
                if(!isCanWeaveMethod(ctMethod)){
                    if(Logger.isTrace()){
                        Logger.trace("{0}.{1}方法不支持织入,accessFlag={2}",clazz,methodName,ctMethod.getMethodInfo().getAccessFlags());
                    }
                    continue;
                }

                LogSwoopRule methodRule = isSupportRule(clazz,methodName);
                if(methodRule==null || methodRule.getWeaves()==null || methodRule.getWeaves().size()==0){
                    if(!havClassRule){
                        methodRule = rule;
                    }else{
                        continue;
                    }
                }
                context.setRule(methodRule);
                context.setCreateInParams(false);

                if(isExcludeMethod(clazz,methodName)){
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
                    Logger.error("weave class error [{0}] method[{1}]",e,clazz,methodName);
                }
            }

            if(Logger.isDebug()){
                Logger.trace("{0}类织入代码完成.",clazz);
            }
            byte[] code = ctClass.toBytecode();
            //classByteMap.put(key,code);
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

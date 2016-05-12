package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.client.config.*;
import com.asiainfo.hlog.client.helper.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 预处理抽象父类,下面可以有不同的实现:<p>
 * >ASM <p>
 * >javassit <p>
 * Created by chenfeng on 2015/4/23.
 */
public abstract class AbstractPreProcessor implements IHLogPreProcessor {

    /**
     * 排除掉hlog内部的一些类,避免hlog的类也被拦截
     */
    protected final String exclude_path = "com.asiainfo.hlog";

    //增加排序功能
    protected List<LogSwoopRule> logSwoopRuleList = null;

    protected List<String> fullClass = new ArrayList<String>();

    protected boolean isSaveWeaveClass = false;

    protected String saveWeaveClassPath = null;

    public void initialize() {
        HLogConfig config = HLogConfig.getInstance();
        logSwoopRuleList = new ArrayList<LogSwoopRule>();
        Set<Path> basePaths = config.getBasePaths().keySet();
        for (Path basePath : basePaths){
            LogSwoopRule logSwoopRule = new LogSwoopRule();
            logSwoopRule.setPath(basePath);
            String[] weaveNames = config.getBasePaths().get(basePath);
            addLogWeaveAndReturnMcode(logSwoopRule, weaveNames);
            logSwoopRuleList.add(logSwoopRule);
            if(basePath.getType()==PathType.METHOD || basePath.getType()==PathType.CLASS){
                fullClass.add(basePath.getFullClassName());
            }
        }

    }

    /**
     * 存储被织入类的字节码
     */
    private void addLogWeaveAndReturnMcode(LogSwoopRule logSwoopRule,String[] depends){

        if(depends==null){
            return;
        }
        Map<String,String> mcodeMap = new HashMap<String, String>();

        for(String weaveMcodeCfg:depends){
            String[] weaveMcode = weaveMcodeCfg.split(":");
            String weaveName = weaveMcode[0];
            if(weaveName.charAt(0)=='+'){
                logSwoopRule.addNewMethodCode(weaveName);
            }else if(weaveMcode.length==2){
                logSwoopRule.getMcodeMap().put(weaveName,weaveMcode[1]);
            }else{
                logSwoopRule.getMcodeMap().put(weaveName,weaveName);
            }
        }
        //return mcodeMap;
    }


    /**
     * 判断某个类是否存在方法级别的日志采集规则,如果存在就以方法级为准,不会整个都采集一致的规则
     * @param className
     * @return
     */
    protected boolean isSupportMethodRule(String className){
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

    /**
     * 获取某个类名称和方法是否有被支持的采集规则
     * @param className 类名称
     * @param methodName 方法名称
     * @return
     */
    public LogSwoopRule getSupportRule(String className,String methodName){
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
    /**
     * 判断方法是否在排除范围
     * @param className
     * @param methodName
     * @return
     */
    protected boolean isExcludeMethod(String className,String methodName){
        return ExcludeRuleUtils.isExcludeMethod(className,methodName);
    }

    /**
     * 判断类路径是否在排除范围
     * @param name
     * @return
     */
    protected boolean isExcludePath(String name){
        return ExcludeRuleUtils.isExcludePath(name);
    }

    /**
     * 保存被植入代码的类到指定目录
     * @param name
     * @param code
     */
    protected void saveWaveClassFile(String name,byte[] code){

        if(!isSaveWeaveClass){
            return;
        }
        FileOutputStream fos = null;
        try {
            File file = new File(saveWeaveClassPath+name+".class");
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }

            fos = new FileOutputStream(file);
            fos.write(code);

        }catch (IOException ioe){
            Logger.error("保存[{0}]异常",ioe,saveWeaveClassPath+name+".class");
        }finally {
            try {
                if(fos!=null){
                    fos.close();
                }
            }catch (IOException ii){}
        }
    }

    public AbstractPreProcessor(){

        isSaveWeaveClass = "yes".equals(System.getProperty(Constants.SYS_KEY_HLOG_SAVE_WEAVE_CLASS));

        if(isSaveWeaveClass){
            saveWeaveClassPath = HLogConfig.tmpdir + File.separator + "log-agent" + File.separator+"weave-class"+ File.separator;
        }
        if(Logger.isDebug()){
            Logger.debug("是否开启[{0}]保存被植class文件到指定目录：{1}",isSaveWeaveClass,saveWeaveClassPath);
        }


    }

}

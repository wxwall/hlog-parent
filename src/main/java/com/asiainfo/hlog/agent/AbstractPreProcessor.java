package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 预处理抽象父类
 * Created by chenfeng on 2015/4/23.
 */
public abstract class AbstractPreProcessor implements IHLogPreProcessor {
    /**
     * 排除方法
     */
    protected Set<String> excludeMethods = new HashSet<String>();

    /**
     * 排除方法表达式
     */
    protected Set<String> excludeMethodRegulars = new HashSet<String>();

    /**
     * 排除路径表达式
     */
    protected Set<String> excludePathRegulars = new HashSet<String>();

    protected boolean isSaveWeaveClass = false;

    protected String saveWeaveClassPath = null;

    public AbstractPreProcessor(){

        isSaveWeaveClass = "yes".equals(System.getProperty(Constants.SYS_KEY_HLOG_SAVE_WEAVE_CLASS));

        if(isSaveWeaveClass){
            saveWeaveClassPath = HLogConfig.tmpdir + File.separator + "log-agent" + File.separator+"weave-class"+ File.separator;
        }
        if(Logger.isDebug()){
            Logger.debug("是否开启[{0}]保存被植class文件到指定目录：{1}",isSaveWeaveClass,saveWeaveClassPath);
        }

        excludeMethods.add("main");
        excludeMethods.add("equals");
        excludeMethods.add("toString");
        excludeMethods.add("hashCode");
        excludeMethods.add("getClass");
        excludeMethods.add("notifyAll");
        excludeMethods.add("notify");
        excludeMethods.add("wait");

        //排除get/set方法
        excludeMethodRegulars.add(".*\\.[s|g]et[A-Z].*");

        //加载配置文件的方法排除信息
        String excludeMethodCfg = HLogConfig.getInstance()
                .getProperty(Constants.KEY_HLOG_EXCLUDE_METHODS);

        if(excludeMethodCfg!=null){
            addRules(excludeMethodCfg,excludeMethodRegulars);
        }

        //加载配置文件的排除信息
        //排除类
        String excludePaths = HLogConfig.getInstance()
                .getProperty(Constants.KEY_HLOG_EXCLUDE_PATHS);
        if(excludePaths!=null){
            addRules(excludePaths,excludePathRegulars);
        }
    }

    private void addRules(String rules,Set<String> set){
        String[] ruleArray = rules.split(",");
        for (String ep : ruleArray){
            if(!set.contains(ep)){
                set.add(ep);
            }
        }
    }

    /**
     * 判断方法是否在排除范围
     * @param className
     * @param methodName
     * @return
     */
    protected boolean isExcludeMethod(String className,String methodName){
        boolean b ;
        b = excludeMethods.contains(methodName);

        if(!b){
            String full = className+"."+methodName;
            for(String p : excludeMethodRegulars){
                Pattern pattern = Pattern.compile(p);
                Matcher matcher = pattern.matcher(full);
                b= matcher.matches();
                if(b){
                    break;
                }
            }
        }
        return b;
    }

    /**
     * 判断类路径是否在排除范围
     * @param name
     * @return
     */
    protected boolean isExcludePath(String name){
        boolean b = false;
        for(String p : excludePathRegulars){
            Pattern pattern = Pattern.compile(p);
            Matcher matcher = pattern.matcher(name);
            b= matcher.matches();
            if(b){
                break;
            }
        }
        return b;
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

    public static void main(String[] args) {
        String pp= ".*\\.[s|g]et[A-Z].*";
        String str = "com.Test.setasss";
        Pattern pattern = Pattern.compile(pp,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);
        boolean b = matcher.find();
        System.out.println(b);
    }


}

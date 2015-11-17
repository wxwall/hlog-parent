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
 * 预处理抽象父类,下面可以有不同的实现:</br>
 * >ASM
 * >javassit
 * Created by chenfeng on 2015/4/23.
 */
public abstract class AbstractPreProcessor implements IHLogPreProcessor {


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

}

package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;

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

    public AbstractPreProcessor(){

        excludeMethods.add("main");
        excludeMethods.add("equals");
        excludeMethods.add("toString");
        excludeMethods.add("hashCode");
        excludeMethods.add("getClass");
        excludeMethods.add("notifyAll");
        excludeMethods.add("notify");
        excludeMethods.add("wait");

        excludeMethodRegulars.add("^[s|g]et[A-Z]{1}.*");

        //加载配置文件的排除信息

        String excludePaths = HLogConfig.getInstance()
                .getProperty(Constants.KEY_HLOG_EXCLUDE_PATHS);
        if(excludePaths!=null){
            String[] excludePathArray = excludePaths.split(",");
            for (String ep : excludePathArray){
                if(!excludePathRegulars.contains(ep)){
                    excludePathRegulars.add(ep);
                }
            }
        }
    }

    /**
     * 判断方法是否在排除范围
     * @param name
     * @return
     */
    protected boolean isExcludeMethod(String name){
        boolean b ;
        b = excludeMethods.contains(name);
        if(!b){
            for(String p : excludeMethodRegulars){
                Pattern pattern = Pattern.compile(p);
                Matcher matcher = pattern.matcher(name);
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


}

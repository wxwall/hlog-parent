package com.asiainfo.hlog.agent;

import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 排除规则工具类</br>
 * 排除需要扫描的植入字节码的路径(package#class.method)
 * 排除方法
 * Created by chenfeng on 2015/6/26.
 */
public abstract class ExcludeRuleUtils {
    /**
     * 排除方法
     */
    private static Set<String> excludeMethods = new HashSet<String>();

    /**
     * 排除方法表达式
     */
    private static Set<String> excludeMethodRegulars = new HashSet<String>();

    /**
     * 排除路径表达式
     */
    private static Set<String> excludePathRegulars = new HashSet<String>();

    static {
        excludeMethods.add("main");
        excludeMethods.add("equals");
        excludeMethods.add("toString");
        excludeMethods.add("hashCode");
        excludeMethods.add("getClass");
        excludeMethods.add("notifyAll");
        excludeMethods.add("notify");
        excludeMethods.add("wait");

        //排除get/set方法
        //excludeMethodRegulars.add(".*\\.[s|g]et[A-Z].*");

        //加载配置文件的方法排除信息
        String excludeMethodCfg = HLogConfig.getInstance()
                .getProperty(Constants.KEY_HLOG_EXCLUDE_METHODS);

        if(excludeMethodCfg!=null){
            addRules(excludeMethodCfg,excludeMethodRegulars);
        }
        if(Logger.isDebug()){
            Logger.debug("配置方法级别的排除规则:{0}",excludeMethodRegulars);
        }

        //加载配置文件的排除信息
        addRules("java.lang.*,sun.*",excludePathRegulars);
        //排除类
        String excludePaths = HLogConfig.getInstance()
                .getProperty(Constants.KEY_HLOG_EXCLUDE_PATHS);
        if(excludePaths!=null){
            addRules(excludePaths,excludePathRegulars);
        }
        if(Logger.isDebug()){
            Logger.debug("配置文件路径的排除规则:{0}",excludePathRegulars);
        }
    }


    private static void addRules(String rules,Set<String> set){
        String[] ruleArray = rules.split(",");
        for (String ep : ruleArray){
            if(!set.contains(ep)){
                set.add(ep);
            }
        }
    }


    public static boolean isGetOrSetMethod(String methodName,String desc){
        if(methodName.startsWith("get") || methodName.startsWith("is")){
            int index = methodName.startsWith("is")?2:3;
            int length = methodName.length();
            if(length==index){
                return  false;
            }
            char four = methodName.charAt(index);
            if(65>four || four>91){
                return false;
            }
            if(!desc.startsWith("()")){
                return false;
            }
            if(desc.endsWith("V")){
                return false;
            }
            return true;
            /*
            String get= "(^get[A-Z]{1})\\D+\\(\\)([I|Z|B|C|S|D|F|J]|(L\\D+;))$";
            String str = methodName+desc;
            Pattern pattern = Pattern.compile(get);
            Matcher matcher = pattern.matcher(str);
            return matcher.find();
            */
        }else if(methodName.startsWith("set")){
            /*
            String set= "(^set[A-Z]{1})\\D+\\(([I|Z|B|C|S|D|F|J]|(L\\D+;?))\\)V$";
            String str = methodName+desc;
            Pattern pattern = Pattern.compile(set);
            Matcher matcher = pattern.matcher(str);
            boolean b = matcher.find();*/
            int length = methodName.length();
            if(length==3){
                return  false;
            }
            char four = methodName.charAt(3);
            if(65>four || four>91){
                return false;
            }
            int descLength = desc.length();
            int size = 0;
            for (int i = 0; i < descLength; i++) {
                if(desc.charAt(i)==';'){
                    size ++;
                }
                if(size>1){
                    return false;
                }
            }
            if(!desc.endsWith("V")){
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * 判断方法是否在排除范围
     * @param className
     * @param methodName
     * @return
     */
    public static boolean isExcludeMethod(String className,String methodName){
        boolean b ;
        b = excludeMethods.contains(methodName);

        if(!b){
            String full = className+"."+methodName;
            b = isExclude(full,excludeMethodRegulars);
        }
        if(b && Logger.isTrace()){
            Logger.trace("判断类{0}.{1}是否在排除范围:{2}",className,methodName,b);
        }
        return b;
    }

    /**
     * 判断类路径是否在排除范围
     * @param name
     * @return
     */
    public static boolean isExcludePath(String name){
        boolean b = isExclude(name,excludePathRegulars);
        if(b && Logger.isTrace()){
            Logger.trace("判断类{0}是否排除范围:{1}",name,b);
        }
        return b;
    }

    private static boolean isExclude(String name,Set<String> excludeRegulars) {
        boolean b = false;
        for(String p : excludeRegulars){
            Pattern pattern = Pattern.compile(p);
            Matcher matcher = pattern.matcher(name);
            b= matcher.matches();
            if(b){
                break;
            }
        }
        return b;
    }

    public static void main(String[] args) {
        String pp= "(^get[A-Z]{1})\\D+\\(\\)([I|Z|B|C|S|D|F|J]|(L\\D+;))$";
        String str = "getAfdfdfFs()Ljava/lang/String;";
        Pattern pattern = Pattern.compile(pp);
        Matcher matcher = pattern.matcher(str);
        boolean b = matcher.find();
        System.out.println(b);

        pp= "(^set[A-Z]{1})\\D+\\(([I|Z|B|C|S|D|F|J]|(L\\D+;?))\\)V$";
        str = "setAfdfdfFs(Ljava/lang/String;Ljava/lang/String;)V";
        pattern = Pattern.compile(pp);
        matcher = pattern.matcher(str);
        b = matcher.find();
        System.out.println(b);

        boolean isGetOrSet = isGetOrSetMethod("setAfdfdfFs","(I)V");
        System.out.println(isGetOrSet);
        isGetOrSet = isGetOrSetMethod("isAfdfdfFs","()Ljava/lang/String;");
        System.out.println(isGetOrSet);

        System.out.println((int)'A');

        String cl1 = "com.al.ec.sm.smo.impl.StaffManageSMOImpl$$EnhancerByCGLIB$$6ac3feb6";
        //"$$EnhancerByCGLIB$$"
        System.out.println(cl1.indexOf("$EnhancerByCGLIB$"));
        String p2 = ".*\\$EnhancerByCGLIB+.*";
        String[] ps = p2.split(",");
        System.out.println(ps.length);
        System.out.println(ps[0]);
        pattern = Pattern.compile(ps[0]);
        matcher = pattern.matcher(cl1);
        b = matcher.matches();
        System.out.println(b);

        String cl2 = "com.al.crm.test.test2.dto.te";

        pattern = Pattern.compile("com.al.crm.*.dto.*");
        matcher = pattern.matcher(cl2);
        b = matcher.matches();
        System.out.println(b);

    }
}

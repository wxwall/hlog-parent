package com.asiainfo.hlog.client.helper;

import com.asiainfo.hlog.client.config.Constants;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志输出,只能在跟踪调试</br>
 * 日志级别设置：-DhlogLevel=debug
 * Created by chenfeng on 2015/4/21.
 */
public abstract class Logger {

    private static Map<String,Integer> levels = new HashMap<String,Integer>(5);

    private static String evn_level = System.getProperty(Constants.SYS_KEY_HLOG_LEVEL,"node");

    private static Integer i_evn_level = 0 ;

    static {
        levels.put("none",0);
        levels.put("error",1);
        levels.put("warn",2);
        levels.put("info",3);
        levels.put("debug",4);
        levels.put("trace",5);

        if(levels.containsKey(evn_level)){
            i_evn_level = levels.get(evn_level);
        }

    }

    public final static PrintStream out = System.out;

    public final static PrintStream err = System.err;

    private static void outprint(String level,String msg,Object ... objects){

        outprint(level,msg,null,objects);
    }

    public static boolean canOutprint(String curLevel,String setLevel){
        if(curLevel==null || !levels.containsKey(curLevel)){
            return false;
        }
        if(setLevel==null || !levels.containsKey(setLevel)){
            return false;
        }
        int il1 = levels.get(curLevel);
        int il2 = levels.get(setLevel);
        if(il2<il1){
            return false;
        }
        return true;
    }

    private static boolean canOutprint(int cur_level){
        if(i_evn_level<cur_level){
            return false;
        }
        return true;
    }

    private static void outprint(String level,String msg,Throwable t,Object ... objects){

        int cur_level = levels.get(level);
        if(!canOutprint(cur_level)){
            return;
        }

        PrintStream localOut = out;

        if(cur_level==1 || cur_level==2){
            localOut = err;
        }

        if(msg!=null){
            if(objects==null){
                localOut.println(msg);
            }else{
                String formatMs = MessageFormat.format(msg, objects);
                localOut.println(formatMs);
            }
        }
        if(t!=null){
            t.printStackTrace();
        }
    }


    public static boolean isTrace(){
        return canOutprint(5);
    }
    public static boolean isDebug(){
        return canOutprint(4);
    }
    public static boolean isInfo(){
        return canOutprint(3);
    }
    public static boolean isWarm(){
        return canOutprint(2);
    }
    public static boolean isError(){
        return canOutprint(1);
    }

    public static void trace(String msg,Object ... objects){
        outprint("trace",msg,objects);
    }

    public static void debug(String msg,Object ... objects){
        outprint("debug",msg,objects);
    }

    public static void info(String msg,Object ... objects){
        outprint("info",msg,objects);
    }

    public static void warn(String msg,Object ... objects){
        outprint("warn",msg,objects);
    }

    public static void error(Throwable t){
        error(null,t);
    }
    public static void error(String msg,Throwable t){
        error(msg, t,null);
    }
    public static void error(String msg,Throwable t,Object ... objects){
        outprint("error",msg,t,objects);
    }
}

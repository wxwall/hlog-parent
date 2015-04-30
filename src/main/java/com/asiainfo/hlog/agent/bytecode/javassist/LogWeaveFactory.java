package com.asiainfo.hlog.agent.bytecode.javassist;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by c on 2015/4/10.
 */
public class LogWeaveFactory {

    private static LogWeaveFactory inst ;

    private Map<String,String> logWeaveName = new HashMap<String ,String>();

    private Map<String,ILogWeave> logWeaveInst = new HashMap<String ,ILogWeave>();

    private LogWeaveFactory(){
        ILogWeave weave1 = new LogIdWeave();
        logWeaveName.put(weave1.getName(),LogIdWeave.class.getName());
        logWeaveInst.put(weave1.getName(),weave1);

        ILogWeave weave2 = new RunningProcessesLogWeave();
        logWeaveName.put(weave2.getName(),RunningProcessesLogWeave.class.getName());
        logWeaveInst.put(weave2.getName(),weave2);

        ILogWeave weave3 = new ErrorLogWeave();
        logWeaveName.put(weave3.getName(),ErrorLogWeave.class.getName());
        logWeaveInst.put(weave3.getName(),weave3);

        ILogWeave weave4 = new LoggerLogWeave();
        logWeaveName.put(weave4.getName(),LoggerLogWeave.class.getName());
        logWeaveInst.put(weave4.getName(),weave4);
    }

    public static LogWeaveFactory getInstance(){
        synchronized (LogWeaveFactory.class){
            if (inst==null){
                inst = new LogWeaveFactory();
            }
        }
        return inst;
    }

    public synchronized ILogWeave getLogWeave(String name){

        if(logWeaveInst.containsKey(name)){
            return logWeaveInst.get(name);
        }

        String className = logWeaveName.get(name);

        if(className==null){
            //TODO 如何来提示
            return null;
        }

        try{
            ILogWeave weave = (ILogWeave)Class.forName(className).newInstance();
            logWeaveInst.put(name,weave);
            return weave;
        }catch (Throwable t){
            t.printStackTrace();
        }

        return null;
    }

}

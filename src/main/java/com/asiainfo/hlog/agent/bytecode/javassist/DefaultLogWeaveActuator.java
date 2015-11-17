package com.asiainfo.hlog.agent.bytecode.javassist;

import java.util.List;

/**
 * 组装代码段
 * Created by chenfeng on 2015/3/23.
 */
public class DefaultLogWeaveActuator implements ILogWeaveActuator {

    public LogWeaveCode executeWeave(LogWeaveContext logWeaveContext,List<ILogWeave> logWeaves) {
        if(logWeaves==null || logWeaves.size()==0){
            return null;
        }
        LogWeaveCode logWeaveCode = new LogWeaveCode();
        for (ILogWeave logWeave : logWeaves){
            logWeaveCode.append(LogWeaveCode.BEFORE,logWeave.beforWeave(logWeaveContext));
            logWeaveCode.append(LogWeaveCode.EXCEPTION,logWeave.exceptionWeave(logWeaveContext));
            logWeaveCode.append(LogWeaveCode.AFTER,logWeave.afterWeave(logWeaveContext));
            logWeaveCode.append(LogWeaveCode.FINALLY,logWeave.finallyWeave(logWeaveContext));
            if(logWeave.interrupt()){
                logWeaveCode.setInterrupt(logWeave.interrupt());
            }
        }

        return logWeaveCode;
    }
}

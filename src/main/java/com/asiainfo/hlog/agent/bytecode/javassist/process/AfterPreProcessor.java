package com.asiainfo.hlog.agent.bytecode.javassist.process;

import com.asiainfo.hlog.agent.bytecode.javassist.LogWeaveCode;
import com.asiainfo.hlog.agent.bytecode.javassist.LogWeaveContext;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Created by chenfeng on 2015/4/14.
 */
public class AfterPreProcessor implements IMethodPreProcessor {

    public CtMethod preProcessor(CtClass ctClass,CtMethod method,
                                 LogWeaveContext logWeaveContext ,LogWeaveCode logWeaveCode)
            throws Exception {

        if(method==null)
            return method;


        method.insertAfter(logWeaveCode.getAfterCode().toString());
        /*
        for (ILogWeave logWeave : logWeaves){
            method.insertAfter(logWeave.beforWeave(logWeaveContext));
        }
        */
        return null;
    }

}

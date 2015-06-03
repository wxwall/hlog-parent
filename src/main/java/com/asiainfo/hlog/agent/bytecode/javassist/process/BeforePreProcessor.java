package com.asiainfo.hlog.agent.bytecode.javassist.process;

import com.asiainfo.hlog.agent.bytecode.javassist.ILogWeave;
import com.asiainfo.hlog.agent.bytecode.javassist.ILogWeaveActuator;
import com.asiainfo.hlog.agent.bytecode.javassist.LogWeaveCode;
import com.asiainfo.hlog.agent.bytecode.javassist.LogWeaveContext;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.List;

/**
 * Created by chenfeng on 2015/4/14.
 */
public class BeforePreProcessor implements IMethodPreProcessor {

    public CtMethod preProcessor(CtClass ctClass,CtMethod method,
                                 LogWeaveContext logWeaveContext ,LogWeaveCode logWeaveCode)
            throws Exception {
        if(method==null || ctClass.isFrozen())
            return method;

        method.insertBefore(logWeaveCode.getBeforeCode().toString());
        /*
        for (ILogWeave logWeave : logWeaves){
            method.insertBefore(logWeave.beforWeave(logWeaveContext));
        }
        */
        //method.insertAfter(logWeaveCode.getAfterCode().toString());

        return null;
    }
}

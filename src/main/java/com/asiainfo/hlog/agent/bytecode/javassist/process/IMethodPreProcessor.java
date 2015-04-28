package com.asiainfo.hlog.agent.bytecode.javassist.process;

import com.asiainfo.hlog.agent.bytecode.javassist.LogWeaveCode;
import com.asiainfo.hlog.agent.bytecode.javassist.LogWeaveContext;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Created by chenfeng on 2015/4/14.
 */
public interface IMethodPreProcessor {
    /**
     * 在方法最前端
     */
    int BEFORE_TYPE = 1;

    /**
     * 在方法返回之前
     */
    int OFTER_TYPE = 3;

    /**
     * 在方法最前端和方法返回之前
     */
    int BEFORE_OFTER_TYPE = 7;

    /**
     * 在环绕处
     */
    int ROUND_TYPE = 15;

    CtMethod preProcessor(CtClass ctClass,CtMethod method,
                          LogWeaveContext logWeaveContext ,LogWeaveCode logWeaveCode) throws Exception;

}

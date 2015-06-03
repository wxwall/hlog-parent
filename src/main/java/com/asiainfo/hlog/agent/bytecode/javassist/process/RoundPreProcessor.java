package com.asiainfo.hlog.agent.bytecode.javassist.process;

import com.asiainfo.hlog.agent.bytecode.javassist.LogWeaveCode;
import com.asiainfo.hlog.agent.bytecode.javassist.LogWeaveContext;
import com.asiainfo.hlog.agent.runtime.LogAgentContext;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by chenfeng on 2015/4/14.
 */
public class RoundPreProcessor implements IMethodPreProcessor {

    //private CtClass exClass = null;

    public RoundPreProcessor(){
        /*
        try {
            exClass = ClassPool.getDefault().get("java.lang.Exception");
        } catch (NotFoundException e) {
            e.printStackTrace();
        }*/
    }


    public CtMethod preProcessor(CtClass ctClass,CtMethod method,
                                 LogWeaveContext logWeaveContext ,LogWeaveCode logWeaveCode)
            throws Exception {
        if(method==null || ctClass.isFrozen())
            return method;

        String methodName = method.getName();

        String oldMethodName = methodName+"$$"+logWeaveContext.getHashCode() ;
        method.setName(oldMethodName);


        CtMethod logAgentMethod = CtNewMethod.copy(method, methodName, ctClass, null);

        //转移方法和入参的Annotation信息
        List<AttributeInfo> attrs = method.getMethodInfo().getAttributes();
        if(attrs!=null){
            ListIterator iterator = attrs.listIterator();
            while (iterator.hasNext()) {
                AttributeInfo attr = (AttributeInfo)iterator.next();
                String name = attr.getName();
                if(name.equals(ParameterAnnotationsAttribute.visibleTag)
                        || name.equals(AnnotationsAttribute.visibleTag)){
                    logAgentMethod.getMethodInfo().addAttribute(attr);
                    iterator.remove();
                }
            }
        }
        //method.getAvailableParameterAnnotations()
        //method.getMethodInfo().removeCodeAttribute();

        StringBuffer bufferCode = new StringBuffer();
        bufferCode.append("{ ");

        bufferCode.append(logWeaveCode.getBeforeCode().toString());

        bufferCode.append("try{");

        bufferCode.append(logWeaveCode.getTryCode().toString());

        CtClass returnCtClass = method.getReturnType();
        boolean haveReturn = !"void".equals(returnCtClass.getName());
        if(haveReturn){
            bufferCode.append(" Object _reObj =  ");
        }
        bufferCode.append(oldMethodName + "($$);\n");

        bufferCode.append(logWeaveCode.getAfterCode().toString());
        if(haveReturn){
            bufferCode.append("return _reObj;");
        }

        bufferCode.append("}catch(Throwable ").append(LogAgentContext.S_AGENT_ERR_PARAM_NAME).append("){");
        bufferCode.append(logWeaveCode.getExceptionCode());
        bufferCode.append("throw ").append(LogAgentContext.S_AGENT_ERR_PARAM_NAME).append(";");
        bufferCode.append("}finally{");
        bufferCode.append(logWeaveCode.getFinallyCode());
        bufferCode.append("}");
        if(!"void".equals(returnCtClass.getName())){
            bufferCode.append(" return null;");
        }
        bufferCode.append("}");

        logAgentMethod.setBody(bufferCode.toString());
        ctClass.addMethod(logAgentMethod);

        /*

        //增加try之前的代码块
        method.insertBefore(logWeaveCode.getTryCode().toString());

        //-----------------创建一个异常块-----------------
        StringBuffer exCode = logWeaveCode.getExceptionCode();
        exCode.append(" throw $e");
        method.addCatch(exCode.toString(),exClass);
        //-----------------异常块结束-----------------

        //-----------------增加前置代码块-----------------
        method.insertBefore(logWeaveCode.getBeforeCode().toString());

        //-----------------增加finally代码块-----------------
        method.insertAfter(logWeaveCode.getAfterCode().toString(),true);


        */

        return logAgentMethod;
    }
}

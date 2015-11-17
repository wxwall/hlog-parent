package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

/**
 * Created by chenfeng on 2015/4/30.
 */
public abstract class AbstractLogWeave implements ILogWeave {

    public static final String Q = "\"";
    public static final String D = ",";
    public static final String S = ";";
    public static final String E = " = ";
    public static final String BL = "(";
    public static final String BR = ")";

    protected StringBuilder getInParams(LogWeaveContext logWeaveContext,StringBuilder codeBuffer,boolean setName){

        if(!logWeaveContext.isCreateInParams()){
            codeBuffer.append("_inParams = new com.asiainfo.hlog.client.model.ParamObjs("+logWeaveContext.getParamNumber()+");");
            String[] names = logWeaveContext.getParamNames();
            for(int i=1;i<=logWeaveContext.getParamNumber();i++){
                codeBuffer.append("_inParams.addParam("+(i-1)+",$"+i+");");
                if(setName){
                    codeBuffer.append("_inParams.addParamName("+(i-1)+",\""+names[i-1]+"\");");
                }
            }
            logWeaveContext.setCreateInParams(true);
        }
        return codeBuffer;
    }

    /**
     * 构建基本日志对象信息:</br>
     * 1、日志ID</br>
     * 2、日志归属ID</br>
     * 3、日志组ID</br>
     * 4、日志类型</br>
     * @param logWeaveContext
     * @param type
     * @param codeBuffer
     */
    protected StringBuilder buildBaseLogData(LogWeaveContext logWeaveContext,String type,String desc,StringBuilder codeBuffer){
        codeBuffer.append("data.setMc(\"").append(type).append("\");");
        codeBuffer.append("data.setId("+ LogAgentContext.S_AGENT_LOG_ID+");");
        codeBuffer.append("data.setPId("+ LogAgentContext.S_AGENT_LOG_PID+");");
        codeBuffer.append("data.setGId(com.asiainfo.hlog.agent.runtime.LogAgentContext.getThreadLogGroupId());");
        codeBuffer.append("data.setTime(System.currentTimeMillis());");
        if(desc!=null){
            codeBuffer.append("data.setDesc(").append(desc).append(");");
        }
        return codeBuffer;
    }

    protected StringBuilder buildReveiceEvent(String className,String methodName,StringBuilder codeBuffer){
        codeBuffer.append("com.asiainfo.hlog.client.model.Event event = new com.asiainfo.hlog.client.model.Event();");
        codeBuffer.append("event.setClassName(\"" + className + "\");");
        codeBuffer.append("event.setMethodName(\"" + methodName + "\");");
        codeBuffer.append("event.setData(data);");
        codeBuffer.append("com.asiainfo.hlog.client.HLogReflex.reveice(event);");
        return codeBuffer;
    }
    protected StringBuilder buildReveiceEvent(StringBuilder codeBuffer){
        codeBuffer.append("com.asiainfo.hlog.client.model.Event event = new com.asiainfo.hlog.client.model.Event();");
        codeBuffer.append("event.setClassName(name);");
        codeBuffer.append("event.setMethodName(null);");
        codeBuffer.append("event.setData(data);");
        codeBuffer.append("com.asiainfo.hlog.client.HLogReflex.reveice(event);");
        return codeBuffer;
    }

    /**
     * 返回管理编码
     * @return
     */
    protected abstract String getMcode();

    protected String getMcode(LogWeaveContext logWeaveContext){
        String mcode = logWeaveContext.getRule().getMcodeMap().get(getName());
        if(mcode==null){
            mcode = getMcode();
        }

        if(mcode==null || mcode.trim().length()==0){
            mcode="h99";
        }
        return mcode;
    }

    @Override
    public boolean interrupt() {
        return false;
    }
}

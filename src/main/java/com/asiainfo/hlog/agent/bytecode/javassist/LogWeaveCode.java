package com.asiainfo.hlog.agent.bytecode.javassist;

import com.asiainfo.hlog.agent.bytecode.javassist.process.IMethodPreProcessor;

/**
 * 组织生成需要植入的java代码
 * Created by chenfeng on 2015/3/17.
 */
public class LogWeaveCode {

    public final static int BEFORE = 0;

    public final static int TRY = 1;

    public final static int AFTER = 2;

    public final static int EXCEPTION = 3;

    public final static int FINALLY = 4;

    private StringBuffer beforeCode ;

    private StringBuffer exceptionCode;

    private StringBuffer afterCode;

    private StringBuffer tryCode ;

    private StringBuffer finallyCode ;

    private int type ;

    private boolean interrupt;

    public LogWeaveCode(){
        this.beforeCode = new StringBuffer();
        this.exceptionCode = new StringBuffer();
        this.afterCode = new StringBuffer();
        this.tryCode = new StringBuffer();
        this.finallyCode = new StringBuffer();
    }

    public int getType() {
        return type;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public StringBuffer getBeforeCode() {
        return beforeCode;
    }

    public StringBuffer getTryCode() {
        return tryCode;
    }

    public StringBuffer getAfterCode() {
        return afterCode;
    }

    public StringBuffer getExceptionCode() {
        return exceptionCode;
    }

    public StringBuffer getFinallyCode() {
        return finallyCode;
    }

    public void append(int position,String code){
        if(code==null || code.trim().length()==0){
            return ;
        }
        switch (position){
            case BEFORE:
                type = type | IMethodPreProcessor.BEFORE_TYPE;
                this.beforeCode.append(code);
                break;
            case TRY:
                type = type | IMethodPreProcessor.ROUND_TYPE;
                this.tryCode.append(code);
                break;
            case EXCEPTION:
                type = type | IMethodPreProcessor.ROUND_TYPE;
                this.exceptionCode.append(code);
                break;
            case AFTER:
                if(type==IMethodPreProcessor.BEFORE_TYPE){
                    type = IMethodPreProcessor.BEFORE_OFTER_TYPE;
                }else{
                    type = type | IMethodPreProcessor.BEFORE_TYPE;
                }
                this.afterCode.append(code);
                break;
            case FINALLY:
                type = type | IMethodPreProcessor.ROUND_TYPE;
                this.finallyCode.append(code);
                break;
        }
    }
}

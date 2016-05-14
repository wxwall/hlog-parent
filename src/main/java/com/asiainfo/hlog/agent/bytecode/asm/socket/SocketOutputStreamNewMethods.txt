package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.agent.runtime.LogAgentContext;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by chenfeng on 2016/4/22.
 */
public class TestASM {

    boolean isWrited = false;
    private void socketWrite0(FileDescriptor fd, byte[] b, int off,
                                     int len) throws IOException{
        System.out.print(new String(b, off, len));
    }
    public String _getLogId(String method){
        String _id = null;
        try{
            if(method.equals("getThreadCurrentLogId")){
                _id =  LogAgentContext.getThreadCurrentLogId();
            }else{
                _id = LogAgentContext.getThreadLogGroupId();
            }
        }catch (Throwable t){
            try{
                _id = (String)Thread.currentThread().getContextClassLoader()
                        .loadClass("com.asiainfo.hlog.agent.runtime.LogAgentContext")
                        .getMethod(method).invoke(null,new Object[0]);
            }catch (Throwable t1){
            }
        }
        return _id;
    }

    public void test(FileDescriptor fd,byte b[], int off, int len) throws IOException {
        System.out.println(getClass().getClassLoader());
        socketWrite0(fd, b, off, len);
    }

    private void _socketWrite(FileDescriptor fd,byte b[], int off, int len) throws IOException {
        if(!isWrited && len>6){
            boolean isHttp = false;
            isWrited = true;
            String gid = _getLogId("getThreadLogGroupId");
            if(gid==null){
                socketWrite0(fd,b,off,len);
                return ;
            }
            for (int index=off;index<len;index++){
                if(!isHttp && b[index]==32){
                    isHttp = isHttpProtocol(b,index);
                    if(!isHttp){
                        break ;
                    }
                }else if(isHttp && b[index]==10 && b[index-1]==13){
                    int newIndex = index+1;
                    //1、发关前部分
                    socketWrite0(fd,b,0,newIndex);
                    String pid = _getLogId("getThreadCurrentLogId");
                    //2、追加Hlog头
                    StringBuilder head = new StringBuilder();
                    head.append("Hlog-Agent-Gid:").append(gid);
                    if(pid!=null){
                        head.append("\r\n").append("Hlog-Agent-Pid:").append(pid);
                    }
                    byte[] headBytes = head.toString().getBytes();
                    socketWrite0(fd,headBytes,0,headBytes.length);
                    //再追加剩余内容
                    socketWrite0(fd,b,newIndex,b.length-newIndex);
                    //返回
                    return ;
                }

            }
        }
        //发送原内容
        socketWrite0(fd,b,off,len);
    }
    private boolean isHttpProtocol(byte b[],int len){
        switch (len){
            case 3:
                if(b[0]==71 && b[1]==69 && b[2]==84){
                    return true;
                }
                if(b[0]==80 && b[1]==85 && b[2]==84){
                    return true;
                }
                break;
            case 4:
                return b[0]==80 && b[1]==79 && b[2]==83 && b[3]==84;
            case 6:
                return b[0]==68 && b[1]==69 && b[2]==76 && b[3]==69 && b[4]==84 && b[5]==69;
        }
        return false;
    }

    private void test2(int id1,long start){
        long _start = System.currentTimeMillis();
    }
    private void test3(){
        long start = 0;
        test2(12,start);
    }

    public static void main(String[] args) throws IOException {
        TestASM testASM = new TestASM();
        byte[] b = "GET sssssssssssss\r\nTest:test\r\nName:flll\r\n\nttttttttttttttttttt11".getBytes();
        testASM._socketWrite(null,b,0,b.length);
    }
}

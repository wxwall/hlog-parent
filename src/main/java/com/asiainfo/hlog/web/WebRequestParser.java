package com.asiainfo.hlog.web;

import com.asiainfo.hlog.client.config.Constants;
import com.asiainfo.hlog.client.config.HLogConfig;
import com.asiainfo.hlog.client.helper.Logger;
import com.asiainfo.hlog.comm.ResourceUtils;

import java.io.*;

/**
 * Created by lenovo on 2016/9/13.
 */
public class WebRequestParser {
    private static int len = "/hlogweb/action/".length();
    public static void parserRequest(HLogHttpRequest req, HLogHttpResponse resp){
        String out = null;
        try {
            String uri = req.getRequestURI();
            String action = uri.substring(uri.indexOf("/hlogweb/action/")+len);
            if(action.equals("files")){
                out = getFileNameList();
                out = out.replace(Constants.FIEL_NAME_HLOG_CONFS+",","");
                out = Constants.FIEL_NAME_HLOG_CONFS+"," + out;
            }else if(action.equals("config")){
                out = configContent(req.getParameter("file"));
            }else if(action.equals("save")){
                String body = readBodyText(req);
                writeFile(req.getParameter("file"),body);
                out = "保存成功";
            }else{
                out = "-1";
            }
            resp.setContentType("text/plain; charset=utf-8");
            resp.getWriter().write(out);
            //
        }catch (Exception e){
            Logger.error("文件不存在",e);
        }
    }

    private static String readBodyText(HLogHttpRequest req) throws  Exception{
        BufferedReader br = null;
        try {
            br =  req.getReader();
            String tmp;
            StringBuilder out = new StringBuilder();
            while((tmp = br.readLine()) != null){
                out.append(tmp).append("\r\n");
            }

            return out.toString();
        }catch (Exception e){
            throw  e;
        }finally {
            if(br!=null){
                br.close();
            }
        }
    }

    private static String getFileNameList(){
        String dir = HLogConfig.getInstance().getHLogAgentDir();
        File file=new File(dir);
        File[] tempList = file.listFiles();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < tempList.length; i++){
            if(tempList[i].isFile() && !tempList[i].getName().endsWith(".jar")){
                sb.append(tempList[i].getName()).append(",");
            }
        }
        return sb.toString();
    }

    private static String configContent(String fileName){
        String dir = HLogConfig.getInstance().getHLogAgentDir();
        try {
            String text = ResourceUtils.read(new FileInputStream(new File(dir+fileName)));
            return text;
        } catch (FileNotFoundException e) {
            Logger.error("文件不存在",e);
        }catch (Exception e) {
            Logger.error("读取文件出错",e);
        }
        return null;
    }

    private static void writeFile(String fileName,String content) throws Exception{
        OutputStreamWriter out = null;
        FileOutputStream fos = null;
        try{
            String filePath = HLogConfig.getInstance().getHLogAgentDir()+fileName;
            File file =new File(filePath);
            if(!file.exists()){
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            out = new OutputStreamWriter(fos,"UTF-8");
            out.write(content);
            out.flush();
        }catch(Exception e){
            throw  e;
        }finally {
            if(fos!=null){
                fos.close();
            }
            if(out!=null){
                out.close();
            }
        }
    }
}

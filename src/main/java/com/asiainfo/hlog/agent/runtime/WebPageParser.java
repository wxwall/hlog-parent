package com.asiainfo.hlog.agent.runtime;

import com.asiainfo.hlog.comm.ResourceUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lenovo on 2016/9/9.
 */
public class WebPageParser {

    public static void returnResourceFile(String fileName, String uri, HttpServletResponse response)
            throws ServletException,
            IOException {

        String filePath = "/web/"+fileName;

        if (filePath.endsWith(".html")) {
            response.setContentType("text/html; charset=utf-8");
        }
        if (fileName.endsWith(".jpg")||fileName.endsWith(".png")||fileName.endsWith(".gif")||fileName.endsWith(".ico")) {
            byte[] bytes = ResourceUtils.readByteArrayFromResource(filePath);
            if (bytes != null) {
                response.getOutputStream().write(bytes);
            }

            return;
        }

        String text = ResourceUtils.readFromResource(filePath);
        if (text == null) {
            response.sendRedirect(uri + "/index.html");
            return;
        }
        if (fileName.endsWith(".css")) {
            response.setContentType("text/css;charset=utf-8");
        } else if (fileName.endsWith(".js")) {
            response.setContentType("text/javascript;charset=utf-8");
        }
        response.getWriter().write(text);
    }
}

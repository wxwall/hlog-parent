package com.asiainfo.hlog.agent.classloader;

import com.asiainfo.hlog.client.helper.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * 自定义类加载器,主要职责让agent端加载和使用第三方的jar</br>
 *
 * Created by chenf on 2015/4/9.
 */
public class ClassLoaderHolder  {

    public static final String tmpdir = System.getProperty("java.io.tmpdir");

    private static  ClassLoader loader = null;

    private static ClassLoaderHolder holder;

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loader.loadClass(name);
    }

    public ClassLoader getParent(){
        return Thread.currentThread().getContextClassLoader();
    }

    public ClassLoader getClassLoader(){
        return this.loader;
    }

    private ClassLoaderHolder(){
        String libs = tmpdir + File.separator + "log-agent";
        File libsFile = new File(libs);
        libsFile.mkdirs();
        URL url = ClassLoaderHolder.class.getResource("");
        if (url != null) {
            String agentJarPath = url.getFile();
            int index = agentJarPath.indexOf("!");
            if(index>-1){
                agentJarPath = agentJarPath.substring(5, agentJarPath.indexOf("!"));
            }
            String jarDir = null;
            JarFile agentJar = null;
            try {
                File jar = new File(agentJarPath);
                jarDir = jar.getParent();
                agentJar = new JarFile(agentJarPath);
                Enumeration<JarEntry> jarEntrys = agentJar.entries();
                List<URL> jarList = new ArrayList<URL>();
                jarList.add(jar.toURI().toURL());
                byte[] bytes = new byte[1024];
                while (jarEntrys.hasMoreElements()) {
                    BufferedInputStream in = null;
                    BufferedOutputStream out = null;
                    File desTemp = null;
                    try {
                        ZipEntry entryTemp = jarEntrys.nextElement();

                        if (entryTemp.isDirectory() || !entryTemp.getName().endsWith(".jar")) {
                            continue;
                        }
                        desTemp = new File(libs + File.separator + entryTemp.getName());

                        if (!desTemp.getParentFile().exists()) {
                            desTemp.getParentFile().mkdirs();
                        }
                        in = new BufferedInputStream(agentJar.getInputStream(entryTemp));
                        out = new BufferedOutputStream(new FileOutputStream(desTemp));
                        int len = in.read(bytes, 0, bytes.length);
                        while (len != -1) {
                            out.write(bytes, 0, len);
                            len = in.read(bytes, 0, bytes.length);
                        }
                        jarList.add(desTemp.toURI().toURL());
                    } catch (Exception ee) {
                        Logger.error("从hlog-agent中解压{0}资源失败",ee,desTemp);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (Exception ine) {
                            }
                        }
                        if (out != null) {
                            try {
                                out.flush();
                                out.close();
                            } catch (Exception ine) {
                            }
                        }
                    }

                }
                //加载扩展的jar
                File extJarFile = new File(jarDir+"/ext/lib");
                if(extJarFile.exists()){
                    File[] fiels = extJarFile.listFiles();
                    for(File file : fiels){
                        if(file.getName().endsWith(".jar")){
                            jarList.add(file.toURI().toURL());
                        }
                    }
                }
                File extClassesFile = new File(jarDir+"/ext/classes");
                if(extClassesFile.exists()){
                    jarList.add(extClassesFile.toURI().toURL());
                }

                //jarLis
                URL[] urls = jarList.toArray(new URL[jarList.size()]);
                loader = new URLClassLoader(urls, null){

                    public Class<?> loadClass(String name) throws ClassNotFoundException {
                        if(name.startsWith("com.asiainfo.hlog.client.model.")){
                            return getInstance().getParent().loadClass(name);
                        }
                        Class<?> clazz = null;
                        try{
                            clazz = super.loadClass(name);
                        }catch (ClassNotFoundException cfe){
                            if(!name.startsWith("org.slf4j")){
                                clazz = getInstance().getParent().loadClass(name);
                            }else{
                                throw  cfe;
                            }
                        }

                        return clazz;
                    }
                };
            } catch (Exception e) {
                Logger.error("从hlog-agent中获取依赖资源失败", e);
            } finally {
                try {
                    agentJar.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static ClassLoaderHolder getInstance(){

        synchronized (ClassLoaderHolder.class){
            if(holder==null){
                holder = new ClassLoaderHolder();
            }
        }

        return holder;
    }
}

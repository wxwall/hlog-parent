package com.asiainfo.hlog.agent.classloader;

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
 * Created by c on 2015/4/9.
 */
public class ClassLoaderHolder  {

    private static  ClassLoader loader = null;

    private static final String tmpdir = System.getProperty("java.io.tmpdir");

    private static ClassLoaderHolder holder;

    //private static ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();

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
        String libs = tmpdir + File.separator + "/log-agent";
        File libsFile = new File(libs);
        libsFile.mkdirs();
        URL url = ClassLoaderHolder.class.getResource("");
        if (url != null) {
            String agentJarPath = url.getFile();
            agentJarPath = agentJarPath.substring(5, agentJarPath.indexOf("!"));

            JarFile agentJar = null;
            try {
                File jar = new File(agentJarPath);
                agentJar = new JarFile(agentJarPath);
                Enumeration<JarEntry> jarEntrys = agentJar.entries();
                List<URL> jarList = new ArrayList<URL>();
                jarList.add(jar.toURI().toURL());
                byte[] bytes = new byte[1024];
                while (jarEntrys.hasMoreElements()) {
                    BufferedInputStream in = null;
                    BufferedOutputStream out = null;
                    try {
                        ZipEntry entryTemp = jarEntrys.nextElement();
                        if (entryTemp.isDirectory() || !entryTemp.getName().endsWith(".jar")) {
                            continue;
                        }
                        File desTemp = new File(libs + File.separator + entryTemp.getName());

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
                        ee.printStackTrace();
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
                e.printStackTrace();
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

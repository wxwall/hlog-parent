package com.asiainfo.hlog.client.config;

/**
 * 定义路径信息,包含下面几个信息:</br>
 * 1、包路径,如com.asiainfo.hlog;</br>
 * 2、类名称,如com.asiainfo.hlog.client.config.Path;</br>
 * 3、方法名称,如com.asiainfo.hlog.client.config.Path#test()
 * Created by chenfeng on 2015/4/23.
 */
public class Path {

    private String packageName;

    private String className;

    private String methodName;

    private PathType type;

    private Path(){
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public PathType getType() {
        return type;
    }

    public void setType(PathType type) {
        this.type = type;
    }

    public static Path build(String path){
        Path p = new Path();
        int start = path.indexOf("#");
        if(start!=-1){
            String pkg = path.substring(0,start);
            String clazz = path.substring(start+1);
            p.setPackageName(pkg);
            int end = clazz.indexOf(".");
            if(end != -1){
                p.setClassName(clazz.substring(0,end));
                p.setMethodName(clazz.substring(end+1));
                p.setType(PathType.METHOD);
            }else{
                p.setClassName(clazz);
                p.setType(PathType.CLASS);
            }

        }else{
            p.setPackageName(path);
            p.setType(PathType.PACKAGE);
        }
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Path path = (Path) o;

        if (className != null ? !className.equals(path.className) : path.className != null) return false;
        if (methodName != null ? !methodName.equals(path.methodName) : path.methodName != null) return false;
        if (!packageName.equals(path.packageName)) return false;
        if (type != path.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = packageName.hashCode();
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder(packageName);
        if(className!=null){
            buff.append("#").append(className);
        }
        if(methodName!=null){
            buff.append(".").append(methodName);
        }
        return buff.toString();
    }

}

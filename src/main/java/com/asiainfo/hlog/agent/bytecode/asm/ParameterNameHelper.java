package com.asiainfo.hlog.agent.bytecode.asm;

import com.asiainfo.hlog.org.objectweb.asm.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过ASM来获取方法的入参名称
 * Created by chenfeng on 2016/4/20.
 */
public class ParameterNameHelper {

    /**
     * 缓存已经处理过的方法信息
     * key owner-name-desc
     * val 参数名称
     */
    private final static Map<String, String[]> parameterNameMap = new ConcurrentHashMap<String, String[]>(40);

    /**
     * 标注没有入参信息
     */
    private static final String[] NULL_PARAM_NAME = new String[0];

    /**
     * 构建一个key
     * @param className
     * @param methodName
     * @param desc
     * @return
     */
    private static String getKey(String className,String methodName,String desc){
        return  className+"-"+methodName+"-"+desc;
    }

    /**
     * 获取方法的参数名称</br>
     * 需要传入方法所在类的二进制流数据、类的名称、方法名称、方法描述</br>
     * @param byteCodes
     * @param className
     * @param methodName
     * @param desc
     * @return
     */
    public static String[] getMethodParameterName(byte[] byteCodes,final String className,String methodName,String desc){
        final String key = getKey(className,methodName,desc);
        if(parameterNameMap.containsKey(key)){
            return parameterNameMap.get(key);
        }
        // 如果没有找到方法，解析该类并缓存
        ClassReader classReader = new ClassReader(byteCodes);
        // 构建匿名类来处理类的字节流
        // 这里不单只解析某个方法,是这个类下所有定义的方法
        classReader.accept(new ClassVisitor(Opcodes.ASM5) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name.charAt(0)!='<') {
                    // 除了构造函数外,其他的所有方法被遍历
                    // 通过desc来获取到参数个数
                    int argumentSize = Type.getArgumentTypes(desc).length;
                    String tmpKey = getKey(className,name,desc);
                    // 如果没有参数直接返回
                    if(argumentSize==0){
                        parameterNameMap.put(tmpKey,NULL_PARAM_NAME);
                        return null;
                    }
                    // 创建ParameterNameVisit来处理visit
                    return new ParameterNameVisit(tmpKey,argumentSize,ASMUtils.isStatic(access));
                }
                return null;
            }
        }, 0);

        return parameterNameMap.get(key);
    }

    /**
     * 遍历出指定方法所有的参数名称
     */
    static class ParameterNameVisit extends MethodVisitor{
        private int argumentSize = 0;
        private String[] argumentNames;
        private String key ;
        private boolean isStatic;
        public ParameterNameVisit(String key,int argumentSize ,boolean isStatic) {
            super(Opcodes.ASM5);
            this.key = key;
            this.isStatic = isStatic;
            this.argumentSize = argumentSize;
            if(this.argumentSize>0){
                argumentNames = new String[argumentSize];
            }
        }

        /**
         * 关注遍历到的参数,计算出是方法的入参信息并放入数组中。
         * @param name
         * @param desc
         * @param signature
         * @param start
         * @param end
         * @param index
         */
        private int paramIndex = 0;
        public void visitLocalVariable(String name, String desc, String signature,
                                       Label start, Label end, int index) {
            if(argumentSize==0){
                return;
            }
            //如果不是静态,跳过第一个参数,因为第一个为this变量.
            if(index==0 && !isStatic) {
                return;
            }
            if(paramIndex<argumentSize){
                argumentNames[paramIndex] = name;
            }
            paramIndex ++ ;
        }

        /**
         * 遍历完方法体
         */
        public void visitEnd() {
            if(argumentNames==null){
                parameterNameMap.put(key,NULL_PARAM_NAME);
            }else if(!parameterNameMap.containsKey(key)){
                parameterNameMap.put(key,argumentNames);
            }
        }
    }
}

package com.asiainfo.hlog.agent.bytecode.javassist;

/**
 * 在不同的代码位置织入不同代码块</br>
 *
 * Created by chenf on 2015/3/16.
 */
public interface ILogWeave extends java.io.Serializable {

    /**
     * 获取依赖的织入器
     * @return
     */
    String[] getDependLogWeave();

    /**
     * 返回代码织入器的名称
     * @return
     */
    String getName();

    /**
     * 排序
     * @return
     */
    int getOrder();

    /**
     * 方法前置代码块
     * public void test(){
     *     //do some business code
     * }
     *
     * public void test(){
     *     //insert beforeWeave
     *     //do some business code
     * }
     *
     * @param logWeaveContext
     * @return
     */
    String beforWeave(LogWeaveContext logWeaveContext);

    /**
     * <pre>
     * try代码前置代码块
     * public void test(){
     *     //do some business code
     *     try{
     *         //insert tryWeave
     *         //do some business code
     *     }.....
     * }
     * </pre>
     * @param logWeaveContext
     * @return
     */
    String tryWeave(LogWeaveContext logWeaveContext);
    /**
     * <pre>
     * try代码前置代码块
     * public void test(){
     *     //do some business code
     *     try{
     *         //do some business code
     *     }catch(Exception _e){
     *         //insert exceptionWeave
     *         throw _e;
     *     }
     * }
     * </pre>
     * @param logWeaveContext
     * @return
     */
    String exceptionWeave(LogWeaveContext logWeaveContext);
    /**
     * <pre>
     * 后置代码块
     * public void test(){
     *     //do some business code
     *     //insert afterWeave
     * }
     * </pre>
     * @param logWeaveContext
     * @return
     */
    String afterWeave(LogWeaveContext logWeaveContext);


    /**
     * 在finally代码块
     * @param logWeaveContext
     * @return
     */
    String finallyWeave(LogWeaveContext logWeaveContext);
}

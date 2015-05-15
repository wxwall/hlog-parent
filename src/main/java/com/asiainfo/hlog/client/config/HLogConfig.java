package com.asiainfo.hlog.client.config;

import com.asiainfo.hlog.client.helper.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * 日志配置数据信息
 * Created by c on 2015/4/10.
 */
public class HLogConfig {

    //=======================================类属性=================================
    /**
     * 按key的降序排列
     * @param map
     */
    private final Comparator<Path> treeMapComparator = new Comparator<Path>(){

        private int getTypeNum(PathType type){

            switch (type){
                case PACKAGE:
                    return 3;
                case CLASS:
                    return 2;
                default:
                    return 1;
            }

        }

        /**
         * 排序规则：method->class->package;com.asiainfo.test->com.asiainfo
         *
         * @param path1
         * @param path2
         * @return
         */
        @Override
        public int compare(Path path1, Path path2) {

            int typeNum1 = getTypeNum(path1.getType());
            int typeNum2 = getTypeNum(path2.getType());

            if(typeNum1!=typeNum2){
                return typeNum1>typeNum2?1:-1;
            }

            if(path1.getType() == path2.getType()){
                return path2.toString().compareToIgnoreCase(path1.toString());
            }

            return 0;
        }
    };


    /**
     * 随机产生一个终端码
     */
    public static String hlogCode = new String("0");


    /**
     * 维持配置数据池
     */
    private Properties configProps = null;


    private static HLogConfig instance = null;


    //=======================================从JVM参数设置的数据=================================
    //====这些参数不可动态变更
    public static final String tmpdir = System.getProperty("java.io.tmpdir");

    /**
     * 是否启用JMX,默认为true
     */
    public static final boolean jmxEnable = "true".equals(
        System.getProperty(Constants.SYS_KEY_HLOG_JMX_ENABLE, "true"));

    public static final String jmxPost =  System.getProperty(Constants.SYS_KEY_HLOG_JMX_POST, "14002");

    //public static final int defJMXPost = 14001 ;

    /**
     * 配置节点名称,可以用于系统模块同类,如受理类服务、PPM类服务,同时这个也是用于配置平台的domain
     */
    public static String hlogDomain = System.getProperty(Constants.SYS_KEY_HLOG_DOMAIN,"hlog");
    /**
     * 配置服务节点别名,如在同一台服务器上,多个节点的domain为受理(bizhall)服务节点,
     * 如果想区分节点名称可以通过serverAlias来取服务节点名称,最大长度只能为12个
     */
    public static String hlogServerAlias = System.getProperty(Constants.SYS_KEY_HLOG_SERVER_ALIAS,"hlogServ0000");


    /**
     * 特定配置名称的配置信息</br>
     * 1、文件名称 hlog-{hlogCfgName}-confs.properties</br>
     * 2、在hlog-confs.properties配置文件中可以当前key的前缀,如 {hlogCfgName}.hlog.server.</br>
     */
    public static String hlogCfgName = System.getProperty(Constants.SYS_KEY_HLOG_CFG_NAME,null);

    public static String hlogCfgNamePix = hlogCfgName+".";


    //=======================================从配置文件或配置平台猎取的数据=========================

    //----------------------------织入阶段配置----------------------------

    /**
     * 代码织入的基础包名和需要织入的代码名称,不可动态
     */
    private Map<Path,String[]> basePaths = new HashMap<Path,String[]>();


    //----------------------------运行时阶段配置----------------------------
    /**
     * 运行时的路径规则和开关配置
     */
    private Map<Path,HLogConfigRule> runtimeCaptureCofnigRule = new TreeMap<Path,HLogConfigRule>(treeMapComparator);

    /**
     * 全局开关标志，当全局开关为off时将关闭所有的捕获，如果全局为on时具体看指定子目录的开关
     */
    private boolean enable = true;

    /**
     * 运行时的路径与处理器关系
     */
    private Map<Path,Set<String>> runtimeHandlerCofnig = new TreeMap<Path,Set<String>>(treeMapComparator);

    private String serverIp;

    private HLogConfig(){

        try{
            InetAddress addr = InetAddress.getLocalHost();
            //获得本机IP
            serverIp=addr.getHostAddress().toString();
        }catch (Exception e){
            Logger.error(e);
        }
        //产生终端码 ip的hashcode +
        hlogCode = Long.toString(((Math.abs(serverIp.hashCode())+
                Math.round(Math.random() * 39999999999l + 10000000000l))+
                System.currentTimeMillis())>>16,32);
        System.out.println(hlogCode);
    }

    public static HLogConfig getInstance(){
        if(instance==null){
            synchronized (HLogConfig.class){
                if (instance==null){
                    instance = new HLogConfig();
                }
            }
        }
        return instance;
    }

    public Properties getProperties(){
        return configProps;
    }

    /**
     * 加载本地的配置信息</br>
     * 默认为hlog-confs.properties文件,该文件一般放在跟hlog-agent.jar同目录下</br>
     * 当在环境变量中开启了hlogCfgName配置,</br>
     * 那么会同时加载hlog-confs.properties和hlog-{hlogCfgName}-confs.properties</br>
     * 两个配置文件.
     */
    private void localProperties(){
        //读取配置文件
        URL url = HLogConfig.class.getResource("");
        String agentJarPath = url.getFile();
        agentJarPath = agentJarPath.substring(5, agentJarPath.indexOf("!"));
        File jarFile = new File(agentJarPath);
        String propFile = jarFile.getParent()+File.separator;

        InputStream in = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(propFile+Constants.FIEL_NAME_HLOG_CONFS);
            in = new BufferedInputStream(fis);
            configProps.load(in);
            if(HLogConfig.hlogCfgName!=null){
                in.close();
                fis.close();

                fis = new FileInputStream(propFile+
                        MessageFormat.format(Constants.FILE_NAME_HLOG_EXT_CONFS,HLogConfig.hlogCfgName));
                in = new BufferedInputStream(fis);

                configProps.load(in);
            }

        } catch (FileNotFoundException e) {
            Logger.error("无法读取本地配置文件[{0}]",e,propFile);
        } catch (IOException e) {
            Logger.error("读取本地配置文件错误[{0}]", e,propFile);
        }finally {
            try {
                if (in!=null){
                    in.close();
                }
                if(fis!=null){
                    fis.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 初始化加载配置信息
     */
    public void initConfig(){

        if(configProps==null){
            configProps = new Properties();
            localProperties();
        }

        if(configProps==null){
            Logger.warn("配置对象[configProps]为空,无法完成代码监控工作.");
            return ;
        }

        //TODO 如果是走配置平台的话，这里将与配置平台建立通信，并加载配置信息
        Boolean uniCfgEnabled = Boolean.parseBoolean(
                configProps.getProperty(Constants.KEY_HLOG_UNICONFIG_ENABLED));
        if(Logger.isDebug()){
            Logger.debug("是否从配置平台读取配置信息:{0}",uniCfgEnabled);
        }
        if(uniCfgEnabled){
            try {
                Properties uniProps = UniConfig.init(configProps);
                if(uniProps!=null){
                    configProps.putAll(uniProps);
                }
            } catch (IOException e) {
                Logger.error("从配置平台获取数据异常:",e);
            }
        }

        //下面是完成代码织入配置信息和运行时开关信息的加载
        Enumeration<?> en = configProps.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String propVal = getProperty(key);
            //读取织入基础路径
            if(key.startsWith(Constants.KEY_BASE_PATH_PREFIX)){
                String basePath = key.substring(Constants.KEY_BASE_PATH_PREFIX.length());
                String[] weaves = propVal.split(",");
                String[] strPaths = basePath.split(",");
                for(int i=0;i<strPaths.length;i++){
                    basePaths.put(Path.build(strPaths[i]), weaves);
                }
            }else if(key.startsWith(Constants.KEY_HLOG_CAPTURE_ENABLE)){
                //读取收集日志路径
                String path = key.substring(Constants.KEY_HLOG_CAPTURE_ENABLE.length());
                HLogConfigRule hLogConfigRule = new HLogConfigRule(path);
                String[] weaves = propVal.split(",");
                for(String weave : weaves){
                    hLogConfigRule.addCaptureWeave(weave);
                }
                String level = getProperty(Constants.KEY_HLOG_LEVLE+path);
                if(level!=null){
                    hLogConfigRule.setLevel(level);
                }
                runtimeCaptureCofnigRule.put(Path.build(path),hLogConfigRule);

            }else if(key.startsWith(Constants.KEY_HLOG_CAPTURE_HANDLER)){
                String path = key.substring(Constants.KEY_HLOG_CAPTURE_HANDLER.length());
                //读取处理器
                String[] handlers = propVal.split(",");
                Set<String> handlersSet = new HashSet<String>(handlers.length);
                for(String handler : handlers){
                    handlersSet.add(handler);
                }
                runtimeHandlerCofnig.put(Path.build(path),handlersSet);
            }
        }
        //读取全局开关
        enable="true".equals(getProperty(Constants.KEY_HLOG_ENABLE));
    }


    public Map<Path, String[]> getBasePaths() {
        return basePaths;
    }

    public Map<Path, HLogConfigRule> getRuntimeCaptureCofnigRule() {
        return runtimeCaptureCofnigRule;
    }

    public Map<Path, Set<String>> getRuntimeHandlerCofnig() {
        return runtimeHandlerCofnig;
    }

    public void setEnable(boolean enable){
        this.enable = enable;
    }
    public boolean isEnable() {
        return enable;
    }

    public String getServerIp() {
        return serverIp;
    }


    public String getProperty(String key,String defVal){
        String val = getProperty(key);
        if(val==null){
            val= defVal;
        }

        return val;
    }

    public String getProperty(String key){
        String val;
        if(HLogConfig.hlogCfgName!=null){
            String tmpKey = HLogConfig.hlogCfgNamePix+key;
            val = configProps.getProperty(tmpKey);
            if(val!=null){
                if(Logger.isDebug()){
                    Logger.debug("property key={0} ,value={1}", tmpKey, val);
                }
                return val;
            }
        }else{
            val = configProps.getProperty(key);
            if(Logger.isDebug()) {
                Logger.debug("property key={0} ,value={1}", key, val);
            }
        }

        return val;
    }

}

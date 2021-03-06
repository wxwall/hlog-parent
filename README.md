# HLog Agent

## :snail: 功能说明
---
一个与开发无关的日志收集代理，在测试环境或正式环境下通过配置来获取应用运行过程中产生的日志信息。
具备的能力：
* 运行过程信息，方法的调用关系
* 异常信息
* 日志输出信息(`log4j`,`logback`)
* 指定接口或方法的入参信息

## :snail: 使用说明
---
日志代理是通过JVM的运行参数来启用能力。
* 在java命令运行

```java
 java HelloWorld -javaagent:hlog-agent.jar -noverify
```

Hlog JVM参数说明

```bash
-javaagent            表示hlog-agent的jar位置
-DhlogDomain          表示服务节点名称，如受理后端服务节点、营销资源服务节点
-DhlogServerAlias     表示服务节点名称，如受理后端服务节点、营销资源服务节点
-DhlogLevel           表示hlog-agent在运行过程中的日志级别，none不输出，
                       级别：trace、debug、info、warn、error
-DhlogSaveWeaveClass 表示是否保存到临时目录，如tomcat的temp下等
-DhlogJmxEnable       表示是否启用jmx，默认启用
-DhlogJmxPost         表示jmx的端口，需要注册的是同一台机器的端口不得重复
```

 * 在中间件服务器上运行
 
每个中间件服务器的设置大不相同，像tomcat需要在sh或bat的运行脚本上指定，而weblogc或websphere可以在控制台上配置。

## :snail: 更多信息访问部门wiki
在[福州21楼](http://192.168.1.22:8083/xwiki/bin/view/Main/logPlatform)
或[你出差了](http://110.90.126.242:8083/xwiki/bin/view/Main/logPlatform)
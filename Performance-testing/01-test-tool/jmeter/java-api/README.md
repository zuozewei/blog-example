# README #

这个项目演示了通过Java API执行JMeter脚本

主要功能包括:
* 在线生成jmx脚本（demo1）
* 加载本地已有jmx脚本（demo2）
* 运行多个Sampler
* 将生成的JMeter测试存储为.jmx文件
* 将测试执行结果存储为.jtl or .csv文件

### 配置说明 ###

* 下载仓库 or **git clone**
* 解压下载的文件，进入该文件夹
* 执行 **mvn clean install** 命令
* 进入 *target* 目录
* 执行脚本命令 **java -Djmeter.home=YOUR_JMETER_LOCATION -jar jmeter-from-code-1.0-SNAPSHOT-jar-with-dependencies.jar**
* 打开 *example.jmx* 在你的 "jmeter.home" 目录中示例脚本，*example.jtl* 是测试结果文件

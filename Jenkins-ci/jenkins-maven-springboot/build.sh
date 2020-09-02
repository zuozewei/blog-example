#!/usr/bin/env bash
#格式化时间
DATE=$(date +%Y%m%d)
#设置程序目录
DIR=/usr/local/app
#设置Jar名称
JARFILE=person-0.0.1-SNAPSHOT.jar

#判断是否存在backp目录，如果不存储就创建
if [ ! -d $DIR/backup ];then
   mkdir -p $DIR/backup
fi
cd $DIR

#杀掉当前运行的旧程序
ps -ef | grep $JARFILE | grep -v grep | awk '{print $2}' | xargs kill -9
#备份旧程序
mv $JARFILE $DIR/backup/$JARFILE$DATE
#部署新程序
mv -f /root/jenkins-in/$JARFILE .
echo "The service will be starting"
#后台启动程序并设置Jvm参数、开启JMX、打印GC日志
java -server -Xms1024M -Xmx1024M -XX:PermSize=256M \
-XX:MaxPermSize=256M -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails \
-Xloggc:./gc.log \
-Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=127.0.0.1 \
-Dcom.sun.management.jmxremote.port=10086 -Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-jar $JARFILE --spring.profiles.active=dev > /nohup 2>&1>out.log &

if [ $? = 0 ];then
        sleep 30
        tail -n 50 out.log
fi

cd backup/
ls -lt|awk 'NR>5{print $NF}'|xargs rm -rf
echo "starting success!!!"
~
~

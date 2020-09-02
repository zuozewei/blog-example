#!/usr/bin/env bash
# 设置系统时间
DATE=$(date +%Y%m%d)
# 设置程序目录
DIR=/usr/local/app
# 设置Jar名称
JARFILE=person-0.0.1-SNAPSHOT.jar
# 设置Docker镜像&容器名称
SERVER_NAME=springboot-docker-demo
# 容器id
CID=$(docker ps | grep "$SERVER_NAME" | awk '{print $1}')
# 镜像id
IID=$(docker images | grep "$SERVER_NAME" | awk '{print $3}')

# 判断是否存在backp目录，如果不存储就创建
if [ ! -d $DIR/backup ];then
   mkdir -p $DIR/backup
fi
cd $DIR

# 杀掉当前的容器及镜像
docker stop $CID
docker rm $CID
docker rmi $IID

# 备份旧程序
mv $JARFILE $DIR/backup/$JARFILE$DATE
# 部署新程序
mv -f /root/jenkins-in/$JARFILE .
# build镜像
docker build -t $SERVER_NAME .

echo "The service will be starting"
# 运行容器
docker run --name $SERVER_NAME -d -p 8081:8081 $SERVER_NAME

cd backup/
ls -lt|awk 'NR>5{print $NF}'|xargs rm -rf
echo "starting success!!!"
~

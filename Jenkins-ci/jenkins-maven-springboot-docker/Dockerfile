# Dockerfile
# 基于的镜像
FROM frolvlad/alpine-oraclejdk8

# 数据挂载点
VOLUME /usr/local/app/logs

# 指定路径下的copy内容到容器中路径下
ADD person-0.0.1-SNAPSHOT.jar /usr/local/app/app.jar

# 指定镜像的默认入口
# -Djava.security.egd=file:/dev/./urandom 可解决tomcat可能启动慢的问题
ENTRYPOINT java ${JAVA_OPTS} ${JVM_GC_LOG_PATH} -Djava.security.egd=file:/dev/./urandom -jar /usr/local/app/app.jar

# 对外端口
EXPOSE 8081

# 工作目录
WORKDIR /usr/local/app

# JVM参数
ARG JAVA_OPTS="-server -Xms1024M -Xmx1024M -XX:PermSize=256M -XX:MaxPermSize=256M \
-XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails \
-Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=127.0.0.1 \
-Dcom.sun.management.jmxremote.port=10086 -Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false"

# log路径
ARG JVM_GC_LOG_PATH="-XX:ErrorFile=/usr/local/app/logs/gc/hs_err_pid%p.log -XX:HeapDumpPath=/usr/local/app/logs/gc"

# JVM环境变量
ENV JAVA_OPTS=${JAVA_OPTS}
ENV JVM_GC_LOG_PATH=${JVM_GC_LOG_PATH}

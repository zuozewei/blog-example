FROM insideo/centos7-java8-build
VOLUME /tmp
ADD mall-admin.jar app.jar
RUN bash -c 'touch /app.jar'
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai  /etc/localtime && echo Asia/Shanghai > /etc/timezone
ENTRYPOINT ["java","-Dapp.id=svc-mall-admin","-javaagent:/opt/skywalking/agent/skywalking-agent.jar","-Dskywalking.agent.service_name=svc-mall-admin","-Dskywalking.collector.backend_service=my-skywalking-oap.skywalking.svc.cluster.local:11800","-jar","-Dspring.profiles.active=prod","-Djava.security.egd=file:/dev/./urandom","/app.jar"]

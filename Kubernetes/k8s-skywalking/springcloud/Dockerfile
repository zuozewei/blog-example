FROM insideo/centos7-java8-build
VOLUME /tmp
ADD mall-admin-1.0-SNAPSHOT.jar app.jar
RUN bash -c 'touch /app.jar'
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["java","-Dapp.id=svc-mall-admin","-javaagent:/opt/skywalking/agent/skywalking-agent.jar","-Dskywalking.agent.service_name=svc-mall-admin","-Dskywalking.collector.backend_service=skywalking-oap:11800","-Dcom.sun.management.jmxremote","-Dcom.sun.management.jmxremote.authenticate=false","-Dcom.sun.management.jmxremote.ssl=false","-Dcom.sun.management.jmxremote.port=1100","-Dcom.sun.management.jmxremote.rmi.port=1100","-Djava.rmi.server.hostname=localhost","-jar","-Dspring.profiles.active=prod","-Djava.security.egd=file:/dev/./urandom","/app.jar"]
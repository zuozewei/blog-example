FROM alpine:3.8

LABEL maintainer="zuozewei@hotmail.com"

ENV SKYWALKING_VERSION=8.1.0

ADD http://mirrors.tuna.tsinghua.edu.cn/apache/skywalking/${SKYWALKING_VERSION}/apache-skywalking-apm-${SKYWALKING_VERSION}.tar.gz /

RUN tar -zxvf /apache-skywalking-apm-${SKYWALKING_VERSION}.tar.gz && \
    mv apache-skywalking-apm-bin skywalking && \
    mv /skywalking/agent/optional-plugins/apm-trace-ignore-plugin* /skywalking/agent/plugins/ && \
    echo -e "\n# Ignore Path" >> /skywalking/agent/config/agent.config && \
    echo "# see https://github.com/apache/skywalking/blob/v8.1.0/docs/en/setup/service-agent/java-agent/agent-optional-plugins/trace-ignore-plugin.md" >> /skywalking/agent/config/agent.config && \
    echo 'trace.ignore_path=${SW_IGNORE_PATH:/health}' >> /skywalking/agent/config/agent.config
#!/bin/bash -e

docker build --tag="registry.cn-beijing.aliyuncs.com/7d/jmeter-base:latest" -f Dockerfile-base .
docker build --tag="registry.cn-beijing.aliyuncs.com/7d/jmeter-master:latest" -f Dockerfile-master .
docker build --tag="registry.cn-beijing.aliyuncs.com/7d/jmeter-slave:latest" -f Dockerfile-slave .


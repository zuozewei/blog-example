#!/usr/bin/env bash

## 获得证书
openssl req -newkey rsa:4096 -nodes -sha256 -keyout ca.key -x509 -days 3650 -out ca.crt

## 生成证书签名请求
openssl req -newkey rsa:4096 -nodes -sha256 -keyout tls.key -out tls.csr

## 生成证书
openssl x509 -req -days 3650 -in tls.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out tls.crt

## 创建 Kubernetes 的 Secret 资源，且将证书文件导入
kubectl create secret generic hub-7d-tls --from-file=tls.crt --from-file=tls.key --from-file=ca.crt -n harbor

#!/usr/bin/env bash
#Script created to launch Jmeter tests directly from the current terminal without accessing the jmeter master pod.
#It requires that you supply the path to the jmx file
#After execution, test script jmx file may be deleted from the pod itself but not locally.

#直接从当前终端启动 Jmeter 测试而创建的脚本，无需访问 Jmeter master pod。
#要求提供 jmx 文件的路径
#执行后，测试脚本 jmx 文件可能会从 pod 本身删除，但不会在本地删除。

working_dir="`pwd`"

# 获取 namesapce 变量
tenant=`awk '{print $NF}' "$working_dir/tenant_export"`

jmx="$1"
[ -n "$jmx" ] || read -p 'Enter path to the jmx file ' jmx

if [ ! -f "$jmx" ];
then
    echo "Test script file was not found in PATH"
    echo "Kindly check and input the correct file path"
    exit
fi

test_name="$(basename "$jmx")"

# 获取 master pod 详细信息

master_pod=`kubectl get po -n $tenant | grep jmeter-master | awk '{print $1}'`

kubectl cp "$jmx" -n $tenant "$master_pod:/$test_name"

## 启动 Jmeter 压测

kubectl exec -ti -n $tenant $master_pod -- /bin/bash /load_test "$test_name"

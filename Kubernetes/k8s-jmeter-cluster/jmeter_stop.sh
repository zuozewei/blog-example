#!/usr/bin/env bash
#Script writtent to stop a running jmeter master test
#Kindly ensure you have the necessary kubeconfig

#编写脚本来停止运行的 jmeter master 测试
#请确保你有必要的 kubeconfig

working_dir=`pwd`

#获取 namesapce 变量
tenant=`awk '{print $NF}' $working_dir/tenant_export`

master_pod=`kubectl get po -n $tenant | grep jmeter-master | awk '{print $1}'`

#kubectl -n $tenant exec -ti $master_pod bash /jmeter/apache-jmeter-5.0/bin/stoptest.sh

kubectl -n $tenant exec -it $master_pod -- bash -c "./jmeter/apache-jmeter-5.0/bin/stoptest.sh"                               

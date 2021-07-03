#!/bin/bash
# author:7DGroup
# 2019/12/08/20/50

cmdsh="/home/gaolou/apache-jmeter-3.1/bin/JMeterPluginsCMD.sh"
case=$1
mNum=$2
durationTime=$3
mark=$4
resultDataFileName="resultData.csv"
#hostIps="101.201.210.163"
nowPwd=`pwd`

echo -e "\033[32m-压力机开始执行。。请等待-\033[1m"

echo testcaseName:${case}_${mNum}_${durationTime}_${mark}
if [ -z ${case} ];then
        echo -e "\033[32m请输入脚本名，线程数，执行时间，备注! 输入脚本名\033[0m"
        echo "ERROR"
        exit 1
fi
if [ -z ${mNum} ];then
        echo -e "\033[32m请输入脚本名，线程数，执行时间，备注! 输入线程数\033[1m"
        echo "ERROR"
        exit 1
fi
if [ -z ${durationTime} ];then
        echo -e "\033[32m请输入脚本名，线程数，执行时间，备注! 输入备注\033[1m"
        echo "ERROR"
        exit 1
fi

oneTest(){
    filename=${case}_${mNum}_${durationTime}_${mark}
    echo "filename:"${filename}
    mkdir -p ${nowPwd}/${case}
    cd ${nowPwd}/${case}
    cp ${nowPwd}/testPlan/${case}.jmx ${nowPwd}/${case}/${case}.jmx
    if [ ! -f ${resultDataFileName} ];then
                echo -e "sceneName,sceneThreadNum,duration(s),interfaceName,interfaceNum,totalCount,tps,errorPersent,avgTims(ms),persentTime(ms)" > ${resultDataFileName}
    fi
    rm -rf ${filename}
    mkdir ${filename}
    cp ${case}.jmx ${nowPwd}/${case}/${filename}/${filename}.jmx
    cd ${nowPwd}/${case}/${filename}/
    sed -i "s#name=\"ThreadGroup\.num_threads\">2<#name=\"ThreadGroup\.num_threads\">$((2*mNum))<#g" ${filename}.jmx
    sed -i "s#name=\"ThreadGroup\.num_threads\">1<#name=\"ThreadGroup\.num_threads\">$((1*mNum))<#g" ${filename}.jmx
    sed -i "s#ERRORXML#${nowPwd}/${case}/${filename}/${filename}_ERROR.xml#g" ${filename}.jmx
    sed -i "s#ThreadGroup.scheduler\">.*</#ThreadGroup.scheduler\">true</#g" ${filename}.jmx
    sed -i "s#LoopController\.loops\">.*</#LoopController\.loops\">-1</#g" ${filename}.jmx
    sed -i "s#LoopController\.continue_forever\">.*</#LoopController\.continue_forever\">true</#g"  ${filename}.jmx
    sed -i "s#ThreadGroup.duration\">.*</#ThreadGroup.duration\">${durationTime}</#g" ${filename}.jmx

   if [ -z ${hostIps} ];then
        #jmeter -n -t ${filename}.jmx -l ${filename}.jtl -j ${filename}.log
        jmeter  -n -t ${filename}.jmx -l ${filename}.jtl -j ${filename}.log -e -o ${filename}
   else
        jmeter  -n -t ${filename}.jmx -R ${hostIps}  -l ${filename}.jtl -j ${filename}.log -e -o ${filename}
         #jmeter -n -t ${filename}.jmx -R ${hostIps} -l ${filename}.jtl -j ${filename}.log
    fi

    ${cmdsh} --generate-csv ${filename}.csv --input-jtl ${filename}.jtl --plugin-type AggregateReport
    local sumThread=`grep "<stringProp name=\"ThreadGroup.num_threads\">" ${filename}.jmx |awk -F\> '{print $2}'|awk -F\< 'BEGIN{sum=0}{sum=sum+$1}END{print sum}'`
    if [ -z ${hostIps} ];then
        hostNum=1
     else
        hostNum=`echo ${hostIps}|awk -F, '{print NF}'`
        let sumThread=sumThread*hostNum
        let mNum=mNum*hostNum
    fi
    sed -n '2,$p' ${filename}.csv|grep -v TOTAL| awk -F, -v case=${filename} -v tnum=${sumThread} -v duration=${durationTime} '{printf("%s,%s,%s,%s,%s,%s,%s,%s,90%time:%s;95%time:%s;99%time:%s\n",case,tnum,duration,
$1,$2,$11,$10,$3,$5,$6,$7)}' >> sDGroup
    local totalGroup=`cat sDGroup|wc -l`
    for((i=1;i<=${totalGroup};i++))
    do
       sed -n "${i}p" sDGroup|awk -F, -v tnum=${mNum} '{printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",$1,$2,$3,$4,tnum,$5,$6,$7,$8,$9)}' >>../${resultDataFileName}
    done
}
oneTest

echo -e "\033[32m-压力已经结束-\033[1m"

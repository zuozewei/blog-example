# -*- coding: utf-8 -*-
# @Time    : 2019/12/11 22:07
# @Author  : 7FGroup
# @name   : Jmeter启动脚本
# @File    : startJmeter.py

import os, sys

def jmeterNumber(caseName, num_threads, ramp_time, duration, remark, hostIps='127.0.0.1'):

    '''
    :param caseName: 脚本名字
    :param num_threads: 线程数
    :param ramp_time: 控制线程步长
    :param duration: 执行时间
    :param remark: 标志
    :param hostIps: 负载参数
    :return: 启动JMeter成功

    启动命令：python startJmeter.py tiaoshi 2 1 30 pythontiaoshi
            - Python：表示通过 Python 脚本语言执行
            - startJmeter.py：表示启动脚本名字
            - tiaoshi：表示 JMeter 脚本名字
            - 2：表示并发数
            - 1：执行步长
            - 30：表示执行时间
            - pythontiaoshi：表示备注，方便一个脚本多次执行好分别每次执行的结果
    '''

    if caseName is None:
        return "测试用例为空"
    if num_threads is None:
        return "虚拟并发数为空"
    if ramp_time is None:
        return "测试步骤为空"
    if duration is None:
        return "执行时间为空"

    # 执行脚本名字
    runJmeterFile = '%s_%s_%s_%s_%s' % (caseName, num_threads, ramp_time, duration, remark)
    print("执行名字脚本：%s" % runJmeterFile)
    thisdir = os.getcwd()

    # 原始脚本
    newdir = os.path.join(thisdir, "testscript", caseName + ".jmx")
    print("当前脚本路径: %s" % newdir)
    if not os.path.exists(newdir):
        print('脚本不存在！请检查脚本')
        return False

    # 保存测试结果路径
    resultFile = os.path.join(thisdir, 'result', runJmeterFile)
    print("脚本执行路径: ", resultFile)

    # 判断结果路径是否存在
    if not os.path.exists(resultFile):
        os.makedirs(resultFile)

    lines = open(newdir, encoding="utf-8").readlines()
    fp = open(os.path.join(thisdir, "result", resultFile, runJmeterFile) + '.jmx', 'w')  # 打开你要写得文件
    for s in lines:
        fp.write(s.replace('num_threads">1</stringProp>', 'num_threads">%s</stringProp>' % num_threads)  # 替换并发数
                 .replace('ramp_time">1</stringProp>', 'ramp_time">%s</stringProp>' % ramp_time)  # 替换步长
                 .replace('scheduler">false</boolProp>', 'scheduler">true</boolProp>')  # 勾选通过时间判断结束
                 .replace('duration"></stringProp>', 'duration">%s</stringProp>' % duration)  # 替换执行时间
                 .replace('name="LoopController.loops">1</stringProp>',
                          'name="LoopController.loops">-1</stringProp>'))  # 勾选通过时间判断结束

    fp.close()
    os.chdir(resultFile)
    print("当前路径: ", os.getcwd())

    # 检查环境变量
    if isEvn():
        # 判断分布式执行方式
        if len(hostIps.split(",")) > 2:
            # 根据自己需求添加执行类型
            Rcmd = 'jmeter -n -t %s.jmx -R %s -l %s.jtl -j %s.log' % (
                runJmeterFile, hostIps, runJmeterFile, runJmeterFile)

            # Rcmd = 'jmeter -n -t %s.jmx -R %s -l %s.jtl -j %s.log -e -o %s' % (runJmeterFile, hostIps, runJmeterFile, runJmeterFile, runJmeterFile)
            print('执行命令：%s' % Rcmd)
            # os.system(Rcmd)
        else:
            # 不生成html报告
            # cmd = 'jmeter -n -t %s.jmx -l %s.jtl -j %s.log' % (runJmeterFile, runJmeterFile, runJmeterFile, runJmeterFile)
            # 自动生成html报表
            cmd = 'jmeter  -n -t %s.jmx -l %s.jtl -j %s.log -e -o %s' % (
                runJmeterFile, runJmeterFile, runJmeterFile, runJmeterFile)
            print('执行命令：%s' % cmd)
            os.system(cmd)

def isEvn():

    '''
    检查环境变量
    :return: True/Fals
    '''

    cmd = 'jmeter -v'
    lin = os.popen(cmd)
    for i in lin:
        if 'The Apache Software Foundation' in i:
            print("Jmeter环境变量配置成功")
            return True
    else:
        print("Jmeter环境变量配置失败")
        return False

if __name__ == '__main__':
    # 分布式ip写法，多个使用逗号隔开
    hostIps = '127.0.0.1'
    if len(sys.argv[1:]) == 5:
        print('参数个数为:', len(sys.argv), '个参数。')
        print('可用参数列表:', str(sys.argv[1:]))
        param = sys.argv[1:]
        print("脚本名字: %s,并发数: %s,步长: %s,执行时间: %s,备注: %s" % (param[0], param[1], param[2], param[3], param[4]))
        jmeterNumber(param[0], param[1], param[2], param[3], param[4], hostIps)
    else:
        print("参数不对")
    pass

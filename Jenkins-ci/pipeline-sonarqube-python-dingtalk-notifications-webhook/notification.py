# coding=utf-8

'''
@author: zuozewei
@file: notification.py
@time: 2019/5/10 18:00
@description：dingTalk通知类
'''

import os
import jenkins
import configparser
import requests
import json
import time
from dingtalkchatbot.chatbot import DingtalkChatbot
from jsonpath import jsonpath

# 获取Jenkins变量
JOB_NAME = str(os.getenv("JOB_NAME"))
BUILD_URL = str(os.getenv("BUILD_URL")) + "console"
BUILD_NUMBER = str(os.getenv("BUILD_NUMBER"))

# 连接jenkins
server = jenkins.Jenkins(url="http://xxx.xxx.xxx.xxxx:8080", username='xxxx', password="xxxx")

def sonarNotification():
    bug = ''
    leak = ''
    code_smell = ''
    coverage = ''
    density = ''
    status = ''
    title = 'xxxx代码扫描通知'
    dingText = ''
    SonarQube_URL = 'http://xxx.xxx.xxx.xxxx:9088/dashboard?id=' + JOB_NAME

    # sonar API
    sonar_Url = 'http://xxx.xxx.xxx.xxxx:9088/api/measures/search?projectKeys=' + JOB_NAME + \
                '&metricKeys=alert_status%2Cbugs%2Creliability_rating%2Cvulnerabilities%2Csecurity_rating%2Ccode_smells%2Csqale_rating%2Cduplicated_lines_density%2Ccoverage%2Cncloc%2Cncloc_language_distribution'

    # 获取sonar指定项目结果
    resopnse = requests.get(sonar_Url).text

    # 转换成josn
    result = json.loads(resopnse)

    # 解析sonar json结果
    for item in result['measures']:
        if item['metric'] == "bugs":
            bug = item['value']
        elif item['metric'] == "vulnerabilities":
            leak = item['value']
        elif item['metric'] == 'code_smells':
            code_smell = item['value']
        elif item['metric'] == 'coverage':
            coverage = item['value']
        elif item['metric'] == 'duplicated_lines_density':
            density = item['value']
        elif item['metric'] == 'alert_status':
            status = item['value']
            print('【Status】：' + status)
        else:
            pass

    textFail = '#### ' + JOB_NAME + ' - CodeScan # ' + BUILD_NUMBER + ' \n' + \
               '##### <font color=#FF0000 size=6 face="黑体">新代码质量: ' + status + '</font> \n' + \
               '##### **版本类型**: ' + '开发版' + '\n' + \
               '##### **Bug数**: ' + bug + '个 \n' + \
               '##### **漏洞数**: ' + leak + '个 \n' + \
               '##### **可能存在问题代码**: ' + code_smell + '行 \n' + \
               '##### **覆盖率**: ' + coverage + '% \n' + \
               '##### **重复率**: ' + density + '% \n' + \
               '##### **SonarQube**:  [查看详情](' + SonarQube_URL + ') \n' + \
               '##### **关注人**: @158xxxx3364 \n' + \
               '> ###### xxxx技术团队 \n'

    textSuccess = '#### ' + JOB_NAME + ' - CodeScan # ' + BUILD_NUMBER + ' \n' + \
                  '##### **新代码质量**: ' + status + '\n' + \
                  '##### **版本类型**: ' + '开发版' + '\n' + \
                  '##### **Bug数**: ' + bug + '个 \n' + \
                  '##### **漏洞数**: ' + leak + '个 \n' + \
                  '##### **可能存在问题代码**: ' + code_smell + '行 \n' + \
                  '##### **覆盖率**: ' + coverage + '% \n' + \
                  '##### **重复率**: ' + density + '% \n' + \
                  '##### **SonarQube**:  [查看详情](' + SonarQube_URL + ') \n' + \
                  '> ###### xxxx技术团队 \n '

    # 判断新代码质量阀状态
    if status == 'ERROR':
        dingText = textFail
    elif status == 'OK':
        dingText = textSuccess

    sendding(title, dingText)

def sendding(title, content):
    at_mobiles = ['186xxxx2487','158xxxx3364']
    Dingtalk_access_token = 'https://oapi.dingtalk.com/robot/send?access_token=xxxxxxxxxx'
    # 初始化机器人小丁
    xiaoding = DingtalkChatbot(Dingtalk_access_token)
    # Markdown消息@指定用户
    xiaoding.send_markdown(title=title, text=content, at_mobiles=at_mobiles)

if __name__ == "__main__":
    sonarNotification()

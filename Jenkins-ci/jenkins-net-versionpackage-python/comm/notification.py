# coding=utf-8

'''
@author: zuozewei
@file: notification.py
@time: 2019/4/25 18:00
@description：dingTalk通知类
'''
import os, jenkins, configparser, requests, json, time
from dingtalkchatbot.chatbot import DingtalkChatbot
from jsonpath import jsonpath

JOB_NAME = str(os.getenv("JOB_NAME"))
print("【JOB_NAME】：" + JOB_NAME)
BUILD_URL = str(os.getenv("BUILD_URL")) + "console"
BUILD_VERSION = str(os.getenv("BUILD_VERSION"))
JENKINS_HOME = os.getenv("JENKINS_HOME")
BUILD_NUMBER = str(os.getenv("BUILD_NUMBER"))
WORKSPACE = os.getenv("WORKSPACE")

versionPath = JENKINS_HOME + "\workspace\Version.ini"

config = configparser.ConfigParser()
config.read(versionPath)
xxx_Major = config.get("xxx", "xxx_Major")
xxx_Minor = config.get("xxx", "xxx_Minor")
xxx_Build = config.get("xxx", "xxx_Build")
xxx_Revision = config.get("xxx", "xxx_Revision")
VERSION = xxx_Major + "." + xxx_Minor + "." + xxx_Build + "." +xxx_Revision
print("【当前版本】：" + VERSION)

packagePath = WORKSPACE + "\\package.ini"

def packagNotification():
    title = 'xxxx打包通知'

    # 获取打包信息
    packagconfig = configparser.ConfigParser()
    packagconfig.read(packagePath)
    packagName = packagconfig.get("package", "name")
    packagRemotedir = packagconfig.get("package", "remotedir")

    downloadlink = 'http://xxx.xx.xxx.xxx:8081/repository/' + packagRemotedir + packagName
    print("【Build】downloadlink：" + downloadlink)

    text = '#### ' + JOB_NAME + ' - Package # ' + BUILD_NUMBER + ' \n' + \
           '##### **版本类型**: ' + '开发版' + '\n' + \
           '##### **当前版本**: ' + VERSION + '\n' + \
           '##### **仓库类型**: ' + 'Nexus 3 OSS' + '\n' + \
           '##### **文件格式**: ' + 'raw' + '\n' + \
           '##### **文件名称**: ' + packagName + '\n' + \
           '##### **文件地址**: [点击下载](' + downloadlink + ') \n' + \
           '> ###### xxx技术团队 \n '

    sendding(title, text)


def sendding(title, content):
    at_mobiles = ['186xxxx2487']
    Dingtalk_access_token = 'https://oapi.dingtalk.com/robot/send?access_token=xxxx'
    # 初始化机器人小丁
    xiaoding = DingtalkChatbot(Dingtalk_access_token)
    # Markdown消息@指定用户
    xiaoding.send_markdown(title=title, text=content, at_mobiles=at_mobiles)

if __name__ == "__main__":
    packagNotification()

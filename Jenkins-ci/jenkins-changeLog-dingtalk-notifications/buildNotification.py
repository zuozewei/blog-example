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

# 获取Jenkins变量
JOB_NAME = str(os.getenv("JOB_NAME"))
BUILD_URL = str(os.getenv("BUILD_URL")) + "console"
BUILD_VERSION = str(os.getenv("BUILD_VERSION"))
JENKINS_HOME = os.getenv("JENKINS_HOME")
BUILD_NUMBER = str(os.getenv("BUILD_NUMBER"))
SCM_CHANGELOG = str(os.getenv("SCM_CHANGELOG"))
WORKSPACE = os.getenv("WORKSPACE")

versionPath = JENKINS_HOME + "\workspace\Version.ini"

# 读取版本号
config = configparser.ConfigParser()
config.read(versionPath)
xxx_Major = config.get("xxx", "xxx_Major")
xxx_Minor = config.get("xxx", "xxx_Minor")
xxx_Build = config.get("xxx", "xxx_Build")
xxx_Revision = config.get("xxx", "xxx_Revision")
VERSION = xxx_Major + "." + xxx_Minor + "." + xxx_Build+ "." + xxx_Revision

# 判断日志内容
if SCM_CHANGELOG == 'None':
    SCM_CHANGELOG = '- No changes'
    print("empty")
else:
    print("not empty")
    pass

def buildNotification():
    title = 'xxx编译通知'

    # 连接jenkins
    server1 = jenkins.Jenkins(url="http://xxxx.xxx.xxx.xxx:8080", username='xxx', password="xxx")
    build_info = server1.get_build_info(JOB_NAME, int(BUILD_NUMBER))
    # dict字典转json数据
    build_info_json = json.dumps(build_info)
    # 把json字符串转json对象
    build_info_jsonobj = json.loads(build_info_json)
    # 获取任务触发原因
    causes = jsonpath(build_info_jsonobj, '$.actions..shortDescription')
    print(causes[0])

    textFail = '#### ' + JOB_NAME + ' - Build # ' + BUILD_NUMBER + ' \n' + \
               '##### <font color=#FF0000 size=6 face="黑体">编译状态: ' + BUILD_STATUS + '</font> \n' + \
               '##### **版本类型**: ' + '开发版' + '\n' + \
               '##### **当前版本**: ' + VERSION + '\n' + \
               '##### **触发类型**: ' + str(causes[0]) + '\n' + \
               '##### **编译日志**:  [查看详情](' + BUILD_URL + ') \n' + \
               '##### **关注人**: @186xxxx2487 \n' + \
               '##### **更新记录**: \n' + \
               SCM_CHANGELOG + '\n' + \
               '> ###### xxx技术团队 \n '

    textSuccess = '#### ' + JOB_NAME + ' - Build # ' + BUILD_NUMBER + ' \n' + \
                  '##### **编译状态**: ' + BUILD_STATUS + '\n' + \
                  '##### **版本类型**: ' + '开发版' + '\n' + \
                  '##### **当前版本**: ' + VERSION + '\n' + \
                  '##### **触发类型**: ' + str(causes[0]) + '\n' + \
                  '##### **编译日志**: [查看详情](' + BUILD_URL + ') \n' + \
                  '##### **更新记录**: \n' + \
                  SCM_CHANGELOG + '\n' + \
                  '> ###### xxx技术团队 \n '

    if BUILD_STATUS == 'SUCCESS':
        dingText = textSuccess
    else:
        dingText = textFail

    sendding(title, dingText)

def sendding(title, content):
    at_mobiles = ['186xxxx2487']
    Dingtalk_access_token = 'https://oapi.dingtalk.com/robot/send?access_token=xxxxx'
    # 初始化机器人小丁
    xiaoding = DingtalkChatbot(Dingtalk_access_token)
    # Markdown消息@指定用户
    xiaoding.send_markdown(title=title, text=content, at_mobiles=at_mobiles)

if __name__ == "__main__":
    buildNotification()
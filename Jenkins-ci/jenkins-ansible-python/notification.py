# coding=utf-8

'''
@author: zuozewei
@file: notification.py
@time: 2019/4/25 18:00
@description：dingTalk通知类
'''
import os, jenkins, json
from dingtalkchatbot.chatbot import DingtalkChatbot
from jsonpath import jsonpath

JOB_NAME = str(os.getenv("JOB_NAME"))
BUILD_URL = str(os.getenv("BUILD_URL")) + "console"
BUILD_NUMBER = str(os.getenv("BUILD_NUMBER"))
Package_Name = str(os.getenv("Package_Name"))
VERSION = Package_Name.split('-')[4].replace('.zip','')
Branch = str(os.getenv("Branch"))
Hosts =  str(os.getenv("Hosts"))

Branch_Name = ''
Eev = ''
Host_name = ''
if Branch == 'dev':
    Branch_Name = '开发版'
    if Hosts == 'Dev_ALL':
        Eev = 'Dev所有环境'
        Host_name =  '- 172.16.106.175' + '\n' + \
                     '- 172.16.106.155' + '\n' + \
                     '- 172.16.106.115' + '\n'
    elif Hosts == 'Dev_AutoTest':
        Eev = 'Dev自动化测试环境'
        Host_name =  '- 172.16.106.175' + '\n'
    elif Hosts == 'Dev_FunctionTest':
        Eev = 'Dev功能测试环境'
        Host_name =  '- 172.16.106.155' + '\n'
    elif Hosts == 'Dev_Develop':
        Eev = 'Dev开发环境'
        Host_name =  '- 172.16.106.115' + '\n'
elif Branch == 'release':
    Branch_Name = '预览版'
    if Hosts == 'Release_ALL':
        Eev = 'Release所有环境'
        Host_name =  '- 172.16.106.58' + '\n' + \
                     '- 172.16.106.168' + '\n' + \
                     '- 172.16.106.203' + '\n'
    elif Hosts == 'Release_AutoTest':
        Eev = 'Release自动化测试环境'
        Host_name =  '- 172.16.106.58' + '\n'
    elif Hosts == 'Release_FunctionTest':
        Eev = 'Release功能测试环境'
        Host_name =  '- 172.16.106.203' + '\n'
    elif Hosts == 'Release_Develop':
        Eev = 'Release开发环境'
        Host_name =  '- 172.16.106.168' + '\n'
print("【版本类型】：" + Branch_Name)
print("【环境类型】：" + Eev)
print("【主机列表】：" + Host_name)

# 连接jenkins
server = jenkins.Jenkins(url="http://172.16.106.251:8080", username='xxx', password="xxx")

# 获取指定项目编译状态
BUILD_STATUS = server.get_build_info(JOB_NAME, int(BUILD_NUMBER))['result']
print("【BUILD_STATUS】：" + BUILD_STATUS)

build_info = server.get_build_info(JOB_NAME, int(BUILD_NUMBER))
# dict字典转json数据
build_info_json = json.dumps(build_info)
# 把json字符串转json对象
build_info_jsonobj = json.loads(build_info_json)
causes = jsonpath(build_info_jsonobj, '$.actions..shortDescription')

def packagNotification():
    title = 'xxx部署通知'

    textFail = '#### ' + JOB_NAME + ' # ' + BUILD_NUMBER + ' \n' + \
               '##### <font color=#FF0000 size=6 face="黑体">部署状态: ' + BUILD_STATUS + '</font> \n' + \
               '##### **版本类型**: ' + Branch_Name + '\n' + \
               '##### **当前版本**: ' + VERSION + '\n' + \
               '##### **文件名称**: ' + Package_Name + '\n' + \
               '##### **触发类型**: ' + str(causes[0]) + '\n' + \
               '##### **部署日志**: [查看详情](' + BUILD_URL + ') \n' + \
               '##### **关注人**: @186xxxx2487 \n' + \
               '##### **部署环境**: ' + Eev + '\n' + \
               '##### **执行主机**: \n' + \
                Host_name + '\n' + \
               '> ###### xxx技术团队 \n '

    textSuccess = '#### ' + JOB_NAME + ' # ' + BUILD_NUMBER + ' \n' + \
              '##### **部署状态**: ' + BUILD_STATUS + '\n' + \
              '##### **版本类型**: ' + Branch_Name + '\n' + \
              '##### **当前版本**: ' + VERSION + '\n' + \
              '##### **文件名称**: ' + Package_Name + '\n' + \
              '##### **触发类型**: ' + str(causes[0]) + '\n' + \
              '##### **部署日志**: [查看详情](' + BUILD_URL + ') \n' + \
              '##### **部署环境**: ' + Eev + '\n' + \
              '##### **执行主机**: \n' + \
               Host_name + '\n' + \
              '> ###### xxx技术团队 \n '

    if BUILD_STATUS == 'SUCCESS':
        dingText = textSuccess
    else:
        dingText = textFail

    sendding(title, dingText)

def sendding(title, content):
    at_mobiles = ['186xxxx2487']
    Dingtalk_access_token_v3c = 'https://oapi.dingtalk.com/robot/send?access_token=xxxxxxxxxx'
    # 初始化机器人小丁
    xiaoding1 = DingtalkChatbot(Dingtalk_access_token_v3c)
    # Markdown消息@指定用户
    xiaoding1.send_markdown(title=title, text=content, at_mobiles=at_mobiles)

if __name__ == "__main__":
    packagNotification()

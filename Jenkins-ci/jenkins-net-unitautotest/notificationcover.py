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

# 获取 Jenkins 变量
JOB_NAME = str(os.getenv("JOB_NAME"))
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
VERSION = xxx_Major + "." + xxx_Minor + "." + xxx_Build + "." + xxx_Revision
reportUrl = 'http://xxx.xxx.xxx.xxx:8080/view/xxx/job/' + JOB_NAME + '/' + BUILD_NUMBER + '/HTML_20Report/'
# 连接jenkins
server = jenkins.Jenkins(url="http://xxx.xxx.xxx.xxx:8080", username='xxx', password="xxx")
testresult = ''
packagePath = WORKSPACE + "\\package.ini"
overageReportUrl = 'http://xxx.xxx.xxx.xxx/xxx/coverage/' + JOB_NAME + '/Coverage_' + BUILD_NUMBER

def unitTestNotification():
    title = 'xxx单测通知'
    last_build_number = server.get_job_info(JOB_NAME)['lastCompletedBuild']['number']
    build_info = server.get_build_info(JOB_NAME, last_build_number)
    # dict字典转json数据
    build_info_json = json.dumps(build_info)
    # 把json字符串转json对象
    build_info_jsonobj = json.loads(build_info_json)
    failCount = jsonpath(build_info_jsonobj, '$.actions..failCount')
    skipCount = jsonpath(build_info_jsonobj, '$.actions..skipCount')
    totalCount = jsonpath(build_info_jsonobj, '$.actions..totalCount')
    successCount = totalCount[0] - skipCount[0] - failCount[0]
    successRate = round((successCount / totalCount[0]) * 100, 1)
    # 判断测试结果
    if successRate == 100:
        testresult = 'SUCCESS'
    else:
        testresult = 'FAILURE'
    testFail = '#### ' + JOB_NAME + ' - UnitTest # ' + BUILD_NUMBER + ' \n' + \
               '##### <font color=#FF0000 size=6 face="黑体">测试结果: ' + testresult + '</font> \n' + \
               '##### **版本类型**: ' + '开发版' + '\n' + \
               '##### **当前版本**: ' + VERSION + '\n' + \
               '##### **用例数**: ' + str(totalCount[0]) + '个 \n' + \
               '##### **通过率**: ' + str(successRate) + '% \n' + \
               '##### **成功**: ' + str(successCount) + '个 \n' + \
               '##### **失败**: ' + str(failCount[0]) + '个 \n' + \
               '##### **忽略**: ' + str(skipCount[0]) + '个 \n' + \
               '##### **测试报告**:  [查看详情](' + reportUrl + ') \n' + \
               '##### **覆盖率报告**:  [查看详情](' + overageReportUrl + ') \n' + \
               '##### **关注人**: @18610902487 \n' + \
               '> ###### xxx技术团队 \n '
    testSuccess = '#### ' + JOB_NAME + ' - UnitTest # ' + BUILD_NUMBER + ' \n' + \
                  '##### **测试结果**: ' + testresult + '\n' + \
                  '##### **版本类型**: ' + '开发版' + '\n' + \
                  '##### **当前版本**: ' + VERSION + '\n' + \
                  '##### **用例数**: ' + str(totalCount[0]) + '个 \n' + \
                  '##### **通过率**: ' + str(successRate) + '% \n' + \
                  '##### **成功**: ' + str(successCount) + '个 \n' + \
                  '##### **失败**: ' + str(failCount[0]) + '个 \n' + \
                  '##### **忽略**: ' + str(skipCount[0]) + '个 \n' + \
                  '##### **测试报告**: [查看详情](' + reportUrl + ') \n' + \
                  '##### **覆盖率报告**:  [查看详情](' + overageReportUrl + ') \n' + \
                  '> ###### xxx技术团队 \n '
    if testresult == 'SUCCESS':
        dingText = testSuccess
    else:
        dingText = testFail
    sendding(title, dingText)

def sendding(title, content):
    at_mobiles = ['186xxxx2487']
    Dingtalk_access_token_v3c = 'https://oapi.dingtalk.com/robot/send?access_token=xxxxxxxx'
    # 初始化机器人小丁
    xiaoding1 = DingtalkChatbot(Dingtalk_access_token_v3c)
    # Markdown消息@指定用户
    xiaoding1.send_markdown(title=title, text=content, at_mobiles=at_mobiles)

if __name__ == "__main__":
    unitTestNotification()

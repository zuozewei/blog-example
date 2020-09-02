# coding=utf-8

'''
@author: zuozewei
@file: SonarQubeDingtalk.py
@time: 2019/3/5 18:00
'''
import  requests,json,jenkins,os,time

# 接收jenkins当前JOB_NAME参数
projectName = os.getenv("JOB_NAME")

def sendding(Dingtalk_access_token,content,title,messageUrl):
    url = Dingtalk_access_token
    pagrem = {
        "msgtype": "link",
        "link": {
            'title':title,
            "text": content,
            'picUrl': messageUrl,
            'messageUrl':'http://172.16.14.251:9088/dashboard?id='+ projectName
        }
    }

    headers = {
        'Content-Type': 'application/json'
    }

    # 发送消息
    requests.post(url, data=json.dumps(pagrem), headers=headers)

def notification():
    # 钉钉hook地址
    Dingtalk_access_token = 'your Dingtalk_access_token'
    # sonar API
    sonar_Url = 'http://172.16.14.251:9088/api/measures/search?projectKeys='+ projectName +'&metricKeys=alert_status%2Cbugs%2Creliability_rating%2Cvulnerabilities%2Csecurity_rating%2Ccode_smells%2Csqale_rating%2Cduplicated_lines_density%2Ccoverage%2Cncloc%2Cncloc_language_distribution'
    # 获取sonar指定项目结果
    resopnse = requests.get(sonar_Url).text
    # 转换成josn
    result = json.loads(resopnse)
    bug = 0
    leak = 0
    code_smell = 0
    coverage = 0
    density = 0
    status = ''
    statusStr = ''

    # 解析sonar json结果
    for item in result['measures']:
        if item['metric']=="bugs":
            bug = item['value']
        elif item['metric']=="vulnerabilities":
            leak = item['value']
        elif item['metric']=='code_smells':
            code_smell = item['value']
        elif item['metric']=='coverage':
            coverage = item['value']
        elif item['metric']=='duplicated_lines_density':
            density = item['value']
        elif item['metric']=='alert_status':
            status = item['value']
        else:
            pass

    # 判断新代码质量阀状态
    if status == 'ERROR':
        # 错误图片
        messageUrl = 'http://www.iconsdb.com/icons/preview/soylent-red/x-mark-3-xxl.png'
        statusStr = '失败'
    elif status == 'OK':
        statusStr = '通过'
        # 正确图片
        messageUrl = 'http://icons.iconarchive.com/icons/paomedia/small-n-flat/1024/sign-check-icon.png'

    # 消息内容。如果太长只会部分展示
    code_reslut=  "Bug数:" + bug + "个，" + \
                  "漏洞数:" + leak + "个，" + \
                  "可能存在问题代码:"+ code_smell + "行，" + \
                  "覆盖率:" + coverage + "%，" + \
                  "重复率:" + density + "%"
    print("静态代码扫描统计："+"状态:"+ status +","+code_reslut)

    # 连接jenkins
    server=jenkins.Jenkins(url="http://172.16.14.251:8080",username='user',password="password")

    # 获取指定项目最后编译number
    get_number = server.get_job_info(projectName)['lastBuild']['number']
    print("BUILD_NUMBER："+ str(get_number))

    sendding(Dingtalk_access_token, content=code_reslut, title=projectName+"#"+str(get_number)+"新代码扫描" + statusStr,messageUrl=messageUrl)

if __name__=="__main__":
    # 等待10秒,确保SonarQube刷新结果
    time.sleep(10)
    notification()

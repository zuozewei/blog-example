# coding=utf-8
import gitlab

# gitlab地址
url = 'http://x.x.x.x:82'
token = ''
# 登录
gl = gitlab.Gitlab(url, token)

# 获取仓库提交次数
def getcommits():
    # 获取指定项目
    project = gl.projects.get(197)
    sum = 0
    list = ['dev','hotfixes','log','master','release']
    for li in list:
        commits = project.commits.list(all=True,query_parameters={'ref_name': li})
        print(li +":" + str(len(commits)))
        sum += len(commits)

    branches = project.branches.list()
    return sum


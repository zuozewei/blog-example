# coding=utf-8

import os, os.path, configparser, zipfile, shutil, nexuscli, ftplib, socket

# 获取Jenkins变量
WORKSPACE = os.getenv("WORKSPACE")
JENKINS_HOME = os.getenv("JENKINS_HOME")
BUILD_VERSION = str(os.getenv("BUILD_VERSION"))
JOB_NAME = str(os.getenv("JOB_NAME"))

# 版本号信息
versionPath = JENKINS_HOME + "\workspace\Version.ini"
packageName = ""
xxx_Major = ''
xxx_Minor = ''
xxx_Build = ''
xxx_Revision = ''

# 打包目录
targetFilePath = WORKSPACE + '\\'
# bulid目录
bulidFilePath = WORKSPACE
Version = ''
zipName = ''

packagePath = WORKSPACE + "\\package.ini"


# 获取版本号信息
def readVersion():
    config = configparser.ConfigParser()
    config.read(versionPath)
    xxx_Major = config.get("xxx", "xxx_Major")
    xxx_Minor = config.get("xxx", "xxx_Minor")
    xxx_Build = config.get("xxx", "xxx_Build")
    xxx_Revision = config.get("xxx", "xxx_Revision")
    Version = BUILD_VERSION + '-' + xxx_Major + '.' + xxx_Minor + '.' + xxx_Build + '.' + xxx_Revision
    print("【Build】version：" + Version)
    zipName = Version + '.zip'
    return zipName


# 包名写入配置文件
def packageConifg(name, remotepath):
    config = configparser.ConfigParser()
    config['package'] = {
        'name': name,
        'remotedir': remotepath
    }
    with open(packagePath, 'w', encoding='UTF-8') as configfile:
        config.write(configfile)

    configfile.close()
    print("【Build】packageConifg is done！")


# 使用zipfile做目录压缩
def zip_dir(dirname, zipfilename):
    filelist = []
    if os.path.isfile(dirname):
        filelist.append(dirname)
    else:
        for root, dirs, files in os.walk(dirname):
            for name in files:
                filelist.append(os.path.join(root, name))

    zf = zipfile.ZipFile(zipfilename, "w", zipfile.zlib.DEFLATED)
    for tar in filelist:
        arcname = tar[len(dirname):]
        # print arcname
        zf.write(tar, arcname)
    print("【Build】zip is done！")


# 清空文件夹及ZIP文件
def cleanFile(bulidFilePath, zipPath):
    # 判断文件夹是否存在
    if os.path.exists(bulidFilePath):
        shutil.rmtree(bulidFilePath)
        print("【Build】cleanFile is done！")
    # 判断zip文件是否存在
    if os.path.exists(zipPath):
        os.remove(zipPath)
        print("【Build】cleanZIP is done！")


# 复制目录
def copytree(src, dst, symlinks=False, ignore=None):
    for item in os.listdir(src):
        s = os.path.join(src, item)
        d = os.path.join(dst, item)
        if os.path.isdir(s):
            shutil.copytree(s, d, symlinks, ignore)
        else:
            shutil.copy2(s, d)


# 上传指定文件到 ftp 服务器
def uploadFile(dir_ftp, filename_ftp, filepath_local, host, port, username, password):
    """
    参数说明：
    * dir_ftp: 在 ftp 服务器上存储文件的路径；
    * filename_ftp: 存储到 ftp 服务器上的文件名；
    * filepath_local: 需要上传的本地源文件路径；
    * host: ftp 服务器地址（ip/域名）；
    * port: ftp 服务器端口号，一般是 21；
    * username、password: 登陆 ftp 服务器时的用户名和密码。
    """
    if not os.path.exists(filepath_local):
        print('【Build】找不到指定的源文件，请检查路径配置。')
        return False
    # connect
    try:
        f = ftplib.FTP()
        f.connect(host=host, port=port)
    except (socket.error, socket.gaierror) as e:
        print('【Build】----ERROR:cannot reach ' + host)
        print(e)
        return False
    # login
    try:
        f.login(user=username, passwd=password)
    except ftplib.error_perm as e:
        print('【Build】----ERROR:cannot login to server ' + host)
        print(e)
        f.quit()
        return False
    print('【Build】****Logged in as ' + username + ' to server ' + host)
    # change folder
    try:
        f.cwd(dir_ftp)
    except ftplib.error_perm as e:
        print('【Build】----ERROR:cannot CD to %s on %s' % (dir_ftp, host))
        print(e)
        f.quit()
        return False
    print('【Build】**** changed to %s folder on %s' % (dir_ftp, host))
    # upload file
    try:
        f.storbinary('STOR ' + filename_ftp, open(filepath_local, 'rb'))
    except ftplib.error_perm as e:
        print('【Build】----ERROR:cannot write %s on %s' % (filename_ftp, host))
        print(e)
        return False
    else:
        print('【Build】****Uploaded ' + filepath_local + ' to ' + host + ' as ' \
              + os.path.join(dir_ftp, filename_ftp))
        f.quit()
        return True


# FTP上传路径
def remotedir():
    if JOB_NAME == 'xxx-Client-Release':
        remotepath = 'xxx-release-info/client/'
    print("【消息内容】：" + remotepath)
    return remotepath


# 编译后的文件夹
def bulidFile():
    if JOB_NAME == 'xxx-Client-Release':
        bulidFilePath = 'D:\\Jenkins-workspace\\Jenkins\\workspace\\bin\\Client\\'
    print("【消息内容】：" + bulidFilePath)
    return bulidFilePath


def unloadNexus(loaclfile, repository):
    nexus_client = nexuscli.nexus_client.NexusClient('http://xxx.xx.xxx.xxx:8081', 'jenkins', 'xxxx')
    upload_count = nexus_client.upload(loaclfile, repository)
    print("【上传数量】：" + str(upload_count))

def packageHistory(zipname):
    Path = WORKSPACE+"\\package_history.txt"
    f = open(Path,'a',encoding='utf-8')
    f.write(zipname)
    f.write("\n")
    f.close()


if __name__ == '__main__':
    # 压缩包路径
    localpath = targetFilePath + readVersion()

    # FTP上传路径
    remotepath = remotedir()

    # 判断是否包含字符串
    if JOB_NAME.find("Client") >= 0:
        # 压缩编译文件
        zip_dir(bulidFile(), localpath)
        # 上传
        # uploadFile(remotepath, readVersion(), localpath, host,21, username, password)
        unloadNexus(localpath, remotepath)
        # 包名写入配置文件
        packageConifg(readVersion(), remotepath)
        # 清理
        cleanFile(bulidFile(), localpath)
        print('【Build】(JOB_NAME.find("Client") >= 0) done')
    else:
        pass

    print('【Build】****done')

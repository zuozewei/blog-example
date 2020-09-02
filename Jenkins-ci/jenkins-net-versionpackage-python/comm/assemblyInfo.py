# coding=utf-8

import os,re,configparser
from git import getcommits

# 接收jenkins当前JOB_NAME参数
workSpace = os.getenv("WORKSPACE")
JENKINS_HOME = os.getenv("JENKINS_HOME")
# 分支提交总次数
Revision = str(getcommits())
Major = ''
Minor = ''
Build = ''
path = workSpace+"\GlobalAssemblyInfo.cs"
versionPath = JENKINS_HOME+"\workspace\Version.ini"

config = configparser.ConfigParser()

with open(path,'r', encoding='UTF-8') as f:
    lines = list(f)
    new_lines = list()
    changed = False

    for line in lines:
        if "Revision = " in line:
            line = '    public const string Revision = "'+ Revision +'";\n'
            changed = True
        elif "Major = " in line:
            Major = re.sub("\D", "", line)
        elif "Minor = " in line:
            Minor = re.sub("\D", "", line)
        elif "Build = " in line:
            Build = re.sub("\D", "", line)
        else:
            pass

        new_lines.append(line)
        if not changed:
            continue

        with open(path, 'w',encoding='UTF-8') as f:
            f.write("".join(new_lines))

    FullVersion = Major + "." + Minor + "." + Build + "." + Revision
    print("【版本号】："+FullVersion)

    config['xxx'] = {
        'xxx_Major': Major,
        'xxx_Minor': Minor,
        'xxx_Build': Build,
        'xxx_Revision': Revision
    }

    with open(versionPath, 'w', encoding='UTF-8') as configfile:
        config.write(configfile)

    configfile.close()
    f.close()

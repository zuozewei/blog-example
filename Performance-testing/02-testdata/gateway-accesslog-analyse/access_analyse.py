#统计每个接口的处理时间
#请提前创建 log 并设置 logdir
import sys
import os
import pandas as pd
from urllib.parse import urlparse
import re

'''
全局参数
'''
mulu=os.path.dirname(__file__)
#日志文件存放路径
logdir="D:\log"
#存放统计所需的日志相关字段
logfile_format=os.path.join(mulu,"access_log.log")

print ("read from logfile \n")

'''
数据加载及预处理
'''
for eachfile in os.listdir(logdir):
    logfile=os.path.join(logdir,eachfile)
    with open(logfile, 'r') as fo:
        for line in fo:
            spline=line.split()
            #过滤字段中异常部分
            if spline[6]=="-":
                pass
            elif spline[6]=="GET":
                pass
            elif spline[-1]=="-":
                pass
            else:
                #解析成url地址
                parsed = urlparse(spline[6])
                # print('path    :', parsed.path)
                #排除数值参数
                interface = ''.join([i for i in parsed.path if not i.isdigit()])
                # print(interface)
                #重新写入文件
                with open(logfile_format, 'a') as fw:
                    fw.write(interface)
                    fw.write('\t')
                    fw.write(spline[-2])
                    fw.write('\n')
print ("output panda")

'''
数据分析
'''
#将统计的字段读入到dataframe中
reader=pd.read_table(logfile_format,sep='\t',engine='python',names=["interface","duration（ms）"] ,header=None,iterator=True)
loop=True
chunksize=10000000
chunks=[]
while loop:
    try:
        chunk=reader.get_chunk(chunksize)
        chunks.append(chunk)
    except StopIteration:
        loop=False
        print ("Iteration is stopped.")

df=pd.concat(chunks)
#df=df.set_index("interface")
#df=df.drop(["GET","-"])

df_groupd=df.groupby('interface')
df_groupd_max=df_groupd.max()
df_groupd_min= df_groupd.min()
df_groupd_mean= df_groupd.mean()
df_groupd_size= df_groupd.size()

'''
数据装载
'''
df_ana=pd.concat([df_groupd_max,df_groupd_min,df_groupd_mean,df_groupd_size],axis=1,keys=["max","min","average","count"])
print ("output excel")
df_ana.to_excel("result.xls")
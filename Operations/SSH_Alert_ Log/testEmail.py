#!/usr/bin/python
# -*- coding: UTF-8 -*-

import smtplib
from email import encoders
from email.header import Header
from email.mime.text import MIMEText
from email.utils import parseaddr, formataddr
import sys
import io
import locale

# 设置标准输出的编码
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

def send_mail(dtime,duser,dip,dhostname):
    #基础信息
    # from_addr = input("From:")
    from_addr = "xxxm@xxxxx.com.cn"
    password = "xxxxxxxx"  # 修复了密码前面的空格
    #to_addr = from_addr
    to_addr = "xxxx@xxxxxx.com.cn"
    # password = raw_input("Password:")
    # to_addr = input("To:")
    
    def _format_addr(s):
        name, addr = parseaddr(s)
        return formataddr((Header(name, 'utf-8').encode(), addr))

    smtp_server = "smtp.xxxxx.com.cn"
    mimetex = '您的机器:',dhostname,'，于:',dtime,'，被IP:',dip,'以账号',duser,'进行登录,请确认是否为公司员工。'
    #构造邮件
    msg = MIMEText(''.join(mimetex), 'plain', 'utf-8')
    msg['From'] = _format_addr("Cloud Platform SSH Alert Logs")
    msg['To'] = _format_addr("xxxxxxx@xxxxxx.com.cn")
    msg['Subject'] = Header("来自Cloud Platform SSH Alert Logs", 'utf-8').encode()
    
    #发送邮件
    try:
        server = smtplib.SMTP_SSL(smtp_server, 465)
        server.set_debuglevel(1)
        server.login(from_addr, password)
        server.sendmail(from_addr, [to_addr], msg.as_string())
        try:
            print("邮件发送成功")
        except UnicodeEncodeError:
            print("Email sent successfully")
    except smtplib.SMTPAuthenticationError as e:
        try:
            print(f"认证错误: {e}")
        except UnicodeEncodeError:
            print(f"Authentication error: {e}")
    except smtplib.SMTPException as e:
        try:
            print(f"SMTP错误: {e}")
        except UnicodeEncodeError:
            print(f"SMTP error: {e}")
    except Exception as e:
        try:
            print(f"发送邮件时发生错误: {e}")
        except UnicodeEncodeError:
            print(f"Error sending email: {e}")
    finally:
        try:
            server.quit()
        except:
            pass


if __name__ == "__main__":
    if len(sys.argv) < 5:
        try:
            print("用法: python testEmail.py <时间> <用户> <IP> <主机名>")
        except UnicodeEncodeError:
            print("Usage: python testEmail.py <time> <user> <IP> <hostname>")
        sys.exit(1)
    send_mail(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
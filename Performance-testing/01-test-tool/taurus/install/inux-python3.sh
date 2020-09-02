#!/bin/bash
# author: zuozewei

# 先安装安装几个必须的包，以方便后续的操作 
yum -y install wget gcc make  zlib-devel readline-devel  bzip2-devel ncurses-devel sqlite-devel gdbm-devel xz-devel tk-devel openssl-devel
# 上Python的官网 下载源码包 
wget https://www.python.org/ftp/python/3.6.1/Python-3.6.1.tar.xz
# 解包，解压缩 
xz -d Python-3.6.1.tar.xz
tar -xvf Python-3.6.1.tar
# 编译 
cd Python-3.6.1
./configure --prefix=/usr/local/python3.6 --enable-optimizations
make
make install
# 为了避免每次都输入一大串路径，加个链接 
ln -s /usr/local/python3.6/bin/python3 /usr/bin/python3

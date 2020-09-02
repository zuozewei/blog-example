#!/bin/bash
# author: zuozewei

# 下载源代码
wget --no-check-certificate https://github.com/pypa/pip/archive/9.0.1.tar.gz
tar -zvxf 9.0.1.tar.gz
cd pip-9.0.1

# 使用 Python 3 安装
python3 setup.py install

# 创建链接
ln -s /usr/local/python3.6/bin/pip /usr/bin/pip3

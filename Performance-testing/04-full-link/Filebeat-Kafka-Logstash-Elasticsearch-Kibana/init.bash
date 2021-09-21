# 需要设置系统内核参数，否则 ES 会因为内存不足无法启动
# 改变设置
sysctl -w vm.max_map_count=262144
# 使之立即生效
sysctl -p


# 创建 logstash 目录，并将 Logstash 的配置文件 logstash.conf 拷贝到该目录
mkdir -p /mydata/logstash

# 需要创建 elasticsearch/data 目录并设置权限，否则 ES 会因为无权限访问而启动失败
mkdir -p /mydata/elasticsearch/data/
chmod 777 /mydata/elasticsearch/data/

# elasticsearch 需要安装中文分词器 IKAnalyzer，并重新启动。
docker exec -it elasticsearch /bin/bash
#此命令需要在容器中运行
elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.6.2/elasticsearch-analysis-ik-7.6.2.zip
docker restart elasticsearch

# logstas h需要安装 json_lines 插件，并重新启动。
docker exec -it logstash /bin/bash
logstash-plugin install logstash-codec-json_lines
docker restart logstash
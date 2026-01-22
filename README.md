# Blog Example

[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/zuozewei/blog-example)

[🇬🇧 English Version](README_EN.md) | 🇨🇳 中文版

> 这里整理了我这些年写博客用到的所有示例代码，从 AI/LLM 到 Kubernetes，从性能测试到 Jenkins CI/CD，都是实战中跑过的代码

## 📖 项目简介

写技术博客这些年，积累了不少示例代码。与其让它们散落在各个项目里，不如整理到一个仓库里，方便自己查阅，也希望能帮到有需要的同学。

这里面的代码都是我在实际项目中用过的，不是那种玩具示例。每个目录都有对应的 README，里面记录了踩过的坑和一些实践经验。配合我的博客文章一起看，效果更佳。

## 🎯 主要技术领域

### 🤖 AI & 大语言模型 (AI-LLM)
- **LangChain 框架实践** - 核心抽象、格式化响应、聊天历史管理、角色扮演机器人
- **OpenAI API 集成** - 首次调用、多轮对话、流式输出
- **Agent 实现** - 从头实现 Agent、使用 LangChain 框架实现 ReAct Agent
- **会议转录系统** - 基于 Whisper 和 DeepSeek 的会议转录与摘要生成

### ☕ Java API 测试 (Java-api-test)
- **数据驱动测试** - TestNG 数据驱动、文本数据驱动、配置文件读取
- **日志框架** - Logback、ELK、Graylog 集成与配置
- **测试报告** - ExtentReport、KLOV 报告生成
- **API 管理** - Swagger 集成
- **断言库** - AssertJ 使用
- **Mock 服务** - Moco 框架实践
- **自动化通知** - 邮件通知集成
- **HTTPS 测试** - SSL/TLS 配置
- **工具类** - Hutool、关联参数处理
- **编码规范** - 阿里巴巴 Java 开发手册

### 🚀 Jenkins CI/CD (Jenkins-ci)
- **应用构建模板** - iOS/Android 应用自动化构建
- **Ansible 集成** - Python 脚本实现自动化部署
- **变更通知** - 钉钉通知集成
- **Maven 构建** - Spring Boot 项目构建
- **Docker 集成** - 容器化构建与部署
- **单元测试** - .NET 项目单元测试
- **版本打包** - .NET 项目版本管理
- **代码质量** - SonarQube 集成、P3C 规范检查
- **YouTrack 集成** - 问题跟踪通知

### ☸️ Kubernetes (Kubernetes)
- **集群部署** - 单节点、高可用集群部署
- **可视化** - Dashboard 2.0+ 部署
- **网络** - Traefik v2.4、网络性能测试
- **存储** - Rook-Ceph、NFS Provisioner、本地 PV
- **监控** - Prometheus、Grafana、SkyWalking
- **日志** - Fluentd、ELK Stack
- **服务发现** - Nacos 部署
- **数据库** - MySQL、MongoDB、Redis、RabbitMQ 部署与监控
- **中间件** - Kafka、Zookeeper 部署
- **指标导出** - 各种 Exporter 配置

### 📊 性能测试 (Performance-testing)
- **测试工具** - JMeter、nGrinder、Taurus
- **代码性能** - ContiPerf 单元测试性能测试
- **测试数据** - 图片生成工具、网关日志分析
- **性能监控** - Telegraf、InfluxDB、Grafana、Prometheus
- **全链路监控** - Spring Cloud Sleuth、Zipkin、ELK
- **Java Agent** - 字节码增强
- **Go 监控** - Prometheus 集成
- **BTrace** - Java 动态追踪

### 📱 自动化测试 (auto-test)
- **移动端测试** - Android 应用自动化测试框架
- **测试报告** - 自定义报告生成
- **元素定位** - 页面元素定位与管理

### 🔧 运维实践 (Operations)
- **系统运维** - Nginx 离线安装、Oracle 备份
- **日志告警** - SSH 日志监控与告警

## 🚀 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/zuozewei/blog-example.git
cd blog-example
```

### 2. 选择你感兴趣的领域

**想试试 AI/LLM？**
```bash
cd AI-LLM/LangChain
# 查看对应目录的 README 获取详细说明
```

**想学习 Java 测试？**
```bash
cd Java-api-test/01-data-driven
# 各子目录都有独立的 README 和示例代码
```

**想部署 Kubernetes？**
```bash
cd Kubernetes/k8s-instal-script
# 包含完整的集群部署脚本
```

**想搞性能测试？**
```bash
cd Performance-testing/01-test-tool/jmeter
# 各种 JMeter 实战案例
```

**想搭 Jenkins 流水线？**
```bash
cd Jenkins-ci/jenkins-maven-springboot
# Spring Boot 项目自动化构建示例
```


## 📚 博客文章

本项目的代码示例配套详细的技术博客文章：

- [技术文章系列整理（持续更新）](https://zuozewei.blog.csdn.net/article/details/82911628)

## 🎯 适合谁使用

- **技术博主**：需要代码示例配合文章讲解
- **测试工程师**：学习 Java 测试框架和性能测试
- **运维工程师**：参考 K8s 部署和监控方案
- **AI 开发者**：快速上手 LangChain 和 OpenAI API
- **学生/初学者**：通过实际代码学习各种技术
- **架构师**：了解不同技术栈的实践方案

## 🏗️ 项目结构

```
blog-example/
├── AI-LLM/                          # AI 与大语言模型
│   ├── LangChain/                   # LangChain 框架实践
│   ├── OpenAI API/                  # OpenAI API 集成
│   ├── 从头实现一个Agent/           # Agent 实现
│   ├── 使用LangChain框架实现ReAct Agent/
│   └── tx-meeting-transcribe/       # 会议转录系统
├── Java-api-test/                   # Java API 测试
│   ├── 00-testdata/                # 测试数据处理
│   │   ├── springboot-fastjson-demo/  # FastJson 示例
│   │   └── springboot-data-backup-recovery-demo/  # 数据备份恢复
│   ├── 01-data-driven/             # 数据驱动测试
│   │   ├── springboot-configuration-demo/  # 配置文件读取
│   │   ├── springboot-data-driven-framework-demo/  # 数据驱动框架
│   │   ├── springboot-testng-data-driven-demo/  # TestNG 数据驱动
│   │   └── springboot-text-data-driven-demo/  # 文本数据驱动
│   ├── 02-environmental-management/# 环境管理
│   │   └── springboot-mulienvironment-demo/  # 多环境配置
│   ├── 03-log-framework/           # 日志框架
│   │   ├── springboot-logback-aop-demo/  # Logback AOP
│   │   ├── springboot-logback-demo/  # Logback 基础
│   │   ├── springboot-logback-elk-demo/  # Logback + ELK
│   │   └── springboot-logback-graylog-demo/  # Logback + Graylog
│   ├── 04-orm-framework/           # ORM 框架
│   │   └── springboot-data-jpa-sample/  # JPA 示例
│   ├── 05-testreport/              # 测试报告
│   │   ├── extentklovreporter-demo/  # KLOV 报告
│   │   ├── extentreport-demo2/     # ExtentReport 示例2
│   │   └── springboot-extentreport-demo1/  # ExtentReport 示例1
│   ├── 06-api-management/          # API 管理
│   │   └── springboot-swagger-demo/  # Swagger 集成
│   ├── 07-assert/                  # 断言库
│   │   └── springboot-assertj-demo/  # AssertJ 示例
│   ├── 08-mock/                    # Mock 服务
│   │   └── moco-demo/            # Moco 框架
│   ├── 09-auto-notification/       # 自动化通知
│   │   └── springboot-mail-sampler/  # 邮件通知
│   ├── 10-https/                   # HTTPS 测试
│   │   └── springboot-https-demo/  # HTTPS 配置
│   ├── 11-tools/                   # 工具类
│   │   ├── springboot-associateparam-demo/  # 关联参数
│   │   └── springboot-hutool-demo/  # Hutool 工具
│   └── 12-Alibaba Java Coding Guidelines/  # 阿里规范
├── Jenkins-ci/                      # Jenkins CI/CD
│   ├── JenkinsTemplateForApp/      # 应用构建模板
│   ├── jenkins-ansible-python/      # Ansible 集成
│   ├── jenkins-changeLog-dingtalk-notifications/  # 变更通知
│   ├── jenkins-maven-springboot/    # Maven 构建
│   ├── jenkins-maven-springboot-docker/  # Docker 集成
│   ├── jenkins-net-unitautotest/    # .NET 单元测试
│   ├── jenkins-net-versionpackage-python/  # 版本打包
│   ├── pipeline-sonarqube-python-dingtalk-notifications-webhook/  # SonarQube + 钉钉
│   ├── sonarqube-p3c/              # P3C 规范检查
│   ├── sonarqube-python-dingtalk-notifications/  # SonarQube 通知
│   └── youtrack-dingtalk-notifications/  # YouTrack 通知
├── Kubernetes/                      # Kubernetes 部署
│   ├── k8s-dashboard/              # Dashboard
│   ├── k8s-ek/                     # Elasticsearch + Kibana
│   ├── k8s-fluentd/               # Fluentd 日志收集
│   ├── k8s-harbor/                # Harbor 镜像仓库
│   ├── k8s-instal-script/          # 集群安装脚本
│   ├── k8s-install-HA-cluster/    # 高可用集群
│   ├── k8s-jmeter-cluster/        # JMeter 集群
│   ├── k8s-kafka-zookeeper/      # Kafka + Zookeeper
│   ├── k8s-kube-promethues/      # Prometheus 监控
│   ├── k8s-kube-promethues-auto-discover/
│   ├── k8s-metrics-server/        # Metrics Server
│   ├── k8s-mongodb-exporter/     # MongoDB 监控
│   ├── k8s-mysql-exporter/       # MySQL 监控
│   ├── k8s-mysql-pv-local/       # MySQL 本地存储
│   ├── k8s-nacos-v1.4/           # Nacos
│   ├── k8s-network-benchmark/    # 网络性能测试
│   ├── k8s-nfs-provisioner/      # NFS 存储提供者
│   ├── k8s-nfs-subdir-external-provisioner/
│   ├── k8s-prometheus/           # Prometheus
│   ├── k8s-rabbitmq-exporter/   # RabbitMQ 监控
│   ├── k8s-redis-exporter/      # Redis 监控
│   ├── k8s-rook-ceph/            # Rook-Ceph 存储
│   ├── k8s-skywalking/          # SkyWalking
│   └── k8s-traefik-v2.4/        # Traefik
├── Performance-testing/           # 性能测试
│   ├── 00-code-performance/      # 代码性能测试
│   ├── 01-test-tool/             # 测试工具
│   │   ├── jmeter/              # JMeter
│   │   │   ├── activemq/       # ActiveMQ 测试
│   │   │   ├── csv-redis-data-set-config/  # Redis 数据集
│   │   │   ├── dunshan-dubbo/   # Dubbo 测试
│   │   │   ├── dynamic-control-tps/  # 动态控制 TPS
│   │   │   ├── file/           # 文件上传测试
│   │   │   ├── hls/            # HLS 流媒体测试
│   │   │   ├── hprose/         # Hprose RPC 测试
│   │   │   ├── java-api/       # Java API 测试
│   │   │   ├── javarequest/    # Java Request
│   │   │   ├── jmeter-sm2-javarequest/  # SM2 加密测试
│   │   │   ├── mongodb/        # MongoDB 测试
│   │   │   ├── openapi-generator-cli/  # OpenAPI 生成器
│   │   │   ├── plugin/         # JMeter 插件
│   │   │   ├── seven_days/     # 七天实战
│   │   │   └── startJmeter/    # 启动 JMeter
│   │   ├── nGrinder/            # nGrinder
│   │   └── taurus/              # Taurus
│   ├── 02-testdata/             # 测试数据
│   │   ├── gateway-accesslog-analyse/  # 网关日志分析
│   │   └── picturetools/        # 图片生成工具
│   ├── 03-performance-monitoring/# 性能监控
│   │   ├── btrace/             # BTrace 追踪
│   │   ├── jmeter-elasticsearch-filebeats-kibana/  # JMeter + ELK
│   │   ├── prometheus-blackbox_exporter-grafana/  # Blackbox Exporter
│   │   ├── prometheus-node_exporter-grafana/  # Node Exporter
│   │   ├── springcloud-log/     # Spring Cloud 日志
│   │   ├── telegraf-Influxdb-grafana-jmx/  # JMX 监控
│   │   ├── telegraf-Influxdb-grafana-linux/  # Linux 监控
│   │   ├── telegraf-Influxdb-grafana-logparser/  # 日志解析
│   │   ├── telegraf-Influxdb-grafana-nvidiagpu/  # GPU 监控
│   │   ├── telegraf-Influxdb-grafana-python-oracle/  # Oracle 监控
│   │   └── telegraf-Influxdb-grafana-window/  # Windows 监控
│   └── 04-full-link/            # 全链路监控
│       ├── Filebeat-Kafka-Logstash-Elasticsearch-Kibana/  # ELK 日志收集
│       ├── go_prometheus/       # Go 监控
│       ├── gor-code/            # Gor 录制回放
│       ├── javaagent/           # Java Agent
│       ├── springboot-demo/     # Spring Boot 示例
│       ├── springboot-flag-track-demo/  # 标志追踪
│       ├── springboot-threadlocal-demo/ # ThreadLocal 示例
│       └── springcloud-sleuth-zipkin-demo/  # 链路追踪
├── auto-test/                    # 自动化测试
│   └── comsevenday/             # 移动端测试
├── Operations/                  # 运维实践
│   ├── Oracle-backup/          # Oracle 备份
│   ├── SSH_Alert_ Log/         # SSH 日志告警
│   └── ubuntu_nginx_offline/   # Nginx 离线安装
└── README.md                   # 项目说明文档
```

## 💡 核心技术亮点

- **实战驱动**：所有代码都来自真实项目，不是玩具示例
- **全栈覆盖**：从开发、测试到运维，一条龙技术栈
- **最新技术**：包含 LangChain、K8s、Prometheus 等前沿技术
- **生产就绪**：很多配置可以直接用于生产环境
- **踩坑记录**：每个示例都记录了实际踩过的坑和解决方案

## 📝 使用说明

### 环境要求

根据具体示例的技术栈，可能需要以下环境：

- **Java 开发** - JDK 8+、Maven/Gradle
- **Python 开发** - Python 3.8+、pip
- **容器化** - Docker、Kubernetes
- **CI/CD** - Jenkins
- **数据库** - MySQL、MongoDB、Redis
- **消息队列** - RabbitMQ、Kafka


## ❓ 常见问题

**Q: 代码可以直接用于生产环境吗？**
A: 部分配置（如 K8s 部署脚本）可以直接使用，但建议根据实际环境调整。

**Q: 需要什么基础才能学习这些代码？**
A: 不同领域要求不同。Java 测试部分需要 Java 基础，K8s 部署需要了解容器概念，AI/LLM 部分建议先看博客文章了解背景。

**Q: 代码会持续更新吗？**
A: 会根据博客文章的发布持续添加新示例，也会修复发现的问题。

**Q: 遇到问题怎么反馈？**
A: 可以在 GitHub 提 Issue，或者通过博客留言交流。


## 🤝 贡献指南

欢迎贡献代码和改进建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 📅 更新日志

### 最近更新

<details>
<summary><b>📆 2026-01</b> - LangChain 框架实践系列</summary>

- ✨ LangChain 框架完整实践（零基础入门、角色扮演机器人、聊天历史管理、格式化响应）
- 🤖 ReAct Agent 双实现（从头实现 + LangChain 框架）
- 🔒 依赖更新：升级 fastjson 版本

</details>

<details>
<summary><b>📆 2025-08</b> - 技术规范文档</summary>

- 📄 添加 GB-T-44260-2024-虚拟电厂资源配置与评估技术规范

</details>

<details>
<summary><b>📆 2025-05</b> - AI 自动化与安全监控</summary>

- 🎙️ AI 自动化某讯会议转录与摘要生成系统（Whisper + DeepSeek）
- 🔐 安全监控之 Linux 核心资产 SSH 连接监测邮件示例

</details>

<details>
<summary><b>📆 2025-04</b> - OpenAI API 与配置优化</summary>

- 🌐 增加 OpenAI API 示例
- 📝 添加会议转录功能
- ⚡ 添加 Nginx 高性能配置文件

</details>

---

### 历史记录

<details>
<summary><b>📜 点击查看完整历史记录（2021-2024）</b></summary>

#### 2024 年

**2024-05**
- 增加 pom.xml 配置文件

**2024-04**
- 新增 JMeter 自定义 Java Sampler 实现国密 SM2 示例代码

**2023-08**
- 更新 application.yml 配置

**2023-06**
- 更新 Prometheus RabbitMQ 部署配置

#### 2022 年

**2022-01**
- 项目 README 文档完善

#### 2021 年

**2021-12**
- Kubernetes 集群部署 Zipkin+Kafka+ElasticSearch 实现链路追踪
- Traefik、Nacos 部署文件更新

**2021-11**
- JMeter 测试 Dubbo 示例
- JMeter 通过 Spring Boot 工程启动示例
- JMeter 基础示例

**2021-10** 🏗️ 项目基础建设期
- **Kubernetes 部署系列**：Nacos、Metrics Server、Kafka+Zookeeper、Traefik、RabbitMQ、MongoDB、Prometheus、MySQL、Redis、NFS Provisioner
- **自动化测试**：移动端 Appium 框架数据驱动示例
- **性能监控**：Java Agent 示例

**2021-09**
- **性能测试**：JMeter 5.1.1、GoReplay、TPS 控制、快速启动、上传下载
- **监控追踪**：Sleuth+Zipkin、Gor、Golang Prometheus、ELK 日志分析
- **其他**：Oracle 备份、Moco Mock 服务

**2021-08**
- CSV vs Redis 性能比较示例
- nGrinder 性能测试示例

**2021-07**
- 阿里 Java 开发手册

**2021-04**
- ThreadLocal 跨线程传递示例
- 流量标记 demo

**2021-03**
- k8s-jmeter 集群部署
- Prometheus 监控配置
- Kubernetes 复杂 MySQL 数据库搭建
- Swagger & openapi-generator 生成 JMeter 脚本
- Python 统计分析 access 日志

**2021-02**
- Spring Boot 日志配置
- kube-prometheus 自动发现

</details>



##  联系方式

- **博客**: [CSDN 博客](https://zuozewei.blog.csdn.net) - 详细技术文章和踩坑记录
- **GitHub**: [zuozewei](https://github.com/zuozewei) - 欢迎提 Issue 和 PR
- **邮箱**: [zuozewei@hotmail.com](mailto:zuozewei@hotmail.com) - 技术交流

如果这个项目对你有帮助，欢迎给个 Star 支持一下！⭐

## 🙏 致谢

感谢所有贡献者和使用者的支持！


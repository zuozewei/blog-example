# Blog Example

[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/zuozewei/blog-example)

🇬🇧 English Version | [🇨🇳 中文版](README.md)

> A collection of all example code I've used for blogging over the years, from AI/LLM to Kubernetes, from performance testing to Jenkins CI/CD, all battle-tested in real projects

## 📖 Project Overview

Over the years of writing technical blogs, I've accumulated quite a bit of example code. Instead of letting them scatter across various projects, I decided to organize them into one repository for easy reference, and hopefully to help those who need them.

The code here has been used in actual projects, not toy examples. Each directory has its own README, recording the pitfalls I've encountered and practical experience. It works best when read alongside my blog posts.

## 🎯 Main Technical Areas

### 🤖 AI & Large Language Models (AI-LLM)
- **LangChain Framework Practice** - Core abstractions, formatted responses, chat history management, role-playing bots
- **OpenAI API Integration** - First call, multi-turn dialogue, streaming output
- **Agent Implementation** - Implementing Agent from scratch, implementing ReAct Agent using LangChain framework
- **Meeting Transcription System** - Meeting transcription and summary generation based on Whisper and DeepSeek

### ☕ Java API Testing (Java-api-test)
- **Data-Driven Testing** - TestNG data-driven, text data-driven, configuration file reading
- **Logging Frameworks** - Logback, ELK, Graylog integration and configuration
- **Test Reports** - ExtentReport, KLOV report generation
- **API Management** - Swagger integration
- **Assertion Libraries** - AssertJ usage
- **Mock Services** - Moco framework practice
- **Automated Notifications** - Email notification integration
- **HTTPS Testing** - SSL/TLS configuration
- **Utility Classes** - Hutool, associated parameter handling
- **Coding Standards** - Alibaba Java Development Guidelines

### 🚀 Jenkins CI/CD (Jenkins-ci)
- **Application Build Templates** - iOS/Android application automated builds
- **Ansible Integration** - Python scripts for automated deployment
- **Change Notifications** - DingTalk notification integration
- **Maven Builds** - Spring Boot project builds
- **Docker Integration** - Containerized builds and deployment
- **Unit Testing** - .NET project unit testing
- **Version Packaging** - .NET project version management
- **Code Quality** - SonarQube integration, P3C specification checks
- **YouTrack Integration** - Issue tracking notifications

### ☸️ Kubernetes (Kubernetes)
- **Cluster Deployment** - Single-node, high-availability cluster deployment
- **Visualization** - Dashboard 2.0+ deployment
- **Networking** - Traefik v2.4, network performance testing
- **Storage** - Rook-Ceph, NFS Provisioner, local PV
- **Monitoring** - Prometheus, Grafana, SkyWalking
- **Logging** - Fluentd, ELK Stack
- **Service Discovery** - Nacos deployment
- **Databases** - MySQL, MongoDB, Redis, RabbitMQ deployment and monitoring
- **Middleware** - Kafka, Zookeeper deployment
- **Metrics Export** - Various Exporter configurations

### 📊 Performance Testing (Performance-testing)
- **Testing Tools** - JMeter, nGrinder, Taurus
- **Code Performance** - ContiPerf unit test performance testing
- **Test Data** - Image generation tools, gateway log analysis
- **Performance Monitoring** - Telegraf, InfluxDB, Grafana, Prometheus
- **Full-Link Monitoring** - Spring Cloud Sleuth, Zipkin, ELK
- **Java Agent** - Bytecode enhancement
- **Go Monitoring** - Prometheus integration
- **BTrace** - Java dynamic tracing

### 📱 Automated Testing (auto-test)
- **Mobile Testing** - Android application automated testing framework
- **Test Reports** - Custom report generation
- **Element Location** - Page element location and management

### 🔧 Operations Practice (Operations)
- **System Operations** - Nginx offline installation, Oracle backup
- **Log Alerts** - SSH log monitoring and alerting

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/zuozewei/blog-example.git
cd blog-example
```

### 2. Choose Your Area of Interest

**Want to try AI/LLM?**
```bash
cd AI-LLM/LangChain
# Check the corresponding directory README for detailed instructions
```

**Want to learn Java testing?**
```bash
cd Java-api-test/01-data-driven
# Each subdirectory has independent README and example code
```

**Want to deploy Kubernetes?**
```bash
cd Kubernetes/k8s-instal-script
# Contains complete cluster deployment scripts
```

**Want to do performance testing?**
```bash
cd Performance-testing/01-test-tool/jmeter
# Various JMeter practical cases
```

**Want to build Jenkins pipelines?**
```bash
cd Jenkins-ci/jenkins-maven-springboot
# Spring Boot project automated build examples
```


## 📚 Blog Posts

The code examples in this project are accompanied by detailed technical blog posts:

- [Technical Article Series (Continuously Updated)](https://zuozewei.blog.csdn.net/article/details/82911628)

## 🎯 Who Should Use This

- **Technical Bloggers**: Need code examples to accompany article explanations
- **Test Engineers**: Learn Java testing frameworks and performance testing
- **Operations Engineers**: Reference K8s deployment and monitoring solutions
- **AI Developers**: Get started quickly with LangChain and OpenAI API
- **Students/Beginners**: Learn various technologies through actual code
- **Architects**: Understand practical solutions across different technology stacks

## 🏗️ Project Structure

```
blog-example/
├── AI-LLM/                          # AI & Large Language Models
│   ├── LangChain/                   # LangChain Framework Practice
│   ├── OpenAI API/                  # OpenAI API Integration
│   ├── 从头实现一个Agent/           # Agent Implementation
│   ├── 使用LangChain框架实现ReAct Agent/
│   └── tx-meeting-transcribe/       # Meeting Transcription System
├── Java-api-test/                   # Java API Testing
│   ├── 00-testdata/                # Test Data Processing
│   │   ├── springboot-fastjson-demo/  # FastJson Examples
│   │   └── springboot-data-backup-recovery-demo/  # Data Backup Recovery
│   ├── 01-data-driven/             # Data-Driven Testing
│   │   ├── springboot-configuration-demo/  # Configuration File Reading
│   │   ├── springboot-data-driven-framework-demo/  # Data-Driven Framework
│   │   ├── springboot-testng-data-driven-demo/  # TestNG Data-Driven
│   │   └── springboot-text-data-driven-demo/  # Text Data-Driven
│   ├── 02-environmental-management/# Environment Management
│   │   └── springboot-mulienvironment-demo/  # Multi-Environment Configuration
│   ├── 03-log-framework/           # Logging Frameworks
│   │   ├── springboot-logback-aop-demo/  # Logback AOP
│   │   ├── springboot-logback-demo/  # Logback Basics
│   │   ├── springboot-logback-elk-demo/  # Logback + ELK
│   │   └── springboot-logback-graylog-demo/  # Logback + Graylog
│   ├── 04-orm-framework/           # ORM Frameworks
│   │   └── springboot-data-jpa-sample/  # JPA Examples
│   ├── 05-testreport/              # Test Reports
│   │   ├── extentklovreporter-demo/  # KLOV Reports
│   │   ├── extentreport-demo2/     # ExtentReport Example 2
│   │   └── springboot-extentreport-demo1/  # ExtentReport Example 1
│   ├── 06-api-management/          # API Management
│   │   └── springboot-swagger-demo/  # Swagger Integration
│   ├── 07-assert/                  # Assertion Libraries
│   │   └── springboot-assertj-demo/  # AssertJ Examples
│   ├── 08-mock/                    # Mock Services
│   │   └── moco-demo/            # Moco Framework
│   ├── 09-auto-notification/       # Automated Notifications
│   │   └── springboot-mail-sampler/  # Email Notifications
│   ├── 10-https/                   # HTTPS Testing
│   │   └── springboot-https-demo/  # HTTPS Configuration
│   ├── 11-tools/                   # Utility Classes
│   │   ├── springboot-associateparam-demo/  # Associated Parameters
│   │   └── springboot-hutool-demo/  # Hutool Utilities
│   └── 12-Alibaba Java Coding Guidelines/  # Alibaba Guidelines
├── Jenkins-ci/                      # Jenkins CI/CD
│   ├── JenkinsTemplateForApp/      # Application Build Templates
│   ├── jenkins-ansible-python/      # Ansible Integration
│   ├── jenkins-changeLog-dingtalk-notifications/  # Change Notifications
│   ├── jenkins-maven-springboot/    # Maven Builds
│   ├── jenkins-maven-springboot-docker/  # Docker Integration
│   ├── jenkins-net-unitautotest/    # .NET Unit Testing
│   ├── jenkins-net-versionpackage-python/  # Version Packaging
│   ├── pipeline-sonarqube-python-dingtalk-notifications-webhook/  # SonarQube + DingTalk
│   ├── sonarqube-p3c/              # P3C Specification Checks
│   ├── sonarqube-python-dingtalk-notifications/  # SonarQube Notifications
│   └── youtrack-dingtalk-notifications/  # YouTrack Notifications
├── Kubernetes/                      # Kubernetes Deployment
│   ├── k8s-dashboard/              # Dashboard
│   ├── k8s-ek/                     # Elasticsearch + Kibana
│   ├── k8s-fluentd/               # Fluentd Log Collection
│   ├── k8s-harbor/                # Harbor Image Registry
│   ├── k8s-instal-script/          # Cluster Installation Scripts
│   ├── k8s-install-HA-cluster/    # High-Availability Cluster
│   ├── k8s-jmeter-cluster/        # JMeter Cluster
│   ├── k8s-kafka-zookeeper/      # Kafka + Zookeeper
│   ├── k8s-kube-promethues/      # Prometheus Monitoring
│   ├── k8s-kube-promethues-auto-discover/
│   ├── k8s-metrics-server/        # Metrics Server
│   ├── k8s-mongodb-exporter/     # MongoDB Monitoring
│   ├── k8s-mysql-exporter/       # MySQL Monitoring
│   ├── k8s-mysql-pv-local/       # MySQL Local Storage
│   ├── k8s-nacos-v1.4/           # Nacos
│   ├── k8s-network-benchmark/    # Network Performance Testing
│   ├── k8s-nfs-provisioner/      # NFS Storage Provisioner
│   ├── k8s-nfs-subdir-external-provisioner/
│   ├── k8s-prometheus/           # Prometheus
│   ├── k8s-rabbitmq-exporter/   # RabbitMQ Monitoring
│   ├── k8s-redis-exporter/      # Redis Monitoring
│   ├── k8s-rook-ceph/            # Rook-Ceph Storage
│   ├── k8s-skywalking/          # SkyWalking
│   └── k8s-traefik-v2.4/        # Traefik
├── Performance-testing/           # Performance Testing
│   ├── 00-code-performance/      # Code Performance Testing
│   ├── 01-test-tool/             # Testing Tools
│   │   ├── jmeter/              # JMeter
│   │   │   ├── activemq/       # ActiveMQ Testing
│   │   │   ├── csv-redis-data-set-config/  # Redis Data Set
│   │   │   ├── dunshan-dubbo/   # Dubbo Testing
│   │   │   ├── dynamic-control-tps/  # Dynamic TPS Control
│   │   │   ├── file/           # File Upload Testing
│   │   │   ├── hls/            # HLS Streaming Testing
│   │   │   ├── hprose/         # Hprose RPC Testing
│   │   │   ├── java-api/       # Java API Testing
│   │   │   ├── javarequest/    # Java Request
│   │   │   ├── jmeter-sm2-javarequest/  # SM2 Encryption Testing
│   │   │   ├── mongodb/        # MongoDB Testing
│   │   │   ├── openapi-generator-cli/  # OpenAPI Generator
│   │   │   ├── plugin/         # JMeter Plugins
│   │   │   ├── seven_days/     # Seven-Day Practice
│   │   │   └── startJmeter/    # Start JMeter
│   │   ├── nGrinder/            # nGrinder
│   │   └── taurus/              # Taurus
│   ├── 02-testdata/             # Test Data
│   │   ├── gateway-accesslog-analyse/  # Gateway Log Analysis
│   │   └── picturetools/        # Image Generation Tools
│   ├── 03-performance-monitoring/# Performance Monitoring
│   │   ├── btrace/             # BTrace Tracing
│   │   ├── jmeter-elasticsearch-filebeats-kibana/  # JMeter + ELK
│   │   ├── prometheus-blackbox_exporter-grafana/  # Blackbox Exporter
│   │   ├── prometheus-node_exporter-grafana/  # Node Exporter
│   │   ├── springcloud-log/     # Spring Cloud Logging
│   │   ├── telegraf-Influxdb-grafana-jmx/  # JMX Monitoring
│   │   ├── telegraf-Influxdb-grafana-linux/  # Linux Monitoring
│   │   ├── telegraf-Influxdb-grafana-logparser/  # Log Parsing
│   │   ├── telegraf-Influxdb-grafana-nvidiagpu/  # GPU Monitoring
│   │   ├── telegraf-Influxdb-grafana-python-oracle/  # Oracle Monitoring
│   │   └── telegraf-Influxdb-grafana-window/  # Windows Monitoring
│   └── 04-full-link/            # Full-Link Monitoring
│       ├── Filebeat-Kafka-Logstash-Elasticsearch-Kibana/  # ELK Log Collection
│       ├── go_prometheus/       # Go Monitoring
│       ├── gor-code/            # Gor Recording Replay
│       ├── javaagent/           # Java Agent
│       ├── springboot-demo/     # Spring Boot Examples
│       ├── springboot-flag-track-demo/  # Flag Tracking
│       ├── springboot-threadlocal-demo/ # ThreadLocal Examples
│       └── springcloud-sleuth-zipkin-demo/  # Distributed Tracing
├── auto-test/                    # Automated Testing
│   └── comsevenday/             # Mobile Testing
├── Operations/                  # Operations Practice
│   ├── Oracle-backup/          # Oracle Backup
│   ├── SSH_Alert_ Log/         # SSH Log Alerts
│   └── ubuntu_nginx_offline/   # Nginx Offline Installation
└── README.md                   # Project Documentation
```

## 💡 Key Highlights

- **Battle-Tested**: All code comes from real projects, not toy examples
- **Full Stack Coverage**: From development, testing to operations, complete technology stack
- **Latest Technologies**: Includes cutting-edge technologies like LangChain, K8s, Prometheus
- **Production Ready**: Many configurations can be used directly in production environments
- **Pitfall Records**: Each example records actual pitfalls encountered and solutions

## 📝 Usage Instructions

### Environment Requirements

Depending on the specific example's technology stack, you may need the following environments:

- **Java Development** - JDK 8+, Maven/Gradle
- **Python Development** - Python 3.8+, pip
- **Containerization** - Docker, Kubernetes
- **CI/CD** - Jenkins
- **Databases** - MySQL, MongoDB, Redis
- **Message Queues** - RabbitMQ, Kafka


## ❓ FAQ

**Q: Can the code be used directly in production environments?**

A: Some configurations (like K8s deployment scripts) can be used directly, but adjustments based on your actual environment are recommended.

**Q: What background knowledge is needed to learn this code?**

A: Different areas have different requirements. The Java testing section needs Java basics, K8s deployment requires understanding container concepts, and the AI/LLM section suggests reading blog posts first to understand the background.

**Q: Will the code be continuously updated?**

A: New examples will be added continuously with the publication of blog posts, and discovered issues will be fixed.

**Q: How do I report issues or ask questions?**

A: You can submit an Issue on GitHub, or communicate through blog comments.


## 🤝 Contributing

Contributions of code and improvement suggestions are welcome!

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Submit a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## 📅 Changelog

### Recent Updates

<details>
<summary><b>📆 2026-01</b> - LangChain Framework Practice Series</summary>

- ✨ Complete LangChain Framework Practice (Zero-Basic Introduction, Role-Playing Bots, Chat History Management, Formatted Responses)
- 🤖 ReAct Agent Dual Implementation (From Scratch + LangChain Framework)
- 🔒 Dependency Update: Upgraded fastjson version

</details>

<details>
<summary><b>📆 2025-08</b> - Technical Specification Documents</summary>

- 📄 Added GB-T-44260-2024 Virtual Power Plant Resource Configuration and Evaluation Technical Specification

</details>

<details>
<summary><b>📆 2025-05</b> - AI Automation and Security Monitoring</summary>

- 🎙️ AI Automated Tencent Meeting Transcription and Summary Generation System (Whisper + DeepSeek)
- 🔐 Security Monitoring: Linux Core Asset SSH Connection Monitoring Email Example

</details>

<details>
<summary><b>📆 2025-04</b> - OpenAI API and Configuration Optimization</summary>

- 🌐 Added OpenAI API Examples
- 📝 Added Meeting Transcription Functionality
- ⚡ Added Nginx High-Performance Configuration File

</details>

---

### Historical Records

<details>
<summary><b>📜 Click to View Complete History (2021-2024)</b></summary>

#### 2024

**2024-05**
- Added pom.xml configuration file

**2024-04**
- Added JMeter Custom Java Sampler Implementation for SM2 Encryption Example Code

**2023-08**
- Updated application.yml configuration

**2023-06**
- Updated Prometheus RabbitMQ deployment configuration

#### 2022

**2022-01**
- Project README documentation improvement

#### 2021

**2021-12**
- Kubernetes cluster deployment Zipkin+Kafka+ElasticSearch for distributed tracing
- Traefik, Nacos deployment file updates

**2021-11**
- JMeter Dubbo testing example
- JMeter Spring Boot project startup example
- JMeter basic examples

**2021-10** 🏗️ Project Foundation Phase
- **Kubernetes Deployment Series**: Nacos, Metrics Server, Kafka+Zookeeper, Traefik, RabbitMQ, MongoDB, Prometheus, MySQL, Redis, NFS Provisioner
- **Automated Testing**: Mobile Appium framework data-driven examples
- **Performance Monitoring**: Java Agent examples

**2021-09**
- **Performance Testing**: JMeter 5.1.1, GoReplay, TPS control, quick start, upload/download
- **Monitoring Tracing**: Sleuth+Zipkin, Gor, Golang Prometheus, ELK log analysis
- **Other**: Oracle backup, Moco mock service

**2021-08**
- CSV vs Redis performance comparison example
- nGrinder performance testing example

**2021-07**
- Alibaba Java Development Guidelines

**2021-04**
- ThreadLocal cross-thread passing example
- Traffic flag demo

**2021-03**
- k8s-jmeter cluster deployment
- Prometheus monitoring configuration
- Kubernetes complex MySQL database setup
- Swagger & openapi-generator generate JMeter scripts
- Python statistical analysis of access logs

**2021-02**
- Spring Boot logging configuration
- kube-prometheus auto-discovery

</details>



##  Contact

- **Blog**: [CSDN Blog](https://zuozewei.blog.csdn.net) - Detailed technical articles and pitfall records
- **GitHub**: [zuozewei](https://github.com/zuozewei) - Welcome to submit Issues and PRs
- **Email**: [zuozewei@hotmail.com](mailto:zuozewei@hotmail.com) - Technical exchange

If this project helps you, please give it a Star to show your support! ⭐

## 🙏 Acknowledgments

Thanks to all contributors and users for their support!

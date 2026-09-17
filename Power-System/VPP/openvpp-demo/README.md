# openvpp-demo

> 专栏《虚拟电厂系统开发实战：从物联接入到市场化运营》配套示例工程
> 定位：以真实商业 VPP 平台为蓝本**抽象脱敏重写**的最小可运行实现，不是玩具 Demo，也不是生产代码
> 技术栈：Java 11 · Spring Boot 2.7 · Maven 多模块

## 模块总览

| 模块 | 职责 | 对应专栏篇目 |
|------|------|--------------|
| `openvpp-common` | 统一返回、枚举常量（资源类型/场景），零业务依赖 | 第 01-04 篇 |
| `openvpp-resource` | 资源档案、物模型、设备影子、台账 | 第 04、07、11 篇 |
| `openvpp-assessment` | 能力评估算法（44260 七指标）、评估策略 | 第 02、12、13 篇 |
| `openvpp-aggregator` | VPP 单元、聚合引擎、准入门槛（47241 四指标） | 第 03、14 篇 |
| `openvpp-gateway` | MQTT/CoAP 协议接入 | 第 06 篇 |
| `openvpp-iot` | 设备认证、断网续传 | 第 09、10 篇 |
| `openvpp-dispatch` | 指令链路、策略引擎 | 第 15、16 篇 |
| `openvpp-settlement` | 基线核算、结算分摊 | 第 17、21 篇 |
| `openvpp-market` | 申报、竞价（简化演示） | 第 19、20 篇 |
| `openvpp-edge` | 边缘侧缓存补传 demo | 第 09 篇 |
| `openvpp-app` | 单体启动入口（演示用） | 第 05、25 篇 |

## 快速开始

```bash
mvn -s settings-openvpp.xml install -DskipTests
cd openvpp-app && mvn -s ../settings-openvpp.xml spring-boot:run

# 验证
curl http://127.0.0.1:8080/api/v1/system/ping
# {"code":0,"message":"success","data":{"service":"openvpp-demo","status":"UP",...}}
```

> `settings-openvpp.xml`：全局 Maven 配置了不可达私服镜像时的逃生通道（显式走公共镜像，不动全局配置）。

## 网关回环测试（第 06 篇）

```bash
mvn -s settings-openvpp.xml -pl openvpp-gateway -am test
# MqttIngestServiceTest：经公共 broker（broker-cn.emqx.io）回环
# CoapIngestServerTest：本机 127.0.0.1 随机端口回环
```

## 章节 tag 对照

| tag | 里程碑 |
|-----|--------|
| `part1-cognition` | 认知与需求篇完成：common + 领域模型骨架 |
| `part2-iot` | 物联接入篇完成（规划中） |
| `part3-core` | 聚合调度核心篇完成（规划中） |
| `part4-market` | 市场与安全篇完成（规划中） |
| `part5-delivery` | 交付与 AI 篇完成（规划中） |

## 脱敏红线

- 不出现真实项目/公司/客户命名（一律 `openvpp-*` 化名）
- 不复用真实私有协议帧与接口路径
- 示例数据全虚构，截图脱敏重制

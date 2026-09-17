# 依赖
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /build
COPY pom.xml settings-openvpp.xml ./
COPY openvpp-common/pom.xml openvpp-common/
COPY openvpp-gateway/pom.xml openvpp-gateway/
COPY openvpp-iot/pom.xml openvpp-iot/
COPY openvpp-resource/pom.xml openvpp-resource/
COPY openvpp-assessment/pom.xml openvpp-assessment/
COPY openvpp-aggregator/pom.xml openvpp-aggregator/
COPY openvpp-dispatch/pom.xml openvpp-dispatch/
COPY openvpp-settlement/pom.xml openvpp-settlement/
COPY openvpp-market/pom.xml openvpp-market/
COPY openvpp-edge/pom.xml openvpp-edge/
COPY openvpp-app/pom.xml openvpp-app/
# 先拉依赖（利用 Docker 层缓存，源码变更不重复下载）
RUN mvn -s settings-openvpp.xml -q dependency:go-offline -DskipTests || true
COPY . .
RUN mvn -s settings-openvpp.xml -q install -DskipTests

# 运行
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /build/openvpp-app/target/openvpp-app-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

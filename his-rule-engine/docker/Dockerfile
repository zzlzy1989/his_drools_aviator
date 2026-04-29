# ===== Stage 1: Maven Build =====
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# 预下载依赖（利用 Docker 缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B -DskipTests

# 复制源码并构建
COPY src ./src
COPY his-common ./his-common
COPY his-gateway ./his-gateway
COPY his-rule-service ./his-rule-service
COPY his-formula-service ./his-formula-service
COPY his-settlement-service ./his-settlement-service
COPY his-drug-service ./his-drug-service
COPY his-quality-service ./his-quality-service
COPY his-drg-service ./his-drg-service

RUN mvn package -DskipTests -B

# ===== Stage 2: JRE Runtime =====
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 创建日志和数据目录
RUN mkdir -p /app/logs /app/data/rules && chown -R appuser:appgroup /app

COPY --from=builder /app/his-gateway/target/*.jar gateway.jar
COPY --from=builder /app/his-rule-service/target/*.jar rule-service.jar
COPY --from=builder /app/his-formula-service/target/*.jar formula-service.jar
COPY --from=builder /app/his-settlement-service/target/*.jar settlement-service.jar
COPY --from=builder /app/his-drug-service/target/*.jar drug-service.jar
COPY --from=builder /app/his-quality-service/target/*.jar quality-service.jar
COPY --from=builder /app/his-drg-service/target/*.jar drg-service.jar

USER appuser

EXPOSE 9000 9001 9002 9003 9004 9005 9006

ENV JAVA_OPTS="-Xms512m -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Djava.security.egd=file:/dev/./urandom \
  -XX:+TieredCompilation \
  -XX:TieredStopAtLevel=1"

# 健康检查脚本
COPY scripts/healthcheck.sh /app/scripts/healthcheck.sh
RUN chmod +x /app/scripts/healthcheck.sh

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar gateway.jar"]

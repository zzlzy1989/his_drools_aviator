# ===== Stage 1: Maven Build =====
FROM maven:3.9-eclipse-temurin-21 AS builder

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app

# 预下载依赖（利用 Docker 缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B -DskipTests

# 复制公共模块
COPY his-common ./his-common

# 复制目标服务源码
COPY ${SERVICE_NAME} ./service-src

# 构建目标服务
RUN mvn package -DskipTests -B -pl :${SERVICE_NAME} -am

# ===== Stage 2: JRE Runtime =====
FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 创建日志和数据目录
RUN mkdir -p /app/logs /app/data/rules && chown -R appuser:appgroup /app

# 复制构建产物
COPY --from=builder --chown=appuser:appgroup /app/service-src/target/*.jar app.jar

# 复制健康检查脚本
COPY docker/scripts/healthcheck.sh /app/scripts/healthcheck.sh
RUN chmod +x /app/scripts/healthcheck.sh

USER appuser

# 暴露端口（由 docker-compose 动态映射）
EXPOSE 9000

# JVM 参数
ENV JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Djava.security.egd=file:/dev/./urandom \
  -XX:+TieredCompilation \
  -XX:TieredStopAtLevel=1 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/heapdump.hprof"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD ["/app/scripts/healthcheck.sh", "${SERVER_PORT:-9000}"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

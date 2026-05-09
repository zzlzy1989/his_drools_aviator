# ===== Stage 1: Maven Build =====
FROM maven:3.9-eclipse-temurin-21 AS builder

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app

# 复制整个项目到 /app
COPY . .

# 构建目标服务（包含依赖）
RUN mvn package -DskipTests -B -pl ${SERVICE_NAME} -am

# ===== Stage 2: JRE Runtime =====
FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 创建日志和数据目录
RUN mkdir -p /app/logs /app/data/rules && chown -R appuser:appgroup /app

# 安装 curl 用于健康检查
RUN apk add --no-cache curl

# 复制构建产物
COPY --from=builder --chown=appuser:appgroup /app/${SERVICE_NAME}/target/*.jar app.jar

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

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
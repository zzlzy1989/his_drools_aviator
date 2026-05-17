---
alwaysApply: true
description: Docker 部署规范
---
# Docker 部署规范 - HIS 动态规则中台

## 触发条件
- 构建 Docker 镜像
- 编写 docker-compose.yml
- 配置容器编排
- 部署到生产环境
- 容器运维操作

---

## 端口约定

| 服务 | 端口 | 说明 | 对外暴露 |
|------|------|------|---------|
| his-gateway | **9000** | API 网关入口 | ✅ 是 |
| his-rule-service | **9001** | 规则管理服务 | ❌ 仅内网/网关 |
| his-settlement-service | **9002** | 结算服务 | ❌ 仅内网/网关 |
| his-drug-service | **9003** | 合理用药服务 | ❌ 仅内网/网关 |
| his-quality-service | **9004** | 质控服务 | ❌ 仅内网/网关 |
| his-drg-service | **9005** | DRG 分组服务 | ❌ 仅内网/网关 |
| Nacos | **8848** | 配置中心 | ❌ 仅内网 |
| MySQL | **3306** | 数据库 | ❌ 仅内网 |
| Redis | **6379** | 缓存（可选） | ❌ 仅内网 |
| Sentinel Dashboard | **8081** | 限流监控面板 | ⚠️ 管理端口 |

---

## 镜像构建规范

### 多阶段构建

```dockerfile
# ===== Stage 1: Maven 构建 =====
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# ===== Stage 2: 运行阶段 =====
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 镜像标签规范

```
{服务名}:{版本}-{日期}

示例:
his-gateway:v1.1.0-20260512
his-rule-service:v1.1.0-20260512
his-settlement-service:v1.1.0-20260512
his-rule-service:latest          # 仅用于开发环境
his-rule-service:stable           # 当前稳定版
```

---

## docker-compose 编排规范

### 目录结构

```
docker/
├── docker-compose.yml          # 主编排文件
├── docker-compose.override.yml # 本地开发覆盖
├── nginx/
│   └── nginx.conf              # Nginx 反向代理配置
├── scripts/
│   ├── init-db.sh              # 数据库初始化
│   └── healthcheck.sh          # 健康检查脚本
└── config/
    ├── gateway.yml             # 网关配置
    ├── application-prod.yml    # 生产环境配置覆盖
    └── sentinel-rules.yml      # Sentinel 限流规则
```

### 微服务主编排文件模板

```yaml
version: '3.8'

services:
  # === API 网关 ===
  gateway:
    build:
      context: ..
      dockerfile: his-gateway/Dockerfile
    container_name: his-gateway
    ports:
      - "9000:9000"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - SENTINEL_DASHBOARD_ADDR=sentinel:8081
    depends_on:
      nacos:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - his-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'

  # === 规则管理服务 ===
  rule-service:
    build:
      context: ..
      dockerfile: his-rule-service/Dockerfile
    container_name: his-rule-service
    ports:
      - "9001:9001"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=his_rule
      - DB_USER=his_app
      - DB_PASSWORD=${DB_PASSWORD}
      - NACOS_SERVER_ADDR=nacos:8848
      - AVIATOR_CACHE_MAX_SIZE=5000
      - DROOLS_RULE_SCAN_INTERVAL=60
    volumes:
      - ../logs/rule:/app/logs
    depends_on:
      mysql:
        condition: service_healthy
      nacos:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9001/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - his-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '2.0'

  # === 结算服务 ===
  settlement-service:
    build:
      context: ..
      dockerfile: his-settlement-service/Dockerfile
    container_name: his-settlement-service
    ports:
      - "9002:9002"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=his_settlement
      - DB_USER=his_app
      - DB_PASSWORD=${DB_PASSWORD}
      - NACOS_SERVER_ADDR=nacos:8848
      - RULE_SERVICE_URL=http://rule-service:9001
    volumes:
      - ../logs/settlement:/app/logs
    depends_on:
      mysql:
        condition: service_healthy
      nacos:
        condition: service_healthy
      rule-service:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9002/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - his-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '2.0'

  # === 合理用药服务 ===
  drug-service:
    build:
      context: ..
      dockerfile: his-drug-service/Dockerfile
    container_name: his-drug-service
    ports:
      - "9003:9003"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=his_drug
      - DB_USER=his_app
      - DB_PASSWORD=${DB_PASSWORD}
      - NACOS_SERVER_ADDR=nacos:8848
    volumes:
      - ../logs/drug:/app/logs
    depends_on:
      mysql:
        condition: service_healthy
      nacos:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9003/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - his-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'

  # === 质控服务 ===
  quality-service:
    build:
      context: ..
      dockerfile: his-quality-service/Dockerfile
    container_name: his-quality-service
    ports:
      - "9004:9004"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=his_quality
      - DB_USER=his_app
      - DB_PASSWORD=${DB_PASSWORD}
      - NACOS_SERVER_ADDR=nacos:8848
    volumes:
      - ../logs/quality:/app/logs
    depends_on:
      mysql:
        condition: service_healthy
      nacos:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9004/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - his-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'

  # === DRG 分组服务 ===
  drg-service:
    build:
      context: ..
      dockerfile: his-drg-service/Dockerfile
    container_name: his-drg-service
    ports:
      - "9005:9005"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=his_drg
      - DB_USER=his_app
      - DB_PASSWORD=${DB_PASSWORD}
      - NACOS_SERVER_ADDR=nacos:8848
    volumes:
      - ../logs/drg:/app/logs
    depends_on:
      mysql:
        condition: service_healthy
      nacos:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9005/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - his-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'

  # === MySQL ===
  mysql:
    image: mysql:8.0
    container_name: his-mysql
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=his_rule
      - MYSQL_CHARACTER_SET_SERVER=utf8mb4
      - MYSQL_COLLATION_SERVER=utf8mb4_general_ci
    command:
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_general_ci
      --max_connections=500
      --innodb-buffer-pool-size=512M
    volumes:
      - mysql-data:/var/lib/mysql
      - ./scripts/init-db.sh:/docker-entrypoint-initdb.d/init-db.sh
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - his-net
    restart: unless-stopped

  # === Nacos (配置中心) ===
  nacos:
    image: nacos/nacos-server:v3.0.3
    container_name: his-nacos
    ports:
      - "8848:8848"
      - "9848:9848"
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_DB_NAME=nacos_config
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - JVM_XMS=256m
      - JVM_XMX=512m
    volumes:
      - nacos-data:/home/nacos/logs
    depends_on:
      mysql:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/v1/console/health/liveness"]
      interval: 15s
      timeout: 5s
      retries: 5
    networks:
      - his-net
    restart: unless-stopped

  # === Sentinel Dashboard (限流监控) ===
  sentinel:
    image: bladex/sentinel-dashboard:1.8.8
    container_name: his-sentinel
    ports:
      - "8081:8081"
    environment:
      - JAVA_OPTS=-Dserver.port=8081 -Dcsp.sentinel.dashboard.server=localhost:8081
    networks:
      - his-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: '0.5'

  # === Redis (可选，二级缓存) ===
  redis:
    image: redis:7-alpine
    container_name: his-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy allkeys-lru
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - his-net
    restart: unless-stopped

networks:
  his-net:
    driver: bridge

volumes:
  mysql-data:
  nacos-data:
  redis-data:
```

---

## 健康检查规范

### 各服务健康检查端点

| 服务 | 检查方式 | 端点 | 预期响应 |
|------|---------|------|---------|
| Gateway | HTTP GET | `/actuator/health` | `{"status":"UP"}` |
| Rule Service | HTTP GET | `/actuator/health` | `{"status":"UP"}` |
| Settlement Service | HTTP GET | `/actuator/health` | `{"status":"UP"}` |
| Drug Service | HTTP GET | `/actuator/health` | `{"status":"UP"}` |
| Quality Service | HTTP GET | `/actuator/health` | `{"status":"UP"}` |
| DRG Service | HTTP GET | `/actuator/health` | `{"status":"UP"}` |
| Nacos | HTTP GET | `/nacos/v1/console/health/liveness` | JSON OK |
| MySQL | TCP | `3306` | 连接成功 |
| Redis | CLI | `redis-cli ping` | `PONG` |

### Spring Boot Health Check 实现

```java
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private KieContainer kieContainer;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 数据库检查
        try (Connection conn = dataSource.getConnection()) {
            result.put("database", conn.isValid(1) ? "UP" : "DOWN");
        } catch (Exception e) {
            result.put("database", "DOWN: " + e.getMessage());
        }
        
        // KIE Base 检查
        try {
            result.put("kieBase", kieContainer.getKieBase() != null ? "UP" : "DOWN");
        } catch (Exception e) {
            result.put("kieBase", "DOWN: " + e.getMessage());
        }
        
        // 缓存检查
        // Caffeine stats...
        
        result.put("overall", "UP");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
```

---

## JVM 调优参数

| 参数 | 开发环境 | 生产环境 | 说明 |
|------|---------|---------|------|
| `-Xms` | 256m | 512m-1024m | 初始堆内存（规则/结算服务建议 1024m） |
| `-Xmx` | 1g | 2g-4g | 最大堆内存（规则/结算服务建议 4g） |
| GC 算法 | G1GC | G1GC | 低延迟适合规则引擎 |
| `MaxGCPauseMillis` | 200ms | 200ms | GC 最大停顿时间 |
| Metaspace | 128m | 256m | 元空间（DRL编译占用较多） |

### 规则引擎专项调优

```bash
# DROOLS 相关
-Ddrools.ruleBaseConfidenceInterval=60     # 规则扫描间隔(秒)
-Ddrools.phreakEnabled=true                 # 启用 Phreak 算法(更高效)

# AVIATOR 相关
-Daviator.cache.maxSize=5000                # 表达式缓存上限
-Daviator.cache.expireMinutes=30            # 缓存过期时间(分钟)

# SPRING 相关
-Dspring.main.lazy-initialization=true      # 延迟初始化(加快启动)

# SENTINEL 相关
-Dcsp.sentinel.dashboard.server=sentinel:8081  # Sentinel Dashboard 地址
-Dcsp.sentinel.api.port=8719                    # Sentinel API 端口
-Dproject.name=his-rule-service                 # 服务名称
```

---

## 环境变量管理

### 必需环境变量

| 变量名 | 说明 | 是否必需 | 示例 |
|--------|------|---------|------|
| `SPRING_PROFILES_ACTIVE` | 环境 Profile | ✅ 必须 | dev/test/prod/docker |
| `DB_HOST` | 数据库地址 | ✅ 必须 | mysql / 192.168.1.100 |
| `DB_PORT` | 数据库端口 | ✅ 必须 | 3306 |
| `DB_NAME` | 数据库名 | ✅ 必须 | his_rule / his_settlement |
| `DB_USER` | 数据库用户 | ✅ 必须 | his_app |
| `DB_PASSWORD` | 数据库密码 | ✅ 必须 | *** |
| `NACOS_SERVER_ADDR` | Nacos 地址 | ✅ 必须 | nacos:8848 |
| `NACOS_NAMESPACE` | Nacos 命名空间 | 可选 | his_prod |
| `NACOS_GROUP` | 配置分组 | 可选 | RULE_ENGINE |
| `SENTINEL_DASHBOARD_ADDR` | Sentinel 地址 | 可选 | sentinel:8081 |
| `SERVER_PORT` | 服务端口 | ✅ 必须 | 9001/9002/9003/9004/9005 |

### 环境变量文件

```bash
# .env.production (不提交到 Git!)
DB_PASSWORD=your-strong-password-here
MYSQL_ROOT_PASSWORD=your-root-password
NACOS_AUTH_TOKEN=nacos-auth-token-if-enabled
```

---

## 安全加固清单

| 检查项 | 要求 | 状态 |
|--------|------|------|
| 容器以非 root 用户运行 | `USER appuser` in Dockerfile | ✅ 已实现 |
| 只读文件系统 | `read_only: true` + tmpfs | ⬜ 待配置 |
| 资源限制 | `deploy.resources.limits` (memory/cpu) | ✅ 已实现 |
| 网络隔离 | 自定义 bridge 网络 | ✅ 已实现 |
| 日志驱动 | json-driver + 大小限制 | ⬜ 待配置 |
| 基础镜像 | alpine/eclipse-temurin-jre | ✅ 已实现 |
| 敏感信息 | 通过 env_file / secrets 注入 | ✅ 已实现 |
| 最小权限 | 不使用 privileged 模式 | ✅ 已实现 |
| Aviator 安全沙箱 | 白名单函数 + 禁止危险调用 | ✅ 代码层面 |

---

## 运维命令速查

```bash
# 启动所有服务
docker compose up -d

# 查看日志
docker compose logs -f gateway
docker compose logs -f rule-service
docker compose logs --tail=200 settlement-service

# 重启单个服务
docker compose restart rule-service

# 进入容器调试
docker compose exec rule-service sh

# 数据库备份
docker compose exec mysql mysqldump -u root -p his_rule > backup_$(date +%Y%m%d).sql

# 规则热更新验证
curl -X POST http://localhost:8080/api/v1/config/push -H "Content-Type: application/json"

# 资源监控
docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"

# 清理未使用的资源
docker system prune -f          # ⚠️ 确认后再执行

# 查看 JVM 状态
docker compose exec rule-service jinfo 1  # PID 通常为 1
docker compose exec rule-service jstat -gcutil 1 1s  # GC 统计
```

---

## 生产部署检查清单

- [ ] JVM 内存参数已根据服务器规格调整
- [ ] 数据库连接池大小已配置（HikariCP: maxPoolSize ≤ DB max_connections * 0.7）
- [ ] Nacos 高可用模式（集群）或确认单点可接受
- [ ] Caffeine 缓存大小合理（不超过 JVM heap 的 20%）
- [ ] 规则热更新灰度策略已配置
- [ ] 健康检查端点已对接监控系统（Prometheus/Grafana）
- [ ] 日志收集已接入（ELK/Loki）
- [ ] 敏感配置已加密存储（Nacos 加密或 Apollo 加密）
- [ ] Sentinel 限流规则已配置
- [ ] 各服务依赖关系正确（depends_on）
- [ ] 网关路由配置正确

---

最后更新: 2026-05-12 | v1.1 (HIS Drools+Aviator 规则引擎专用 - 微服务架构版)

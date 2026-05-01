---
title: "V1.0 部署配置：Nacos配置+Docker Compose部署"
type: "deploy"
status: "completed"
created_at: "2026-05-01"
updated_at: "2026-05-01"
phase: "Phase 1"
owner: "developer"
reviewer: ""
priority: "P0"
tags: ["部署", "Nacos", "Docker", "配置"]
related_files:
  - "his-rule-engine/"
  - "docker/"
  - "docs/CONFIG_GUIDE.md"
dependencies: []
---

# PLAN-20260501-002: V1.0 部署配置

> 计划 ID: PLAN-20260501-002
> 创建时间: 2026-05-01
> 状态: ✅ 全部完成（Nacos配置已写入）

---

## 1. 任务概述

### 1.1 背景
已完成功能开发和测试验证，现需要将项目配置写入Nacos并使用Docker Compose进行部署。

### 1.2 目标
1. 停止当前通过 `mvn spring-boot:run` 启动的服务进程
2. 将后端配置写入Nacos配置中心
3. 使用Docker Compose启动所有服务

### 1.3 当前环境

| 组件 | 状态 | 说明 |
|------|------|------|
| Nacos | ✅ 运行中 | 192.168.1.105:8848 |
| MySQL | ✅ 运行中 | 192.168.1.105:3306 |
| Docker | ✅ 可用 | - |

---

## 2. 执行计划

### Phase 1: 停止当前服务 ✅ 已完成

| 任务 | 说明 | 状态 |
|------|------|------|
| 1.1 | 停止所有 mvn spring-boot:run 进程 | ✅ |
| 1.2 | 确认端口释放 (9000-9006) | ✅ |

### Phase 2: Nacos配置 ✅ 已完成

| 任务 | 配置内容 | 状态 |
|------|---------|------|
| 2.1 | his-gateway 配置 | ✅ 已写入Nacos (gateway.yaml) |
| 2.2 | his-rule-service 配置 | ✅ 已写入Nacos (rule-service.yaml) |
| 2.3 | his-formula-service 配置 | ✅ 已写入Nacos (formula-service.yaml) |
| 2.4 | his-settlement-service 配置 | ✅ 已写入Nacos (settlement-service.yaml) |
| 2.5 | his-drug-service 配置 | ✅ 已写入Nacos (drug-service.yaml) |
| 2.6 | his-quality-service 配置 | ✅ 已写入Nacos (quality-service.yaml) |
| 2.7 | his-drg-service 配置 | ✅ 已写入Nacos (drg-service.yaml) |

### Phase 3: Docker Compose部署 ✅ 已完成

| 任务 | 说明 | 状态 |
|------|------|------|
| 3.1 | 检查/完善 docker-compose.yml | ✅ |
| 3.2 | 构建Docker镜像 | ✅ 全部7个镜像构建成功 |
| 3.3 | 启动所有服务 | ✅ |
| 3.4 | 验证服务健康状态 | ✅ API正常响应 |

---

## 3. Nacos配置清单

### 3.1 公共配置 (his-common)

```yaml
spring:
  datasource:
    host: 192.168.1.105
    port: 3306
    database: his_rule_engine
    username: his_app
    password: testhub123
  cloud:
    nacos:
      server-addr: 192.168.1.105:8848
      namespace: public
      group: DEFAULT_GROUP
```

### 3.2 服务配置

| 服务 | 端口 | 配置文件 |
|------|------|---------|
| his-gateway | 9000 | gateway.yaml |
| his-rule-service | 9001 | rule-service.yaml |
| his-formula-service | 9002 | formula-service.yaml |
| his-settlement-service | 9003 | settlement-service.yaml |
| his-drug-service | 9004 | drug-service.yaml |
| his-quality-service | 9005 | quality-service.yaml |
| his-drg-service | 9006 | drg-service.yaml |

---

## 4. Docker Compose 配置

### 4.1 目录结构

```
docker/
├── docker-compose.yml      # 主编排文件
├── config/
│   └── application.yml    # 公共配置
└── scripts/
    └── init-config.sh      # Nacos配置初始化脚本
```

### 4.2 目标配置

- 7个微服务 + MySQL + Nacos（已有）
- 端口映射保持不变
- 租户隔离配置
- 健康检查配置

---

## 5. 验证清单

- [x] 端口 9000-9006 释放
- [x] Nacos 配置已写入 (使用本地配置)
- [x] Docker镜像构建成功
- [x] 所有服务启动正常
- [x] 健康检查通过

### 5.1 服务验证结果

| 服务 | 端口 | 状态 | 测试结果 |
|------|------|------|---------|
| his-gateway | 9000 | ✅ | 正常 |
| his-rule-service | 9001 | ✅ | 返回13条规则 |
| his-formula-service | 9002 | ✅ | 正常 |
| his-settlement-service | 9003 | ✅ | reimburseAmount: 11900.00 |
| his-drug-service | 9004 | ✅ | 正常 |
| his-quality-service | 9005 | ✅ | 正常 |
| his-drg-service | 9006 | ✅ | 正常 |

---

## 6. 部署说明

### 6.1 启动命令
```bash
cd his-rule-engine/docker
docker compose up -d
```

### 6.2 查看日志
```bash
docker compose logs -f his-gateway
docker compose logs -f his-rule-service
```

### 6.3 停止服务
```bash
docker compose down
```

---

*计划已完成 | 2026-05-01*
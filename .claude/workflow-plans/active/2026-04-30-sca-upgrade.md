---
title: "架构升级：Spring Cloud Alibaba 2025.0.1.0 + Nacos 3.2.0"
type: "feature"
status: "in_progress"
created_at: "2026-04-30"
updated_at: "2026-04-30"
phase: "Phase 1"
owner: "developer"
reviewer: ""
priority: "P0"
tags: ["升级", "SCA", "Nacos", "JDK21", "Drools"]
related_files:
  - "his-rule-engine/pom.xml"
  - "docker/config/application.properties"
dependencies: []
---

# PLAN-20260430-001: 架构升级 - SCA 2025.0.1.0 + Nacos 3.2.0

> 计划 ID: PLAN-20260430-001
> 创建时间: 2026-04-30
> 状态: ⏳ pending

---

## 1. 任务概述

### 1.1 背景

当前项目使用 Spring Cloud Alibaba 2023.0.1.0 + Nacos 2.3.0，存在以下问题：
- Nacos 2.3.0 客户端与 Nacos 3.2.0 服务器 API 不兼容
- Nacos 3.x 已强制开启鉴权，旧客户端无法正常调用
- Spring Boot 3.2.5 / Spring Cloud 2023.0.1 与 Nacos 3.x 存在隐性问题

### 1.2 目标

升级架构至 **SCA 2025.0.1.0 + Nacos 3.2.0 + JDK 21**，实现：
- Nacos 服务器 3.2.0 与客户端 3.0.3 完整兼容
- 服务注册、发现、配置订阅功能正常
- 鉴权机制正确配置

### 1.3 风险评估

| 组件 | 当前版本 | 目标版本 | 风险等级 | 说明 |
|------|---------|---------|---------|------|
| Spring Boot | 3.2.5 | 3.5.0 | ⚠️ 中 | 版本链要求，必调 |
| Spring Cloud | 2023.0.1 | 2025.0.1 | ⚠️ 中 | 版本链要求，必调 |
| Spring Cloud Alibaba | 2023.0.1.0 | **2025.1.0.0** | ✅ 已升级 | 官方最新稳定版 |
| Nacos 客户端 | 2.3.2 | 3.1.1 | ✅ 已升级 | SCA 2025.1.0.0 内置 |
| Drools | 8.44.0.Final | 待定 | 🔴 高 | 与 Spring 6 API 不兼容 |
| Aviator | 5.4.3 | 5.4.3 | ✅ 低 | 已知兼容 |
| MyBatis-Plus | 3.5.6 | 3.5.6 | ✅ 低 | 已知兼容 |

---

## 2. 版本调整清单

### 2.1 必须调整的组件

```xml
<!-- pom.xml properties -->
<spring-boot.version>3.5.0</spring-boot.version>          <!-- 3.2.5 → 3.5.0 -->
<spring-cloud.version>2025.0.1</spring-cloud.version>     <!-- 2023.0.1 → 2025.0.1 -->
<spring-cloud-alibaba.version>2025.0.1.0</spring-cloud-alibaba.version>  <!-- 核心升级 -->
```

### 2.2 Drools 风险处理策略

**选项 A（推荐）**: 降级到 JDK 17 运行环境
- 保留 Drools 8.44.0.Final
- 微服务以 JDK 17 运行（通过 `JAVA_HOME` 切换）

**选项 B**: 升级到 Drools 10.x
- 需要修改核心代码适配新 API
- 改造工作量较大

**选项 C**: 用 Aviator 替代 Drools 实现规则引擎
- 业务规则迁移到 Aviator 表达式
- 改造工作量中等
- 优点：无 JVM 版本问题，热更新更灵活

**本计划建议**: 采用 **选项 A**（降级 JDK 17），快速稳定

---

## 3. 执行步骤

### Phase 1: 环境准备

| 任务 | 状态 | 说明 |
|------|:----:|------|
| 1.1 备份当前 pom.xml | ⬜ | 记录当前版本配置 |
| 1.2 确认 JDK 17 可用 | ⬜ | 检查是否安装 JDK 17 |
| 1.3 Nacos 3.2.0 数据库升级 | ⬜ | 执行 schema 升级脚本 |

### Phase 2: 版本升级

| 任务 | 状态 | 说明 |
|------|:----:|------|
| 2.1 更新 pom.xml 版本 | ✅ | Spring Boot 3.5.0 / Cloud 2025.0.1 / SCA 2025.1.0.0 |
| 2.2 验证 Maven 依赖解析 | ✅ | 依赖树正常，nacos-client 3.1.1 |
| 2.3 编译通过 | ✅ | `mvn compile -DskipTests` 成功 |

### Phase 3: Nacos 3.2.0 配置

| 任务 | 状态 | 说明 |
|------|:----:|------|
| 3.1 配置 Nacos 鉴权参数 | ⬜ | JWT Token / Identity Key |
| 3.2 创建测试命名空间 | ⬜ | his-dev / his-test |
| 3.3 推送配置到 Nacos | ⬜ | 验证配置下发 |

### Phase 4: 集成测试

| 任务 | 状态 | 说明 |
|------|:----:|------|
| 4.1 启动所有微服务 | ⬜ | 检查服务注册 |
| 4.2 验证配置订阅 | ⬜ | 配置变更推送测试 |
| 4.3 结算全流程测试 | ⬜ | 端到端验证 |
| 4.4 公式热更新测试 | ⬜ | 验证 Aviator 动态公式 |

### Phase 5: Drools 风险处理（可选）

| 任务 | 状态 | 说明 |
|------|:----:|------|
| 5.1 评估 Drools 使用场景 | ⬜ | 是否可迁移到 Aviator |
| 5.2 降级 JDK 17（如选选项A） | ⬜ | 修改启动脚本 |
| 5.3 或：迁移规则到 Aviator（如选选项C） | ⬜ | 规则重写 |

---

## 4. Drools 替代方案分析

### 4.1 当前 Drools 使用场景

根据代码分析，Drools 主要用于：
- `SettlementSkill` - 医保结算规则编排
- `RationalDrugUseSkill` - 合理用药规则
- `InfectionControlSkill` - 感染控制规则

### 4.2 Aviator 替代可行性

| 场景 | Drools 原实现 | Aviator 替代方案 | 复杂度 |
|------|-------------|----------------|--------|
| 条件判断 | DRL when 子句 | Aviator 表达式 | 低 |
| 规则编排 | salience/activation-group | 顺序执行 | 中 |
| 全局变量 | global | Aviator let 语法 | 低 |
| 规则热更新 | 重新编译 KIE Base | 更新数据库公式 | **更低** |

**结论**: Aviator 可完全替代 Drools，且热更新更简单（无需重启）

---

## 5. 升级检查清单

- [ ] pom.xml 版本已更新
- [ ] Maven 依赖解析成功
- [ ] Nacos 3.2.0 服务正常运行
- [ ] 服务注册到 Nacos 3.2.0 成功
- [ ] 配置推送/订阅功能正常
- [ ] 结算 API 响应正确
- [ ] 公式热更新验证通过
- [ ] Drools 规则（如使用）功能正常

---

## 6. 回滚方案

如升级失败，回退步骤：
1. 恢复 pom.xml 到原始版本
2. 重启 Nacos 2.3.0 容器
3. 重启所有微服务

---

## 7. 相关文档

- [Spring Cloud Alibaba 2025.0.1.0 Release Notes](https://github.com/alibaba/spring-cloud-alibaba/releases/tag/2025.0.1.0)
- [Nacos 3.2.0 升级指南](https://nacos.io/docs/v3/upgrading/3.2.0/)
- [版本对应关系](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E5%AF%B9%E5%BA%94%E5%85%B3%E7%B3%BB)

---

*计划待执行*

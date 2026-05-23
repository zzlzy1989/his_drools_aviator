---
title: "登录功能完整实现：数据库认证 + 密码加密 + 安全防护"
type: "feature"
status: "completed"
created_at: "2026-05-20 14:00:00"
updated_at: "2026-05-21 07:50:00"
completed_at: "2026-05-21 15:54:00"
phase: "Phase 1"
owner: "developer"
reviewer: ""
priority: "P0"
tags: ["auth", "login", "security", "database", "jwt", "bcrypt"]
related_files:
  - "his-rule-engine/his-gateway/src/main/java/com/his/gateway/controller/AuthController.java"
  - "his-rule-engine/his-gateway/src/main/java/com/his/gateway/filter/JwtAuthenticationFilter.java"
  - "his-rule-engine/his-common/his-common-web/src/main/java/com/his/common/web/result/ErrorCode.java"
  - "his-rule-engine-web/src/views/login/Login.vue"
  - "his-rule-engine-web/src/api/request.ts"
  - "his-rule-engine-web/src/stores/user.ts"
dependencies: []
---

# PLAN-20260520-001: 登录功能完整实现

> 计划 ID: PLAN-20260520-001
> 创建时间: 2026-05-20 14:00:00
> 状态: completed

---

## 1. 任务概述

### 1.1 问题分析

当前登录功能存在以下问题：

1. **硬编码认证**：`AuthController.login()` 中用户名密码直接硬编码为 `"admin"/"admin"`
2. **无用户数据表**：数据库中没有 `his_auth_user` 用户表
3. **密码明文比对**：直接 `equals` 比较明文密码，无加密存储
4. **无登录失败限制**：无限次尝试无防暴力破解
5. **Token 信息不完整**：JWT 中只包含 userId 和 tenantId，缺少角色信息
6. **前端硬编码 API 地址**：`Login.vue` 中硬编码了网关地址
7. **用户信息不完整**：登录后未存储 username 到 localStorage/userStore

### 1.2 目标

1. 创建 `his_auth_user` 用户表，支持用户名、密码（bcrypt 加密）、角色、租户关联
2. 重构 `AuthController`，从数据库查询用户并验证 bcrypt 密码
3. 添加登录失败计数和账户锁定机制
4. 完善 JWT Token 信息（增加角色、用户名）
5. 前端对接改进（使用 axios 统一请求、完善 userStore）
6. 添加登录相关错误码
7. 初始化默认 admin 用户（密码 bcrypt 加密存储）

---

## 2. 技术方案

### 2.1 架构设计

```
前端 Login.vue
  ↓ POST /api/v1/auth/login {username, password}
网关 Gateway (AuthController)
  ↓ 查询 his_auth_user 表
MySQL 数据库
  ↓ 返回用户记录（bcrypt 密码）
AuthController
  ↓ BCrypt.checkpw(password, user.password)
  ↓ 检查账户锁定状态
  ↓ 生成 JWT Token（含 userId, tenantId, role, username）
  ↓ 返回 {code, data: {token, userId, tenantId, username, role}}
前端
  ↓ 存储 token/userId/tenantId/username/role 到 localStorage + userStore
  ↓ 跳转到首页
```

### 2.2 关键技术决策

**Gateway 中使用 JdbcTemplate**（而非 MyBatis-Plus）

Gateway 基于 Spring WebFlux（响应式），MyBatis-Plus 基于 JDBC（阻塞式），两者存在架构冲突。选择使用 JdbcTemplate，最小化改动。

---

## 3. 执行步骤

### Step 1: 创建用户表与初始数据 ✅
### Step 2: 扩展错误码 ✅
### Step 3: Gateway 添加依赖与配置 ✅
### Step 4: 创建用户实体与 DTO ✅
### Step 5: 创建认证服务 ✅
### Step 6: 重构 AuthController ✅
### Step 7: 前端登录对接优化 ✅
### Step 8: 构建验证与测试 ✅

---

## 4. 进度跟踪

| 时间 | 操作 | 状态变更 | 备注 |
|------|------|---------|------|
| 2026-05-20 14:00 | 创建计划 | in_progress | 初始创建 |
| 2026-05-21 07:45 | Step 1 完成 | ✅ | 用户表创建 + 初始数据插入（admin/operator01） |
| 2026-05-21 07:46 | Step 2 完成 | ✅ | ErrorCode 添加 HIS-A05/A06/A07 |
| 2026-05-21 07:47 | Step 3 完成 | ✅ | Gateway pom.xml 添加 jdbc/bcrypt/validation 依赖 + 数据源配置 |
| 2026-05-21 07:48 | Step 4 完成 | ✅ | AuthUser 实体 + LoginRequest/LoginResponse DTO 创建（使用 JdbcTemplate，无 Mapper） |
| 2026-05-21 07:49 | Step 5 完成 | ✅ | AuthService/AuthServiceImpl 实现（BCrypt校验+锁定机制+JWT增强） |
| 2026-05-21 07:49 | Step 6 完成 | ✅ | AuthController 重构为响应式（Mono+Schedulers.boundedElastic） |
| 2026-05-21 07:50 | Step 7 完成 | ✅ | 前端 Login.vue 使用 loginApi + userStore，auth.ts API 封装 |
| 2026-05-21 15:54 | Step 8 完成 | ✅ | 运行时功能测试全部通过 |

---

## 5. 完成检查清单

- [x] his_auth_user 表创建完成，初始 admin 用户插入
- [x] ErrorCode 认证错误码扩展完成
- [x] Gateway JdbcTemplate 配置完成
- [x] AuthUser 实体创建完成
- [x] AuthService 认证服务实现完成
- [x] AuthController 重构完成
- [x] 前端登录对接优化完成
- [x] 构建验证通过（后端 Maven compile ✅，前端 Vite build ✅）
- [x] 运行时功能测试通过（admin/operator01 登录成功、错误密码返回 HIS-A05、SQL注入/XSS防护验证）

---

*计划结束*

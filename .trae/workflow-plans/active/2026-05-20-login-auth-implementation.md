---
title: "登录功能完整实现：数据库认证 + 密码加密 + 安全防护"
type: "feature"
status: "in_progress"
created_at: "2026-05-20 14:00:00"
updated_at: "2026-05-21 07:50:00"
completed_at: null
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
> 状态: in_progress

---

## 1. 任务概述

### 1.1 问题分析

当前登录功能存在以下问题：

1. **硬编码认证**：`AuthController.login()` 中用户名密码直接硬编码为 `"admin"/"admin"`，注释明确写着"简单验证：实际应查询数据库验证用户密码"
2. **无用户数据表**：数据库 `his_rule_engine` 中没有 `his_auth_user` 用户表，无法存储和查询用户信息
3. **密码明文比对**：直接 `equals` 比较明文密码，无加密存储
4. **无登录失败限制**：无限次尝试无防暴力破解
5. **Token 信息不完整**：JWT 中只包含 userId 和 tenantId，缺少角色信息
6. **前端硬编码 API 地址**：`Login.vue` 中 `fetch('http://localhost:9000/api/v1/auth/login')` 硬编码了网关地址
7. **用户信息不完整**：登录后未存储 username 到 localStorage/userStore

### 1.2 目标

1. 创建 `his_auth_user` 用户表，支持用户名、密码（bcrypt 加密）、角色、租户关联
2. 重构 `AuthController`，从数据库查询用户并验证 bcrypt 密码
3. 添加登录失败计数和账户锁定机制
4. 完善 JWT Token 信息（增加角色、用户名）
5. 前端对接改进（使用 axios 统一请求、完善 userStore）
6. 添加登录相关错误码
7. 初始化默认 admin 用户（密码 bcrypt 加密存储）
8. 编写单元测试

### 1.3 范围

- **包含**:
  - 数据库表设计与创建
  - 后端认证逻辑重构（Gateway 模块）
  - 密码加密（BCrypt）
  - 登录失败限制
  - JWT Token 增强
  - 前端登录对接优化
  - 错误码扩展
  - 单元测试
- **不包含**:
  - 用户注册功能（后续迭代）
  - 权限管理（RBAC，后续迭代）
  - OAuth2/SSO 集成（后续迭代）
  - 密码修改功能（后续迭代）

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

### 2.2 数据库设计

#### his_auth_user 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| username | VARCHAR(64) | UK NOT NULL | 用户名 |
| password | VARCHAR(128) | NOT NULL | bcrypt 加密密码 |
| real_name | VARCHAR(64) | | 真实姓名 |
| email | VARCHAR(128) | | 邮箱 |
| phone | VARCHAR(32) | | 手机号 |
| avatar | VARCHAR(256) | | 头像URL |
| role | VARCHAR(32) | NOT NULL DEFAULT 'operator' | 角色: super_admin/admin/operator/viewer |
| tenant_id | VARCHAR(64) | NOT NULL DEFAULT '' | 所属租户ID |
| status | VARCHAR(16) | NOT NULL DEFAULT 'active' | 状态: active/disabled/locked |
| login_fail_count | INT | NOT NULL DEFAULT 0 | 连续登录失败次数 |
| last_login_time | DATETIME | | 最后登录时间 |
| last_login_ip | VARCHAR(64) | | 最后登录IP |
| locked_until | DATETIME | | 锁定截止时间 |
| create_by | VARCHAR(64) | | 创建人 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_by | VARCHAR(64) | | 更新人 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | NOT NULL DEFAULT 0 | 逻辑删除 |

索引：
- `uk_username` UNIQUE (username)
- `idx_tenant_status` (tenant_id, status)
- `idx_role` (role)

### 2.3 接口规范

#### POST /api/v1/auth/login

**请求**:
```json
{
  "username": "admin",
  "password": "admin"
}
```

**成功响应** (code=0):
```json
{
  "code": "0",
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": "admin",
    "username": "admin",
    "realName": "系统管理员",
    "tenantId": "T001",
    "role": "super_admin"
  },
  "timestamp": 1716192000000
}
```

**失败响应**:
```json
{
  "code": "HIS-A05",
  "message": "用户名或密码错误",
  "timestamp": 1716192000000
}
```

#### 错误码扩展

| 错误码 | 说明 |
|--------|------|
| HIS-A05 | 用户名或密码错误 |
| HIS-A06 | 账户已被锁定，请稍后重试 |
| HIS-A07 | 账户已被禁用 |

### 2.4 安全措施

1. **密码加密**：使用 BCrypt 算法（Spring Security Crypto），strength=10
2. **防暴力破解**：连续5次登录失败后锁定账户30分钟
3. **防SQL注入**：使用 MyBatis-Plus 参数绑定，禁止字符串拼接
4. **防XSS**：用户名输入过滤特殊字符
5. **JWT安全**：HMAC-SHA256 签名，24小时过期，密钥≥256位
6. **登录日志**：记录登录成功/失败事件到 audit_log

### 2.5 涉及模块与文件

| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `sql/V10__auth_user.sql` | 新增 | 用户表DDL + 初始数据 |
| `his-gateway/pom.xml` | 修改 | 添加 spring-security-crypto + mybatis-plus 依赖 |
| `his-gateway/src/.../entity/AuthUser.java` | 新增 | 用户实体类 |
| `his-gateway/src/.../mapper/AuthUserMapper.java` | 新增 | 用户Mapper |
| `his-gateway/src/.../mapper/AuthUserMapper.xml` | 新增 | Mapper XML |
| `his-gateway/src/.../service/AuthService.java` | 新增 | 认证服务 |
| `his-gateway/src/.../service/impl/AuthServiceImpl.java` | 新增 | 认证服务实现 |
| `his-gateway/src/.../controller/AuthController.java` | 修改 | 重构为数据库认证 |
| `his-gateway/src/.../config/MybatisPlusConfig.java` | 新增 | MyBatis-Plus 配置 |
| `his-gateway/src/.../dto/LoginRequest.java` | 新增 | 登录请求DTO |
| `his-gateway/src/.../dto/LoginResponse.java` | 新增 | 登录响应DTO |
| `his-common/his-common-web/.../result/ErrorCode.java` | 修改 | 添加认证错误码 |
| `his-rule-engine-web/src/views/login/Login.vue` | 修改 | 使用 axios + 完善 userStore |
| `his-rule-engine-web/src/stores/user.ts` | 修改 | 增加 username/role/realName |
| `his-rule-engine-web/src/api/auth.ts` | 新增 | 登录API封装 |
| `his-rule-engine-web/vite.config.ts` | 修改 | 添加 API 代理 |

---

## 3. 执行步骤

### Step 1: 创建用户表与初始数据

- **目标**: 在数据库中创建 `his_auth_user` 表，并插入默认 admin 用户
- **操作**:
  1. 编写 `V10__auth_user.sql` DDL
  2. admin 密码使用 BCrypt 加密存储（BCrypt hash of "admin"）
  3. 执行 SQL 到数据库
- **验收标准**: 表创建成功，`SELECT * FROM his_auth_user WHERE username='admin'` 返回记录，密码为 bcrypt hash

### Step 2: 扩展错误码

- **目标**: 在 ErrorCode 枚举中添加登录相关错误码
- **操作**:
  1. 在 `ErrorCode.java` 的认证区域添加 HIS-A05/A06/A07
- **验收标准**: 编译通过，错误码可引用

### Step 3: Gateway 添加依赖与配置

- **目标**: 为 Gateway 添加 BCrypt 和 MyBatis-Plus 依赖，配置数据源
- **操作**:
  1. `pom.xml` 添加 `spring-security-crypto`、`mybatis-plus-spring-boot3-starter`、`mysql-connector-j`
  2. `application.yml` 添加数据源配置（复用 his_rule_engine 数据库）
  3. 添加 MyBatis-Plus 配置类
  4. 排除 Gateway 与 MyBatis-Plus 的自动配置冲突
- **验收标准**: Gateway 启动不报错，数据源连接正常

### Step 4: 创建用户实体与 Mapper

- **目标**: 创建 AuthUser 实体和 AuthUserMapper
- **操作**:
  1. 创建 `AuthUser.java` 实体类（@TableName("his_auth_user")）
  2. 创建 `AuthUserMapper.java` 接口（extends BaseMapper）
  3. 创建 `AuthUserMapper.xml`
- **验收标准**: 编译通过

### Step 5: 创建认证服务

- **目标**: 实现 AuthService，包含登录验证、密码校验、失败计数、账户锁定逻辑
- **操作**:
  1. 创建 `AuthService` 接口
  2. 创建 `AuthServiceImpl` 实现类
  3. 实现 `login(username, password)` 方法：
     - 查询用户（by username）
     - 检查账户状态（disabled/locked）
     - BCrypt 密码校验
     - 失败计数 + 锁定逻辑
     - 成功则重置失败计数、更新最后登录时间/IP
     - 生成 JWT Token
  4. 实现 `generateToken()` 方法（增强 JWT claims）
- **验收标准**: 编译通过，逻辑完整

### Step 6: 重构 AuthController

- **目标**: 重构 AuthController 使用 AuthService 替代硬编码认证
- **操作**:
  1. 注入 AuthService
  2. 重写 `login()` 方法调用 `authService.login()`
  3. 返回增强的登录响应（含 username/realName/role）
  4. 添加输入校验（用户名/密码非空、长度限制、XSS 过滤）
  5. 添加登录审计日志
- **验收标准**: 编译通过，接口行为符合规范

### Step 7: 前端登录对接优化

- **目标**: 优化前端登录流程，使用统一 API 层和完善的用户状态管理
- **操作**:
  1. 创建 `src/api/auth.ts` 登录 API 封装
  2. 修改 `Login.vue`：使用 axios 替代 fetch，移除硬编码 URL
  3. 完善 `userStore`：增加 username/role/realName 字段
  4. 修改 `vite.config.ts`：添加 `/api` 代理到网关
  5. 修改路由守卫：增加角色判断
- **验收标准**: 前端登录流程完整，使用 admin/admin 可成功登录

### Step 8: 构建验证与测试

- **目标**: 全面验证登录功能
- **操作**:
  1. 后端 Maven 编译通过
  2. 前端 Vite 构建通过
  3. 启动后端服务，验证登录接口
  4. 启动前端，验证完整登录流程
  5. 测试错误密码、账户锁定等场景
- **验收标准**: 所有场景验证通过

---

## 4. 测试计划

### 4.1 功能测试

- [ ] 使用 admin/admin 登录成功，获取正确 token
- [ ] 使用错误密码登录失败，返回 HIS-A05
- [ ] 连续5次错误密码后账户锁定，返回 HIS-A06
- [ ] 锁定30分钟后可重新登录
- [ ] 禁用账户登录失败，返回 HIS-A07
- [ ] Token 包含正确的 userId/tenantId/role/username
- [ ] 前端登录后正确存储所有用户信息
- [ ] 前端登出后清除所有用户信息

### 4.2 安全测试

- [ ] 密码在数据库中为 bcrypt hash，非明文
- [ ] SQL 注入防护：用户名输入 `' OR 1=1 --` 不生效
- [ ] XSS 防护：用户名输入 `<script>alert(1)</script>` 被过滤
- [ ] Token 过期后请求返回 401

---

## 5. 风险评估

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|:----:|---------|
| Gateway 添加 MyBatis-Plus 与 WebFlux 冲突 | 高 | 中 | Gateway 是 WebFlux，需排除 spring-boot-starter-web，使用 R2DBC 或独立数据源 |
| BCrypt 依赖引入 Spring Security 全家桶 | 中 | 低 | 仅引入 spring-security-crypto，不引入 spring-security-web |
| Gateway 不可用 MyBatis-Plus（WebFlux 限制） | 高 | 高 | 方案B：将认证逻辑移到独立微服务 his-auth-service |

---

## 6. 关键技术决策

### 决策1: Gateway 中使用 MyBatis-Plus

**问题**: Gateway 基于 Spring WebFlux（响应式），MyBatis-Plus 基于 JDBC（阻塞式），两者存在架构冲突。

**方案A（推荐）**: Gateway 中直接使用 Spring JDBC (JdbcTemplate) 查询用户表
- 优点：轻量、无冲突、Gateway 本身认证逻辑简单
- 缺点：需要手写 SQL

**方案B**: 创建独立认证微服务 his-auth-service
- 优点：架构清晰、可独立扩展
- 缺点：增加服务数量、增加运维复杂度

**方案C**: Gateway 中使用 R2DBC
- 优点：响应式兼容
- 缺点：学习成本、生态不成熟

**选择方案A**：Gateway 中使用 JdbcTemplate，最小化改动，避免引入 MyBatis-Plus 与 WebFlux 的冲突。

---

## 7. 进度跟踪

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
| 2026-05-21 07:50 | Step 8 进行中 | 🔄 | 后端编译✅ 前端构建✅，运行时测试待完成（端口9000被旧进程占用） |

---

## 8. 完成检查清单

- [x] his_auth_user 表创建完成，初始 admin 用户插入
- [x] ErrorCode 认证错误码扩展完成
- [x] Gateway JdbcTemplate 配置完成
- [x] AuthUser 实体创建完成
- [x] AuthService 认证服务实现完成
- [x] AuthController 重构完成
- [x] 前端登录对接优化完成
- [x] 构建验证通过（后端 Maven compile ✅，前端 Vite build ✅）
- [ ] 运行时功能测试（需重启 Gateway 服务）
- [ ] 安全测试

---

*计划结束*

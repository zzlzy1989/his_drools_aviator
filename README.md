# HIS 动态规则中台 - 基于 Spring Cloud Alibaba + Nacos

## 📖 项目简介

本项目是一个**医疗业务规则剥离引擎**，旨在将 HIS 系统中高频变化的业务规则（医保报销、质控、合理用药、DRG 分组等）从核心系统中抽离，封装为可独立热加载的规则单元。通过 **Drools + Aviator** 混合架构，实现规则的灵活编排和高性能计算，并通过 **Nacos** 配置中心实现公式的实时动态刷新。

**核心价值**：

- 医院可自主维护业务规则，不再依赖 HIS 厂商的代码修改周期
- 规则变更从"2\~4周"缩短至"分钟级"
- 底层 HIS 退化为稳定的事务底座，上层业务逻辑灵活可控

***

## 🛠️ 技术栈

| 组件                   | 版本           |  状态 | 用途                |
| :------------------- | :----------- | :-: | :---------------- |
| Spring Boot          | 3.5.0        |  ✅  | 基础框架              |
| Spring Cloud         | 2025.0.0     |  ✅  | 微服务框架            |
| Spring Cloud Alibaba | 2025.0.0.0   |  ✅  | Nacos 注册中心 + 配置中心 |
| Drools               | 8.44.0.Final |  ✅  | 规则引擎（规则流编排）       |
| Aviator              | 5.4.3        |  ✅  | 高性能表达式求值          |
| Caffeine             | 3.1.8        |  ✅  | 本地缓存（编译后表达式）      |
| MyBatis-Plus         | 3.5.6        |  ✅  | ORM 框架            |
| MySQL                | 8.0+         |  ✅  | 存储规则定义、事实数据       |
| Nacos Server         | 3.0.3        |  ✅  | 服务注册 + 配置中心      |
| OpenFeign            | -            |  ✅  | 声明式服务调用           |
| Maven                | 3.8+         |  ✅  | 构建工具              |
| JDK                  | 21           |  ✅  | 运行环境              |
| Vue 3                | 3.4.x        |  ✅  | 前端框架              |
| Element Plus         | 2.7+         |  ✅  | 前端 UI 组件库         |
| AntV X6              | 2.x          |  ✅  | 规则流可视化编辑器         |

> ✅ 已集成   📋 待实现   ⏳ 规划中

***

## 📦 环境要求

- JDK 21 或更高版本
- Maven 3.8+
- Nacos Server 3.0+（[下载地址](https://github.com/alibaba/nacos/releases)）
- MySQL 8.0（仅当需要持久化规则库时）
- Docker & Docker Compose（推荐容器化部署）
- Git

***

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/zzlzy1989/his_drools_aviator.git
cd his_drools_aviator
```

### 2. 启动 Nacos Server ✅

**方式一：Docker 启动（推荐）**

```bash
# 启动 Nacos 3.0.3 单机模式
docker run -d --name his-nacos \
  --restart unless-stopped \
  -p 8848:8848 -p 9848:9848 -p 9849:9849 \
  -e MODE=standalone \
  -e NACOS_AUTH_TOKEN=your-secret-token-here \
  -e NACOS_AUTH_IDENTITY_KEY=nacos-auth \
  -e NACOS_AUTH_IDENTITY_VALUE=my-identity-value \
  -e JVM_XMS=256m -e JVM_XMX=512m \
  nacos/nacos-server:v3.0.3

# 验证启动
curl http://localhost:8848/nacos/v1/console/server/state
```

**方式二：本地下载启动**

```bash
# 解压并启动
unzip nacos-server-3.0.x.zip
cd nacos/bin
# Linux/Mac
sh startup.sh -m standalone
# Windows
startup.cmd -m standalone
```

访问 Nacos 控制台：<http://localhost:8848/nacos> （默认账号密码：nacos/nacos）

### 3. 创建 Nacos 配置 <span style="color:orange">📋 待实现</span>

在 Nacos 中创建 Data ID 为 `his-rule-engine.yaml`，Group 为 `HIS_RULE_GROUP` 的配置，内容如下：

```yaml
formulas:
  reimburse:
    resident: "round((totalFee - 500) * 0.65, 2)"
    employee: "round((totalFee - 1000) * 0.85, 2)"
  drg:
    weight: "(baseWeight + extraPoints) * severityFactor"
```

### 4. 配置数据库 <span style="color:orange">📋 待实现</span>

数据库配置：本地127.0.0.1/92.168.1.105 端口 3306,用户名root,密码testhub123
如需要持久化规则操作日志或规则元数据，请创建 MySQL 数据库并执行以下脚本：

```sql
CREATE DATABASE `his_rule_engine` CHARACTER SET utf8mb4;

USE `his_rule_engine`;

CREATE TABLE `rule_formula` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rule_key` varchar(128) NOT NULL COMMENT '规则标识',
  `formula_text` text NOT NULL COMMENT '公式表达式',
  `version` int DEFAULT '1',
  `status` tinyint DEFAULT '1' COMMENT '1-生效 0-失效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_key` (`rule_key`)
);
```

修改各服务 `application.yml` 中的数据库连接参数。

### 5. 编译项目

```bash
cd his-rule-engine
mvn clean compile
```

### 6. 启动服务（Docker 方式 - 推荐）

```bash
cd his-rule-engine/docker

# 创建 .env 配置文件
cp .env.example .env

# 构建并启动所有服务
docker compose up -d

# 查看服务状态
docker ps --filter name=his-

# 查看服务日志
docker compose logs -f his-gateway
```

### 7. 启动服务（本地开发方式）

```bash
# 启动网关（端口 9000）
cd his-gateway
mvn spring-boot:run

# 启动规则管理服务（端口 9001）
cd his-rule-service
mvn spring-boot:run

# 启动医保结算服务（端口 9003）
cd his-settlement-service
mvn spring-boot:run

# ... 其他服务按需启动
```

### 8. 测试规则调用 ✅

```bash
# 结算测试（示例）
curl -X POST http://localhost:9000/api/v1/settlements \
  -H "Content-Type: application/json" \
  -d '{"patientType":"resident","totalFee":2000}'
```

预期返回：`{"code":"0","data":{...}}`

### 9. 前端访问 ✅

前端服务已部署至 Nginx，访问地址：http://localhost:8999/

***

## 📁 项目模块结构

```
his_drools_aviator/
├── .trae/                          # Trae IDE Harness 工程配置
│   ├── agent.md                    # AI 行为规范（身份/约束/流程/计划检查）
│   ├── MEMORY.md                   # 项目事实知识（技术栈/目录/API/环境）
│   ├── settings.local.json         # 权限配置 + 计划管理设置
│   │
│   ├── rules/                      # 编码规范（按领域模块化）
│   │   ├── README.md               # 规则索引 + 触发场景速查
│   │   ├── workflow.md             # 7 步强制工作流程 + 3 阻塞节点
│   │   ├── code-style.md           # Java/Spring/Drools/Aviator 编码规范
│   │   ├── testing.md              # JUnit5/MockMvc/DRL 规则测试规范
│   │   ├── security.md             # SQL 注入/XSS/Aviator 表达式注入防护
│   │   ├── api-design.md           # REST API 设计规范
│   │   ├── database.md             # MySQL 表结构/查询优化/索引规范
│   │   ├── error-handling.md       # ErrorCode 体系/异常层级/日志规范
│   │   ├── docker-deploy.md        # Docker 多阶段构建/JVM 优化/服务编排
│   │   ├── performance.md          # Caffeine 缓存/KieBase 分组/并发控制
│   │   ├── documentation.md        # DRL 规则/Aviator 公式注释规范
│   │   └── git-commit-message.md   # Conventional Commits 提交规范
│   │
│   ├── commands/                   # 自定义快捷命令
│   │   ├── README.md               # 命令清单 + 使用说明
│   │   ├── health-check.md         # ((command:health)) 环境检查
│   │   ├── show-context.md         # ((command:context)) 任务上下文
│   │   ├── project-status.md       # ((command:status)) 项目状态
│   │   ├── analyze-task.md         # ((command:analyze-task)) 任务分析
│   │   ├── check-memory.md         # ((command:check-memory)) 记忆检查
│   │   ├── summarize.md            # ((command:summarize)) 会话总结
│   │   ├── review.md               # ((command:review)) 代码审查
│   │   ├── knowledge-writeback.md  # ((command:knowledge-writeback)) 知识回写
│   │   └── plan.md                 # ((command:plan)) 计划管理
│   │
│   ├── hooks/                      # 事件驱动拦截脚本
│   │   ├── README.md               # Hook 配置说明 + 与 agent.md 关系
│   │   ├── pre-execute-shell.sh    # Shell 执行前 - 危险命令拦截
│   │   ├── post-execute-shell.sh   # Shell 执行后 - 日志记录
│   │   ├── pre-write-file.sh       # 文件写入前 - 受保护文件确认
│   │   ├── post-read-file.sh       # 文件读取后 - 敏感信息提醒
│   │   ├── pre-browser.sh          # 浏览器启动前 - CDP 提示
│   │   ├── pre-search.sh           # 搜索前 - 敏感词过滤
│   │   ├── pre-task-start.sh       # 任务开始前 - 计划检查/创建提示
│   │   ├── post-task-complete.sh   # 任务完成后 - 知识回写 + 计划状态检查
│   │   └── post-plan-update.sh     # 计划变更后 - 索引更新 + 变更日志
│   │
│   ├── domain/                     # 业务知识库（持续积累）
│   │   ├── README.md               # 知识库索引
│   │   ├── glossary.md             # HIS/Drools/Aviator 术语表
│   │   ├── rules.md                # 医保结算/费用校验业务规则
│   │   ├── state-machines.md       # 规则生命周期/结算流程状态机
│   │   ├── edge-cases.md           # BigDecimal 精度/规则冲突边界
│   │   ├── decisions.md            # 架构/数据/集成决策记录
│   │   └── modules/                # 模块概述（6 个微服务）
│   │
│   ├── workflow-plans/             # 计划生成与跟踪系统
│   │   ├── README.md               # 使用指南
│   │   ├── TEMPLATE.md             # 计划模板
│   │   ├── INDEX.md                # 计划跟踪索引（自动更新）
│   │   ├── plan.sh                 # 计划管理 CLI 工具
│   │   ├── active/                 # 活跃计划（pending/in_progress）
│   │   ├── completed/              # 已完成计划
│   │   └── archived/               # 已归档计划（7 天后自动归档）
│   │
│   ├── memory/                     # 日记系统
│   │   ├── README.md               # 日记使用说明
│   │   └── TEMPLATE.md             # 日记模板
│   │
│   ├── CHANGELOG.md                # Harness 配置变更历史
│   ├── SUMMARY.md                  # 实施总结
│   └── MIGRATION_GUIDE.md          # Harness 工程跨项目迁移指南
│
├── .gitignore                      # Git 忽略规则
├── README.md                       # 项目说明
├── PRD.md                          # 产品需求文档（v1.0）
├── DEVELOPMENT_GUIDE.md            # v1.0 开发指导文档
├── drools_aviator.md               # 架构设计文档
├── test-his-ui-drug.md             # 测试用例文档
│
└── his-rule-engine/                # 后端代码（微服务架构）
    ├── pom.xml                     # 父 POM（版本管理、依赖统一）
    │
    ├── his-common/                 # 公共模块
    │   ├── pom.xml
    │   ├── his-common-core/        #   核心：枚举、异常、Fact 对象
    │   ├── his-common-web/         #   Web：统一响应、异常处理、Swagger
    │   ├── his-common-drools/      #   Drools 引擎封装
    │   └── his-common-aviator/     #   Aviator 引擎封装
    │
    ├── his-gateway/                # API 网关 (9000)
    │   └── Spring Cloud Gateway + Nacos + Sentinel
    │
    ├── his-rule-service/           # 规则管理服务 (9001)
    │   └── 规则 CRUD、版本管理、DRL 发布
    │
    ├── his-formula-service/        # 公式管理服务 (9002)
    │   └── 公式 CRUD、语法校验、Nacos 同步
    │
    ├── his-settlement-service/     # 医保结算服务 (9003)
    │   └── 费用结算、报销计算、规则执行
    │
    ├── his-drug-service/           # 合理用药服务 (9004)
    │   └── 处方审核、配伍禁忌、极量检查
    │
    ├── his-quality-service/        # 质控服务 (9005)
    │   └── 院感防控、质控规则、拦截卡控
    │
    └── his-drg-service/            # DRG分组服务 (9006)
        └── DRG/DIP 分组、权重计算、标准分值
```

***

## 📖 使用说明

### 一、Harness 工程体系概览

本项目采用 **Harness 工程体系**，通过 `.trae/` 和 `.claude/` 目录下的配置，实现 AI 辅助开发的标准化、自动化。

**核心价值**：

- ✅ **规范约束**：11 个编码规范文件，覆盖 Java/Spring/Drools/Aviator 全技术栈
- ✅ **流程管控**：7 步强制工作流程 + 3 阻塞节点，确保开发质量
- ✅ **知识积累**：领域知识库 + 项目记忆，AI 不会遗忘业务上下文
- ✅ **计划跟踪**：自动生成任务计划，跟踪进度，自动归档
- ✅ **安全防护**：10 个钩子脚本，拦截危险操作，保护关键文件

### 二、日常开发流程

#### 1. 开始新任务

```
# 查看当前任务上下文
((command:context))

# 分析任务复杂度
((command:analyze-task))
```

AI 会自动：

- 检查是否有活跃计划（`.trae/workflow-plans/active/`）
- 如无计划，提示创建新计划
- 加载相关规范文件（根据任务类型自动匹配）

#### 2. 遵循 7 步工作流程

| 步骤     | 名称   | 说明                    |  阻塞节点  |
| ------ | ---- | --------------------- | :----: |
| Step 1 | 梳理业务 | 理解需求，查阅 domain/ 知识库   |    -   |
| Step 2 | 规划改动 | 输出改动清单，确认方案           | ★ 必须确认 |
| Step 3 | 实现代码 | 按规范编写代码               |    -   |
| Step 4 | 实现审查 | 检查 BigDecimal、异常处理、安全 | ★ 必须检查 |
| Step 5 | 质量验证 | 验证公式语法、金额精度、规则触发      | ★ 必须验证 |
| Step 6 | 测试验证 | 运行单元测试、集成测试           |    -   |
| Step 7 | 知识回写 | 更新 domain/ 文件，积累知识    |    -   |

#### 3. 任务完成

```
# 总结会话并回写知识
((command:summarize))

# 更新项目状态
((command:status))
```

AI 会自动：

- 更新计划状态（pending → in\_progress → completed）
- 将新知识写入 `domain/` 目录
- 更新 `CHANGELOG.md` 记录变更

### 三、命令系统

#### 开发辅助命令

| 命令                                | 说明      | 使用场景        |
| --------------------------------- | ------- | ----------- |
| `((command:health))`              | 环境健康检查  | 开发环境异常排查    |
| `((command:context))`             | 显示任务上下文 | 开始新任务前      |
| `((command:status))`              | 项目状态总览  | 了解项目进度      |
| `((command:analyze-task))`        | 分析任务复杂度 | 评估工作量       |
| `((command:check-memory))`        | 检查记忆状态  | 确认 AI 知识完整性 |
| `((command:summarize))`           | 会话总结    | 任务完成时       |
| `((command:review))`              | 代码审查    | 代码提交前       |
| `((command:knowledge-writeback))` | 知识回写    | 积累业务知识      |

#### 计划管理命令

| 命令                                                   | 说明     | 示例                                                                         |
| ---------------------------------------------------- | ------ | -------------------------------------------------------------------------- |
| `((command:plan list))`                              | 查看活跃计划 | 了解当前任务进度                                                                   |
| `((command:plan create <标题> <类型> <阶段> <负责人> <优先级>))` | 创建新计划  | `((command:plan create 实现医保报销规则 feature Phase 2 张三 P0))`                   |
| `((command:plan update <文件名> <状态> <备注>))`            | 更新计划状态 | `((command:plan update 2026-04-26-feature-reimburse.md in_progress 开始开发))` |
| `((command:plan show <文件名>))`                        | 查看计划详情 | `((command:plan show 2026-04-26-feature-reimburse.md))`                    |
| `((command:plan reindex))`                           | 重建索引   | 索引异常时手动修复                                                                  |

**计划类型**：`feature` | `bugfix` | `refactor` | `config` | `docs` | `test` | `deploy` | `research`

**优先级**：`P0`（紧急） | `P1`（高） | `P2`（中） | `P3`（低）

### 四、钩子机制（Hooks）

钩子脚本在特定事件触发时自动执行，无需手动调用：

| 钩子                      | 触发时机      | 功能                 |
| ----------------------- | --------- | ------------------ |
| `pre-task-start.sh`     | 任务开始前     | 检查活跃计划，提示创建/关联     |
| `post-task-complete.sh` | 任务完成后     | 知识回写 + 计划状态检查      |
| `post-plan-update.sh`   | 计划变更后     | 更新索引 + 变更日志        |
| `pre-write-file.sh`     | 文件写入前     | 保护关键文件（如 agent.md） |
| `post-read-file.sh`     | 文件读取后     | 提醒敏感信息（如密码）        |
| `pre-execute-shell.sh`  | Shell 执行前 | 拦截危险命令（如 rm -rf）   |
| `post-execute-shell.sh` | Shell 执行后 | 记录命令执行日志           |

### 五、规范文件速查

根据任务类型，AI 会自动加载对应规范：

| 任务类型          | 关联规范                                                                                       | 说明     |
| ------------- | ------------------------------------------------------------------------------------------ | ------ |
| 新建规则          | `workflow.md` → `code-style.md` → `database.md` → `api-design.md` → `testing.md`           | 完整开发流程 |
| 编写 DRL        | `workflow.md` → `code-style.md(Drools)` → `documentation.md` → `testing.md(DRL测试)`         | 规则开发专用 |
| 编写 Aviator 公式 | `workflow.md` → `code-style.md(Aviator)` → `security.md` → `performance.md` → `testing.md` | 公式开发专用 |
| 开发 API        | `workflow.md` → `api-design.md` → `error-handling.md` → `security.md` → `testing.md`       | 接口开发专用 |
| Docker 部署     | `docker-deploy.md` → `performance.md` → `security.md(Docker加固)`                            | 部署专用   |
| 性能优化          | `performance.md` → `database.md(查询优化)` → `code-style.md(BigDecimal)`                       | 调优专用   |

### 六、知识库维护

#### domain/ 目录结构

```
domain/
├── glossary.md          # 术语表（HIS/Drools/Aviator 专业术语）
├── rules.md             # 业务规则（医保结算/费用校验）
├── state-machines.md    # 状态机（规则生命周期/结算流程）
├── edge-cases.md        # 边界案例（BigDecimal 精度/规则冲突）
├── decisions.md         # 架构决策记录（为什么这样设计）
└── modules/             # 模块知识（6 个微服务）
    ├── config/overview.md
    ├── formulas/overview.md
    ├── rules/overview.md
    ├── settlements/overview.md
    ├── skills/overview.md
    └── tenants/overview.md
```

#### 何时更新知识库

| 场景      | 更新文件                         | 内容             |
| ------- | ---------------------------- | -------------- |
| 发现新业务术语 | `glossary.md`                | 术语定义、使用场景      |
| 实现新业务规则 | `rules.md`                   | 规则逻辑、触发条件      |
| 遇到边界问题  | `edge-cases.md`              | 问题描述、解决方案      |
| 做出架构决策  | `decisions.md`               | 决策背景、方案对比、最终选择 |

***

## 🔧 故障排查记录

> 记录部署和开发过程中遇到的问题及解决方案，避免重复踩坑。

### 1. Docker 容器间网络不通导致服务反复重启

**现象**：Gateway 服务启动后反复重启，日志报 `NacosException: Client not connected, current status:STARTING`

**根因**：Nacos 容器在 `bridge` 默认网络，而其他微服务容器在 `docker_his-net` 自定义网络。Gateway 容器内无法通过 `nacos` 主机名解析到 Nacos 服务，导致 Nacos 注册失败，服务启动后立即崩溃重启。

**排查步骤**：
```bash
# 1. 检查容器所在网络
docker inspect his-nacos --format='{{range $k, $v := .NetworkSettings.Networks}}{{$k}}: {{$v.IPAddress}}{{"\n"}}{{end}}'
docker inspect his-gateway --format='{{range $k, $v := .NetworkSettings.Networks}}{{$k}}: {{$v.IPAddress}}{{"\n"}}{{end}}'

# 2. 在容器内测试 DNS 解析
docker exec his-gateway wget -qO- "http://nacos:8848/nacos/v1/ns/instance/list?serviceName=his-rule-service"

# 3. 查看错误日志
docker logs his-gateway 2>&1 | grep -A 3 "Caused by"
```

**解决方案**：将 Nacos 容器连接到 `docker_his-net` 网络并添加别名
```bash
docker network connect --alias nacos docker_his-net his-nacos
docker restart his-gateway
```

**预防措施**：在 `docker-compose.yml` 中确保所有服务使用同一个 `networks` 配置，Nacos 服务必须加入 `his-net` 网络：
```yaml
services:
  nacos:
    networks:
      - his-net    # 确保与其他服务在同一网络
```

---

### 2. JWT 白名单路径匹配失败

**现象**：前端调用 `/api/v2/flows` 接口返回 `HIS-401 JWT Token缺失`，即使该路径已在白名单中

**根因**：`JwtAuthenticationFilter.isWhiteListed()` 方法中，`/api/v2/flows/**` 替换 `**` 后变成 `/api/v2/flows/`（带尾部斜杠），而实际请求路径是 `/api/v2/flows`（无尾部斜杠），导致 `path.startsWith(basePath)` 匹配失败。

**错误代码**：
```java
private boolean isWhiteListed(String path) {
    return WHITE_LIST.stream().anyMatch(pattern -> {
        String basePath = pattern.replace("**", "");
        return path.equals(basePath) || path.startsWith(basePath);
        // "/api/v2/flows".startsWith("/api/v2/flows/") → false
    });
}
```

**修复代码**：
```java
private boolean isWhiteListed(String path) {
    return WHITE_LIST.stream().anyMatch(pattern -> {
        if (pattern.contains("**")) {
            String basePath = pattern.replace("**", "").replaceAll("/+$", "");
            return path.equals(basePath) || path.startsWith(basePath + "/");
        }
        return path.equals(pattern) || path.startsWith(pattern + "/");
    });
}
```

**关键点**：
- 替换 `**` 后需去除尾部斜杠（`.replaceAll("/+$", "")`）
- `startsWith` 判断时需补回斜杠（`basePath + "/"`），避免 `/api/v2/flowsxxx` 误匹配

---

### 3. Spring Cloud Alibaba 与 Nacos Server 版本不兼容

**现象**：微服务启动报 `NacosException: Client not connected` 或 `requestToServer failed`

**根因**：Spring Cloud Alibaba 2025.0.0.0 内置 Nacos Client 3.0.x，需要 Nacos Server 3.0+ 配合。使用 Nacos Server 2.3.x 会导致 gRPC 协议不兼容。

**版本对应关系**：

| Spring Cloud Alibaba | Nacos Client | Nacos Server（最低） |
|:---|:---|:---|
| 2025.0.0.0 | 3.0.x | 3.0.0+ |
| 2023.0.x | 2.x | 2.2.0+ |
| 2022.0.x | 2.x | 2.2.0+ |

**解决方案**：升级 Nacos Server 到 3.0.3
```bash
docker pull nacos/nacos-server:v3.0.3
```

---

### 4. Nacos 服务注册未启用

**现象**：服务启动正常但 Nacos 控制台看不到服务注册信息

**根因**：`application.yml` 中 `service-registry.auto-registration.enabled` 设为 `false`

**解决方案**：修改各服务的 `application.yml`
```yaml
spring:
  cloud:
    service-registry:
      auto-registration:
        enabled: true   # 必须为 true
```

---

### 5. 前端规则流设计器节点拖拽位置偏移

**现象**：从节点面板拖拽节点到画布时，节点落在鼠标位置之外

**根因**：`FlowEditor.vue` 的 `handleDrop` 函数中，坐标计算未考虑画布容器的偏移量，且未使用 `graph.clientToGraph()` 将客户端坐标转换为图坐标

**修复代码**：
```typescript
function handleDrop(event: DragEvent) {
  event.preventDefault()
  const nodeType = event.dataTransfer?.getData('nodeType') as NodeType
  if (!nodeType || !graph) return

  const rect = graphRef.value!.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top

  const position = graph.clientToGraph({ x, y })  // 关键：客户端坐标 → 图坐标
  const nodeId = `node_${Date.now()}`
  const config = nodeTypes.find(nt => nt.type === nodeType)

  const nodeData: NodeDTO = {
    nodeId,
    type: nodeType,
    label: config?.label || '新节点',
    x: position.x - 60,  // 居中偏移
    y: position.y - 25,
  }
  // ...
}
```

**关键点**：
- 必须用 `getBoundingClientRect()` 获取画布偏移
- 必须用 `graph.clientToGraph()` 转换坐标系
- 减去节点宽高的一半实现居中放置

---

### 6. Gateway 路由缺少 API 路径

**现象**：前端调用 `/api/v2/flows/**` 接口返回 404

**根因**：`GatewayRoutesConfig.java` 和 `application.yml` 中未配置 `/api/v2/flows/**` 的路由规则

**解决方案**：在路由配置中添加
```java
.route("rule-service", r -> r
    .path("/api/v1/rules/**", "/api/v1/rule-groups/**", "/api/v2/flows/**")
    .filters(f -> f.stripPrefix(0))
    .uri("lb://his-rule-service"))
```

**预防措施**：新增 API 路径时，必须同步更新以下两处：
1. `GatewayRoutesConfig.java`（Java 配置）
2. `application.yml`（YAML 配置）
3. `JwtAuthenticationFilter.java` 白名单（如需免认证）

---

### 7. Nginx 代理前端 API 请求 404

**现象**：浏览器访问 `http://localhost:8999` 页面正常，但 API 调用 404

**根因**：Nginx 配置未将 `/api` 请求代理到 Gateway 服务

**解决方案**：Nginx 配置中添加 API 代理
```nginx
server {
    listen 80;
    location /api/ {
        proxy_pass http://host.docker.internal:9000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
}
```

**关键点**：
- `/api/` 路径代理到 Gateway（端口 9000）
- `/` 路径服务前端静态文件
- `try_files` 确保 Vue Router history 模式正常工作

---

### 8. MySQL 授权问题导致 Nacos 初始化失败

**现象**：Nacos 启动报数据库连接失败或表不存在

**根因**：MySQL 8.0 默认不授权远程访问，且 Nacos 3.0 需要独立的数据库

**解决方案**：
```sql
-- 创建 Nacos 数据库
CREATE DATABASE `his_nacos_v3` CHARACTER SET utf8mb4;

-- 授权远程访问
GRANT ALL PRIVILEGES ON his_nacos_v3.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

**预防措施**：Docker 部署时，MySQL 容器需在初始化脚本中完成建库和授权操作

***

> 最后更新: 2026-05-04
| 新增微服务模块 | `modules/<name>/overview.md` | 模块职责、接口、依赖     |

### 七、常见问题

#### Q1：AI 不遵循规范怎么办？

**A**：检查以下文件是否存在且内容正确：

- `.trae/agent.md` - AI 行为规范
- `.trae/rules/workflow.md` - 工作流程
- `.trae/rules/code-style.md` - 编码规范

如被修改，检查 `CHANGELOG.md` 查看变更历史。

#### Q2：如何查看当前计划状态？

```
((command:plan list))
```

或查看文件：`.trae/workflow-plans/INDEX.md`

#### Q3：计划文件太多怎么办？

已完成 7 天以上的计划会自动归档到 `archived/` 目录。也可手动归档：

```bash
mv .trae/workflow-plans/active/xxx.md .trae/workflow-plans/archived/
```

#### Q4：如何跨项目复用 Harness 配置？

查看 `.trae/MIGRATION_GUIDE.md`，包含完整的迁移步骤和注意事项。

#### Q5：.trae 和 .claude 目录有什么区别？

- `.trae/` - Trae IDE 使用
- `.claude/` - Claude Code 使用
- 内容完全同步，保持功能一致

***

## 📊 服务端口规划

| 服务                 |  端口  | 说明         |  状态  |
| :----------------- | :--: | :--------- | :--: |
| Nacos Server       | 8848 | 服务注册 + 配置中心 | ✅ 已部署 |
| API Gateway        | 9000 | 统一入口，路由转发  | ✅ 已部署 |
| Rule Service       | 9001 | 规则管理       | ✅ 已部署 |
| Formula Service    | 9002 | 公式管理       | ✅ 已部署 |
| Settlement Service | 9003 | 医保结算       | ✅ 已部署 |
| Drug Service       | 9004 | 合理用药       | ✅ 已部署 |
| Quality Service    | 9005 | 质控         | ✅ 已部署 |
| DRG Service        | 9006 | DRG/DIP 分组 | ✅ 已部署 |
| Frontend (Nginx)   | 8999 | Vue 3 前端   | ✅ 已部署 |

> ✅ 已部署 = 已容器化部署，服务正常运行

***

## ⚙️ 核心配置说明

### Nacos 配置刷新 <span style="color:orange">📋 待实现</span>

- 所有公式均通过 `@RefreshScope` + `NacosConfigManager` 实现动态更新
- 修改 Nacos 中的 `formulas.*` 配置后，应用会自动重新加载公式并刷新 Aviator 缓存，**无需重启**

### Drools 规则热加载 <span style="color:orange">📋 待实现</span>

- 规则文件放在 `src/main/resources/rules/` 下
- 生产环境建议将 `.drl` 文件也存放在 Nacos 或数据库中，通过 `KieScanner` 实现热部署

### Aviator 表达式缓存 <span style="color:orange">📋 待实现</span>

- 使用 Caffeine 缓存编译后的 `Expression` 对象
- 缓存大小：5000 条，过期时间：30 分钟
- 公式变更时自动失效对应的缓存条目

***

## 🧪 示例：医保结算规则流 <span style="color:orange">📋 待实现</span>

### 事实对象（Fact）

```java
public class SettlementFact {
    private String patientType;      // resident / employee
    private BigDecimal totalFee;
    private BigDecimal deductible;
    private BigDecimal ratio;
    private BigDecimal finalAmount;
    // getters/setters
}
```

### Drools 规则文件（reimbursement.drl）

```drools
import com.his.rule.fact.SettlementFact;
import com.his.rule.engine.aviator.AviatorHelper;

global NacosFormulaManager formulaManager;
global AviatorHelper aviatorHelper;

rule "Determine Deductible"
    when
        $f: SettlementFact(patientType == "resident")
    then
        $f.setDeductible(new BigDecimal("500"));
        update($f);
end

rule "Calculate Reimbursement"
    when
        $f: SettlementFact(deductible != null, totalFee != null)
    then
        String formula = formulaManager.getFormula("reimburse.resident");
        BigDecimal amount = aviatorHelper.executeFormula(formula, $f);
        $f.setFinalAmount(amount);
        update($f);
end
```

***

## 📊 性能指标（目标） <span style="color:orange">📋 待实现</span>

| 场景                   | 目标耗时    |
| :------------------- | :------ |
| 单条规则匹配（Drools）       | < 5ms   |
| Aviator 表达式执行（已缓存）   | < 0.5ms |
| Nacos 配置变更到生效（含缓存刷新） | < 2s    |
| 规则引擎冷启动（加载 100 条规则）  | \~800ms |

***

## 🧰 常见问题

**Q：Nacos 配置不生效？**\
A：检查 `application.yml` 中的 `spring.cloud.nacos.config` 是否正确，并确保应用已添加 `@RefreshScope`。

**Q：Aviator 表达式报错** **`Unknown variable`？**\
A：请在 `FormulaValidator` 中定义允许的变量白名单，或在执行前将所需变量全部放入 `env` 中。

**Q：如何发布新的规则文件？**\
A：本示例从 classpath 加载 `.drl` 文件，重新打包即可。生产环境建议将规则文件放入 Nacos 或数据库，通过 `KieScanner` 动态加载。

**Q：支持多租户（多个医院）吗？**\
A：可在 Nacos 中使用不同的 `namespace` 或 `group` 来隔离医院的配置；Drools 会话也可按租户独立构建。

***

## 🔧 扩展开发指南

1. **新增一个业务规则集**
   - 定义新的事实对象（Fact）
   - 编写对应的 `.drl` 文件
   - 在 Nacos 中添加公式配置
   - 实现业务服务调用规则引擎
2. **接入自己的数据库作为规则源**
   - 实现 `RuleProvider` 接口，从数据库读取规则内容
   - 移除 `application.yml` 中的 Nacos 依赖（可选）
3. **集成监控**
   - 暴露 `/actuator/health` 端点
   - 记录每次规则调用的耗时和命中率（Aviator 缓存统计）

***

## 📄 许可证

Apache License 2.0

***

## 👥 贡献者

欢迎提交 Issue 或 Pull Request。

**项目维护者**：蓝天

***

## 🔗 相关文档

- [Spring Cloud Alibaba 官方文档](https://sca.aliyun.com/docs/)
- [Nacos 配置管理](https://nacos.io/zh-cn/docs/configuration-management.html)
- [Drools 用户手册](https://docs.drools.org/)
- [Aviator 表达式引擎指南](https://github.com/killme2008/aviator)


# Harness 工程 - HIS Drools+Aviator 规则引擎
是
> AI Agent 的工作框架，规范化 HIS 规则引擎开发流程

---

## 概述

Harness 是一个 AI Agent 工程框架，通过结构化的规范、命令、钩子和记忆系统，确保 AI 输出的一致性和质量。本配置已针对 **HIS 医疗业务规则剥离引擎** 进行定制。

**核心架构**: Drools (规则匹配) + Aviator (表达式计算) + Nacos (配置中心)

---

## 目录结构

```
.trae/
├── agent.md               # AI 行为规范（身份/约束/流程/计划检查）
├── MEMORY.md              # 项目事实知识（技术栈/目录/API/环境）
├── settings.local.json    # 权限配置 + 计划管理设置
│
├── commands/              # 自定义快捷命令
│   ├── README.md          # 命令清单 + 使用说明
│   ├── health-check.md    # ((command:health)) 环境检查
│   ├── show-context.md    # ((command:context)) 任务上下文
│   ├── project-status.md  # ((command:status)) 项目状态
│   ├── analyze-task.md    # ((command:analyze-task)) 任务分析
│   ├── check-memory.md    # ((command:check-memory)) 记忆检查
│   ├── summarize.md       # ((command:summarize)) 会话总结
│   ├── review.md          # ((command:review)) 代码审查
│   ├── knowledge-writeback.md  # ((command:knowledge-writeback)) 知识回写
│   └── plan.md            # ((command:plan)) 计划管理
│
├── rules/                 # 模块化规范文件
│   ├── README.md          # 规则索引 + 触发场景速查
│   ├── workflow.md        # 7 步强制工作流程 + 3 阻塞节点
│   ├── code-style.md      # Java/Spring/Drools/Aviator 编码规范
│   ├── testing.md         # JUnit5/MockMvc/DRL 规则测试规范
│   ├── security.md        # SQL 注入/XSS/Aviator 表达式注入防护
│   ├── api-design.md      # REST API 设计规范
│   ├── database.md        # MySQL 表结构/查询优化/索引规范
│   ├── error-handling.md  # ErrorCode 体系/异常层级/日志规范
│   ├── docker-deploy.md   # Docker 多阶段构建/JVM 优化/服务编排
│   ├── performance.md     # Caffeine 缓存/KieBase 分组/并发控制
│   ├── documentation.md   # DRL 规则/Aviator 公式注释规范
│   └── git-commit-message.md  # Conventional Commits 提交规范
│
├── hooks/                 # 事件驱动拦截脚本
│   ├── README.md          # Hook 配置说明 + 与 agent.md 关系
│   ├── pre-execute-shell.sh    # Shell 执行前 - 危险命令拦截
│   ├── post-execute-shell.sh   # Shell 执行后 - 日志记录
│   ├── pre-write-file.sh       # 文件写入前 - 受保护文件确认
│   ├── post-read-file.sh       # 文件读取后 - 敏感信息提醒
│   ├── pre-browser.sh          # 浏览器启动前 - CDP 提示
│   ├── pre-search.sh           # 搜索前 - 敏感词过滤
│   ├── pre-task-start.sh       # 任务开始前 - 计划检查/创建提示
│   ├── post-task-complete.sh   # 任务完成后 - 知识回写 + 计划状态检查
│   └── post-plan-update.sh     # 计划变更后 - 索引更新 + 变更日志
│
├── domain/                # 业务知识库（持续积累）
│   ├── README.md          # 知识库索引
│   ├── glossary.md        # HIS/Drools/Aviator 术语表
│   ├── rules.md           # 医保结算/费用校验业务规则
│   ├── state-machines.md  # 规则生命周期/结算流程状态机
│   ├── edge-cases.md      # BigDecimal 精度/规则冲突边界
│   ├── decisions.md       # 架构/数据/集成决策记录
│   └── modules/           # 模块概述（6 个微服务）
│       ├── config/overview.md
│       ├── formulas/overview.md
│       ├── rules/overview.md
│       ├── settlements/overview.md
│       ├── skills/overview.md
│       └── tenants/overview.md
│
├── workflow-plans/        # 计划生成与跟踪系统
│   ├── README.md          # 使用指南
│   ├── TEMPLATE.md        # 计划模板
│   ├── INDEX.md           # 计划跟踪索引（自动更新）
│   ├── plan.sh            # 计划管理 CLI 工具
│   ├── active/            # 活跃计划（pending/in_progress）
│   ├── completed/         # 已完成计划
│   └── archived/          # 已归档计划（7 天后自动归档）
│
├── memory/                # 日记系统
│   ├── README.md          # 日记使用说明
│   └── TEMPLATE.md        # 日记模板
│
├── CHANGELOG.md           # Harness 配置变更历史
├── SUMMARY.md             # 实施总结
└── MIGRATION_GUIDE.md     # Harness 工程跨项目迁移指南
```

---

## 核心文件

### agent.md

Agent 的项目说明书，定义:
- 身份定位：HIS 规则引擎专家（非通用开发者）
- 技术栈约束：Java 17+ / Spring Boot 3.x / Drools 8.x / Aviator 5.x
- 强制使用 BigDecimal 处理金额
- 组件使用规则：Caffeine 缓存、Nacos 配置中心
- API 端点规范：`/api/v1/rules/*`, `/api/v1/settlement/*`

### MEMORY.md

长期记忆文件，包含:
- 项目元数据（HIS 规则剥离引擎 v1.0）
- 技术栈完整版本号表
- 目录结构说明
- API 端点清单
- 数据库核心表结构
- 环境配置信息

---

## 快速开始

### 1. 理解任务
使用 `((command:context))` 查看当前上下文

### 2. 执行任务
按 rules/ 中的规范执行：
- 开发新功能 → 遵循 workflow.md 的 7 步流程
- 编写代码 → 遵循 code-style.md 的 Java/Drools 规范
- 设计 API → 遵循 api-design.md 的 REST 规范

### 3. 关键阻塞节点
- ★ Step 2: 规划阶段 - 必须确认方案可行性
- ★ Step 4: 实现阶段 - 必须检查 BigDecimal 使用、异常处理
- ★ Step 5: 验证阶段 - 必须验证公式语法、金额精度

### 4. 知识回写
任务完成后更新 domain/ 相关文件

---

## 常用命令

| 命令 | 说明 |
|------|------|
| `((command:health))` | 健康检查（环境/依赖/配置） |
| `((command:context))` | 显示当前任务上下文 |
| `((command:status))` | 项目状态总览 |
| `((command:analyze-task))` | 分析任务复杂度 |
| `((command:check-memory))` | 检查记忆状态 |

---

## 技术栈速查

| 层次 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.5.x |
| 规则引擎 | Drools | 8.44.0.Final |
| 表达式引擎 | Aviator | 5.2.6 |
| 配置中心 | Nacos | 2.5.0 / 3.2.0 |
| 本地缓存 | Caffeine | 3.2.3 |
| 数据库 | MySQL | 8.0+ |
| JDK | Java | 17+ |

---

## 核心原则

1. **确定性 > 建议**: 所有规范必须遵守，非可选建议
2. **BigDecimal 强制**: 金额计算禁止使用 double/float
3. **公式安全**: Aviator 表达式必须经过语法校验后才能执行
4. **规则隔离**: 不同业务域的 Drools 规则分组到不同 KieBase
5. **缓存优先**: 高频表达式编译结果必须缓存（Caffeine）

---

最后更新: 2026-04-26 | 适配 HIS Drools+Aviator 规则引擎项目

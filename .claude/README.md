# Harness 工程 - HIS Drools+Aviator 规则引擎

> AI Agent 的工作框架，规范化 HIS 规则引擎开发流程

---

## 概述

Harness 是一个 AI Agent 工程框架，通过结构化的规范、命令、钩子和记忆系统，确保 AI 输出的一致性和质量。本配置已针对 **HIS 医疗业务规则剥离引擎** 进行定制。

**核心架构**: Drools (规则匹配) + Aviator (表达式计算) + Nacos (配置中心)

---

## 目录结构

```
.claude/
├── agent.md               # Agent 项目说明书 (核心)
├── MEMORY.md              # 长期记忆
│
├── commands/              # 自定义快捷命令
│   ├── README.md
│   ├── health-check.md    # ((command:health))
│   ├── show-context.md    # ((command:context))
│   ├── project-status.md  # ((command:status))
│   ├── analyze-task.md    # ((command:analyze-task))
│   ├── check-memory.md    # ((command:check-memory))
│   └── summarize.md       # ((command:summarize))
│
├── rules/                 # 模块化规范文件
│   ├── README.md          # 规则说明
│   ├── code-style.md      # Java/Spring/Drools 编码风格
│   ├── testing.md         # 测试规范（JUnit5/MockMvc/DRL测试）
│   ├── security.md        # 安全检查（SQL注入/表达式注入）
│   ├── api-design.md      # REST API 设计
│   ├── database.md        # 数据库设计（MySQL表结构）
│   ├── error-handling.md  # 错误处理（ErrorCode体系）
│   ├── workflow.md        # 工作流程（7步+3阻塞节点）
│   ├── docker-deploy.md   # Docker 部署
│   ├── documentation.md   # 文档规范（DRL/Aviator注释）
│   └── performance.md     # 性能优化（缓存/并发）
│
├── hooks/                 # 事件驱动拦截
│   ├── README.md          # Hook 配置说明
│   ├── pre-execute-shell.sh
│   ├── post-execute-shell.sh
│   ├── pre-write-file.sh
│   └── ...
│
├── domain/                # 业务知识库
│   ├── README.md
│   ├── glossary.md        # HIS/规则引擎术语表
│   ├── rules.md           # 业务规则（医保结算等）
│   ├── state-machines.md  # 状态机（规则生命周期/结算流程）
│   ├── edge-cases.md      # 边界情况（BigDecimal精度等）
│   └── decisions.md       # 决策记录（20条）
│
└── memory/                # 每日笔记
    └── TEMPLATE.md
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

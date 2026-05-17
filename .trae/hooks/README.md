---
alwaysApply: true
---
# Hooks 配置说明 - HIS Drools+Aviator 规则引擎

## 概述

Hooks 是事件驱动的拦截脚本，在特定操作前后自动执行。所有脚本均已适配 **HIS 规则引擎** 项目，提供生产级安全检查。

### ⚠️ 重要说明

**Trae IDE 目前不支持自动触发 `.trae/hooks/` 目录下的脚本**。

因此，hooks 的核心逻辑已集成到 `agent.md` 的行为规范中：

| Hook 脚本 | agent.md 对应章节 | 触发方式 |
|-----------|------------------|---------|
| `pre-task-start.sh` | §3.2 任务开始前的必做动作 | AI 每次任务开始时自动执行 |
| `post-task-complete.sh` | §3.3 任务完成后的必做动作 | AI 每次任务完成时自动执行 |
| `post-plan-update.sh` | 通过 `((command:plan))` 调用 | 用户或 AI 手动触发 |

**hooks/ 目录保留原因**:
1. 作为参考文档，记录完整的事件拦截逻辑
2. 如未来 Trae 支持 hooks 自动触发，可直接启用
3. 可在本地终端手动运行测试

## 目录结构

```
.trae/hooks/
├── README.md                    # 本文档
├── pre-execute-shell.sh         # Shell 命令执行前 — 危险命令拦截
├── post-execute-shell.sh        # Shell 命令执行后 — 日志记录
├── pre-write-file.sh            # 文件写入前 — 受保护文件强制确认
├── post-read-file.sh            # 文件读取后 — 敏感信息提醒
├── pre-browser.sh               # 浏览器启动前 — CDP/无头模式提示
├── pre-search.sh                # 搜索前 — 敏感词过滤
├── pre-task-start.sh            # 任务开始前 — 计划检查/创建提示
├── post-task-complete.sh        # 任务完成后 — 知识回写 + 计划状态检查
└── post-plan-update.sh          # 计划状态变更后 — 索引更新 + 变更日志
```

---

## 钩子详解

### pre-execute-shell.sh ⚡ 强化版

**触发时机**: 执行 shell 命令前

**用途**: **危险命令直接拦截 (exit 1)**

**检查项（三级防护）**:

| 级别 | 行为 | 检测内容 |
|------|------|---------|
| 🔴 **拦截** | exit 1, 禁止执行 | `rm -rf /`, `DROP DATABASE`, `TRUNCATE`, `docker system prune -a`, `mkfs`, 规则库删除等 |
| 🟡 **警告** | 允许 + 提示 | `rm -rf`（非根路径）, `mvn clean`, 数据库备份覆盖等 |
| 🟢 **提醒** | 允许 + 轻量提示 | 操作涉及 `.env*`, `application-*.yml`, Nacos 配置等 |

**HIS 特定危险命令**:
- `DROP TABLE his_*` — 删除规则引擎核心表
- `DELETE FROM rule_definition` — 清空规则定义
- `DELETE FROM aviator_formula` — 清空公式库
- `rm -rf src/main/resources/rules/` — 删除 DRL 规则文件

---

### pre-write-file.sh ⚡ 强化版

**触发时机**: 写入文件前

**用途**: **受保护文件写入必须获得显式授权**

**受保护文件列表**（HIS 规则引擎 4 类）:

| 类别 | 示例 |
|------|------|
| Harness 核心 | `agent.md`, `MEMORY.md`, `settings.local.json`, `workflow.md` |
| 环境与密钥 | `.env*`, `application-*.yml`, `bootstrap.yml` |
| 构建配置 | `pom.xml`, `.m2/settings.xml` |
| 规则与公式 | `src/main/resources/rules/*.drl`, Nacos 配置中的 formulas |

---

### post-task-complete.sh ✨ 强化版

**触发时机**: TaskStop 事件（任务完成时）

**用途**: 1. 自动触发 Step 7 知识回写流程
       2. 自动检查并提示更新关联计划状态

**行为**:
```
任务完成 → 知识回写三问 → 检查活跃计划 → 提示更新计划状态
```

**HIS 特定知识回写方向**:
1. 新术语？→ `domain/glossary.md`（HIS/Drools/Aviator 术语）
2. 新规则？→ `domain/rules.md`（医保结算/费用校验规则）
3. 新边界情况？→ `domain/edge-cases.md`（BigDecimal精度/规则冲突）
4. 重要决策？→ `domain/decisions.md`（架构/数据/集成决策）

---

### pre-task-start.sh ✨ 新增

**触发时机**: 新任务开始时

**用途**: 检查当前活跃计划，提示是否需要创建新计划

**行为**:
```
任务开始 → 检查活跃计划 → 显示列表或提示创建
```

**输出示例**:
```
当前活跃计划数: 2

活跃计划列表：
  📄 2026-04-26-feature-reimburse-rule-engine.md
     标题: 实现医保报销规则引擎
     状态: pending | 类型: feature
```

---

### post-plan-update.sh ✨ 新增

**触发时机**: 计划状态变更时（手动或自动）

**用途**: 1. 自动更新 INDEX.md 索引
       2. 记录变更到 CHANGELOG.md
       3. completed 状态时触发知识回写提示

**用法**:
```bash
bash .trae/hooks/post-plan-update.sh <plan_file> <old_status> <new_status>
```

---

## 计划管理集成

### plan.sh 工具位置

`plan.sh` 核心工具保留在 `.trae/workflow-plans/plan.sh`，hooks 通过调用它实现自动化。

### 自动触发流程

```
[任务开始]
    │
    ▼
pre-task-start.sh ──→ 检查活跃计划
    │
    ▼
[执行任务]
    │
    ▼
[任务完成]
    │
    ▼
post-task-complete.sh ──→ 知识回写 + 计划状态检查
    │
    ▼
[用户更新计划状态]
    │
    ▼
post-plan-update.sh ──→ 索引更新 + 变更日志 + 知识回写
```

### 手动调用 plan.sh

```bash
# 创建计划
bash .trae/workflow-plans/plan.sh create "任务标题" feature "Phase 1" "负责人" P0

# 更新状态
bash .trae/workflow-plans/plan.sh update 文件名.md in_progress

# 查看列表
bash .trae/workflow-plans/plan.sh list

# 更新索引
bash .trae/workflow-plans/plan.sh reindex
```

---

## 使用说明

所有钩子脚本均为通用安全检查，已针对 HIS 规则引擎项目进行适配：
- 危险命令黑名单增加了数据库表删除和规则库清空的检测
- 受保护文件列表增加了 DRL 规则文件和 Nacos 配置的保护
- 知识回写引导更新为 HIS 领域知识库结构
- **新增计划管理集成**: 任务生命周期与计划状态自动关联

---

最后更新: 2026-04-26 | HIS Drools+Aviator 规则引擎版

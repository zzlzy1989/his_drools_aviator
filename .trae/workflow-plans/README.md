# Workflow Plans 使用指南

> HIS Drools+Aviator 规则引擎 - 计划生成与跟踪系统  
> 版本: v1.0 | 最后更新: 2026-04-26

---

## 1. 概述

本系统借鉴 Claude 的 `settings.local.json` 配置结构，在 `.trae` 目录下实现了完整的计划生成与跟踪功能。支持计划的创建、状态更新、完成归档和自动索引。

### 1.1 核心特性

| 特性 | 说明 |
|------|------|
| **自动命名** | 按 `{date}-{type}-{slug}.md` 格式自动生成文件名 |
| **元数据跟踪** | YAML frontmatter 包含 title/type/status/phase/owner 等必填字段 |
| **状态流转** | pending → in_progress → completed/cancelled |
| **自动归档** | 完成 7 天后自动移动到 archived/ 目录 |
| **索引同步** | INDEX.md 自动汇总所有计划状态 |

---

## 2. 目录结构

```
.trae/workflow-plans/
├── TEMPLATE.md          # 计划模板
├── INDEX.md             # 计划跟踪索引（自动生成）
├── plan.sh              # 计划管理脚本
├── README.md            # 本文档
│
├── active/              # 活跃计划（pending/in_progress）
│   └── 2026-04-26-feature-reimburse-rule-engine.md
│
├── completed/           # 已完成计划（completed）
│
└── archived/            # 已归档计划（7天后自动归档）
```

---

## 3. 配置文件

### 3.1 settings.local.json

位置: `.trae/settings.local.json`

```json
{
    "plansDirectory": ".trae/workflow-plans",
    "workflow": {
        "plansDirectory": ".trae/workflow-plans",
        "planSettings": {
            "autoGenerate": true,
            "namingPattern": "{date}-{task-type}-{slug}.md",
            "requiredMetadata": [
                "title", "type", "status", "created_at",
                "updated_at", "phase", "owner"
            ],
            "statusValues": ["pending", "in_progress", "completed", "cancelled"],
            "taskTypes": [
                "feature", "bugfix", "refactor", "config",
                "docs", "test", "deploy", "research"
            ],
            "autoArchive": {
                "enabled": true,
                "daysAfterCompletion": 7
            }
        }
    }
}
```

### 3.2 配置参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `plansDirectory` | string | `.trae/workflow-plans` | 计划文件存储目录 |
| `autoGenerate` | boolean | `true` | 是否自动生成计划 |
| `namingPattern` | string | `{date}-{type}-{slug}.md` | 文件命名模式 |
| `requiredMetadata` | array | 见上表 | 必填元数据字段 |
| `statusValues` | array | 见上表 | 允许的状态值 |
| `taskTypes` | array | 见上表 | 允许的任务类型 |
| `autoArchive.enabled` | boolean | `true` | 是否启用自动归档 |
| `autoArchive.daysAfterCompletion` | number | `7` | 完成后多少天归档 |

---

## 4. 使用方法

### 4.1 通过脚本操作（推荐）

```bash
# 创建新计划
bash .trae/workflow-plans/plan.sh create "任务标题" feature "Phase 1" "负责人" P0

# 更新计划状态
bash .trae/workflow-plans/plan.sh update 文件名.md in_progress "开始开发"

# 查看活跃计划
bash .trae/workflow-plans/plan.sh list

# 查看计划详情
bash .trae/workflow-plans/plan.sh show 文件名.md

# 更新索引
bash .trae/workflow-plans/plan.sh reindex
```

### 4.2 通过 Trae AI 自动创建

当触发以下任务类型时，AI 会自动创建计划：

| 触发条件 | 计划类型 | 示例 |
|---------|---------|------|
| 新功能开发 | feature | 实现医保报销规则引擎 |
| Bug 修复 | bugfix | 修复报销金额计算精度问题 |
| 代码重构 | refactor | 重构 Skill Pipeline 执行器 |
| 配置修改 | config | 更新 Nacos 配置中心地址 |
| 文档编写 | docs | 编写 DRL 规则文档 |
| 测试编写 | test | 补充结算服务单元测试 |
| 部署相关 | deploy | 配置 Docker Compose 编排 |
| 技术调研 | research | 调研 Drools 7.x 升级到 8.x |

### 4.3 手动创建

1. 复制 `TEMPLATE.md` 到 `active/` 目录
2. 按命名规范重命名: `YYYY-MM-DD-{type}-{slug}.md`
3. 填写 YAML frontmatter 元数据
4. 填写计划正文内容

---

## 5. 计划文件结构

### 5.1 YAML Frontmatter（必填）

```yaml
---
title: "任务标题"              # 必填
type: "feature"               # 必填: feature|bugfix|refactor|...
status: "pending"             # 必填: pending|in_progress|completed|cancelled
created_at: "2026-04-26 15:30:00"  # 必填
updated_at: "2026-04-26 15:30:00"  # 必填
completed_at: null            # 完成时填写
phase: "Phase 2"              # 必填: Phase 1~5
owner: "developer"            # 必填
reviewer: ""                  # 可选
priority: "P0"                # 必填: P0|P1|P2|P3
tags: []                      # 可选标签
related_files: []             # 相关文件
dependencies: []              # 依赖的其他计划
---
```

### 5.2 正文结构

| 章节 | 说明 |
|------|------|
| 1. 任务概述 | 背景、目标、范围 |
| 2. 技术方案 | 涉及模块、文件清单、技术要点 |
| 3. 执行步骤 | 分步骤描述，含目标和验收标准 |
| 4. 测试计划 | 单元测试和集成测试用例 |
| 5. 风险评估 | 风险/影响/概率/应对措施 |
| 6. 进度跟踪 | 时间线记录 |
| 7. 完成检查清单 | 完成前的检查项 |

---

## 6. 状态流转

```
[创建] → pending ──→ in_progress ──→ completed ──→ [7天后归档]
                         │
                         └──→ cancelled ──→ [立即归档]
```

| 状态 | 目录 | 说明 |
|------|------|------|
| `pending` | active/ | 待开始 |
| `in_progress` | active/ | 进行中 |
| `completed` | completed/ | 已完成（自动移动） |
| `cancelled` | archived/ | 已取消（自动移动） |

---

## 7. 命名规范

### 7.1 文件命名

格式: `YYYY-MM-DD-{type}-{slug}.md`

| 部分 | 说明 | 示例 |
|------|------|------|
| `YYYY-MM-DD` | 创建日期 | 2026-04-26 |
| `{type}` | 任务类型 | feature/bugfix/refactor |
| `{slug}` | 标题转小写+连字符 | reimburse-rule-engine |

示例: `2026-04-26-feature-reimburse-rule-engine.md`

### 7.2 计划 ID

格式: `PLAN-YYYYMMDD-XXX`

- `YYYYMMDD`: 创建日期
- `XXX`: 当日序号（001, 002, ...）

示例: `PLAN-20260426-001`

---

## 8. 验证功能

### 8.1 验证目录结构

```bash
ls -la .trae/workflow-plans/
ls -la .trae/workflow-plans/active/
```

### 8.2 验证配置文件

```bash
cat .trae/settings.local.json
# 确认 plansDirectory 和 planSettings 配置正确
```

### 8.3 验证计划文件

```bash
# 检查 YAML frontmatter
head -20 .trae/workflow-plans/active/*.md

# 检查索引
cat .trae/workflow-plans/INDEX.md
```

### 8.4 验证脚本功能

```bash
# 显示帮助
bash .trae/workflow-plans/plan.sh help

# 创建测试计划
bash .trae/workflow-plans/plan.sh create "测试计划" feature "Phase 1" "测试" P1

# 列出活跃计划
bash .trae/workflow-plans/plan.sh list

# 查看计划详情
bash .trae/workflow-plans/plan.sh show 2026-04-26-feature-*.md
```

---

## 9. 与 Claude 配置对比

| 特性 | Claude (.claude) | Trae (.trae) | 说明 |
|------|------------------|--------------|------|
| 配置位置 | `.claude/settings.local.json` | `.trae/settings.local.json` | 对应 |
| 计划目录 | `.claude/workflow-plans` | `.trae/workflow-plans` | 对应 |
| 权限控制 | `permissions.allow` | `permissions.allow` | 对应 |
| 工作流 | `workflow.allow` | `workflow.allow` | 对应 |
| 计划设置 | — | `workflow.planSettings` | Trae 扩展 |
| 自动命名 | — | `namingPattern` | Trae 扩展 |
| 元数据校验 | — | `requiredMetadata` | Trae 扩展 |
| 自动归档 | — | `autoArchive` | Trae 扩展 |

---

## 10. 常见问题

### Q: 为什么不能通过 Bash 操作 .trae 目录？

Trae 系统出于安全考虑，禁止通过 Bash 操作 `.trae`、`.git`、`.vscode` 等目录。请使用 Write 工具直接创建/修改文件。

### Q: 如何批量更新索引？

运行 `bash .trae/workflow-plans/plan.sh reindex` 或手动编辑 INDEX.md。

### Q: 计划文件会被 Git 跟踪吗？

会。`.trae/workflow-plans/` 目录不在 `.gitignore` 中，所有计划文件都会被 Git 跟踪。

### Q: 如何删除已完成的计划？

不建议删除。已完成计划应保留在 `completed/` 目录，7 天后自动归档到 `archived/`。

---

*文档结束*

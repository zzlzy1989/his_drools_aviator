# Hooks 配置说明 - HIS Drools+Aviator 规则引擎

## 概述

Hooks 是事件驱动的拦截脚本，在特定操作前后自动执行。所有脚本均已适配 **HIS 规则引擎** 项目，提供生产级安全检查。

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
└── post-task-complete.sh        # 任务完成后 — 知识回写触发
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

### post-task-complete.sh ✨

**触发时机**: TaskStop 事件（任务完成时）

**用途**: 自动触发 Step 7 知识回写流程

**行为**:
```
任务完成 → 显示三问自省提示 → 引导更新 domain/ 知识库
```

**HIS 特定知识回写方向**:
1. 新术语？→ `domain/glossary.md`（HIS/Drools/Aviator 术语）
2. 新规则？→ `domain/rules.md`（医保结算/费用校验规则）
3. 新边界情况？→ `domain/edge-cases.md`（BigDecimal精度/规则冲突）
4. 重要决策？→ `domain/decisions.md`（架构/数据/集成决策）

---

## 使用说明

所有钩子脚本均为通用安全检查，已针对 HIS 规则引擎项目进行适配：
- 危险命令黑名单增加了数据库表删除和规则库清空的检测
- 受保护文件列表增加了 DRL 规则文件和 Nacos 配置的保护
- 知识回写引导更新为 HIS 领域知识库结构

---

最后更新: 2026-04-26 | HIS Drools+Aviator 规则引擎版

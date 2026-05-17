---
alwaysApply: true
---
# Harness 工程跨项目迁移指南

> 将 HIS Drools+Aviator 规则引擎 的 Harness 工程体系复用到任何 AI 辅助开发项目

**适用场景**: 任何使用 Claude Code / Trae / Cursor 等 AI 编码助手的项目
**核心理念**: 从 Vibe Coding（经验驱动）到 Harness 工程（系统约束）的通用方法论

---

## 一、快速了解：什么是 Harness 工程？

### 1.1 核心问题

AI 编码助手（如 Claude Code, Trae, Cursor 等）默认行为：
- 每次会话从零开始，不记得上次学到的业务知识
- 编码风格不一致，容易遗漏边界情况
- 可能跳过关键步骤（如安全检查、测试）
- 无法积累项目经验，重复犯错

### 1.2 Harness 解决方案(包含 Trae 配置)

通过在 `.trae/` 目录下建立 **六层结构化体系**，让 AI 助手：

```
┌──────────────────────────────────────────────────┐
│                  .trae/ 目录                     │
│                                                   │
│  agent.md              → AI 是谁？该怎么做？       │
│  MEMORY.md             → 项目是什么？（静态事实）    │
│  settings.local.json   → 权限配置 + 计划管理设置    │
│                                                   │
│  rules/*.md            → 编码规范是什么？（按领域）  │
│  workflow.md           → 工作流程是什么？（7步强制） │
│  commands/*.md         → 有哪些快捷命令？           │
│  hooks/*.sh            → 哪些操作必须拦截？         │
│  domain/*              → 业务知识库（持续积累）      │
│  workflow-plans/*      → 计划生成与跟踪系统         │
│  memory/*              → 日记系统（每日笔记）        │
└──────────────────────────────────────────────────┘
```

### 1.3 效果对比

| 维度 | 无 Harness (Vibe Coding) | 有 Harness |
|------|--------------------------|------------|
| 编码一致性 | 随机波动 | 规则驱动，稳定一致 |
| 安全保障 | 依赖 AI "自觉" | hook 硬性拦截 |
| 知识积累 | 每次从零开始 | 跨会话持久化 |
| 质量审查 | 可跳过 | 阻塞节点强制执行 |
| 新人上手 | 问老员工 | 读 domain/ 即可 |

---

## 二、迁移方法：三步法

### 方法 A: 快速迁移（推荐新项目）

适用于：**全新项目**或**现有项目首次引入 Harness**

```bash
# 1. 复制骨架目录到目标项目
cp -r source_project/.trae target_project/.trae

# 2. 删除 HIS Drools+Aviator 规则引擎 特有内容（见第三节"需替换文件清单"）
# 3. 填写项目特定内容（见第四节"自定义清单"）
```

### 方法 B: 渐进式迁移

适用于：**已有大量代码的成熟项目**，不想一次性改动太多

```bash
# Phase 1 (Day 1): 只复制核心骨架
#   - agent.md + MEMORY.md + workflow.md + hooks/
#   - 先跑起来，体验流程约束

# Phase 2 (Day 2-3): 补充规则层
#   - 复制 rules/ 并修改为你的技术栈规范
#   - 先做 code-style.md + security.md（最高频）

# Phase 3 (Day 3-5): 构建知识库
#   - 从 domain/glossary.md 开始
#   - 随着日常开发逐步填充其他文件
```

### 方法 C: 最小可行版本（MVP）

适用于：**想先试试效果**的项目

只需 4 个文件即可启动：

```
.trae/
├── agent.md              # 从模板修改（~100行）
├── MEMORY.md             # 从模板修改（~50行）
├── rules/
│   └── workflow.md       # 直接复用（通用）
└── hooks/
    └── pre-execute-shell.sh  # 直接复用（通用）
```

---

## 三、文件分类：哪些可以直接复用？

### ✅ 可直接复用的文件（通用逻辑，无需修改）

这些文件包含的是 **方法论和通用模式**，与具体项目无关：

| 文件 | 说明 | 修改需求 |
|------|------|---------|
| `rules/workflow.md` | 7 步工作流程 | ⚠️ 仅微调任务分类 |
| `hooks/pre-execute-shell.sh` | 危险命令拦截 | ❌ 不需要改 |
| `hooks/pre-write-file.sh` | 受保护文件确认 | ⚠️ 仅改保护列表 |
| `commands/review.md` | 实现审查命令 | ❌ 不需要改 |
| `commands/knowledge-writeback.md` | 知识回写命令 | ❌ 不需要改 |
| `memory/TEMPLATE.md` | 日记模板 | ❌ 不需要改 |
| `memory/README.md` | 日记系统说明 | ❌ 不需要改 |
| `domain/state-machines.md` | 状态机编写规范 | ⚠️ 替换示例 |
| `domain/edge-cases.md` | 边界情况清单 | ⚠️ 替换示例 |
| `domain/rules.md` | 业务规则格式 | ⚠️ 替换示例 |
| `domain/decisions.md` | 决策记录格式 | ⚠️ 替换示例 |
| `workflow-plans/TEMPLATE.md` | 计划模板 | ❌ 不需要改 |
| `workflow-plans/plan.sh` | 计划管理 CLI 工具 | ❌ 不需要改 |

> **可直接复用**: ~13 个文件（约 40%），节省大量时间

### ⚠️ 需要修改后复用的文件（结构保留，内容替换）

这些文件的 **框架和格式是通用的**，但内容必须替换为你的项目信息：

| 文件 | 需要修改的内容 | 参考章节 |
|------|--------------|---------|
| `agent.md` | 项目名称/技术栈/硬性禁止项/组件规范 + 计划检查逻辑 | §4.1 |
| `MEMORY.md` | 项目元信息/技术栈版本/目录结构/API/环境配置 | §4.2 |
| `settings.local.json` | 权限路径、受保护文件列表、计划管理设置 | §4.3 |
| `rules/code-style.md` | 编码语言/框架/命名规范/组件约定 | §4.4 |
| `rules/testing.md` | 测试框架/测试目录/覆盖率要求 | §4.5 |
| `rules/security.md` | 特定安全规则/敏感信息模式 | §4.6 |
| `rules/database.md` | ORM/数据库类型/命名约定/索引策略 | §4.7 |
| `rules/error-handling.md` | 错误码前缀/异常基类/日志配置 | §4.8 |
| `rules/docker-deploy.md` | 端口/镜像名/服务编排 | §4.9 |
| `rules/performance.md` | 缓存方案/异步任务框架/性能指标 | §4.10 |
| `domain/glossary.md` | 全部术语替换为你的领域术语 | §4.11 |
| `domain/modules/*/overview.md` | 替换为你的模块 | §4.12 |
| `workflow-plans/INDEX.md` | 初始化为空索引 | §4.13 |

### ❌ 不需要复用的文件（HIS Drools+Aviator 规则引擎 专属）

| 文件 | 原因 | 替代方案 |
|------|------|---------|
| `HARNESS_IMPROVEMENT_PLAN.md` | HIS Drools+Aviator 规则引擎 改进方案的详细记录 | 用你自己的规划文档替代 |
| `CHANGELOG.md` | HIS Drools+Aviator 规则引擎 的变更历史 | 创建新的 CHANGELOG |
| `SUMMARY.md` | HIS Drools+Aviator 规则引擎 的实施总结 | 本文档就是通用版总结 |

### ⚠️ 重要说明：Hooks 与 agent.md 的关系

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

---

## 四、自定义清单（逐文件修改指南）

### 4.1 agent.md — AI 身份与行为规范

```markdown
## 必须修改的部分

### 1. 身份定位
你是 **[你的项目名称]** 的 AI 开发助手，一个 [一句话描述项目]。

### 2. 技术栈约束
- **后端**: [你的后端框架] + [版本]
- **前端**: [你的前端框架] + [版本]
- **数据库**: [数据库类型]
- **缓存**: [缓存方案]

### 2.2 硬性禁止事项
| # | 禁止项 | 原因 |
|---|--------|------|
| 1 | [你的禁止项1] | [原因] |
| 2 | [你的禁止项2] | [原因] |
| ... | ... | ... |

### 2.3 组件使用强制规则
[如果有封装组件，列出强制使用规则]

### 2.4 API/文件命名规则
[你的命名约定]
```

**提示**: 这是 AI 每次会话首先读取的文件，务必准确反映项目现状。

---

### 4.2 MEMORY.md — 项目事实知识

```markdown
## 必须修改的部分

### 1. 项目元信息
| 属性 | 值 |
|------|-----|
| 名称 | [你的项目名] |
| 定位 | [一句话描述] |
| 许可 | [开源协议/内部项目] |

### 2. 技术栈详情
[完整列出所有技术组件及版本号]

### 3. 目录结构
[画出项目的实际目录树]

### 4. API 端点全集
[列出所有 API 路径和前缀]

### 5. 数据库模型概要
[核心表关系图]

### 6. 环境与部署
[端口/服务/常用命令]

### 7. 配置管理要点
[配置文件位置和环境变量]

### 8. 历史经验与常见问题
[随着项目推进逐步积累]
```

**原则**: MEMORY.md 只放 **事实性知识**（是什么），不放 **行为规范**（怎么做）。后者归 agent.md。

---

### 4.3 settings.local.json — 权限配置

```json
{
  "permissions": {
    "allow": [
      // 全局允许的操作（保持通用）
      "readFile",
      "writeFile",
      "listDirectory",
      "searchFiles"
    ],
    "deny": [
      // 受保护文件列表 — 改为你的关键文件
      {
        "path": "[你的关键配置文件]",
        "description": "[说明为什么保护]"
      },
      {
        "path": "[你的数据库配置]",
        "description": "含敏感连接信息"
      }
    ]
  }
}
```

---

### 4.4 rules/code-style.md — 编码规范

**修改要点**:

| 章节 | HIS Drools+Aviator 规则引擎 内容 | 你的替换 |
|------|-------------|---------|
| Java 基础规范 | Java 17+ / BigDecimal 强制 | [你的后端框架规范] |
| Spring Boot 规范 | Spring Boot 3.x / @RequiredArgsConstructor | [你的框架规范] |
| Drools DRL 规范 | Drools 8.x / rule 命名规范 | [你的规则引擎规范] 或删除 |
| Aviator 公式规范 | Aviator 5.x / BigDecimal 类型安全 | [你的表达式引擎规范] 或删除 |

**建议**: 如果你不使用 Drools/Aviator，可以删除相关章节，保留 Java/Spring 部分。

---

### 4.5 rules/testing.md — 测试规范

**修改要点**:

| 章节 | HIS Drools+Aviator 规则引擎 内容 | 你的替换 |
|------|-------------|---------|
| 前端测试 | Vitest | [你的前端测试框架] |
| 后端测试 | Django APITestCase | [你的后端测试框架] 或删除 |
| API 测试矩阵 | 10 种场景 | 保留（通用） |
| UI 自动化 | Selenium/Playwright/browser-use | [你的 UI 测试工具] 或删除 |

**可保留**: API 测试矩阵（10 场景覆盖）是通用的，建议保留。

---

### 4.6 rules/security.md — 安全规范

**大部分通用，小量调整**:

- **SQL 注入 / XSS / CSRF 章节**: 通用，保留
- **Shell 危险命令黑名单**: 通用，保留
- **依赖安全扫描**: 改为你的包管理器（pip/npm/go mod/maven）
- **Docker 安全加固**: 通用，保留
- **HIS Drools+Aviator 规则引擎 特定安全规则**: 替换为你的项目特有规则

---

### 4.7 rules/database.md — 数据库规范

**完全替换为你的数据库相关规范**:

```markdown
## 你的数据库规范

### 模型命名
- 表名: [你的约定]
- 字段名: [你的约定]
- 外键: [你的约定]

### 迁移管理
- [你的 ORM 迁移工具和流程]

### 查询优化
- [你的查询优化策略]

### 索引规范
- [必须索引的场景]
```

如果你不用关系型数据库（如用 MongoDB/Firebase），此文件可大幅简化或替换。

---

### 4.8~4.10 其他规则文件

| 文件 | 核心修改点 |
|------|-----------|
| **error-handling.md** | 错误码前缀（如 `TH-` → `YOUR-`）、异常基类名、日志框架 |
| **docker-deploy.md** | 端口号、服务名、镜像名、健康检查端点 |
| **performance.md** | 缓存方案（Redis/Memcached/本地）、异步任务框架、性能阈值 |

---

### 4.11 domain/glossary.md — 术语表（最重要！）

这是 **最能体现项目特色** 的文件。必须全部重写：

```markdown
# 术语表 - [你的项目名]

## Layer 1: 技术术语（从 HIS Drools+Aviator 规则引擎 复制并增删）
[保留通用技术术语，删掉你不需要的]

## Layer 2: 平台领域术语（完全重写）
| 术语 | 英文 | 说明 |
|------|------|------|
| [你的核心实体1] | [英文] | [说明] |
| [你的核心实体2] | [英文] | [说明] |
| ... | ... | ... |

## Layer 3: 业务领域术语（完全重写）
[你的行业/领域专用术语]

## 缩写速查表
[你的项目缩写]
```

**目标数量**: ≥80 条（HIS Drools+Aviator 规则引擎 达到了 103 条）

---

### 4.12 domain/modules/ — 模块概述

根据你的项目模块创建对应目录和 overview.md：

```bash
# 示例：电商项目
mkdir -p domain/modules/{users,products,orders,payments,inventory,shipping,analytics}

# 每个 overview.md 包含：
# - 模块职责（1句话）
# - 核心模型/数据结构
# - API 端点列表
# - 关键业务规则引用
# - 关联模块
# - 前端页面位置
```

---

### 4.13 workflow-plans/ — 计划生成与跟踪系统

借鉴 Claude 的 `settings.local.json` 配置结构，实现完整的计划管理：

```bash
# 创建目录结构
mkdir -p workflow-plans/{active,completed,archived}

# 复制模板文件
cp TEMPLATE.md workflow-plans/
cp plan.sh workflow-plans/
```

**核心文件说明**:

| 文件 | 说明 |
|------|------|
| `TEMPLATE.md` | 计划模板（YAML frontmatter + 正文结构） |
| `INDEX.md` | 计划跟踪索引（自动更新，汇总所有计划状态） |
| `plan.sh` | 计划管理 CLI 工具（create/update/list/show/reindex） |
| `README.md` | 使用指南 |
| `active/` | 活跃计划（pending/in_progress） |
| `completed/` | 已完成计划 |
| `archived/` | 已归档计划（完成 7 天后自动归档） |

**计划命名规范**: `YYYY-MM-DD-{type}-{slug}.md`

**计划 ID 格式**: `PLAN-YYYYMMDD-XXX`

**状态流转**: `pending → in_progress → completed → [7天后归档]`

**配置示例** (`settings.local.json`):

```json
{
    "workflow": {
        "plansDirectory": ".trae/workflow-plans",
        "planSettings": {
            "autoGenerate": true,
            "namingPattern": "{date}-{task-type}-{slug}.md",
            "requiredMetadata": ["title", "type", "status", "created_at", "updated_at", "phase", "owner"],
            "statusValues": ["pending", "in_progress", "completed", "cancelled"],
            "taskTypes": ["feature", "bugfix", "refactor", "config", "docs", "test", "deploy", "research"],
            "autoArchive": { "enabled": true, "daysAfterCompletion": 7 }
        }
    }
}
```

---

## 五、迁移检查清单

### Phase 1: 骨架搭建（预计 30 分钟）

- [ ] 在项目根目录创建 `.trae/` 目录
- [ ] 复制 `agent.md` 模板并填写项目信息
- [ ] 复制 `MEMORY.md` 模板并填写项目事实
- [ ] 复制 `workflow.md`（直接可用）
- [ ] 复制 `hooks/pre-execute-shell.sh`（直接可用）
- [ ] 复制 `commands/review.md` 和 `knowledge-writeback.md`
- [ ] 创建 `memory/TEMPLATE.md` 和 `memory/README.md`
- [ ] 更新 `settings.local.json` 权限配置
- [ ] **验证**: 启动 AI 会话，确认能读取到 agent.md

### Phase 2: 规则定制（预计 1-2 小时）

- [ ] 复制 `rules/code-style.md` 并适配你的技术栈
- [ ] 复制 `rules/security.md` 并调整安全规则
- [ ] 复制 `rules/testing.md` 并适配测试框架
- [ ] 按需复制 `rules/database.md` / `error-handling.md` / `docker-deploy.md` / `performance.md`
- [ ] 更新 `rules/README.md` 索引
- [ ] **验证**: 让 AI 写一个小功能，观察是否遵循规范

### Phase 3: 知识库构建（持续进行，建议 1 周内初版）

- [ ] 创建 `domain/glossary.md`（目标 ≥50 条起步）
- [ ] 创建 `domain/rules.md`（目标 ≥5 条核心业务规则）
- [ ] 创建 `domain/state-machines.md`（≥2 个核心状态机）
- [ ] 创建 `domain/edge-cases.md`（≥5 条已知边界情况）
- [ ] 创建 `domain/decisions.md`（记录已有的重要决策）
- [ ] 创建 `domain/modules/` 目录和各模块概述
- [ ] 创建 `domain/README.md` 索引
- [ ] **验证**: 新成员读 domain/ 后能否理解项目全貌

### Phase 4: 试运行与调优（持续）

- [ ] 完成 3-5 个真实任务，走通 7 步流程
- [ ] 收集 AI 输出中的问题，补充到 rules/ 或 domain/
- [ ] 检查是否有遗漏的危险命令未加入黑名单
- [ ] 评估单文件大小，超过 500 行的考虑拆分
- [ ] 建立定期审计习惯（月度/季度）

---

## 六、常见问题

### Q1: 我的不是 Web 项目，能用吗？

**可以**。Harness 工程的核心是 **方法论**，不限于 Web 开发：
- 移动端项目（iOS/Android/Flutter）：保留代码风格+安全+流程，去掉 Web 特定内容
- 数据科学项目：强化 testing.md（实验可复现性）和 performance.md
- DevOps/基础设施项目：强化 docker-deploy.md 和 security.md
- 库/SDK 项目：强化 api-design.md（公开 API 设计）和 documentation.md

### Q2: 文件太多，AI 上下文装不下怎么办？

**按需加载是设计初衷**。实际上：
- 每次任务只加载 3-5 个相关文件（agent.md + MEMORY.md + 1-2 个 rules + 1-2 个 domain）
- 单次加载约 500-1000 行，远低于上下文窗口限制
- `HARNESS_IMPROVEMENT_PLAN.md`(1034行) 这类历史文档不应被常规加载

### Q3: 团队协作怎么办？

Harness 文件应纳入 **Git 版本控制**：
```bash
# 推荐: 将 .trae/ 加入 Git（排除敏感信息）
echo ".trae/settings.local.json" >> .gitignore  # 含个人权限配置
git add .trae/
git commit -m "docs: 初始化 Harness 工程配置"
```

团队成员共享同一套规范，确保 AI 输出一致性。

### Q4: 与现有 CLAUDE.md / AGENTS.md 冲突吗？

**不冲突，互补关系**：
- `CLAUDE.md`: 项目级 README（面向人类读者 + AI 的简要介绍）
- `.trae/agent.md`: AI 详细行为规范（面向 AI 的深度指令）
- 建议 CLAUDE.md 保持简洁（<100行），详细内容放 `.trae/` 目录

### Q5: 如何判断迁移是否成功？

**成功标志**：
1. AI 主动按 7 步流程工作（而非直接开始编码）
2. 危险命令被自动拦截
3. 任务完成后主动询问是否需要知识回写
4. 新团队成员读 domain/ 后能快速上手
5. 跨会话时 AI 记住了之前学到的业务知识

---

## 七、文件速查卡

打印此页作为迁移时的快速参考：

```
.trae/
├── agent.md               ⭐⭐⭐ 必须自定义 (身份+技术栈+禁令)
├── MEMORY.md              ⭐⭐⭐ 必须自定义 (项目事实)
├── settings.local.json    ⭐⭐   需调整 (权限+保护文件)
│
├── rules/
│   ├── workflow.md        ⭐     微调即可 (通用7步流程)
│   ├── code-style.md      ⭐⭐⭐ 必须自定义 (编码规范)
│   ├── testing.md         ⭐⭐   需调整 (测试框架)
│   ├── security.md        ⭐⭐   需调整 (安全规则)
│   ├── database.md        ⭐⭐⭐ 必须自定义 (或删除)
│   ├── error-handling.md  ⭐⭐   需调整 (错误码前缀)
│   ├── docker-deploy.md   ⭐⭐   需调整 (端口/服务)
│   ├── performance.md     ⭐⭐   需调整 (性能方案)
│   ├── api-design.md      ⭐     可选保留
│   └── documentation.md   ⭐     可选保留
│
├── commands/
│   ├── review.md          ✅     直接复用
│   └── knowledge-writeback.md ✅ 直接复用
│
├── hooks/
│   ├── pre-execute-shell.sh   ✅ 直接复用
│   ├── pre-write-file.sh      ⭐ 需调整(保护列表)
│   └── post-task-complete.sh  ✅ 直接复用
│
├── domain/
│   ├── glossary.md        ⭐⭐⭐ 必须完全重写
│   ├── state-machines.md  ⭐⭐   替换示例
│   ├── rules.md           ⭐⭐   替换示例
│   ├── edge-cases.md      ⭐⭐   替换示例
│   ├── decisions.md       ⭐⭐   替换示例
│   └── modules/           ⭐⭐⭐ 按模块重建
│
└── memory/
    ├── TEMPLATE.md        ✅     直接复用
    └── README.md          ✅     直接复用

图例: ⭐⭐⭐=必须自定义  ⭐⭐=需调整  ⭐=微调  ✅=直接复用
```

---

## 八、最小启动模板

如果不想阅读全文，复制以下最小配置即可开始：

```bash
#!/bin/bash
# 一键初始化 Harness 骨架（在项目根目录执行）

set -e

PROJECT_NAME="${1:-my-project}"
TRAE_DIR=".trae"

echo "🚀 初始化 Harness 工程骨架 for ${PROJECT_NAME}..."

# 1. 创建目录结构
mkdir -p "${TRAE_DIR}"/{rules,commands,hooks,domain/modules,memory}

# 2. 创建 agent.md 最小模板
cat > "${TRAE_DIR}/agent.md" << 'AGENT_EOF'
# AGENTS.md - {PROJECT_NAME}

> AI 行为规范。每次会话首先读取此文件。

## 1. 身份定位
你是 **{PROJECT_NAME}** 的 AI 开发助手。

## 2. 核心约束
- [在此填写技术栈]
- [在此填写禁止事项]

## 3. 工作流程
严格按 rules/workflow.md 执行 7 步流程。

## 4. 快速参考
- 项目事实 → **MEMORY.md**
- 业务知识 → **domain/**
- 编码规范 → **rules/**
AGENT_EOF

# 3. 创建 MEMORY.md 最小模板
cat > "${TRAE_DIR}/MEMORY.md" << 'MEM_EOF'
# MEMORY.md - {PROJECT_NAME}

> 项目事实性知识。

## 1. 项目元信息
| 属性 | 值 |
|------|-----|
| 名称 | {PROJECT_NAME} |

## 2. 技术栈
[在此填写]

## 3. 目录结构
[在此填写]
MEM_EOF

# 4. 创建 memory 系统
cat > "${TRAE_DIR}/memory/TEMPLATE.md" << 'TPL_EOF'
# {date} 日记

## 今日完成
-

## 学到的知识
-

## 遇到的问题
-
TPL_EOF

echo "# memory/\n\n日记系统。每日一个文件。\n\n## 使用方式\n1. 复制 TEMPLATE.md 为 YYYY-MM-DD.md\n2. 记录当日重要发现\n3. 定期整理到 domain/" > "${TRAE_DIR}/memory/README.md"

echo ""
echo "✅ Harness 骨架已创建!"
echo "📝 下一步:"
echo "   1. 编辑 agent.md 填写项目信息"
echo "   2. 编辑 MEMORY.md 填写项目事实"
echo "   3. 复制 rules/workflow.md 和 hooks/ (从参考项目)"
echo "   4. 按 MIGRATION_GUIDE.md 完成剩余配置"
```

保存为 `init-harness.sh` 后运行:

```bash
chmod +x init-harness.sh
./init-harness.sh "你的项目名"
```

---

*最后更新: 2026-04-26 | 基于 HIS Drools+Aviator 规则引擎 Harness 工程实践*
*适用范围: 所有 AI 辅助开发项目（Claude Code / Trae / Cursor / Windsurf 等）*

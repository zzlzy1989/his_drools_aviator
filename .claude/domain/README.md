# Domain 知识库索引 - HIS Drools+Aviator 规则引擎

> 业务领域知识库，按稳定性分层组织

---

## 目录结构

```
.claude/domain/
├── README.md                    # 本文档（索引）
├── glossary.md                  # [重写] 术语表 v1.0 (HIS/规则引擎/Drools/Aviator)
├── rules.md                     # [重写] 业务规则 (医保结算/费用校验/公式计算)
├── state-machines.md            # [重写] 核心状态机 (规则生命周期/结算流程)
├── edge-cases.md                # [重写] 边界情况 (金额精度/规则冲突/并发)
├── decisions.md                 # [重写] 决策记录 (20 条, 架构/数据/集成/业务)
│
└── modules/                     # [重构] 模块知识库
    ├── drools-engine/overview.md      # Drools 规则引擎核心
    ├── aviator-formula/overview.md    # Aviator 表达式引擎
    ├── settlement/overview.md         # 医保结算模块
    ├── skill-agent/overview.md        # Skill/Agent 插件架构
    └── cache-config/overview.md       # 缓存与配置中心
```

---

## 知识分层体系

```
┌─────────────────────────────────────────────┐
│  Layer A: 最稳定（极少变化）                    │
│  ┌─────────────────────────────────────┐   │
│  │ glossary.md (术语定义)               │   │
│  │ rules.md (业务规则)                  │   │
│  └─────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│  Layer B: 较稳定（随版本迭代）                   │
│  ┌─────────────────────────────────────┐   │
│  │ state-machines.md (状态流转)         │   │
│  │ edge-cases.md (边界情况)             │   │
│  │ modules/*/overview.md (模块概述)      │   │
│  └─────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│  Layer C: 中等稳定（随决策积累）                  │
│  ┌─────────────────────────────────────┐   │
│  │ decisions.md (架构/数据/集成/业务决策)  │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

---

## 文件速查表

| 文件 | 类型 | 条目数 | 稳定性 | 触发场景 |
|------|------|--------|--------|---------|
| glossary.md | 术语表 | 60+ 条 | ⭐⭐⭐ 最高 | 所有任务（术语参考） |
| rules.md | 业务规则 | 15+ 条 | ⭐⭐⭐ 高 | 开发/审查时约束检查 |
| state-machines.md | 状态机 | 4 个 | ⭐⭐ 较高 | 涉及状态变更时 |
| edge-cases.md | 边界情况 | 10+ 条 | ⭐⭐ 较高 | 测试/编码时防遗漏 |
| decisions.md | 决策记录 | 20 条 | ⭐ 中等 | 架构讨论/新人入职 |
| modules/* | 模块概述 | 5 个 | ⭐⭐ 较高 | 接手某模块时快速了解 |

---

## 与其他层的关系

```
domain/ (知识层)          ← 被引用自 →
├── glossary.md           → rules/code-style.md (术语一致性)
├── rules.md              → workflow.md Step 5 (质量审查依据)
├── state-machines.md     → database.md (状态字段设计)
├── edge-cases.md         → testing.md (边界测试覆盖)
├── decisions.md          → agent.md (设计理念传承)
└── modules/*.md          → 各 rules/*.md (模块特定规范)
```

---

## 使用指南

### 新人接手模块
1. 读 `modules/{module_name}/overview.md` 了解全貌
2. 查 `glossary.md` 确认术语含义
3. 看 `state-machines.md` 理解核心流程
4. 参考 `decisions.md` 了解设计决策背景

### 开发新功能前
1. 检 `rules.md` 是否存在相关业务约束
2. 查 `edge-cases.md` 是否有已知边界情况
3. 确认 `state-machines.md` 的状态流转是否受影响

### 任务完成后（知识回写）
1. 新术语？→ 追加到 `glossary.md`
2. 新规则？→ 追加到 `rules.md`
3. 新发现边界？→ 追加到 `edge-cases.md`
4. 做了重要决定？→ 追加到 `decisions.md`

---

最后更新: 2026-04-26 | v1.0 完成 (6 个文件 + 5 个模块 = 11 个知识文件)

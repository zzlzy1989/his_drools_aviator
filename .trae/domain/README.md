---
alwaysApply: true
---
# Domain 知识库索引 - HIS Drools+Aviator 规则引擎

> 业务领域知识库，按稳定性分层组织

---

## 目录结构

```
.trae/domain/
├── README.md                    # 本文档（索引）
├── glossary.md                  # [更新] 术语表 v1.1 (120+ 条, HIS/规则引擎/Drools/Aviator/网关/DRG)
├── rules.md                     # [更新] 业务规则 v1.1 (40+ 条, 含 RuleFlow/DRG/质控/网关)
├── state-machines.md            # [更新] 核心状态机 v1.1 (7 个, 含 RuleFlow 状态机)
├── edge-cases.md                # [更新] 边界情况 v1.1 (19 条, 含 DRG/用药/质控/网关)
├── decisions.md                 # [更新] 决策记录 v1.1 (30 条, 含微服务拆分/Skill重构)
│
└── modules/                     # [重构] 模块知识库 (10 个模块)
    ├── gateway/overview.md            # API 网关服务 (JWT/限流/路由)
    ├── rules/overview.md              # Drools 规则引擎核心 (含 RuleFlow)
    ├── formulas/overview.md           # Aviator 公式管理
    ├── settlements/overview.md        # 医保结算 (Skill Pipeline)
    ├── skills/overview.md             # Skill/Agent 插件架构
    ├── config/overview.md             # 配置中心 (Nacos/Apollo)
    ├── tenants/overview.md            # 多租户管理
    ├── drg/overview.md                # DRG 分组服务
    ├── drug/overview.md               # 合理用药审核服务
    └── quality/overview.md            # 医疗质控服务
```

---

## 知识分层体系

```
┌─────────────────────────────────────────────┐
│  Layer A: 最稳定（极少变化）                    │
│  ┌─────────────────────────────────────┐   │
│  │ glossary.md (术语定义, 120+ 条)      │   │
│  │ rules.md (业务规则, 40+ 条)          │   │
│  └─────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│  Layer B: 较稳定（随版本迭代）                   │
│  ┌─────────────────────────────────────┐   │
│  │ state-machines.md (7 个状态机)       │   │
│  │ edge-cases.md (19 条边界情况)        │   │
│  │ modules/*/overview.md (10 个模块)    │   │
│  └─────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│  Layer C: 中等稳定（随决策积累）                  │
│  ┌─────────────────────────────────────┐   │
│  │ decisions.md (30 条决策, 4 类)       │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

---

## 文件速查表

| 文件 | 类型 | 条目数 | 稳定性 | 触发场景 |
|------|------|--------|--------|---------|
| glossary.md | 术语表 | 120+ 条 | ⭐⭐⭐ 最高 | 所有任务（术语参考） |
| rules.md | 业务规则 | 40+ 条 | ⭐⭐⭐ 高 | 开发/审查时约束检查 |
| state-machines.md | 状态机 | 7 个 | ⭐⭐ 较高 | 涉及状态变更时 |
| edge-cases.md | 边界情况 | 19 条 | ⭐⭐ 较高 | 测试/编码时防遗漏 |
| decisions.md | 决策记录 | 30 条 | ⭐ 中等 | 架构讨论/新人入职 |
| modules/* | 模块概述 | 10 个 | ⭐⭐ 较高 | 接手某模块时快速了解 |

---

## 模块速查表

| 模块 | 服务 | 核心职责 |
|------|------|---------|
| gateway | his-gateway | API 网关、JWT 认证、限流路由 |
| rules | his-rule-service | Drools 规则定义 + RuleFlow 流程编排 |
| formulas | his-formula-service | Aviator 公式管理与校验 |
| settlements | his-settlement-service | 医保结算 + Skill Pipeline |
| skills | his-common + his-settlement | ISkill 接口 + 已实现 Skill |
| config | 配置中心 | Nacos 配置热更新 |
| tenants | 多租户 | 租户隔离、资源配额 |
| drg | his-drg-service | DRG 分组计算 |
| drug | his-drug-service | 合理用药审核 |
| quality | his-quality-service | 医疗质控检测 |

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

最后更新: 2026-05-10 | v1.1 (6 个基础文件 + 10 个模块 = 16 个知识文件)

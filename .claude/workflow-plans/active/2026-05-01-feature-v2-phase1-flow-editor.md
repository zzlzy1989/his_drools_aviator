---
title: "V2.0 Phase 1: 规则可视化编排"
type: "feature"
status: "in_progress"
created_at: "2026-05-01"
updated_at: "2026-05-02"
phase: "Phase 1"
owner: "developer"
reviewer: ""
priority: "P0"
tags: ["规则可视化", "编排", "V2", "Phase1", "Vue3", "AntVX6"]
related_files:
  - "his-rule-engine/"
  - "his-rule-engine/docs/V2_SRS.md"
  - "his-rule-engine/docs/V2_SDD.md"
  - "docs/frontend-development-plan.md"
dependencies: []
---

# PLAN-20260501-004: V2.0 Phase 1 - 规则可视化编排

> 计划 ID: PLAN-20260501-004
> 创建时间: 2026-05-01
> 状态: 🚧 开发中
> 优先级: P0
> 预计工期: 3周

---

## 1. 任务概述

### 1.1 背景
V2.0二期计划中的核心模块，实现拖拽式规则流设计器，支持可视化编排医保结算、用药审核等业务规则流程，降低规则配置门槛。

### 1.2 目标
- 实现拖拽式规则流设计器（Vue 3 + AntV X6）
- 支持条件节点、动作节点、公式节点、子流程节点
- 规则流执行引擎，支持拓扑排序执行
- 版本管理、导入导出功能

### 1.3 范围
**包含:**
- 规则流设计器前端（画布、节点面板、属性配置）
- 规则流管理后端API
- 规则流执行引擎
- 版本管理与回滚
- 导入导出功能

**不包含:**
- 测试沙箱（Phase 2）
- 监控大屏（Phase 3）
- 规则市场（Phase 4）

---

## 2. 功能分解

### 2.1 前端 - 规则流设计器 (Vue 3 + AntV X6)

| 任务 | 说明 | 工作量 | 优先级 |
|------|------|--------|--------|
| F1.1 | 项目初始化 - Vue 3 + Vite + Element Plus + Pinia + AntV X6 | 0.5天 | P0 |
| F1.2 | FlowEditor画布组件 - 节点拖拽、连线、缩放 | 2天 | P0 |
| F1.3 | 节点面板 - 节点类型列表、拖拽源 | 1天 | P0 |
| F1.4 | 属性配置面板 - 节点属性编辑、表达式编辑器 | 2天 | P0 |
| F1.5 | 规则库选择器 - 从已发布规则中选择 | 1天 | P1 |
| F1.6 | 工具栏 - 保存/发布/撤销/重做 | 1天 | P0 |
| F1.7 | 预览模式 - 查看规则流执行路径 | 1天 | P1 |
| F1.8 | 版本历史 - 查看历史版本、版本对比 | 1天 | P1 |

**前端技术栈确认（来自frontend-development-plan.md）:**
- Vue 3 + Composition API + `<script setup>`
- Vite 5.x
- Element Plus 2.7+
- Pinia 2.x
- AntV X6 2.x (规则流编辑器)
- Monaco Editor 0.45+ (DRL/Aviator代码编辑)

### 2.2 后端 - 规则流服务

| 任务 | 说明 | 工作量 |
|------|------|--------|
| B1.1 | 表结构创建 - rule_flow, rule_flow_history | 0.5天 |
| B1.2 | RuleFlowController - CRUD + 发布 + 回滚 | 1天 |
| B1.3 | RuleFlowService - 业务逻辑、校验 | 2天 |
| B1.4 | RuleFlowEngine - 拓扑排序执行引擎 | 2天 |
| B1.5 | 版本管理 - 历史记录、版本对比 | 1天 |
| B1.6 | 导入导出 - JSON序列化和反序列化 | 1天 |

### 2.3 集成与测试

| 任务 | 说明 | 工作量 |
|------|------|--------|
| T1.1 | 前后端联调 - 完整流程验证 | 2天 |
| T1.2 | 单元测试 - RuleFlowEngine测试 | 1天 |
| T1.3 | 规则流执行测试 - 模拟真实场景 | 1天 |

---

## 3. 技术方案

### 3.1 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue 3 | 3.4+ (Composition API + `<script setup>`) |
| 构建工具 | Vite | 5.x |
| UI框架 | Element Plus | 2.7+ |
| 状态管理 | Pinia | 2.x |
| 流程图库 | AntV X6 | 2.x |
| 代码编辑器 | Monaco Editor | 0.45+ (DRL/Aviator语法高亮) |
| 后端框架 | Spring Boot | 3.5.0 |
| 数据库 | MySQL | 8.0 |
| ORM | MyBatis-Plus | 3.5.6 |

### 3.2 规则流数据模型

```json
{
  "flowId": "reimburse-flow-v1",
  "flowName": "医保结算流程",
  "version": 1,
  "nodes": [
    {"nodeId": "node-001", "type": "start", "label": "开始"},
    {"nodeId": "node-002", "type": "condition", "label": "患者类型判断", "expression": "fact.patientType == 'RESIDENT'", "branches": {"true": "node-003", "false": "node-004"}},
    {"nodeId": "node-003", "type": "action", "label": "居民医保结算", "ruleKey": "rule.reimburse.resident"},
    {"nodeId": "node-004", "type": "action", "label": "职工医保结算", "ruleKey": "rule.reimburse.employee"},
    {"nodeId": "node-005", "type": "formula", "label": "计算报销金额", "formulaKey": "formula.reimburse.calculate"},
    {"nodeId": "node-006", "type": "end", "label": "结束"}
  ],
  "edges": [
    {"source": "node-001", "target": "node-002"},
    {"source": "node-002", "target": "node-003", "label": "true"},
    {"source": "node-002", "target": "node-004", "label": "false"},
    {"source": "node-003", "target": "node-005"},
    {"source": "node-004", "target": "node-005"},
    {"source": "node-005", "target": "node-006"}
  ]
}
```

### 3.3 节点类型

| 类型 | 说明 | 参数 |
|------|------|------|
| start | 开始节点 | label |
| end | 结束节点 | label |
| condition | 条件节点 | expression, branches |
| action | 动作节点 | ruleKey, timeout |
| formula | 公式节点 | formulaKey, params |
| subflow | 子流程节点 | flowId, async |

### 3.4 API 接口

```
GET    /api/v2/flows              - 分页查询规则流
GET    /api/v2/flows/{id}         - 获取规则流详情
POST   /api/v2/flows              - 创建规则流
PUT    /api/v2/flows/{id}         - 更新规则流
DELETE /api/v2/flows/{id}         - 删除规则流
POST   /api/v2/flows/{id}/publish - 发布规则流
POST   /api/v2/flows/{id}/rollback - 回滚到历史版本
GET    /api/v2/flows/{id}/versions - 获取版本历史
GET    /api/v2/flows/{id}/compare - 版本对比
GET    /api/v2/flows/{id}/export  - 导出规则流
POST   /api/v2/flows/import       - 导入规则流
```

---

## 4. 数据库设计

### 4.1 rule_flow 表

```sql
CREATE TABLE rule_flow (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    flow_key        VARCHAR(128) NOT NULL COMMENT '规则流唯一标识',
    flow_name       VARCHAR(128) NOT NULL COMMENT '规则流名称',
    flow_definition JSON         NOT NULL COMMENT '规则流图定义',
    version         INT          NOT NULL DEFAULT 1 COMMENT '版本号',
    status          VARCHAR(16)  NOT NULL DEFAULT 'draft' COMMENT 'draft/active/inactive',
    category        VARCHAR(32)  DEFAULT NULL COMMENT '分类',
    tenant_id       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '租户ID',
    create_by       VARCHAR(64)  DEFAULT NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       VARCHAR(64)  DEFAULT NULL,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_key_tenant (flow_key, tenant_id),
    KEY idx_status_tenant (status, tenant_id)
) ENGINE=InnoDB COMMENT='规则流定义';
```

### 4.2 rule_flow_history 表

```sql
CREATE TABLE rule_flow_history (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    flow_id         BIGINT       NOT NULL COMMENT '规则流ID',
    version         INT          NOT NULL COMMENT '版本号',
    flow_definition JSON         NOT NULL COMMENT '规则流定义快照',
    change_desc     VARCHAR(512) DEFAULT NULL COMMENT '变更说明',
    change_by       VARCHAR(64)  NOT NULL COMMENT '变更人',
    change_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_flow_version (flow_id, version)
) ENGINE=InnoDB COMMENT='规则流历史版本';
```

---

## 5. 前端项目结构

```
his-rule-engine-web/
├── src/
│   ├── api/
│   │   ├── request.ts           # Axios实例 + 拦截器
│   │   └── rule-flow.ts         # 规则流API (Phase 1)
│   ├── components/
│   │   ├── Layout/              # 布局组件
│   │   ├── CodeEditor/          # Monaco Editor封装
│   │   └── FlowEditor/          # 规则流编辑器 (核心)
│   ├── composables/
│   │   ├── useFlowEditor.ts     # 规则流编辑器逻辑
│   │   └── useTable.ts          # 表格分页逻辑
│   ├── stores/
│   │   └── flow-editor.ts       # 规则流编辑器状态
│   ├── views/
│   │   └── flow/
│   │       ├── FlowList.vue     # 规则流列表
│   │       ├── FlowEditor.vue   # 可视化编辑器
│   │       └── FlowHistory.vue  # 版本历史
│   └── router/
│       └── modules/flow.ts      # 规则流路由
```

### 5.1 开发阶段（基于frontend-development-plan.md）

| 阶段 | 任务 | 说明 |
|------|------|------|
| 第一阶段 | 项目初始化与基础框架 (3天) | Vite + Vue3 + TS + Element Plus + Pinia + Router + Axios |
| 第四阶段 | V2 规则流编排 (5天) | AntV X6集成、节点面板、画布交互、属性面板、规则库选择器 |
| 第八阶段 | 联调测试与部署 (3天) | 后端联调、性能优化、Nginx部署 |

**总计前端工期: 约11天（含项目初始化5天 + 规则流编排5天 + 联调部署1天）**

---

## 6. 执行计划

### Week 1: 前端基础 + 数据库 ✅

| Day | 任务 | 状态 |
|-----|------|------|
| 1 | Vue 3项目初始化，Element Plus + Pinia + AntV X6集成 | ✅ |
| 2 | 节点面板组件，拖拽功能 | ✅ |
| 3 | 属性配置面板，表达式编辑器 | ✅ |
| 4 | 规则库选择器，工具栏 | ✅ |
| 5 | 数据库表创建，后端基础架构 | ✅ |

### Week 2: 后端核心 + 前端联调 🚧

| Day | 任务 | 状态 |
|-----|------|------|
| 6 | RuleFlowController + Service 开发 | ✅ |
| 7 | RuleFlowEngine 执行引擎 | ✅ |
| 8 | 前后端联调，完整流程验证 | 🔄 进行中 |
| 9 | 版本管理，历史记录 | ⏳ |
| 10 | 单元测试编写 | ⏳ |

### Week 3: 功能完善 + 测试

| Day | 任务 | 状态 |
|-----|------|------|
| 11 | 导入导出功能 | ⏳ |
| 12 | 版本回滚功能 | ⏳ |
| 13 | 预览模式 | ⏳ |
| 14 | 规则流执行测试 | ⏳ |
| 15 | 文档编写，代码review | ⏳ |

---

## 6. 验收标准

- [ ] 可拖拽创建规则流，包含6种节点类型
- [ ] 规则流可发布并正确执行
- [ ] 条件分支正确根据表达式结果路由
- [ ] 版本历史可查看，版本可对比
- [ ] 支持JSON导入导出
- [ ] 版本回滚功能正常
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 规则流执行P99 < 100ms

---

## 7. 风险与应对

| 风险 | 影响 | 概率 | 应对 |
|------|------|------|------|
| AntV X6定制复杂 | 中 | 中 | 预留2天缓冲，提前调研示例 |
| 表达式解析安全性 | 高 | 低 | 使用白名单函数，正则校验 |
| 循环依赖检测 | 中 | 中 | 执行前拓扑排序检测死循环 |

---

## 8. 里程碑

| 阶段 | 完成时间 | 主要交付 |
|------|---------|---------|
| Week 1 | +1周 | 前端画布 + 数据库 |
| Week 2 | +2周 | 后端核心 + 联调 |
| Week 3 | +3周 | 功能完善 + 测试 |

---

*计划创建 | 2026-05-01*
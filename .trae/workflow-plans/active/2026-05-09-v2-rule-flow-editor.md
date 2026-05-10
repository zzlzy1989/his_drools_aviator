---
title: "V2.0 规则可视化编排"
type: "feature"
status: "pending"
created_at: "2026-05-09"
updated_at: "2026-05-10"
completed_at: null
phase: "Phase 2"
owner: "developer"
reviewer: ""
priority: "P0"
tags: ["V2.0", "可视化", "流程编辑器", "规则编排", "前端"]
related_files:
  - "his-rule-engine/his-rule-service/"
  - "his-rule-engine-web/"
dependencies:
  - "PLAN-20260509-001"
---

# PLAN-20260509-002: V2.0 规则可视化编排

> 计划 ID: PLAN-20260509-002  
> 创建时间: 2026-05-09  
> 更新时间: 2026-05-10  
> 状态: ⏳ pending  

---

## 1. 任务概述

### 1.1 背景
当前规则配置依赖 DRL 文件和数据库手动编辑，缺乏可视化编排能力，非技术人员难以维护规则流程。

### 1.2 目标
- 开发前端流程编辑器，支持拖拽式规则编排
- 后端提供规则流程定义和执行的 API
- 实现前端流程图到后端 DRL/Skill 管道的转换

### 1.3 范围
- **包含**: 流程编辑器 UI、流程定义 API、流程图解析引擎、流程执行引擎
- **不包含**: 规则内容编辑（使用现有规则管理功能）

### 1.4 当前实现状态
| 组件 | 当前状态 | 说明 |
|------|---------|------|
| 后端 RuleFlowController | ✅ 已实现 | 11 个 REST API（CRUD、发布、回滚、版本、导入导出、执行） |
| 前端 FlowEditor 组件 | ✅ 已实现 | FlowEditor.vue + types.ts |
| 前端 FlowList 页面 | ✅ 已实现 | FlowList.vue 流程列表 |
| 前端路由配置 | ✅ 已实现 | /flow 路由已配置 |
| 流程定义数据表 | ✅ 已实现 | flow_definition 表 |
| 流程执行引擎 | ⏳ 待完善 | 需完善 FlowParser 和 FlowExecutor |

---

## 2. 技术方案

### 2.1 前端技术栈
| 组件 | 技术选型 | 说明 |
|------|---------|------|
| 流程图框架 | X6 / LogicFlow | 阿里/滴滴开源流程图引擎 |
| UI 框架 | Vue 3 + Element Plus | 与现有前端一致 |
| 状态管理 | Pinia | 流程定义状态管理 |

### 2.2 后端 API 设计

| API | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| 创建流程 | POST | `/api/v1/flows` | 创建规则流程定义 | ✅ |
| 查询流程 | GET | `/api/v1/flows/{id}` | 获取流程定义详情 | ✅ |
| 更新流程 | PUT | `/api/v1/flows/{id}` | 更新流程定义 | ✅ |
| 发布流程 | POST | `/api/v1/flows/{id}/publish` | 发布流程到执行引擎 | ✅ |
| 执行流程 | POST | `/api/v1/flows/{id}/execute` | 执行规则流程 | ✅ |
| 流程列表 | GET | `/api/v1/flows` | 分页查询流程列表 | ✅ |
| 删除流程 | DELETE | `/api/v1/flows/{id}` | 删除流程 | ✅ |
| 回滚流程 | POST | `/api/v1/flows/{id}/rollback` | 回滚到指定版本 | ✅ |
| 版本历史 | GET | `/api/v1/flows/{id}/versions` | 获取版本列表 | ✅ |
| 导出流程 | GET | `/api/v1/flows/{id}/export` | 导出JSON定义 | ✅ |
| 导入流程 | POST | `/api/v1/flows/import` | 导入JSON定义 | ✅ |

### 2.3 流程定义数据结构

```json
{
  "flowId": "flow_reimburse_001",
  "flowName": "医保报销流程",
  "description": "职工/居民医保报销规则流程",
  "nodes": [
    {
      "nodeId": "node_1",
      "nodeType": "skill",
      "nodeKey": "InsuranceIdentitySkill",
      "position": { "x": 100, "y": 100 }
    },
    {
      "nodeId": "node_2",
      "nodeType": "skill",
      "nodeKey": "DeductibleSkill",
      "position": { "x": 100, "y": 250 }
    },
    {
      "nodeId": "node_3",
      "nodeType": "rule_group",
      "nodeKey": "rule.reimbursement.employee",
      "position": { "x": 100, "y": 400 }
    },
    {
      "nodeId": "node_4",
      "nodeType": "formula",
      "nodeKey": "formula.reimburse.employee.basic",
      "position": { "x": 100, "y": 550 }
    }
  ],
  "edges": [
    { "source": "node_1", "target": "node_2", "condition": "PASS" },
    { "source": "node_2", "target": "node_3", "condition": "PASS" },
    { "source": "node_3", "target": "node_4", "condition": "PASS" }
  ]
}
```

---

## 3. 执行计划

### Phase 1: 后端 API 开发 (已完成)

| 步骤 | 操作 | 交付物 | 状态 |
|------|------|--------|------|
| 1.1 | 设计 flow_definition 表结构 | SQL 脚本 | ✅ |
| 1.2 | 创建 FlowDefinition 实体类 | Java 实体 | ✅ |
| 1.3 | 实现 FlowController API | Controller + Service | ✅ |
| 1.4 | 实现流程解析引擎 | FlowParser 解析器 | ⏳ |
| 1.5 | 实现流程执行引擎 | FlowExecutor 执行器 | ⏳ |

### Phase 2: 前端编辑器开发 (2 天)

| 步骤 | 操作 | 交付物 | 状态 |
|------|------|--------|------|
| 2.1 | 初始化 X6 流程图框架 | 基础画布 | ⏳ |
| 2.2 | 实现节点拖拽 | Skill/Rule/Formula 节点 | ⏳ |
| 2.3 | 实现连线功能 | 条件连线 | ⏳ |
| 2.4 | 实现流程保存 | 前后端联调 | ⏳ |
| 2.5 | 实现流程发布/执行 | 完整流程 | ⏳ |

### Phase 3: 联调与测试 (2 天)

| 步骤 | 操作 | 交付物 | 状态 |
|------|------|--------|------|
| 3.1 | 前后端联调 | 功能验证 | ⏳ |
| 3.2 | 流程执行测试 | 集成测试 | ⏳ |
| 3.3 | 优化交互体验 | UI 优化 | ⏳ |

---

## 4. 数据库设计

```sql
CREATE TABLE `flow_definition` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `flow_key`        VARCHAR(128) NOT NULL COMMENT '流程Key: flow.reimburse.employee',
    `flow_name`       VARCHAR(256) NOT NULL COMMENT '流程名称',
    `description`     VARCHAR(512) DEFAULT NULL COMMENT '流程说明',
    `flow_data`       JSON         NOT NULL COMMENT '流程图数据(nodes/edges)',
    `category`        VARCHAR(64)  NOT NULL DEFAULT 'REIMBURSEMENT' COMMENT '分类',
    `version`         INT          NOT NULL DEFAULT 1 COMMENT '版本号',
    `status`          VARCHAR(32)  NOT NULL DEFAULT 'draft' COMMENT '状态: draft/published/inactive',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '',
    `create_by`       VARCHAR(64)  DEFAULT NULL,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(64)  DEFAULT NULL,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_key_tenant` (`flow_key`, `tenant_id`),
    KEY `idx_category_status` (`category`, `status`)
) ENGINE=InnoDB COMMENT='流程定义';
```

---

## 5. 验收标准

- [x] flow_definition 表结构正确创建
- [x] 后端 11 个 API 全部实现
- [ ] 前端流程编辑器可拖拽创建节点
- [ ] 流程图可保存到数据库
- [ ] 流程图可解析为可执行的 Skill/DRL/Formula 序列
- [ ] 流程可正确执行并返回结果
- [ ] 支持流程版本管理

---

## 6. 风险评估

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|:----:|---------|
| 流程图框架兼容性问题 | 高 | 低 | 提前评估 X6 和 LogicFlow |
| 流程图解析逻辑复杂 | 高 | 中 | 先实现简单线性流程，再支持分支 |
| 前后端数据格式不一致 | 中 | 中 | 提前定义 JSON Schema |
| 性能问题（大图渲染） | 中 | 低 | 节点超过 50 个时分页/分组 |

---

## 7. 进度跟踪

| 时间 | 操作 | 状态变更 | 备注 |
|------|------|---------|------|
| 2026-05-09 | 创建计划 | pending | 初始创建 |
| 2026-05-10 | 更新计划 | pending | 补充当前实现状态、API 完成度 |
| | | | |

---

*计划待执行*

---
title: "V2.0 规则市场模块"
type: "feature"
status: "in_progress"
created_at: "2026-05-15"
updated_at: "2026-05-15"
phase: "Phase 2"
owner: "developer"
reviewer: ""
priority: "P0"
tags: ["V2.0", "规则市场", "模板共享", "跨医院复用"]
related_files:
  - "his-rule-engine/his-market-service/"
  - "his-rule-engine-web/src/views/market/"
dependencies: ["2026-05-12-monitor-service.md"]
---

# PLAN-20260515-001: V2.0 规则市场

> 计划 ID: PLAN-20260515-001
> 创建时间: 2026-05-15
> 状态: ✅ completed

---

## 1. 任务概述

### 1.1 目标
实现规则市场模块，支持跨医院规则模板共享：
1. his-market-service (9008) - 规则市场服务后端
2. 模板发布/订阅/安装功能
3. 模板分类检索

### 1.2 技术选型
| 组件 | 技术 | 说明 |
|------|------|------|
| 后端 | Spring Boot 3.5 + MyBatis-Plus | 模板管理CRUD |
| 前端 | Vue 3 + Element Plus | 模板市场页面 |
| 存储 | MySQL | 模板数据存储 |
| 端口 | 9008 | 新服务 |

### 1.3 功能范围
| 优先级 | 功能 | 说明 |
|--------|------|------|
| P0 | 模板发布 | 规则管理员发布规则模板到市场 |
| P0 | 模板分类检索 | 分类（医保/用药/质控/DRG）+ 搜索 |
| P0 | 模板订阅安装 | 一键导入模板到本租户 |
| P1 | 模板版本管理 | 模板升级，订阅者可选择性更新 |
| P1 | 模板收藏 | 收藏常用模板到个人空间 |
| P2 | 模板评分评论 | 1-5星评分 + 评论文字 |

---

## 2. 数据库设计

### 2.1 核心表结构

```sql
-- 规则模板表
CREATE TABLE rule_template (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    template_key    VARCHAR(128) NOT NULL COMMENT '模板唯一标识',
    name            VARCHAR(256) NOT NULL COMMENT '模板名称',
    category        VARCHAR(64)  NOT NULL COMMENT '分类: REIMBURSE/DRUG/QUALITY/DRG',
    tags            VARCHAR(512) DEFAULT NULL COMMENT '标签，逗号分隔',
    description     TEXT         DEFAULT NULL COMMENT '模板描述',
    version         VARCHAR(32)  NOT NULL DEFAULT '1.0.0' COMMENT '版本号',
    content         JSON         NOT NULL COMMENT '模板内容(JSON包含rules/formulas/flows)',
    provider_id     VARCHAR(64)  NOT NULL COMMENT '发布者租户ID',
    provider_name   VARCHAR(256) DEFAULT NULL COMMENT '发布者租户名称',
    published_by    VARCHAR(64)  DEFAULT NULL COMMENT '发布人',
    status          VARCHAR(32)  NOT NULL DEFAULT 'published' COMMENT '状态: draft/published/inactive',
    install_count   INT          NOT NULL DEFAULT 0 COMMENT '安装次数',
    tenant_id       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '租户ID(创建者)',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_key (template_key),
    KEY idx_category (category),
    KEY idx_provider (provider_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则模板';

-- 模板安装记录表
CREATE TABLE template_install (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    template_id     BIGINT       NOT NULL COMMENT '模板ID',
    tenant_id       VARCHAR(64)  NOT NULL COMMENT '安装租户ID',
    installed_by    VARCHAR(64)  DEFAULT NULL COMMENT '安装人',
    installed_version VARCHAR(32) NOT NULL COMMENT '安装时的版本',
    install_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_sync_version VARCHAR(32) DEFAULT NULL COMMENT '最后同步版本',
    last_sync_time  DATETIME     DEFAULT NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'active' COMMENT '状态: active/unsubscribed',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_tenant (template_id, tenant_id),
    KEY idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板安装记录';

-- 模板收藏表
CREATE TABLE template_favorite (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    template_id     BIGINT       NOT NULL COMMENT '模板ID',
    user_id        VARCHAR(64)  NOT NULL COMMENT '用户ID',
    tenant_id       VARCHAR(64)  NOT NULL COMMENT '租户ID',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_user (template_id, user_id),
    KEY idx_tenant_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板收藏';

-- 模板评分表
CREATE TABLE template_rating (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    template_id     BIGINT       NOT NULL COMMENT '模板ID',
    user_id        VARCHAR(64)  NOT NULL COMMENT '用户ID',
    tenant_id       VARCHAR(64)  NOT NULL COMMENT '租户ID',
    rating          INT          NOT NULL COMMENT '评分 1-5',
    comment         TEXT         DEFAULT NULL COMMENT '评论内容',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_user (template_id, user_id),
    KEY idx_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板评分';
```

---

## 3. 后端实现

### 3.1 模块结构
```
his-market-service (9008)
├── pom.xml
├── src/main/java/com/his/market/
│   ├── MarketServiceApplication.java
│   ├── entity/
│   │   ├── RuleTemplate.java
│   │   ├── TemplateInstall.java
│   │   ├── TemplateFavorite.java
│   │   └── TemplateRating.java
│   ├── mapper/
│   │   ├── RuleTemplateMapper.java
│   │   ├── TemplateInstallMapper.java
│   │   ├── TemplateFavoriteMapper.java
│   │   └── TemplateRatingMapper.java
│   ├── service/
│   │   ├── RuleTemplateService.java
│   │   └── TemplateInstallService.java
│   ├── controller/
│   │   └── RuleTemplateController.java
│   └── dto/
│       ├── RuleTemplateDTO.java
│       └── TemplateContentDTO.java
```

### 3.2 API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/market/templates | 模板列表（分页+筛选） |
| GET | /api/v1/market/templates/{id} | 模板详情 |
| POST | /api/v1/market/templates | 发布模板 |
| PUT | /api/v1/market/templates/{id} | 更新模板 |
| DELETE | /api/v1/market/templates/{id} | 删除模板 |
| POST | /api/v1/market/templates/{id}/install | 安装模板 |
| GET | /api/v1/market/my-templates | 我的发布模板 |
| GET | /api/v1/market/subscribed | 已订阅模板 |
| POST | /api/v1/market/templates/{id}/favorite | 收藏 |
| DELETE | /api/v1/market/templates/{id}/favorite | 取消收藏 |
| POST | /api/v1/market/templates/{id}/rate | 评分 |

### 3.3 模板内容结构
```json
{
  "rules": [
    {
      "ruleKey": "rule.reimburse.resident",
      "ruleName": "居民医保报销规则",
      "ruleContent": "package com.his.rules...\nrule '1. 身份校验'...",
      "category": "REIMBURSE",
      "salience": 100
    }
  ],
  "formulas": [
    {
      "formulaKey": "formula.reimburse.resident.basic",
      "formulaName": "居民医保基本报销",
      "formulaText": "(totalFee - deductible) * ratio",
      "category": "REIMBURSE"
    }
  ],
  "flows": [
    {
      "flowKey": "flow.settlement.resident",
      "flowName": "居民医保结算流程",
      "flowDefinition": { "nodes": [], "edges": [] }
    }
  ]
}
```

---

## 4. 前端实现

### 4.1 页面结构
```
src/views/market/
├── MarketPage.vue       # 规则市场主页
└── TemplateDetail.vue   # 模板详情/预览

src/api/
└── market.ts           # API 调用
```

### 4.2 路由配置
```ts
{
  path: 'market',
  name: 'Market',
  component: () => import('@/views/market/MarketPage.vue'),
  meta: { title: '规则市场' }
}
```

---

## 5. 实施计划

| 阶段 | 任务 | 工期 |
|------|------|------|
| 1 | 数据库表创建 | 0.5天 |
| 2 | his-market-service 后端CRUD | 1天 |
| 3 | 模板安装功能 | 1天 |
| 4 | 前端市场页面 | 1天 |
| 5 | 前后端联调 | 1天 |

---

## 6. 验收标准

- [ ] his-market-service 启动成功 (端口 9008)
- [ ] 模板发布/编辑/删除正常
- [ ] 模板列表支持分类筛选和搜索
- [ ] 模板安装功能正常
- [ ] 前端市场页面显示正常
- [ ] Docker 镜像构建成功

---

## 7. 进度

| 时间 | 操作 | 状态 |
|------|------|------|
| 2026-05-15 | 创建计划 | ⏳ |

---
title: "DESIGN.md 前端设计系统集成"
type: "feature"
status: "completed"
created_at: "2026-05-19 10:00:00"
updated_at: "2026-05-20 12:00:00"
completed_at: "2026-05-20 12:00:00"
phase: "Completed"
owner: "developer"
reviewer: ""
priority: "P1"
tags: ["frontend", "design-system", "DESIGN.md", "ui-refactor"]
related_files:
  - "his-rule-engine-web/src/assets/styles/index.scss"
  - "his-rule-engine-web/src/assets/styles/variables.scss"
  - "his-rule-engine-web/src/components/Layout/Layout.vue"
  - "his-rule-engine-web/src/views/dashboard/Dashboard.vue"
  - "his-rule-engine-web/src/views/login/Login.vue"
dependencies: []
---

# PLAN-20260519-002: DESIGN.md 前端设计系统集成

> 计划 ID: PLAN-20260519-002
> 创建时间: 2026-05-19 10:00:00
> 状态: completed

---

## 1. 任务概述

### 1.1 背景

当前 HIS 规则中台前端（`his-rule-engine-web`）使用 Element Plus 默认主题，视觉风格较为通用，缺乏统一的品牌设计语言。各页面样式分散在组件 `<style>` 中，缺乏全局设计 token 体系，导致：
- 页面间视觉一致性不足
- 修改主题需要逐页调整
- AI 辅助生成页面时缺乏设计规范约束

[awesome-design-md](https://github.com/VoltAgent/awesome-design-md) 提供了一种基于纯 Markdown 的设计系统文档（DESIGN.md），可被 AI 编码代理读取后生成一致风格的 UI 代码。本项目计划引入 DESIGN.md 机制，建立 HIS 专属的前端设计系统。

### 1.2 目标

1. **建立 HIS 专属 DESIGN.md**：基于 IBM Carbon（管理后台）+ Sentry（监控大屏）+ Stripe（结算页面）混合风格，创建符合医疗行业特征的设计规范文档
2. **创建 SCSS 变量体系**：将 DESIGN.md 的设计 token 映射为 Element Plus 可消费的 SCSS 变量
3. **逐页重构现有页面**：按 DESIGN.md 规范重构核心页面，建立视觉一致性
4. **建立 AI 辅助开发流程**：后续新增页面时，AI 代理可依据 DESIGN.md 生成符合规范的 UI 代码

### 1.3 范围

- **包含**:
  - 下载并定制 DESIGN.md 设计规范
  - 创建 SCSS 变量/主题文件体系
  - 重构 Layout / Login / Dashboard / RuleList / FormulaList / SettlementList / Monitor 等核心页面
  - 创建 HIS 专属业务组件（ResultLevelTag / RuleStatusBadge / StatCard）
  - 更新前端开发计划文档
- **不包含**:
  - 后端 API 变更
  - 新增业务功能
  - 移动端适配（本期仅桌面端）

---

## 2. 技术方案

### 2.1 设计风格选型

| 模块 | 风格来源 | 核心特征 | 适用页面 |
|------|---------|---------|---------|
| **管理后台** | IBM Carbon | 方正(0px圆角)、IBM Blue(#0f62fe)、企业级可信赖感 | 规则管理、公式管理、规则组、用药审核、质控管理、DRG管理、系统管理 |
| **监控大屏** | Sentry | 深色画布(#1f1633)、Electric Lime(#c2ef4e)高亮、数据密集 | 监控大屏、执行日志、性能指标、告警面板 |
| **结算/金融** | Stripe | 紫色渐变、优雅专业、金融级精致感 | 结算管理、DRG费用、费用明细 |
| **登录页** | 自定义 | 医疗蓝渐变、专业可信 | 登录页 |

### 2.2 涉及模块

- `his-rule-engine-web` - 前端工程全部模块

### 2.3 新增/修改文件

| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `his-rule-engine-web/DESIGN.md` | 新增 | HIS 专属设计系统文档（IBM Carbon 基础 + 医疗扩展） |
| `his-rule-engine-web/src/assets/styles/variables.scss` | 新增 | SCSS 设计 token 变量（映射 DESIGN.md） |
| `his-rule-engine-web/src/assets/styles/mixins.scss` | 新增 | SCSS 混入（卡片、状态标签、响应式断点） |
| `his-rule-engine-web/src/assets/styles/reset.scss` | 新增 | 样式重置（基于 DESIGN.md 排版规范） |
| `his-rule-engine-web/src/assets/styles/element-theme.scss` | 新增 | Element Plus 主题覆盖 |
| `his-rule-engine-web/src/assets/styles/monitor-theme.scss` | 新增 | 监控大屏深色主题变量 |
| `his-rule-engine-web/src/assets/styles/settlement-theme.scss` | 新增 | 结算页面 Stripe 风格变量 |
| `his-rule-engine-web/src/assets/styles/index.scss` | 修改 | 引入新增样式文件 |
| `his-rule-engine-web/src/components/Layout/Layout.vue` | 修改 | 按 Carbon 风格重构侧边栏/头部 |
| `his-rule-engine-web/src/views/login/Login.vue` | 修改 | 医疗蓝渐变登录页 |
| `his-rule-engine-web/src/views/dashboard/Dashboard.vue` | 修改 | Carbon 风格仪表盘 |
| `his-rule-engine-web/src/views/rule/RuleList.vue` | 修改 | Carbon 风格规则列表 |
| `his-rule-engine-web/src/views/formula/FormulaList.vue` | 修改 | Carbon 风格公式列表 |
| `his-rule-engine-web/src/views/settlement/SettlementList.vue` | 修改 | Stripe 风格结算列表 |
| `his-rule-engine-web/src/views/monitor/Dashboard.vue` | 修改 | Sentry 风格监控大屏 |
| `his-rule-engine-web/src/components/ResultLevelTag.vue` | 新增 | PASS/WARN/BLOCK 结果级别标签组件 |
| `his-rule-engine-web/src/components/RuleStatusBadge.vue` | 新增 | 规则状态徽章组件 |
| `his-rule-engine-web/src/components/StatCard.vue` | 新增 | 统计卡片组件 |
| `his-rule-engine-web/src/components/MonitorCard.vue` | 新增 | 监控大屏卡片组件（深色） |
| `his-rule-engine-web/vite.config.ts` | 修改 | 配置 SCSS 全局变量注入 |

### 2.4 技术要点

1. **DESIGN.md → SCSS Token 映射**：DESIGN.md 中的 colors/typography/spacing/rounded 等 token 需要一对一映射为 SCSS 变量，供 Element Plus 和自定义组件消费
2. **Element Plus 主题覆盖**：通过 `cssVars` 和 SCSS 变量覆盖 Element Plus 默认主题，而非 fork 组件库
3. **多主题切换**：管理后台（亮色 Carbon）与监控大屏（暗色 Sentry）通过 CSS 变量 + `.monitor-theme` 类名切换
4. **AI 辅助开发流程**：DESIGN.md 放在项目根目录，AI 代理生成新页面时自动读取设计规范

---

## 3. 执行步骤

### Step 1: 下载 DESIGN.md 原始文件 ✅
### Step 2: 创建 HIS 专属 DESIGN.md ✅
### Step 3: 创建 SCSS 变量体系 ✅
### Step 4: 创建 HIS 业务组件 ✅
### Step 5: 重构 Layout 布局组件 ✅
### Step 6: 重构 Login 登录页 ✅
### Step 7: 重构 Dashboard 仪表盘 ✅
### Step 8: 重构规则/公式管理页面 ✅
### Step 9: 重构结算管理页面 ✅
### Step 10: 重构监控大屏 ✅
### Step 11: 验证与收尾 ✅

---

## 4. 进度跟踪

| 时间 | 操作 | 状态变更 | 备注 |
|------|------|---------|------|
| 2026-05-19 10:00 | 创建计划 | pending | 初始创建 |
| 2026-05-19 11:00 | Step 1-4 完成 | in_progress | 下载DESIGN.md、创建HIS DESIGN.md、SCSS变量体系、业务组件 |
| 2026-05-19 14:00 | Step 5-7 完成 | in_progress | Layout/Login/Dashboard 重构 |
| 2026-05-19 18:00 | Step 8-9 完成 | in_progress | 规则/公式/结算页面重构 |
| 2026-05-20 10:00 | Step 10 完成 | in_progress | 监控大屏重构 |
| 2026-05-20 12:00 | Step 11 完成 | completed | 验证与收尾，构建通过 |

---

## 5. 完成检查清单

- [x] DESIGN.md 创建完成（含 9 大章节）
- [x] SCSS 变量体系创建完成
- [x] Element Plus 主题覆盖完成
- [x] HIS 业务组件创建完成（4个）
- [x] Layout 布局重构完成
- [x] Login 页面重构完成
- [x] Dashboard 仪表盘重构完成
- [x] 规则/公式管理页面重构完成
- [x] 结算管理页面重构完成
- [x] 监控大屏重构完成
- [x] 全页面视觉走查通过
- [x] 构建验证通过
- [x] 前端开发计划文档更新

---

*计划结束*

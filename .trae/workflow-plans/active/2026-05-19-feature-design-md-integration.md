---
title: "DESIGN.md 前端设计系统集成"
type: "feature"
status: "pending"
created_at: "2026-05-19 10:00:00"
updated_at: "2026-05-19 10:00:00"
completed_at: null
phase: "Phase 6"
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
> 状态: pending  

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

### Step 1: 下载 DESIGN.md 原始文件

- **目标**: 获取 IBM Carbon / Sentry / Stripe 三种风格的原始 DESIGN.md
- **操作**:
  1. 克隆 `awesome-design-md` 仓库到临时目录
  2. 提取 `design-md/ibm/DESIGN.md`、`design-md/sentry/DESIGN.md`、`design-md/stripe/DESIGN.md`
  3. 保存到 `his-rule-engine-web/design-references/` 目录作为参考
- **验收标准**: 三个 DESIGN.md 文件已下载并可在项目中查看

### Step 2: 创建 HIS 专属 DESIGN.md

- **目标**: 基于 IBM Carbon 风格，融合医疗行业特征，创建 HIS 项目的设计系统文档
- **操作**:
  1. 以 IBM Carbon DESIGN.md 为基础框架
  2. 扩展 HIS 业务语义色（PASS/WARN/BLOCK、规则状态色、结算结果色）
  3. 添加中文字体回退链（PingFang SC / Microsoft YaHei）
  4. 添加监控大屏深色主题章节（基于 Sentry）
  5. 添加结算页面金融主题章节（基于 Stripe）
  6. 添加 HIS 业务组件样式规范（ResultLevelTag / RuleStatusBadge / StatCard / MonitorCard）
  7. 添加 Agent Prompt Guide 章节，提供可直接使用的 AI 提示词模板
- **验收标准**: DESIGN.md 包含完整的 9 大章节，覆盖管理后台 + 监控大屏 + 结算页面三种场景

### Step 3: 创建 SCSS 变量体系

- **目标**: 将 DESIGN.md 的设计 token 映射为可消费的 SCSS 变量
- **操作**:
  1. 创建 `variables.scss`：颜色、字体、间距、圆角等基础 token
  2. 创建 `mixins.scss`：卡片、状态标签、响应式断点等混入
  3. 创建 `reset.scss`：基于 DESIGN.md 排版规范的全局重置
  4. 创建 `element-theme.scss`：Element Plus 主题变量覆盖
  5. 创建 `monitor-theme.scss`：监控大屏深色主题变量
  6. 创建 `settlement-theme.scss`：结算页面 Stripe 风格变量
  7. 修改 `vite.config.ts`：配置 SCSS 全局变量自动注入
  8. 修改 `index.scss`：引入所有新增样式文件
- **验收标准**: 所有 SCSS 变量文件创建完成，Vite 构建无报错，Element Plus 组件使用新主题色

### Step 4: 创建 HIS 业务组件

- **目标**: 基于 DESIGN.md 组件样式规范，创建 HIS 专属业务组件
- **操作**:
  1. 创建 `ResultLevelTag.vue`：PASS(绿)/WARN(黄)/BLOCK(红) 结果级别标签
  2. 创建 `RuleStatusBadge.vue`：草稿/已发布/已停用/校验中 状态徽章
  3. 创建 `StatCard.vue`：仪表盘统计卡片（Carbon 风格：方正、无阴影、hairline 边框）
  4. 创建 `MonitorCard.vue`：监控大屏卡片（Sentry 风格：深色、lime 高亮）
- **验收标准**: 四个组件创建完成，Storybook 或独立页面可预览各状态

### Step 5: 重构 Layout 布局组件

- **目标**: 按 IBM Carbon 风格重构主布局
- **操作**:
  1. 侧边栏：深色(#161616) → Carbon inverse-canvas 风格，0px 圆角菜单项
  2. 顶部导航：白色背景 + hairline 底边框，IBM Blue 激活态
  3. 主内容区：surface-1(#f4f4f4) 背景
  4. 面包屑：Carbon 风格，ink-muted 二级文字
  5. 用户下拉：Carbon 风格，方形菜单
- **验收标准**: Layout 整体视觉呈现 IBM Carbon 企业级风格，与默认 Element Plus 主题有明显差异

### Step 6: 重构 Login 登录页

- **目标**: 创建医疗行业专业感的登录页
- **操作**:
  1. 背景：医疗蓝渐变（#0f62fe → #0043ce），替代当前紫色渐变
  2. 登录卡片：白色、0px 圆角（Carbon 方正风格）、hairline 边框
  3. 输入框：surface-1 背景、0px 圆角、IBM Blue 聚焦态
  4. 登录按钮：IBM Blue 填充、0px 圆角、白色文字
  5. 标题：IBM Plex Sans / PingFang SC，weight 300 大标题
- **验收标准**: 登录页呈现医疗行业专业感，与 Carbon 设计语言一致

### Step 7: 重构 Dashboard 仪表盘

- **目标**: 按 Carbon 风格重构仪表盘
- **操作**:
  1. 统计卡片：使用 StatCard 组件，方正、hairline 边框、无阴影
  2. 图表区域：ECharts 使用 IBM Blue / Carbon Green / Carbon Yellow 配色
  3. 快捷入口：button-primary (IBM Blue) + button-ghost 风格
  4. 间距：遵循 4px 基准网格
- **验收标准**: 仪表盘整体视觉与 Carbon 设计系统一致

### Step 8: 重构规则/公式管理页面

- **目标**: 按 Carbon 风格重构规则管理和公式管理列表页
- **操作**:
  1. 搜索栏：surface-1 输入框、0px 圆角、hairline 边框
  2. 表格：hairline 边框、surface-1 斑马纹、无阴影
  3. 状态列：使用 RuleStatusBadge 组件
  4. 操作按钮：button-primary (IBM Blue) + button-ghost + button-danger
  5. 分页：Carbon 风格分页器
- **验收标准**: 列表页视觉与 Carbon 设计系统一致，状态标签清晰可辨

### Step 9: 重构结算管理页面

- **目标**: 按 Stripe 风格重构结算相关页面
- **操作**:
  1. 结算列表：Stripe 紫色渐变头部、优雅卡片布局
  2. 费用明细：精致的数据展示、Stripe 风格金额排版
  3. 结果级别：使用 ResultLevelTag 组件
  4. 结算执行表单：Stripe 风格输入框和按钮
- **验收标准**: 结算页面呈现金融级精致感，与 Stripe 设计语言呼应

### Step 10: 重构监控大屏

- **目标**: 按 Sentry 风格重构监控大屏
- **操作**:
  1. 整体布局：深色画布(#1f1633)、全屏沉浸式
  2. 数据卡片：使用 MonitorCard 组件，Electric Lime 高亮关键指标
  3. 图表配色：accent-lime / accent-pink / accent-violet 三色系
  4. 实时数据：脉冲动画、lime 色闪烁告警
  5. 日志区域：code-block 样式，Monaco 等宽字体
- **验收标准**: 监控大屏呈现深色数据密集风格，关键数据用 lime 色高亮

### Step 11: 验证与收尾

- **目标**: 全面验证设计系统一致性，完成收尾工作
- **操作**:
  1. 全页面视觉走查：检查所有已重构页面的一致性
  2. 响应式验证：1920px / 1440px / 1280px 三个断点
  3. Element Plus 组件主题一致性检查
  4. 深色/亮色主题切换测试
  5. 浏览器兼容性：Chrome / Firefox / Edge
  6. 构建验证：`pnpm build` 无报错
  7. 更新前端开发计划文档
- **验收标准**: 所有页面视觉一致，构建无报错，无样式回归

---

## 4. 测试计划

### 4.1 视觉回归测试

- [ ] Layout 布局与 Carbon 风格一致
- [ ] Login 页面医疗蓝渐变正确
- [ ] Dashboard 统计卡片方正无阴影
- [ ] 规则/公式列表 Carbon 风格一致
- [ ] 结算页面 Stripe 风格一致
- [ ] 监控大屏 Sentry 深色风格一致
- [ ] ResultLevelTag PASS/WARN/BLOCK 配色正确
- [ ] RuleStatusBadge 各状态配色正确

### 4.2 功能回归测试

- [ ] 登录流程正常
- [ ] 侧边栏导航正常
- [ ] 规则 CRUD 操作正常
- [ ] 公式 CRUD 操作正常
- [ ] 结算执行正常
- [ ] 监控数据展示正常
- [ ] Element Plus 组件交互正常（下拉、弹窗、表单等）

### 4.3 构建测试

- [ ] `pnpm dev` 开发服务器正常启动
- [ ] `pnpm build` 生产构建无报错
- [ ] `pnpm lint` 代码规范检查通过

---

## 5. 风险评估

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|:----:|---------|
| Element Plus 主题覆盖不完整 | 中 | 中 | 优先使用 CSS 变量覆盖，必要时使用 ::v-deep 穿透 |
| 多主题（亮/暗）切换冲突 | 高 | 低 | 使用 CSS 变量 + 作用域类名隔离，避免全局污染 |
| Carbon 方正风格(0px圆角)与 Element Plus 默认圆角冲突 | 中 | 高 | 全局覆盖 $--border-radius-base 为 0px |
| IBM Plex Sans 中文字体回退不一致 | 低 | 中 | 明确指定 PingFang SC / Microsoft YaHei 回退链 |
| 重构过程中引入功能回归 | 高 | 低 | 逐页重构，每页完成后立即验证功能 |
| DESIGN.md 规范与实际实现偏差 | 中 | 中 | 定期视觉走查，及时更新 DESIGN.md |

---

## 6. 进度跟踪

| 时间 | 操作 | 状态变更 | 备注 |
|------|------|---------|------|
| 2026-05-19 10:00 | 创建计划 | pending | 初始创建 |
| | | | |

---

## 7. 完成检查清单

- [ ] DESIGN.md 创建完成（含 9 大章节）
- [ ] SCSS 变量体系创建完成
- [ ] Element Plus 主题覆盖完成
- [ ] HIS 业务组件创建完成（4个）
- [ ] Layout 布局重构完成
- [ ] Login 页面重构完成
- [ ] Dashboard 仪表盘重构完成
- [ ] 规则/公式管理页面重构完成
- [ ] 结算管理页面重构完成
- [ ] 监控大屏重构完成
- [ ] 全页面视觉走查通过
- [ ] 构建验证通过
- [ ] 前端开发计划文档更新

---

*计划结束*

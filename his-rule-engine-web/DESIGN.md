---
version: "1.0"
name: HIS-Rule-Engine-Design-System
description: >
  HIS 动态规则中台设计系统 — 基于 IBM Carbon 企业级框架，融合医疗行业语义色，
  集成 Sentry 深色监控大屏风格和 Stripe 金融结算风格。
  管理后台采用 Carbon 方正美学（0px圆角、IBM Blue主色、hairline边框），
  监控大屏采用 Sentry 深色画布（Electric Lime高亮、数据密集），
  结算页面采用 Stripe 金融精致感（Indigo渐变、tabular数字排版）。

colors:
  primary: "#0f62fe"
  primary-hover: "#0050e6"
  primary-press: "#0043ce"
  primary-deep: "#002d9c"
  on-primary: "#ffffff"
  ink: "#161616"
  ink-secondary: "#525252"
  ink-muted: "#8c8c8c"
  canvas: "#ffffff"
  surface-1: "#f4f4f4"
  surface-2: "#e0e0e0"
  inverse-canvas: "#161616"
  inverse-surface-1: "#262626"
  inverse-ink: "#ffffff"
  inverse-ink-muted: "#c6c6c6"
  hairline: "#e0e0e0"
  hairline-strong: "#161616"
  semantic-pass: "#24a148"
  semantic-pass-bg: "#defbe6"
  semantic-warn: "#b28600"
  semantic-warn-bg: "#fff8e1"
  semantic-block: "#da1e28"
  semantic-block-bg: "#fff1f1"
  semantic-info: "#0f62fe"
  semantic-info-bg: "#edf5ff"
  status-draft: "#8c8c8c"
  status-draft-bg: "#e0e0e0"
  status-published: "#24a148"
  status-published-bg: "#defbe6"
  status-disabled: "#525252"
  status-disabled-bg: "#f4f4f4"
  status-validating: "#0f62fe"
  status-validating-bg: "#edf5ff"
  medical-blue: "#0f62fe"
  medical-blue-deep: "#0043ce"
  monitor-canvas-dark: "#1f1633"
  monitor-night: "#150f23"
  monitor-ink-deep: "#1f1633"
  monitor-accent-lime: "#c2ef4e"
  monitor-accent-pink: "#fa7faa"
  monitor-accent-violet: "#6a5fc1"
  monitor-accent-violet-deep: "#422082"
  monitor-on-dark-muted: "#bdb8c0"
  monitor-hairline-violet: "#362d59"
  settlement-primary: "#533afd"
  settlement-primary-deep: "#4434d4"
  settlement-primary-soft: "#665efd"
  settlement-ink: "#0d253d"
  settlement-ink-secondary: "#273951"
  settlement-canvas-soft: "#f6f9fc"
  settlement-hairline: "#e3e8ee"

typography:
  display-xl:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 42px
    fontWeight: 300
    lineHeight: 1.20
    letterSpacing: 0
  display-lg:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 32px
    fontWeight: 300
    lineHeight: 1.25
    letterSpacing: 0
  headline:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 24px
    fontWeight: 400
    lineHeight: 1.33
    letterSpacing: 0
  subhead:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 20px
    fontWeight: 400
    lineHeight: 1.40
    letterSpacing: 0
  body-lg:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 16px
    fontWeight: 400
    lineHeight: 1.50
    letterSpacing: 0
  body:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.50
    letterSpacing: 0.16px
  body-emphasis:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 1.29
    letterSpacing: 0.16px
  caption:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 1.33
    letterSpacing: 0.32px
  button:
    fontFamily: "IBM Plex Sans, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.29
    letterSpacing: 0.16px
  code:
    fontFamily: "IBM Plex Mono, Monaco, Menlo, 'Courier New', monospace"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.50
    letterSpacing: 0
  tabular:
    fontFamily: "IBM Plex Mono, Monaco, Menlo, 'Courier New', monospace"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.50
    letterSpacing: -0.42px
    fontFeature: "tnum"

rounded:
  none: 0px
  xs: 2px
  sm: 4px
  md: 6px
  lg: 8px
  pill: 9999px

spacing:
  xxs: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  xxl: 48px
  section: 96px

components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button}"
    rounded: "{rounded.none}"
    padding: 12px 16px
  button-primary-hover:
    backgroundColor: "{colors.primary-hover}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button}"
    rounded: "{rounded.none}"
  button-primary-press:
    backgroundColor: "{colors.primary-press}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button}"
    rounded: "{rounded.none}"
  button-secondary:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.inverse-ink}"
    typography: "{typography.button}"
    rounded: "{rounded.none}"
    padding: 12px 16px
  button-ghost:
    backgroundColor: transparent
    textColor: "{colors.primary}"
    typography: "{typography.button}"
    rounded: "{rounded.none}"
    padding: 12px 16px
  button-danger:
    backgroundColor: "{colors.semantic-block}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button}"
    rounded: "{rounded.none}"
    padding: 12px 16px
  text-input:
    backgroundColor: "{colors.surface-1}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    padding: 11px 16px
    border: "1px solid {colors.hairline}"
  text-input-focused:
    backgroundColor: "{colors.surface-1}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    padding: 11px 16px
    border: "2px solid {colors.primary}"
  text-input-error:
    backgroundColor: "{colors.surface-1}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    padding: 11px 16px
    border: "2px solid {colors.semantic-block}"
  feature-card:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    padding: 24px
    border: "1px solid {colors.hairline}"
  stat-card:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    padding: 24px
    border: "1px solid {colors.hairline}"
  stat-card-value:
    typography: "{typography.display-lg}"
    textColor: "{colors.ink}"
  stat-card-label:
    typography: "{typography.caption}"
    textColor: "{colors.ink-muted}"
  result-level-tag-pass:
    backgroundColor: "{colors.semantic-pass-bg}"
    textColor: "{colors.semantic-pass}"
    typography: "{typography.body-emphasis}"
    rounded: "{rounded.xs}"
    padding: 2px 8px
  result-level-tag-warn:
    backgroundColor: "{colors.semantic-warn-bg}"
    textColor: "{colors.semantic-warn}"
    typography: "{typography.body-emphasis}"
    rounded: "{rounded.xs}"
    padding: 2px 8px
  result-level-tag-block:
    backgroundColor: "{colors.semantic-block-bg}"
    textColor: "{colors.semantic-block}"
    typography: "{typography.body-emphasis}"
    rounded: "{rounded.xs}"
    padding: 2px 8px
  rule-status-draft:
    backgroundColor: "{colors.status-draft-bg}"
    textColor: "{colors.status-draft}"
    typography: "{typography.body-emphasis}"
    rounded: "{rounded.xs}"
    padding: 2px 8px
  rule-status-published:
    backgroundColor: "{colors.status-published-bg}"
    textColor: "{colors.status-published}"
    typography: "{typography.body-emphasis}"
    rounded: "{rounded.xs}"
    padding: 2px 8px
  rule-status-disabled:
    backgroundColor: "{colors.status-disabled-bg}"
    textColor: "{colors.status-disabled}"
    typography: "{typography.body-emphasis}"
    rounded: "{rounded.xs}"
    padding: 2px 8px
  rule-status-validating:
    backgroundColor: "{colors.status-validating-bg}"
    textColor: "{colors.status-validating}"
    typography: "{typography.body-emphasis}"
    rounded: "{rounded.xs}"
    padding: 2px 8px
  top-nav:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    height: 48px
    borderBottom: "1px solid {colors.hairline}"
  sidebar:
    backgroundColor: "{colors.inverse-canvas}"
    textColor: "{colors.inverse-ink-muted}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    width: 200px
  sidebar-item:
    backgroundColor: transparent
    textColor: "{colors.inverse-ink-muted}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    padding: 12px 16px
  sidebar-item-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.body-emphasis}"
    rounded: "{rounded.none}"
    padding: 12px 16px
  footer:
    backgroundColor: "{colors.inverse-canvas}"
    textColor: "{colors.inverse-ink-muted}"
    typography: "{typography.caption}"
    rounded: "{rounded.none}"
    padding: 64px 32px
  monitor-card:
    backgroundColor: "{colors.monitor-ink-deep}"
    textColor: "{colors.inverse-ink}"
    typography: "{typography.body-lg}"
    rounded: "{rounded.md}"
    padding: 24px
    border: "1px solid {colors.monitor-hairline-violet}"
  monitor-stat-value:
    typography: "{typography.display-lg}"
    textColor: "{colors.monitor-accent-lime}"
  monitor-stat-label:
    typography: "{typography.caption}"
    textColor: "{colors.monitor-on-dark-muted}"
  monitor-code-block:
    backgroundColor: "{colors.monitor-night}"
    textColor: "{colors.inverse-ink}"
    typography: "{typography.code}"
    rounded: "{rounded.sm}"
    padding: 16px
  settlement-card:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.settlement-ink}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
    padding: 32px
    border: "1px solid {colors.settlement-hairline}"
  settlement-amount:
    typography: "{typography.tabular}"
    textColor: "{colors.settlement-ink}"
  settlement-header:
    backgroundColor: "linear-gradient(135deg, {colors.settlement-primary}, {colors.settlement-primary-deep})"
    textColor: "{colors.on-primary}"
    typography: "{typography.headline}"
    rounded: "{rounded.lg}"
    padding: 32px
---

## 1. Overview

HIS 动态规则中台的设计系统基于 **IBM Carbon Design System** 构建，融合了三种视觉风格以适配不同业务场景：

**管理后台（Carbon 风格）**：方正美学是核心 — 0px 圆角、hairline 1px 边框、无阴影。IBM Blue (`#0f62fe`) 作为唯一的品牌强调色，承载所有链接、主操作按钮和聚焦态。白色画布 + 浅灰 surface-1 + 炭黑 ink 覆盖 95% 的表面。层次通过 1px 发丝线和表面色变化传达，而非投影。IBM Plex Sans 在 42px 显示尺寸使用 weight 300（轻量显示是品牌声音），14px 正文使用 weight 400 + 0.16px letter-spacing。

**监控大屏（Sentry 风格）**：深紫午夜画布 (`#1f1633`)，Electric Lime (`#c2ef4e`) 高亮关键数据指标，Hot Pink (`#fa7faa`) 作为辅助标点色。数据密集型布局，卡片使用深色表面 + 紫色发丝线边框。代码/日志区域使用等宽字体 code-block 样式。

**结算页面（Stripe 风格）**：Indigo 紫 (`#533afd`) 作为金融主色，tabular 等宽数字排版用于金额展示，柔和的 canvas-soft (`#f6f9fc`) 背景，圆角卡片 (8px) 带来金融级精致感。渐变头部使用 primary → primary-deep 渐变。

**Key Characteristics:**
- Carbon 方正美学：所有管理后台组件 0px 圆角，深度通过表面色和发丝线传达
- 轻量显示字体：Plex Sans weight 300 用于 24px+ 标题，安静权威
- 单一品牌色：IBM Blue 承载所有主操作，避免多色干扰
- 医疗语义色扩展：PASS(绿)/WARN(黄)/BLOCK(红) 三级审核结果色，规则状态色
- 多主题隔离：管理后台(亮色)、监控大屏(暗色)、结算(金融) 通过 CSS 变量 + 作用域类名切换
- 4px 基准网格：所有间距对齐 4px 网格

## 2. Colors

### 2.1 管理后台 — Carbon 调色板

> 适用页面：规则管理、公式管理、规则组、用药审核、质控管理、DRG管理、系统管理、登录

#### Brand & Accent
- **IBM Blue** (`{colors.primary}` — `#0f62fe`): 唯一品牌强调色。链接、主按钮、聚焦环、CTA 横幅。
- **Blue Hover** (`{colors.primary-hover}` — `#0050e6`): 主按钮悬停态。
- **Blue Press** (`{colors.primary-press}` — `#0043ce`): 主按钮按下态。
- **Blue Deep** (`{colors.primary-deep}` — `#002d9c`): 深层强调，极少使用。

#### Surface
- **Canvas** (`{colors.canvas}` — `#ffffff`): 默认页面背景。
- **Surface 1** (`{colors.surface-1}` — `#f4f4f4`): 输入框、交替行条纹、微妙区域带。
- **Surface 2** (`{colors.surface-2}` — `#e0e0e0`): 禁用字段、分隔线填充。
- **Hairline** (`{colors.hairline}` — `#e0e0e0`): 卡片、输入框、分隔线的 1px 边框。
- **Hairline Strong** (`{colors.hairline-strong}` — `#161616`): 聚焦输入框的下划线（Carbon 签名聚焦处理）。

#### Text
- **Ink** (`{colors.ink}` — `#161616`): 所有标题和强调正文。
- **Ink Secondary** (`{colors.ink-secondary}` — `#525252`): 二级文字、副标题。
- **Ink Muted** (`{colors.ink-muted}` — `#8c8c8c`): 三级文字、禁用、辅助文本。

#### Inverse (侧边栏/深色区域)
- **Inverse Canvas** (`{colors.inverse-canvas}` — `#161616`): 侧边栏背景。
- **Inverse Surface 1** (`{colors.inverse-surface-1}` — `#262626`): 侧边栏悬停项。
- **Inverse Ink** (`{colors.inverse-ink}` — `#ffffff`): 侧边栏标题文字。
- **Inverse Ink Muted** (`{colors.inverse-ink-muted}` — `#c6c6c6`): 侧边栏正文。

### 2.2 HIS 业务语义色

#### 审核结果级别 (PASS/WARN/BLOCK)
- **PASS** — 文字 `{colors.semantic-pass}` `#24a148` / 背景 `{colors.semantic-pass-bg}` `#defbe6`
- **WARN** — 文字 `{colors.semantic-warn}` `#b28600` / 背景 `{colors.semantic-warn-bg}` `#fff8e1`
- **BLOCK** — 文字 `{colors.semantic-block}` `#da1e28` / 背景 `{colors.semantic-block-bg}` `#fff1f1`

#### 规则状态
- **草稿** — 文字 `{colors.status-draft}` `#8c8c8c` / 背景 `{colors.status-draft-bg}` `#e0e0e0`
- **已发布** — 文字 `{colors.status-published}` `#24a148` / 背景 `{colors.status-published-bg}` `#defbe6`
- **已停用** — 文字 `{colors.status-disabled}` `#525252` / 背景 `{colors.status-disabled-bg}` `#f4f4f4`
- **校验中** — 文字 `{colors.status-validating}` `#0f62fe` / 背景 `{colors.status-validating-bg}` `#edf5ff`

#### 医疗品牌色
- **Medical Blue** (`{colors.medical-blue}` — `#0f62fe`): 登录页渐变起点、医疗品牌标识。
- **Medical Blue Deep** (`{colors.medical-blue-deep}` — `#0043ce`): 登录页渐变终点。

### 2.3 监控大屏 — Sentry 调色板

> 适用页面：监控大屏、执行日志、性能指标、告警面板

- **Canvas Dark** (`{colors.monitor-canvas-dark}` — `#1f1633`): 主画布背景。
- **Night** (`{colors.monitor-night}` — `#150f23`): 卡片背景、代码块。
- **Ink Deep** (`{colors.monitor-ink-deep}` — `#1f1633`): 深层表面。
- **Accent Lime** (`{colors.monitor-accent-lime}` — `#c2ef4e`): 关键数据高亮、重要指标。
- **Accent Pink** (`{colors.monitor-accent-pink}` — `#fa7faa`): 辅助标点色、图表点。
- **Accent Violet** (`{colors.monitor-accent-violet}` — `#6a5fc1`): 标签芯片、辅助强调。
- **Accent Violet Deep** (`{colors.monitor-accent-violet-deep}` — `#422082`): 聚光灯卡片。
- **On Dark Muted** (`{colors.monitor-on-dark-muted}` — `#bdb8c0`): 二级文字、说明。
- **Hairline Violet** (`{colors.monitor-hairline-violet}` — `#362d59`): 深色卡片 1px 边框。

### 2.4 结算页面 — Stripe 调色板

> 适用页面：结算管理、DRG费用、费用明细

- **Settlement Primary** (`{colors.settlement-primary}` — `#533afd`): 金融主色。
- **Settlement Primary Deep** (`{colors.settlement-primary-deep}` — `#4434d4`): 渐变终点。
- **Settlement Primary Soft** (`{colors.settlement-primary-soft}` — `#665efd`): 悬停态。
- **Settlement Ink** (`{colors.settlement-ink}` — `#0d253d`): 深色正文。
- **Settlement Ink Secondary** (`{colors.settlement-ink-secondary}` — `#273951`): 二级文字。
- **Settlement Canvas Soft** (`{colors.settlement-canvas-soft}` — `#f6f9fc`): 柔和背景。
- **Settlement Hairline** (`{colors.settlement-hairline}` — `#e3e8ee`): 边框。

## 3. Typography

### 3.1 Font Family

- **主字体**: IBM Plex Sans — IBM 开源字体，几何感、略带人文主义，专为企业 UI 设计。SIL OFL 许可，可从 Google Fonts 获取。
- **中文回退**: PingFang SC (macOS) → Microsoft YaHei (Windows) → sans-serif
- **等宽字体**: IBM Plex Mono → Monaco → Menlo → Courier New

### 3.2 Hierarchy

| Token | Size | Weight | Line Height | Letter Spacing | Use |
|---|---|---|---|---|---|
| `{typography.display-xl}` | 42px | 300 | 1.20 | 0 | 大型页面标题 |
| `{typography.display-lg}` | 32px | 300 | 1.25 | 0 | 区域标题 |
| `{typography.headline}` | 24px | 400 | 1.33 | 0 | 卡片标题、页面子标题 |
| `{typography.subhead}` | 20px | 400 | 1.40 | 0 | 区域副标题 |
| `{typography.body-lg}` | 16px | 400 | 1.50 | 0 | 主要正文、表单标签 |
| `{typography.body}` | 14px | 400 | 1.50 | 0.16px | 默认正文、表格单元格 |
| `{typography.body-emphasis}` | 14px | 600 | 1.29 | 0.16px | 强调正文、选中标签 |
| `{typography.caption}` | 12px | 400 | 1.33 | 0.32px | 说明文字、元数据 |
| `{typography.button}` | 14px | 400 | 1.29 | 0.16px | 所有按钮标签 |
| `{typography.code}` | 14px | 400 | 1.50 | 0 | DRL/Aviator 代码 |
| `{typography.tabular}` | 14px | 400 | 1.50 | -0.42px | 金额、数字（等宽） |

### 3.3 Principles

- **轻量显示是品牌声音**：Plex Sans weight 300 用于 24px+ 标题，切换到 400/600 会失去品牌特征。
- **0.16px letter-spacing** 在 body 尺寸是 Carbon 精度细节，不要移除。
- **等宽 tabular** 用于金额展示：fontFeature tnum 确保数字对齐。
- **中文回退链**：Plex Sans 不含中文字符，必须指定 PingFang SC / Microsoft YaHei。

## 4. Component Stylings

### 4.1 Buttons

所有管理后台按钮 **0px 圆角**（Carbon 签名）。

| Variant | Background | Text | Border | Use |
|---------|-----------|------|--------|-----|
| Primary | `{colors.primary}` | `{colors.on-primary}` | none | 主操作（保存、提交、发布） |
| Primary Hover | `{colors.primary-hover}` | `{colors.on-primary}` | none | 悬停态 |
| Primary Press | `{colors.primary-press}` | `{colors.on-primary}` | none | 按下态 |
| Secondary | `{colors.ink}` | `{colors.inverse-ink}` | none | 次要操作 |
| Ghost | transparent | `{colors.primary}` | none | 第三级操作 |
| Danger | `{colors.semantic-block}` | `{colors.on-primary}` | none | 删除、停用 |

### 4.2 Cards

| Type | Background | Border | Shadow | Rounded | Use |
|------|-----------|--------|--------|---------|-----|
| Feature Card | `{colors.canvas}` | 1px `{colors.hairline}` | none | 0px | 功能展示、信息卡片 |
| Stat Card | `{colors.canvas}` | 1px `{colors.hairline}` | none | 0px | 仪表盘统计 |
| Monitor Card | `{colors.monitor-ink-deep}` | 1px `{colors.monitor-hairline-violet}` | none | 6px | 监控大屏数据卡片 |
| Settlement Card | `{colors.canvas}` | 1px `{colors.settlement-hairline}` | none | 8px | 结算信息卡片 |

### 4.3 Form Inputs

| State | Background | Border | Use |
|-------|-----------|--------|-----|
| Default | `{colors.surface-1}` | 1px `{colors.hairline}` | 默认输入框 |
| Focused | `{colors.surface-1}` | 2px `{colors.primary}` | 聚焦态（Carbon 签名） |
| Error | `{colors.surface-1}` | 2px `{colors.semantic-block}` | 校验失败 |

### 4.4 HIS Business Components

#### ResultLevelTag
审核结果级别标签，用于结算结果、用药审核、质控检查。

| Level | Background | Text | Example |
|-------|-----------|------|---------|
| PASS | `{colors.semantic-pass-bg}` | `{colors.semantic-pass}` | 通过 |
| WARN | `{colors.semantic-warn-bg}` | `{colors.semantic-warn}` | 警告 |
| BLOCK | `{colors.semantic-block-bg}` | `{colors.semantic-block}` | 拦截 |

#### RuleStatusBadge
规则/公式状态徽章。

| Status | Background | Text | Example |
|--------|-----------|------|---------|
| DRAFT | `{colors.status-draft-bg}` | `{colors.status-draft}` | 草稿 |
| PUBLISHED | `{colors.status-published-bg}` | `{colors.status-published}` | 已发布 |
| DISABLED | `{colors.status-disabled-bg}` | `{colors.status-disabled}` | 已停用 |
| VALIDATING | `{colors.status-validating-bg}` | `{colors.status-validating}` | 校验中 |

### 4.5 Navigation

| Element | Background | Text | Active |
|---------|-----------|------|--------|
| Top Nav | `{colors.canvas}` | `{colors.ink}` | IBM Blue underline |
| Sidebar | `{colors.inverse-canvas}` | `{colors.inverse-ink-muted}` | `{colors.primary}` bg + white text |
| Breadcrumb | `{colors.canvas}` | `{colors.ink-secondary}` | `{colors.ink}` |

## 5. Layout Principles

### 5.1 Spacing System

- **Base unit**: 4px（Carbon 4 像素网格）
- **Tokens**: xxs(4) · xs(8) · sm(12) · md(16) · lg(24) · xl(32) · xxl(48) · section(96)
- 卡片内边距：feature-card 24px，stat-card 24px
- 按钮内边距：12px vertical · 16px horizontal
- 表单输入内边距：11px vertical · 16px horizontal

### 5.2 Grid & Container

- Carbon 16 列网格（桌面），缩放到 8/4 列（平板/手机）
- 最大内容宽度 1584px
- 卡片网格：桌面 4 列、平板 2 列、手机 1 列
- 侧边栏固定宽度 200px

### 5.3 Whitespace Philosophy

Carbon 使用精确的 4 像素网格对齐作为留白系统。区域通过浅灰行 (`{colors.surface-1}`) 分隔，而非大垂直间距。内容设计上偏密集 — HIS 用户期望在一页上看到大量信息。

## 6. Depth & Elevation

| Level | Treatment | Use |
|-------|-----------|-----|
| 0 (flat) | 无阴影、无边框 | 正文、页脚 |
| 1 (hairline) | 1px `{colors.hairline}` 边框 | 功能卡片、输入框、列表项 |
| 2 (surface lift) | `{colors.surface-1}` 背景 | 交替行、悬停卡片 |
| 3 (focus ring) | 2px `{colors.primary}` 轮廓 | 聚焦输入框、聚焦按钮 |

管理后台 **不使用投影**。深度通过表面色变化和 1px 发丝线传达。

监控大屏同样不使用投影，深度通过深色表面层级传达（canvas-dark → ink-deep → night）。

## 7. Do's and Don'ts

### Do ✅
- 使用 0px 圆角（管理后台），这是 Carbon 的签名特征
- 使用 IBM Blue 作为唯一品牌色
- 使用 hairline 边框代替阴影
- 使用 weight 300 显示字体
- 使用 ResultLevelTag 展示审核结果
- 使用 RuleStatusBadge 展示规则状态
- 金额使用 tabular 等宽数字
- 监控大屏使用 Electric Lime 高亮关键指标

### Don't ❌
- 不要在管理后台使用圆角（除结算页面的 8px）
- 不要使用投影（管理后台和监控大屏均不使用）
- 不要使用多种品牌色（IBM Blue 是唯一主色）
- 不要在显示标题使用 weight 700（会失去品牌声音）
- 不要移除 body 的 0.16px letter-spacing
- 不要在金额展示使用比例字体（必须用 tabular）
- 不要在监控大屏使用白色背景
- 不要在结算页面使用 Carbon 的 0px 圆角（结算用 8px）

## 8. Responsive Behavior

| Breakpoint | Width | Columns | Sidebar | Use |
|-----------|-------|---------|---------|-----|
| Desktop XL | ≥1584px | 16 | 200px fixed | 全功能 |
| Desktop | ≥1200px | 12 | 200px fixed | 标准 |
| Tablet | ≥768px | 8 | collapsed | 简化 |
| Mobile | <768px | 4 | hidden | 最小可用 |

- 触控目标最小 44px × 44px
- 表格在平板以下切换为卡片列表
- 监控大屏仅支持 Desktop XL / Desktop 断点
- 结算页面在平板以下堆叠为单列

## 9. Agent Prompt Guide

### Quick Color Reference

```
管理后台主色: #0f62fe (IBM Blue)
通过/成功: #24a148 / 背景 #defbe6
警告: #b28600 / 背景 #fff8e1
拦截/错误: #da1e28 / 背景 #fff1f1
侧边栏背景: #161616
页面背景: #ffffff
内容区背景: #f4f4f4
边框: #e0e0e0
正文: #161616 / 二级 #525252 / 三级 #8c8c8c

监控大屏背景: #1f1633
监控关键指标: #c2ef4e (Lime)
监控辅助色: #fa7faa (Pink) / #6a5fc1 (Violet)

结算主色: #533afd (Indigo)
结算背景: #f6f9fc
```

### Ready-to-use Prompts

**管理后台页面：**
```
按照项目根目录 DESIGN.md 的 Carbon 管理后台风格，构建 XXX 页面。
要求：0px圆角、IBM Blue主色、hairline边框、无阴影、4px网格间距、
Plex Sans weight 300 标题、0.16px body letter-spacing。
使用 Element Plus 组件但覆盖主题变量。
```

**监控大屏页面：**
```
按照项目根目录 DESIGN.md 的 Sentry 监控大屏风格，构建 XXX 页面。
要求：深色画布(#1f1633)、Electric Lime(#c2ef4e)高亮关键数据、
深色卡片+紫色hairline边框、等宽代码区域、数据密集布局。
```

**结算页面：**
```
按照项目根目录 DESIGN.md 的 Stripe 结算风格，构建 XXX 页面。
要求：Indigo紫(#533afd)主色、8px圆角卡片、tabular等宽金额、
柔和背景(#f6f9fc)、渐变头部、金融级精致感。
使用 ResultLevelTag 展示 PASS/WARN/BLOCK 结果。
```

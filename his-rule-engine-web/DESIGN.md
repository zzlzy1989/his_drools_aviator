---
version: "2.0"
name: HIS-Rule-Engine-Design-System
description: >
  HIS 动态规则中台设计系统 — 基于 Apple 设计语言，融合医疗行业语义色，
  集成 Apple 暗色监控大屏风格和 Apple 金融结算风格。
  管理后台采用 Apple 极简美学（pill胶囊按钮、SF Pro负字距标题、Action Blue主色、大面积留白），
  监控大屏采用 Apple 暗色tile画布（Sky Link Blue高亮、数据密集），
  结算页面采用 Apple 金融精致感（tabular等宽数字排版、近黑色头部、18px圆角卡片）。

colors:
  primary: "#0066cc"
  primary-focus: "#0071e3"
  primary-on-dark: "#2997ff"
  on-primary: "#ffffff"
  ink: "#1d1d1f"
  ink-secondary: "#333333"
  ink-muted: "#7a7a7a"
  canvas: "#ffffff"
  canvas-parchment: "#f5f5f7"
  surface-pearl: "#fafafc"
  surface-tile-1: "#272729"
  surface-tile-2: "#2a2a2c"
  surface-tile-3: "#252527"
  surface-black: "#000000"
  surface-chip-translucent: "#d2d2d7"
  divider-soft: "#f0f0f0"
  hairline: "#e0e0e0"
  body-on-dark: "#ffffff"
  body-muted-on-dark: "#cccccc"
  semantic-pass: "#34c759"
  semantic-pass-bg: "#e8f9ed"
  semantic-warn: "#ff9f0a"
  semantic-warn-bg: "#fff5e5"
  semantic-block: "#ff3b30"
  semantic-block-bg: "#ffe5e3"
  semantic-info: "#0066cc"
  semantic-info-bg: "#e5f0ff"
  status-draft: "#7a7a7a"
  status-draft-bg: "#f5f5f7"
  status-published: "#34c759"
  status-published-bg: "#e8f9ed"
  status-disabled: "#333333"
  status-disabled-bg: "#f5f5f7"
  status-validating: "#0066cc"
  status-validating-bg: "#e5f0ff"
  medical-blue: "#0066cc"
  medical-blue-deep: "#004999"
  monitor-canvas-dark: "#1d1d1f"
  monitor-surface-card: "#1c1c1e"
  monitor-surface-elevated: "#2c2c2e"
  monitor-surface-elevated-2: "#3a3a3c"
  monitor-ink-on-dark: "#ffffff"
  monitor-ink-on-dark-secondary: "#cccccc"
  monitor-ink-on-dark-muted: "#8e8e93"
  monitor-ink-on-dark-faint: "#48484a"
  monitor-accent-lime: "#30d158"
  monitor-accent-green: "#34c759"
  monitor-accent-orange: "#ff9f0a"
  monitor-accent-pink: "#ff375f"
  monitor-accent-violet: "#bf5af2"
  monitor-accent-teal: "#64d2ff"
  monitor-hairline-dark: "#38383a"
  monitor-hairline-dark-subtle: "#2c2c2e"
  monitor-semantic-pass: "#30d158"
  monitor-semantic-pass-bg: "rgba(48, 209, 88, 0.15)"
  monitor-semantic-warn: "#ff9f0a"
  monitor-semantic-warn-bg: "rgba(255, 159, 10, 0.15)"
  monitor-semantic-block: "#ff453a"
  monitor-semantic-block-bg: "rgba(255, 69, 58, 0.15)"
  monitor-semantic-info: "#2997ff"
  monitor-semantic-info-bg: "rgba(41, 151, 255, 0.15)"
  settlement-primary: "#0066cc"
  settlement-primary-deep: "#004999"
  settlement-primary-soft: "#3399ff"
  settlement-ink: "#1d1d1f"
  settlement-ink-secondary: "#333333"
  settlement-canvas-soft: "#f5f5f7"
  settlement-hairline: "#e0e0e0"

typography:
  hero-display:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 56px
    fontWeight: 600
    lineHeight: 1.07
    letterSpacing: -0.28px
  display-lg:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 40px
    fontWeight: 600
    lineHeight: 1.10
    letterSpacing: 0
  display-md:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 34px
    fontWeight: 600
    lineHeight: 1.47
    letterSpacing: -0.374px
  lead:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 28px
    fontWeight: 400
    lineHeight: 1.14
    letterSpacing: 0.196px
  tagline:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 21px
    fontWeight: 600
    lineHeight: 1.19
    letterSpacing: 0.231px
  body-strong:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 17px
    fontWeight: 600
    lineHeight: 1.24
    letterSpacing: -0.374px
  body:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 17px
    fontWeight: 400
    lineHeight: 1.47
    letterSpacing: -0.374px
  caption:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.43
    letterSpacing: -0.224px
  caption-strong:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 1.29
    letterSpacing: -0.224px
  button:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 17px
    fontWeight: 400
    lineHeight: 1.0
    letterSpacing: 0
  button-utility:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.29
    letterSpacing: -0.224px
  code:
    fontFamily: "SF Mono, Monaco, Menlo, 'Courier New', monospace"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.50
    letterSpacing: 0
  tabular:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 17px
    fontWeight: 400
    lineHeight: 1.47
    letterSpacing: -0.374px
    fontFeature: tnum
  tabular-large:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 34px
    fontWeight: 600
    lineHeight: 1.0
    letterSpacing: -0.374px
    fontFeature: tnum
  fine-print:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 1.0
    letterSpacing: -0.12px
  nav-link:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 1.0
    letterSpacing: -0.12px
  stat-value:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 48px
    fontWeight: 600
    lineHeight: 1.0
    letterSpacing: -0.28px

rounded:
  none: 0px
  xs: 5px
  sm: 8px
  md: 11px
  lg: 18px
  pill: 9999px
  full: 9999px

spacing:
  xxs: 4px
  xs: 8px
  sm: 12px
  md: 17px
  lg: 24px
  xl: 32px
  xxl: 48px
  section: 80px

components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: 11px 22px
  button-primary-focus:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.pill}"
  button-primary-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.pill}"
  button-secondary-pill:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.primary}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: 11px 22px
  button-ghost:
    backgroundColor: transparent
    textColor: "{colors.primary}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: 11px 22px
  button-danger:
    backgroundColor: "{colors.semantic-block}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: 11px 22px
  button-dark-utility:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.body-on-dark}"
    typography: "{typography.button-utility}"
    rounded: "{rounded.sm}"
    padding: 8px 15px
  button-pearl-capsule:
    backgroundColor: "{colors.surface-pearl}"
    textColor: "{colors.ink-secondary}"
    typography: "{typography.caption}"
    rounded: "{rounded.md}"
    padding: 8px 14px
  text-input:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.sm}"
    padding: 8px 12px
    border: "1px solid {colors.hairline}"
  text-input-focused:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.sm}"
    padding: 8px 12px
    border: "2px solid {colors.primary-focus}"
  text-input-error:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.sm}"
    padding: 8px 12px
    border: "2px solid {colors.semantic-block}"
  feature-card:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
    padding: 24px
    border: "1px solid {colors.hairline}"
  stat-card:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
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
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  result-level-tag-warn:
    backgroundColor: "{colors.semantic-warn-bg}"
    textColor: "{colors.semantic-warn}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  result-level-tag-block:
    backgroundColor: "{colors.semantic-block-bg}"
    textColor: "{colors.semantic-block}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  rule-status-draft:
    backgroundColor: "{colors.status-draft-bg}"
    textColor: "{colors.status-draft}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  rule-status-published:
    backgroundColor: "{colors.status-published-bg}"
    textColor: "{colors.status-published}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  rule-status-disabled:
    backgroundColor: "{colors.status-disabled-bg}"
    textColor: "{colors.status-disabled}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  rule-status-validating:
    backgroundColor: "{colors.status-validating-bg}"
    textColor: "{colors.status-validating}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  global-nav:
    backgroundColor: "{colors.surface-black}"
    textColor: "{colors.body-on-dark}"
    typography: "{typography.nav-link}"
    height: 44px
  sub-nav-frosted:
    backgroundColor: "{colors.canvas-parchment}"
    textColor: "{colors.ink}"
    typography: "{typography.tagline}"
    height: 52px
  sidebar:
    backgroundColor: "{colors.canvas-parchment}"
    textColor: "{colors.ink-muted}"
    typography: "{typography.body}"
    rounded: "{rounded.none}"
    width: 220px
  sidebar-item:
    backgroundColor: transparent
    textColor: "{colors.ink-muted}"
    typography: "{typography.body}"
    rounded: "{rounded.md}"
    padding: 8px 14px
  sidebar-item-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.md}"
    padding: 8px 14px
  footer:
    backgroundColor: "{colors.canvas-parchment}"
    textColor: "{colors.ink-secondary}"
    typography: "{typography.fine-print}"
    padding: 64px
  monitor-card:
    backgroundColor: "{colors.monitor-surface-card}"
    textColor: "{colors.monitor-ink-on-dark}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
    padding: 24px
    border: "1px solid {colors.monitor-hairline-dark}"
  monitor-stat-value:
    typography: "{typography.stat-value}"
    textColor: "{colors.monitor-accent-lime}"
  monitor-stat-label:
    typography: "{typography.caption}"
    textColor: "{colors.monitor-ink-on-dark-muted}"
  monitor-code-block:
    backgroundColor: "{colors.surface-black}"
    textColor: "{colors.monitor-ink-on-dark}"
    typography: "{typography.code}"
    rounded: "{rounded.md}"
    padding: 16px
  settlement-card:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.settlement-ink}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
    padding: 32px
    border: "1px solid {colors.settlement-hairline}"
  settlement-amount:
    typography: "{typography.tabular-large}"
    textColor: "{colors.settlement-ink}"
  settlement-amount-label:
    typography: "{typography.caption}"
    textColor: "{colors.ink-muted}"
  settlement-header:
    backgroundColor: "{colors.surface-tile-1}"
    textColor: "{colors.body-on-dark}"
    typography: "{typography.lead}"
    rounded: "{rounded.lg}"
    padding: 32px

---

## 1. Overview

HIS 动态规则中台的设计系统基于 **Apple 设计语言** 构建，融合了三种视觉风格以适配不同业务场景：

**管理后台（Apple 极简风格）**：内容为先的极简美学是核心 — pill 胶囊按钮、SF Pro Display 负字距标题、大面积留白。Action Blue (`#0066cc`) 作为唯一的品牌交互色，承载所有链接、主操作按钮和聚焦态。白色画布 + 羊皮纸灰白 (`#f5f5f7`) + 近黑 ink (`#1d1d1f`) 覆盖 95% 的表面。层次通过表面色变化和交替明暗 tile 传达，而非投影。SF Pro Display 在 56px 显示尺寸使用 weight 600 + -0.28px letter-spacing（Apple 紧凑标题是品牌声音），17px 正文使用 weight 400 + -0.374px letter-spacing。

**监控大屏（Apple 暗色风格）**：近黑色画布 (`#1d1d1f`)，Sky Link Blue (`#2997ff`) 高亮关键数据指标，Apple Green (`#30d158`) 作为健康/通过标点色。数据密集型布局，卡片使用暗色表面 + 微妙分隔线。代码/日志区域使用 SF Mono 等宽字体。微妙色阶层次通过 2-3 个色阶差异传达深度。

**结算页面（Apple 金融风格）**：Action Blue (`#0066cc`) 作为金融主色，tabular 等宽数字排版（`tnum` OpenType 特性）用于金额展示，柔和的 canvas-parchment (`#f5f5f7`) 背景，18px 圆角卡片带来金融级精致感。近黑色头部区域创造沉稳金融权威感。

**Key Characteristics:**
- Apple 极简美学：pill 胶囊按钮、负字距标题、大面积留白
- 负字距标题是品牌声音：SF Pro Display weight 600 + 负字距用于 28px+ 标题
- 单一品牌色：Action Blue 承载所有主操作，避免多色干扰
- 医疗语义色扩展：PASS(绿)/WARN(橙)/BLOCK(红) 三级审核结果色，规则状态色
- 多主题隔离：管理后台(亮色)、监控大屏(暗色)、结算(金融) 通过 CSS 变量 + 作用域类名切换
- 4px 基准网格：所有间距对齐 4px 网格

## 2. Colors

### 2.1 管理后台 — Apple 调色板

> 适用页面：规则管理、公式管理、规则组、用药审核、质控管理、DRG管理、系统管理、登录

#### Brand & Accent
- **Action Blue** (`{colors.primary}` — `#0066cc`): 唯一品牌交互色。链接、主按钮、聚焦环、CTA。
- **Focus Blue** (`{colors.primary-focus}` — `#0071e3`): 键盘聚焦环，略亮于 Action Blue。
- **Sky Link Blue** (`{colors.primary-on-dark}` — `#2997ff`): 暗色表面上的链接色。

#### Surface
- **Canvas** (`{colors.canvas}` — `#ffffff`): 默认页面背景。
- **Canvas Parchment** (`{colors.canvas-parchment}` — `#f5f5f7`): Apple 标志性灰白，交替亮色 tile、footer。
- **Surface Pearl** (`{colors.surface-pearl}` — `#fafafc`): 次要按钮填充。
- **Surface Tile 1** (`{colors.surface-tile-1}` — `#272729`): 主暗色 tile 表面。
- **Surface Tile 2** (`{colors.surface-tile-2}` — `#2a2a2c`): 微亮一步的暗色 tile。
- **Surface Tile 3** (`{colors.surface-tile-3}` — `#252527`): 微暗一步的暗色 tile。
- **Surface Black** (`{colors.surface-black}` — `#000000`): 全局导航栏、视频播放器。
- **Hairline** (`{colors.hairline}` — `#e0e0e0`): 卡片、输入框、分隔线的 1px 边框。
- **Divider Soft** (`{colors.divider-soft}` — `#f0f0f0`): 次要分隔线。

#### Text
- **Ink** (`{colors.ink}` — `#1d1d1f`): 所有标题和强调正文，近黑而非纯黑。
- **Ink Secondary** (`{colors.ink-secondary}` — `#333333`): 二级文字。
- **Ink Muted** (`{colors.ink-muted}` — `#7a7a7a`): 三级文字、禁用、辅助文本。
- **Body On Dark** (`{colors.body-on-dark}` — `#ffffff`): 暗色表面上的文字。
- **Body Muted On Dark** (`{colors.body-muted-on-dark}` — `#cccccc`): 暗色表面上的次要文字。

### 2.2 HIS 业务语义色

#### 审核结果级别 (PASS/WARN/BLOCK)
- **PASS** — 文字 `{colors.semantic-pass}` `#34c759` / 背景 `{colors.semantic-pass-bg}` `#e8f9ed`
- **WARN** — 文字 `{colors.semantic-warn}` `#ff9f0a` / 背景 `{colors.semantic-warn-bg}` `#fff5e5`
- **BLOCK** — 文字 `{colors.semantic-block}` `#ff3b30` / 背景 `{colors.semantic-block-bg}` `#ffe5e3`

#### 规则状态
- **草稿** — 文字 `{colors.status-draft}` `#7a7a7a` / 背景 `{colors.status-draft-bg}` `#f5f5f7`
- **已发布** — 文字 `{colors.status-published}` `#34c759` / 背景 `{colors.status-published-bg}` `#e8f9ed`
- **已停用** — 文字 `{colors.status-disabled}` `#333333` / 背景 `{colors.status-disabled-bg}` `#f5f5f7`
- **校验中** — 文字 `{colors.status-validating}` `#0066cc` / 背景 `{colors.status-validating-bg}` `#e5f0ff`

#### 医疗品牌色
- **Medical Blue** (`{colors.medical-blue}` — `#0066cc`): 登录页渐变起点、医疗品牌标识。
- **Medical Blue Deep** (`{colors.medical-blue-deep}` — `#004999`): 登录页渐变终点。

### 2.3 监控大屏 — Apple 暗色调色板

> 适用页面：监控大屏、执行日志、性能指标、告警面板

- **Canvas Dark** (`{colors.monitor-canvas-dark}` — `#1d1d1f`): 主画布背景。
- **Surface Card** (`{colors.monitor-surface-card}` — `#1c1c1e`): 卡片背景。
- **Surface Elevated** (`{colors.monitor-surface-elevated}` — `#2c2c2e`): 提升表面。
- **Surface Elevated 2** (`{colors.monitor-surface-elevated-2}` — `#3a3a3c`): 二级提升表面。
- **Accent Lime** (`{colors.monitor-accent-lime}` — `#30d158`): 关键数据高亮、健康指标。
- **Accent Green** (`{colors.monitor-accent-green}` — `#34c759`): 通过状态。
- **Accent Orange** (`{colors.monitor-accent-orange}` — `#ff9f0a`): 警告状态。
- **Accent Pink** (`{colors.monitor-accent-pink}` — `#ff375f`): 错误/告警。
- **Accent Violet** (`{colors.monitor-accent-violet}` — `#bf5af2`): 标签芯片、辅助强调。
- **Accent Teal** (`{colors.monitor-accent-teal}` — `#64d2ff`): 信息辅助色。
- **Ink On Dark** (`{colors.monitor-ink-on-dark}` — `#ffffff`): 暗色表面主文字。
- **Ink On Dark Muted** (`{colors.monitor-ink-on-dark-muted}` — `#8e8e93`): 暗色表面辅助文字。
- **Hairline Dark** (`{colors.monitor-hairline-dark}` — `#38383a`): 暗色卡片 1px 边框。

#### 监控大屏语义色（半透明背景）
- **PASS** — 文字 `{colors.monitor-semantic-pass}` `#30d158` / 背景 `{colors.monitor-semantic-pass-bg}` `rgba(48,209,88,0.15)`
- **WARN** — 文字 `{colors.monitor-semantic-warn}` `#ff9f0a` / 背景 `{colors.monitor-semantic-warn-bg}` `rgba(255,159,10,0.15)`
- **BLOCK** — 文字 `{colors.monitor-semantic-block}` `#ff453a` / 背景 `{colors.monitor-semantic-block-bg}` `rgba(255,69,58,0.15)`
- **INFO** — 文字 `{colors.monitor-semantic-info}` `#2997ff` / 背景 `{colors.monitor-semantic-info-bg}` `rgba(41,151,255,0.15)`

### 2.4 结算页面 — Apple 金融调色板

> 适用页面：结算管理、DRG费用、费用明细

- **Settlement Primary** (`{colors.settlement-primary}` — `#0066cc`): 金融主色（与管理后台一致）。
- **Settlement Primary Deep** (`{colors.settlement-primary-deep}` — `#004999`): 深层强调。
- **Settlement Primary Soft** (`{colors.settlement-primary-soft}` — `#3399ff`): 柔和强调。
- **Settlement Ink** (`{colors.settlement-ink}` — `#1d1d1f`): 深色正文。
- **Settlement Ink Secondary** (`{colors.settlement-ink-secondary}` — `#333333`): 二级文字。
- **Settlement Canvas Soft** (`{colors.settlement-canvas-soft}` — `#f5f5f7`): 柔和背景。
- **Settlement Hairline** (`{colors.settlement-hairline}` — `#e0e0e0`): 边框。

## 3. Typography

### 3.1 Font Family

- **主字体**: SF Pro Display / SF Pro Text — Apple 专有字体，Display 优化 ≥ 19px，Text 优化 < 20px。
- **中文回退**: PingFang SC (macOS) → Microsoft YaHei (Windows) → sans-serif
- **等宽字体**: SF Mono → Monaco → Menlo → Courier New

### 3.2 Hierarchy

| Token | Size | Weight | Line Height | Letter Spacing | Feature | Use |
|---|---|---|---|---|---|---|
| `{typography.hero-display}` | 56px | 600 | 1.07 | -0.28px | — | 大型页面标题 |
| `{typography.display-lg}` | 40px | 600 | 1.10 | 0 | — | 区域标题 |
| `{typography.display-md}` | 34px | 600 | 1.47 | -0.374px | — | 卡片标题、页面子标题 |
| `{typography.lead}` | 28px | 400 | 1.14 | 0.196px | — | 产品 tile 副文案 |
| `{typography.tagline}` | 21px | 600 | 1.19 | 0.231px | — | 子标题、子导航 |
| `{typography.body-strong}` | 17px | 600 | 1.24 | -0.374px | — | 强调正文 |
| `{typography.body}` | 17px | 400 | 1.47 | -0.374px | — | 默认正文、表格单元格 |
| `{typography.caption}` | 14px | 400 | 1.43 | -0.224px | — | 说明文字、元数据 |
| `{typography.caption-strong}` | 14px | 600 | 1.29 | -0.224px | — | 状态标签、强调说明 |
| `{typography.button}` | 17px | 400 | 1.0 | 0 | — | 所有按钮标签 |
| `{typography.button-utility}` | 14px | 400 | 1.29 | -0.224px | — | 工具按钮标签 |
| `{typography.code}` | 14px | 400 | 1.50 | 0 | — | DRL/Aviator 代码 |
| `{typography.tabular}` | 17px | 400 | 1.47 | -0.374px | tnum | 金额、数字（等宽） |
| `{typography.tabular-large}` | 34px | 600 | 1.0 | -0.374px | tnum | 总金额展示 |
| `{typography.fine-print}` | 12px | 400 | 1.0 | -0.12px | — | 法律细文 |
| `{typography.nav-link}` | 12px | 400 | 1.0 | -0.12px | — | 导航链接 |
| `{typography.stat-value}` | 48px | 600 | 1.0 | -0.28px | — | 监控大屏统计数值 |

### 3.3 Principles

- **负字距是品牌声音**：SF Pro Display 在 34–56px 使用 -0.28px 到 -0.374px 字距，形成"Apple 紧凑"标题感，切换到正字距会失去品牌特征。
- **-0.374px letter-spacing** 在 body 尺寸是 Apple 精度细节，不要移除。
- **等宽 tabular** 用于金额展示：fontFeature tnum 确保数字对齐，这是金融页面的签名。
- **中文回退链**：SF Pro 不含中文字符，必须指定 PingFang SC / Microsoft YaHei。
- **17px 正文**：Apple 选择 17px 作为正文基准，比常见的 14/16px 更大更舒适。

## 4. Component Stylings

### 4.1 Buttons

所有管理后台按钮使用 **pill 胶囊圆角**（Apple 签名）。

| Variant | Background | Text | Border | Rounded | Use |
|---------|-----------|------|--------|---------|-----|
| Primary | `{colors.primary}` | `{colors.on-primary}` | none | pill | 主操作（保存、提交、发布） |
| Secondary Pill | `{colors.canvas}` | `{colors.primary}` | none | pill | 次要操作 |
| Ghost | transparent | `{colors.primary}` | none | pill | 第三级操作 |
| Danger | `{colors.semantic-block}` | `{colors.on-primary}` | none | pill | 删除、停用 |
| Dark Utility | `{colors.ink}` | `{colors.body-on-dark}` | none | sm | 暗色表面工具按钮 |
| Pearl Capsule | `{colors.surface-pearl}` | `{colors.ink-secondary}` | none | md | 次要胶囊按钮 |

### 4.2 Cards

| Type | Background | Border | Shadow | Rounded | Use |
|------|-----------|--------|--------|---------|-----|
| Feature Card | `{colors.canvas}` | 1px `{colors.hairline}` | none | 18px | 功能展示、信息卡片 |
| Stat Card | `{colors.canvas}` | 1px `{colors.hairline}` | none | 18px | 仪表盘统计 |
| Monitor Card | `{colors.monitor-surface-card}` | 1px `{colors.monitor-hairline-dark}` | none | 18px | 监控大屏数据卡片 |
| Settlement Card | `{colors.canvas}` | 1px `{colors.settlement-hairline}` | none | 18px | 结算信息卡片 |
| Settlement Header | `{colors.surface-tile-1}` | none | none | 18px | 结算头部区域 |

### 4.3 Form Inputs

| State | Background | Border | Rounded | Use |
|-------|-----------|--------|---------|-----|
| Default | `{colors.canvas}` | 1px `{colors.hairline}` | sm | 默认输入框 |
| Focused | `{colors.canvas}` | 2px `{colors.primary-focus}` | sm | 聚焦态 |
| Error | `{colors.canvas}` | 2px `{colors.semantic-block}` | sm | 校验失败 |
| Search | `{colors.canvas}` | none | pill | 搜索框 |

### 4.4 HIS Business Components

#### ResultLevelTag
审核结果级别标签，pill 胶囊形态，用于结算结果、用药审核、质控检查。

| Level | Background | Text | Rounded | Example |
|-------|-----------|------|---------|---------|
| PASS | `{colors.semantic-pass-bg}` | `{colors.semantic-pass}` | pill | 通过 |
| WARN | `{colors.semantic-warn-bg}` | `{colors.semantic-warn}` | pill | 警告 |
| BLOCK | `{colors.semantic-block-bg}` | `{colors.semantic-block}` | pill | 拦截 |

#### RuleStatusBadge
规则/公式状态徽章，pill 胶囊形态。

| Status | Background | Text | Rounded | Example |
|--------|-----------|------|---------|---------|
| 草稿 | `{colors.status-draft-bg}` | `{colors.status-draft}` | pill | 草稿 |
| 已发布 | `{colors.status-published-bg}` | `{colors.status-published}` | pill | 已发布 |
| 已停用 | `{colors.status-disabled-bg}` | `{colors.status-disabled}` | pill | 已停用 |
| 校验中 | `{colors.status-validating-bg}` | `{colors.status-validating}` | pill | 校验中 |

### 4.5 Navigation

#### Global Nav
- Background `{colors.surface-black}`, text `{colors.body-on-dark}`, height 44px。
- 纯黑背景，Apple 标志性纤薄导航。

#### Sub Nav Frosted
- Background `{colors.canvas-parchment}`, text `{colors.ink}`, height 52px。
- 羊皮纸色背景，产品子导航。

#### Sidebar
- Background `{colors.canvas-parchment}`, text `{colors.ink-muted}`, width 220px。
- Apple 风格不使用深色侧边栏，采用羊皮纸色背景。
- Active item: Background `{colors.primary}`, text `{colors.on-primary}`, rounded `{rounded.md}`。

### 4.6 Monitor Components

#### Monitor Stat Card
- 数值: `{typography.stat-value}` 48px + `{colors.monitor-accent-lime}` Apple Green。
- 标签: `{typography.caption}` 14px + `{colors.monitor-ink-on-dark-muted}`。

#### Monitor Code Block
- Background `{colors.surface-black}`, text `{colors.monitor-ink-on-dark}`, typography `{typography.code}` SF Mono。

### 4.7 Settlement Components

#### Settlement Amount
- `{typography.tabular-large}` 34px + tnum — 大号等宽金额是结算页面的视觉焦点。
- Settlement Amount Label: `{typography.caption}` 14px。

#### Settlement Header
- Background `{colors.surface-tile-1}` 近黑色，text `{colors.body-on-dark}` 白色。
- 创造沉稳金融权威感，与 Stripe 的渐变头部不同。

## 5. Layout

### 5.1 Spacing System

- **Base unit**: 4px
- **Tokens**: `{spacing.xxs}` 4px · `{spacing.xs}` 8px · `{spacing.sm}` 12px · `{spacing.md}` 17px · `{spacing.lg}` 24px · `{spacing.xl}` 32px · `{spacing.xxl}` 48px · `{spacing.section}` 80px

### 5.2 Grid & Container

- 居中容器，最大宽度约 980px（Apple 的经典内容宽度）。
- 卡片网格在桌面端 4-up，平板 2-up，移动 1-up。
- 产品 tile 全幅铺满，内容居中。

### 5.3 Whitespace Philosophy

Apple 使用大量留白作为设计语言的核心。区段之间通过颜色变化（白 ↔ 羊皮纸 ↔ 近黑）分隔，而非通过边框或分隔线。内容密度异常低 — 每个信息块有充足的呼吸空间。留白本身就是最强大的设计元素。

## 6. Elevation & Depth

### 管理后台

| Level | Treatment | Use |
|---|---|---|
| 0 (flat) | 无阴影，无边框 | 默认 — 正文、标题、footer |
| 1 (hairline) | 1px `{colors.hairline}` 边框 | 工具卡片、配置器芯片 |
| 2 (product shadow) | `rgba(0, 0, 0, 0.22) 3px 5px 30px` | 产品图像 — 系统中唯一的投影 |

### 监控大屏

| Level | Treatment | Use |
|---|---|---|
| 0 (flat) | 无阴影，无边框 | 默认暗色表面 |
| 1 (hairline) | 1px `{colors.monitor-hairline-dark}` 边框 | 监控卡片、代码块 |
| 2 (surface lift) | `{colors.monitor-surface-elevated}` 在 `{colors.monitor-canvas-dark}` 上 | 按钮、输入框 |
| 3 (glow) | `rgba(41, 151, 255, 0.12) 0 0 20px` | 关键数据高亮光晕 |

Apple 抵制装饰性阴影。深度通过表面色变化和颜色交替传达。管理后台系统中只有一个投影，专用于产品图像。

## 7. Shapes

### Border Radius Scale

| Token | Value | Use |
|---|---|---|
| `{rounded.none}` | 0px | 产品 tile（全幅区段） |
| `{rounded.xs}` | 5px | 小型徽章 |
| `{rounded.sm}` | 8px | 输入框、工具按钮 |
| `{rounded.md}` | 11px | 侧边栏项、胶囊按钮 |
| `{rounded.lg}` | 18px | 工具卡片、结算卡片 |
| `{rounded.pill}` | 9999px | 所有 CTA 按钮、标签芯片、搜索框 |

Apple 的按钮语法以 pill 胶囊为核心 — 所有主操作按钮使用 `{rounded.pill}`。卡片使用 `{rounded.lg}` 18px 创造柔和感。这与 IBM Carbon 的 0px 圆角截然不同。

## 8. Section Rhythm

Apple 的区段节奏是可预测的脉冲：

**管理后台**：亮色 hero → 暗色产品 tile → 亮色工具 tile → 暗色 tile → 羊皮纸色 footer

**监控大屏**：纯黑导航 → 近黑画布 → 暗色卡片网格 → 代码块 → 暗色 footer

**结算页面**：纯黑导航 → 近黑色头部 → 羊皮纸色画布 → 白色卡片 → 羊皮纸色 footer

## 9. Do's and Don'ts

### Do
- 使用 pill 胶囊按钮作为所有主操作
- 使用 SF Pro Display 负字距标题
- 使用大面积留白让内容呼吸
- 使用表面色变化传达层次
- 使用 tabular tnum 确保金额数字对齐
- 使用半透明背景的 pill 标签在暗色表面上

### Don't
- 不要使用 0px 圆角按钮（这是 IBM Carbon 风格，不是 Apple）
- 不要在标题上使用正字距（Apple 使用负字距）
- 不要使用装饰性投影（系统中只有一个投影）
- 不要使用渐变头部（Apple 不使用装饰性渐变）
- 不要在暗色表面上使用 Action Blue 链接（使用 Sky Link Blue）
- 不要使用深色侧边栏（Apple 使用羊皮纸色侧边栏）

## 10. Responsive Behavior

| Breakpoint | Width | Layout |
|---|---|---|
| Desktop | ≥ 980px | 4-up card grid, full sidebar |
| Tablet | 768–979px | 2-up card grid, collapsed sidebar |
| Mobile | < 768px | 1-up card stack, hidden sidebar |

- 导航在移动端折叠为汉堡菜单
- 产品 tile 在所有断点保持全幅
- 卡片网格从 4-up → 2-up → 1-up
- 侧边栏在平板及以下折叠

## 11. Agent Prompt Guide

### Quick Color Reference

**管理后台**: Action Blue `#0066cc` · Ink `#1d1d1f` · Canvas `#ffffff` · Parchment `#f5f5f7`

**监控大屏**: Sky Link Blue `#2997ff` · Lime `#30d158` · Canvas Dark `#1d1d1f` · Card `#1c1c1e`

**结算页面**: Action Blue `#0066cc` · Ink `#1d1d1f` · Parchment `#f5f5f7` · Tile `#272729`

### Ready-to-Use Prompts

- "Build a rule management page using Apple design: white canvas, pill buttons, SF Pro Display headlines with negative letter-spacing, Action Blue accents"
- "Create a monitoring dashboard with Apple dark tiles: near-black canvas, Sky Link Blue highlights, Apple Green stat values, SF Mono code blocks"
- "Design a settlement page with Apple financial aesthetic: parchment background, tabular tnum figures for amounts, near-black header, 18px rounded cards"

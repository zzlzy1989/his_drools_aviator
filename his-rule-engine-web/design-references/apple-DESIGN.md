---
version: "1.0"
name: Apple-Admin-design-reference
description: >
  HIS 管理后台设计参考 — 基于 Apple 设计语言。
  以内容为先的极简美学为核心：大面积留白、SF Pro Display 负字距标题、
  单一 Action Blue (#0066cc) 交互色、pill 胶囊按钮、
  交替明暗 tile 区段。UI 退隐让内容说话。
  适用于规则管理、公式管理、规则组、用药审核、质控管理、DRG管理、系统管理、登录。

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
  code:
    fontFamily: "SF Mono, Monaco, Menlo, 'Courier New', monospace"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.50
    letterSpacing: 0

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
  product-tile-light:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.display-lg}"
    rounded: "{rounded.none}"
    padding: 80px
  product-tile-parchment:
    backgroundColor: "{colors.canvas-parchment}"
    textColor: "{colors.ink}"
    typography: "{typography.display-lg}"
    rounded: "{rounded.none}"
    padding: 80px
  product-tile-dark:
    backgroundColor: "{colors.surface-tile-1}"
    textColor: "{colors.body-on-dark}"
    typography: "{typography.display-lg}"
    rounded: "{rounded.none}"
    padding: 80px
  store-utility-card:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    padding: 24px
  configurator-option-chip:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.caption}"
    rounded: "{rounded.pill}"
    padding: 12px 16px
  search-input:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.pill}"
    padding: 12px 20px
    height: 44px
  footer:
    backgroundColor: "{colors.canvas-parchment}"
    textColor: "{colors.ink-secondary}"
    typography: "{typography.fine-print}"
    padding: 64px
---

## Overview

Apple 的设计语言是**内容为先的极简主义**。每个页面由全幅 tile 区段堆叠而成 — 交替的白色与近黑色画布，每个 tile 居中放置一个标题、一行标语、两个蓝色胶囊 CTA 和一个产品渲染图。没有任何东西与内容竞争。排版自信而安静；颜色要么是纯白、羊皮纸色，要么是近黑色 tile；交互元素只有一个安静的蓝色。

密度异常低。每个 tile 约占一个视口高度，没有装饰性 chrome — 没有边框、没有渐变、没有装饰性框架、标题上没有阴影。层次仅在产品图像需要呼吸时出现（一个柔和的 `rgba(0, 0, 0, 0.22) 3px 5px 30px` 阴影）。结果是一个更像博物馆画廊的目录：墙壁消失，展品接管。

**Key Characteristics:**
- **内容优先**：UI 退隐让内容说话，大面积留白是核心设计语言
- **交替明暗 tile**：白色/羊皮纸色 ↔ 近黑色，颜色变化本身就是区段分隔
- **单一蓝色交互色**：`{colors.primary}` (#0066cc) 承载所有交互元素，没有第二品牌色
- **两种按钮语法**：蓝色胶囊 CTA (`{rounded.pill}`) 和紧凑工具矩形 (`{rounded.sm}`)
- **SF Pro Display + SF Pro Text**：显示尺寸使用负字距，形成 Apple 标志性"紧凑标题"感
- **极简阴影**：仅在产品图像需要呼吸时使用一个柔和投影
- **双层导航**：纤薄全局导航 + 产品子导航，右侧常驻主 CTA
- **区段节奏**：亮色 hero → 暗色产品 tile → 亮色工具 tile → 暗色 tile → 羊皮纸色 footer

## Colors

### Brand & Accent
- **Action Blue** (`{colors.primary}` — #0066cc): 唯一品牌交互色。所有文本链接、蓝色胶囊 CTA、聚焦环。按下态通过缩放变换而非色值变化实现。
- **Focus Blue** (`{colors.primary-focus}` — #0071e3): 略亮的蓝色，专用于键盘聚焦环 (`outline: 2px solid`)。
- **Sky Link Blue** (`{colors.primary-on-dark}` — #2997ff): 暗色表面上的链接色，Action Blue 在暗色 tile 上会消失。

### Surface
- **Pure White** (`{colors.canvas}` — #ffffff): 主画布。内容、工具卡片、商店 tile。
- **Parchment** (`{colors.canvas-parchment}` — #f5f5f7): Apple 标志性灰白。用于交替亮色 tile、footer、商店工具区段默认画布。
- **Pearl Button** (`{colors.surface-pearl}` — #fafafc): 近白色，用于次要"幽灵"按钮填充。
- **Near-Black Tile 1** (`{colors.surface-tile-1}` — #272729): 首页产品网格的主暗色 tile 表面。
- **Near-Black Tile 2** (`{colors.surface-tile-2}` — #2a2a2c): 微亮一步 — 用于暗色 tile 直接上下相邻时创造最微弱的分隔。
- **Near-Black Tile 3** (`{colors.surface-tile-3}` — #252527): 微暗一步 — 用于堆叠底部和嵌入视频/播放器框架。
- **Pure Black** (`{colors.surface-black}` — #000000): 保留给真正的虚空 — 视频播放器背景、全局导航栏背景。
- **Translucent Chip Gray** (`{colors.surface-chip-translucent}` — #d2d2d7): 圆形控制按钮的半透明灰色芯片基色，生产中 ~64% alpha。

### Text
- **Near-Black Ink** (`{colors.ink}` — #1d1d1f): 所有标题和段落的颜色。选择近黑而非纯黑，保持页面的摄影感而非印刷感。
- **Ink Secondary** (`{colors.ink-secondary}` — #333333): Pearl Button 表面上的正文。
- **Ink Muted** (`{colors.ink-muted}` — #7a7a7a): 禁用按钮文本和法律细文。
- **Body On Dark** (`{colors.body-on-dark}` — #ffffff): 暗色 tile 和全局导航栏上的所有文本。
- **Body Muted On Dark** (`{colors.body-muted-on-dark}` — #cccccc): 暗色 tile 上的次要文案。

### Hairlines & Borders
- **Divider Soft** (`{colors.divider-soft}` — #f0f0f0): 次要按钮的"边框"色调 — 作为环形阴影而非硬线。
- **Hairline** (`{colors.hairline}` — #e0e0e0): 商店工具卡片和配置器芯片的 1px 发丝线边框。

### HIS Business Semantic Colors
- **PASS** — 文字 `{colors.semantic-pass}` #34c759 / 背景 `{colors.semantic-pass-bg}` #e8f9ed
- **WARN** — 文字 `{colors.semantic-warn}` #ff9f0a / 背景 `{colors.semantic-warn-bg}` #fff5e5
- **BLOCK** — 文字 `{colors.semantic-block}` #ff3b30 / 背景 `{colors.semantic-block-bg}` #ffe5e3

### Rule Status Colors
- **草稿** — 文字 `{colors.status-draft}` #7a7a7a / 背景 `{colors.status-draft-bg}` #f5f5f7
- **已发布** — 文字 `{colors.status-published}` #34c759 / 背景 `{colors.status-published-bg}` #e8f9ed
- **已停用** — 文字 `{colors.status-disabled}` #333333 / 背景 `{colors.status-disabled-bg}` #f5f5f7
- **校验中** — 文字 `{colors.status-validating}` #0066cc / 背景 `{colors.status-validating-bg}` #e5f0ff

## Typography

### Font Family
- **Display**: `SF Pro Display, system-ui, -apple-system, sans-serif` — Apple 专有显示字体，优化 ≥ 19px。
- **Body / UI**: `SF Pro Text, system-ui, -apple-system, sans-serif` — 文本优化变体，用于 < 20px 的正文、说明、按钮、链接。
- **Code**: `SF Mono, Monaco, Menlo, Courier New, monospace` — DRL/Aviator 代码。
- **中文回退**: PingFang SC (macOS) → Microsoft YaHei (Windows) → sans-serif

### Hierarchy

| Token | Size | Weight | Line Height | Letter Spacing | Use |
|---|---|---|---|---|---|
| `{typography.hero-display}` | 56px | 600 | 1.07 | -0.28px | 大型页面标题；Apple 标志性紧凑字距 |
| `{typography.display-lg}` | 40px | 600 | 1.10 | 0 | Tile 标题、区域标题 |
| `{typography.display-md}` | 34px | 600 | 1.47 | -0.374px | 区段标题 |
| `{typography.lead}` | 28px | 400 | 1.14 | 0.196px | 产品 tile 副文案 |
| `{typography.tagline}` | 21px | 600 | 1.19 | 0.231px | 子 tile 标语、子导航分类名 |
| `{typography.body-strong}` | 17px | 600 | 1.24 | -0.374px | 行内强调 |
| `{typography.body}` | 17px | 400 | 1.47 | -0.374px | 默认正文 |
| `{typography.caption}` | 14px | 400 | 1.43 | -0.224px | 次要说明 |
| `{typography.caption-strong}` | 14px | 600 | 1.29 | -0.224px | 状态标签、强调说明 |
| `{typography.button}` | 17px | 400 | 1.0 | 0 | 主按钮标签 |
| `{typography.button-utility}` | 14px | 400 | 1.29 | -0.224px | 工具按钮标签 |
| `{typography.fine-print}` | 12px | 400 | 1.0 | -0.12px | 法律细文、元数据 |
| `{typography.nav-link}` | 12px | 400 | 1.0 | -0.12px | 导航链接 |
| `{typography.code}` | 14px | 400 | 1.50 | 0 | DRL/Aviator 代码 |

### Principles
- **负字距是品牌声音**：SF Pro Display 在 34–56px 使用 -0.28px 到 -0.374px 字距，形成"Apple 紧凑"标题感。
- **17px 正文**：Apple 选择 17px 作为正文基准，比常见的 14/16px 更大更舒适。
- **SF Pro Text 负字距**：正文 -0.374px 是 Apple 的精确细节，不要移除。
- **中文回退链**：SF Pro 不含中文字符，必须指定 PingFang SC / Microsoft YaHei。

## Layout

### Spacing System
- **Base unit**: 4px
- **Tokens**: `{spacing.xxs}` 4px · `{spacing.xs}` 8px · `{spacing.sm}` 12px · `{spacing.md}` 17px · `{spacing.lg}` 24px · `{spacing.xl}` 32px · `{spacing.xxl}` 48px · `{spacing.section}` 80px
- **Section padding**: 80px 在主要区段之间（桌面端），折叠到 48px 在移动端。
- **Card internal padding**: 24px 在工具卡片；80px 在产品 tile。

### Grid & Container
- 居中容器，最大宽度约 980px（Apple 的经典内容宽度）。
- 卡片网格在桌面端 4-up，平板 2-up，移动 1-up。
- 产品 tile 全幅铺满，内容居中。

### Whitespace Philosophy
Apple 使用大量留白作为设计语言的核心。区段之间通过颜色变化（白 ↔ 羊皮纸 ↔ 近黑）分隔，而非通过边框或分隔线。内容密度异常低 — 每个信息块有充足的呼吸空间。留白本身就是最强大的设计元素。

## Elevation & Depth

| Level | Treatment | Use |
|---|---|---|
| 0 (flat) | 无阴影，无边框 | 默认 — 正文、标题、footer |
| 1 (hairline) | 1px `{colors.hairline}` 边框 | 工具卡片、配置器芯片 |
| 2 (product shadow) | `rgba(0, 0, 0, 0.22) 3px 5px 30px` | 产品图像在表面上需要呼吸时 — 系统中唯一的投影 |

Apple 抵制装饰性阴影。深度通过表面色变化和颜色交替传达。系统中只有一个投影，专用于产品图像。

## Shapes

### Border Radius Scale

| Token | Value | Use |
|---|---|---|
| `{rounded.none}` | 0px | 产品 tile（全幅区段） |
| `{rounded.xs}` | 5px | 小型徽章 |
| `{rounded.sm}` | 8px | 输入框、工具按钮 |
| `{rounded.md}` | 11px | 侧边栏项、胶囊按钮 |
| `{rounded.lg}` | 18px | 工具卡片、配置器选项 |
| `{rounded.pill}` | 9999px | 所有 CTA 按钮、标签芯片、搜索框 |

Apple 的按钮语法以 pill 胶囊为核心 — 所有主操作按钮使用 `{rounded.pill}`。工具按钮使用较小的 `{rounded.sm}`。卡片使用 `{rounded.lg}` 18px 创造柔和感。

## Components

### Buttons

| Variant | Background | Text | Rounded | Use |
|---------|-----------|------|---------|-----|
| Primary | `{colors.primary}` | `{colors.on-primary}` | pill | 主操作（保存、提交、发布） |
| Secondary Pill | `{colors.canvas}` | `{colors.primary}` | pill | 次要操作（了解更多、取消） |
| Ghost | transparent | `{colors.primary}` | pill | 第三级操作 |
| Danger | `{colors.semantic-block}` | `{colors.on-primary}` | pill | 删除、停用 |
| Dark Utility | `{colors.ink}` | `{colors.body-on-dark}` | sm | 暗色表面上的工具按钮 |
| Pearl Capsule | `{colors.surface-pearl}` | `{colors.ink-secondary}` | md | 次要胶囊按钮 |

### Cards

| Type | Background | Border | Shadow | Rounded | Use |
|------|-----------|--------|--------|---------|-----|
| Feature Card | `{colors.canvas}` | 1px `{colors.hairline}` | none | 18px | 功能展示、信息卡片 |
| Stat Card | `{colors.canvas}` | 1px `{colors.hairline}` | none | 18px | 仪表盘统计 |
| Product Tile Light | `{colors.canvas}` | none | none | 0px | 亮色全幅区段 |
| Product Tile Parchment | `{colors.canvas-parchment}` | none | none | 0px | 羊皮纸色全幅区段 |
| Product Tile Dark | `{colors.surface-tile-1}` | none | none | 0px | 暗色全幅区段 |
| Store Utility Card | `{colors.canvas}` | 1px `{colors.hairline}` | none | 18px | 商店工具卡片 |

### Form Inputs

| State | Background | Border | Rounded | Use |
|-------|-----------|--------|---------|-----|
| Default | `{colors.canvas}` | 1px `{colors.hairline}` | sm | 默认输入框 |
| Focused | `{colors.canvas}` | 2px `{colors.primary-focus}` | sm | 聚焦态 |
| Error | `{colors.canvas}` | 2px `{colors.semantic-block}` | sm | 校验失败 |
| Search | `{colors.canvas}` | none | pill | 搜索框 |

### HIS Business Components

#### ResultLevelTag
审核结果级别标签，pill 胶囊形态。

| Level | Background | Text | Rounded |
|-------|-----------|------|---------|
| PASS | `{colors.semantic-pass-bg}` | `{colors.semantic-pass}` | pill |
| WARN | `{colors.semantic-warn-bg}` | `{colors.semantic-warn}` | pill |
| BLOCK | `{colors.semantic-block-bg}` | `{colors.semantic-block}` | pill |

#### RuleStatusBadge
规则/公式状态徽章，pill 胶囊形态。

| Status | Background | Text | Rounded |
|--------|-----------|------|---------|
| 草稿 | `{colors.status-draft-bg}` | `{colors.status-draft}` | pill |
| 已发布 | `{colors.status-published-bg}` | `{colors.status-published}` | pill |
| 已停用 | `{colors.status-disabled-bg}` | `{colors.status-disabled}` | pill |
| 校验中 | `{colors.status-validating-bg}` | `{colors.status-validating}` | pill |

### Navigation

**`global-nav`** — 全局顶部导航，纯黑背景。
- Background `{colors.surface-black}`, text `{colors.body-on-dark}`, height 44px。

**`sub-nav-frosted`** — 产品子导航，羊皮纸色背景。
- Background `{colors.canvas-parchment}`, text `{colors.ink}`, height 52px。

**`sidebar`** — 侧边栏，羊皮纸色背景（Apple 风格不使用深色侧边栏）。
- Background `{colors.canvas-parchment}`, text `{colors.ink-muted}`, width 220px。
- Active item: Background `{colors.primary}`, text `{colors.on-primary}`, rounded `{rounded.md}`。

### Footer
- Background `{colors.canvas-parchment}`, text `{colors.ink-secondary}`, typography `{typography.fine-print}`, padding 64px。

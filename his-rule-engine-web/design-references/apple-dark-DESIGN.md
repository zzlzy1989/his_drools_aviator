---
version: "1.0"
name: Apple-Dark-Monitor-design-reference
description: >
  HIS 监控大屏设计参考 — 基于 Apple 暗色 tile 设计语言。
  深邃的近黑色画布，Sky Link Blue (#2997ff) 高亮关键数据指标，
  Apple Green (#34c759) 作为辅助健康标点色。
  数据密集型布局，卡片使用暗色表面 + 微妙分隔线。
  代码/日志区域使用 SF Mono 等宽字体。
  适用于监控大屏、执行日志、性能指标、告警面板。

colors:
  primary: "#2997ff"
  primary-focus: "#0a84ff"
  primary-deep: "#0071e3"
  on-primary: "#ffffff"
  canvas-dark: "#1d1d1f"
  surface-tile-1: "#272729"
  surface-tile-2: "#2a2a2c"
  surface-tile-3: "#252527"
  surface-black: "#000000"
  surface-elevated: "#2c2c2e"
  surface-elevated-2: "#3a3a3c"
  surface-card: "#1c1c1e"
  ink-on-dark: "#ffffff"
  ink-on-dark-secondary: "#cccccc"
  ink-on-dark-muted: "#8e8e93"
  ink-on-dark-faint: "#48484a"
  accent-lime: "#30d158"
  accent-green: "#34c759"
  accent-orange: "#ff9f0a"
  accent-pink: "#ff375f"
  accent-violet: "#bf5af2"
  accent-indigo: "#5e5ce6"
  accent-teal: "#64d2ff"
  hairline-dark: "#38383a"
  hairline-dark-subtle: "#2c2c2e"
  semantic-pass: "#30d158"
  semantic-pass-bg: "rgba(48, 209, 88, 0.15)"
  semantic-warn: "#ff9f0a"
  semantic-warn-bg: "rgba(255, 159, 10, 0.15)"
  semantic-block: "#ff453a"
  semantic-block-bg: "rgba(255, 69, 58, 0.15)"
  semantic-info: "#2997ff"
  semantic-info-bg: "rgba(41, 151, 255, 0.15)"

typography:
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
  headline:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 28px
    fontWeight: 600
    lineHeight: 1.14
    letterSpacing: 0.196px
  subhead:
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
  stat-value:
    fontFamily: "SF Pro Display, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 48px
    fontWeight: 600
    lineHeight: 1.0
    letterSpacing: -0.28px
  stat-label:
    fontFamily: "SF Pro Text, system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.43
    letterSpacing: -0.224px
  code:
    fontFamily: "SF Mono, Monaco, Menlo, 'Courier New', monospace"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.50
    letterSpacing: 0
  code-strong:
    fontFamily: "SF Mono, Monaco, Menlo, 'Courier New', monospace"
    fontSize: 14px
    fontWeight: 700
    lineHeight: 1.50
    letterSpacing: 0
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
  button-secondary-pill:
    backgroundColor: "{colors.surface-elevated}"
    textColor: "{colors.primary}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: 11px 22px
  button-ghost-on-dark:
    backgroundColor: transparent
    textColor: "{colors.primary}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: 11px 22px
  monitor-card:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.ink-on-dark}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
    padding: 24px
    border: "1px solid {colors.hairline-dark}"
  monitor-stat-card:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.ink-on-dark}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
    padding: 24px
    border: "1px solid {colors.hairline-dark}"
  monitor-stat-value:
    typography: "{typography.stat-value}"
    textColor: "{colors.accent-lime}"
  monitor-stat-label:
    typography: "{typography.stat-label}"
    textColor: "{colors.ink-on-dark-muted}"
  monitor-code-block:
    backgroundColor: "{colors.surface-black}"
    textColor: "{colors.ink-on-dark}"
    typography: "{typography.code}"
    rounded: "{rounded.md}"
    padding: 16px
  monitor-alert-card:
    backgroundColor: "{colors.semantic-block-bg}"
    textColor: "{colors.semantic-block}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    padding: 16px
  monitor-chart-card:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.ink-on-dark}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
    padding: 24px
    border: "1px solid {colors.hairline-dark}"
  pill-tag-pass:
    backgroundColor: "{colors.semantic-pass-bg}"
    textColor: "{colors.semantic-pass}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  pill-tag-warn:
    backgroundColor: "{colors.semantic-warn-bg}"
    textColor: "{colors.semantic-warn}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  pill-tag-block:
    backgroundColor: "{colors.semantic-block-bg}"
    textColor: "{colors.semantic-block}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  pill-tag-info:
    backgroundColor: "{colors.semantic-info-bg}"
    textColor: "{colors.semantic-info}"
    typography: "{typography.caption-strong}"
    rounded: "{rounded.pill}"
    padding: 4px 12px
  global-nav-dark:
    backgroundColor: "{colors.surface-black}"
    textColor: "{colors.ink-on-dark}"
    typography: "{typography.button-utility}"
    height: 44px
  search-input-dark:
    backgroundColor: "{colors.surface-elevated}"
    textColor: "{colors.ink-on-dark}"
    typography: "{typography.body}"
    rounded: "{rounded.pill}"
    padding: 12px 20px
    height: 44px
---

## Overview

Apple 的暗色设计语言将产品展示转化为沉浸式深色体验。近黑色 tile (`{colors.surface-tile-1}` ~ #272729) 作为主画布，Sky Link Blue (`{colors.primary}` — #2997ff) 作为唯一高亮色，Apple Green (`{colors.accent-lime}` — #30d158) 标记健康/通过指标。数据密集但不拥挤 — Apple 的暗色美学通过微妙的表面色差异和精确的间距维持可读性。

暗色表面上的层次通过微妙的色阶变化传达：从纯黑 (`#000000`) 到近黑 tile (`#272729`) 到提升表面 (`#2c2c2e`)，每一步仅 2-3 个色阶差异。卡片使用 `{colors.surface-card}` (#1c1c1e) 配合 `{colors.hairline-dark}` (#38383a) 1px 边框，在深色画布上创造可感知但不突兀的边界。

**Key Characteristics:**
- **近黑色画布**：`#1d1d1f` ~ `#272729` 范围的深色表面，不是纯黑而是近黑，保持"摄影感"
- **Sky Link Blue 高亮**：`#2997ff` 在暗色表面上清晰可辨，承载所有交互和数据高亮
- **Apple Green 健康指标**：`#30d158` 标记通过/健康状态，与 Apple 系统绿色一致
- **微妙色阶层次**：通过 2-3 个色阶差异传达深度，而非投影
- **SF Mono 代码区域**：等宽字体用于日志和规则代码，保持 Apple 一致性
- **pill 胶囊标签**：所有状态标签使用半透明背景 + pill 圆角
- **数据密集但不拥挤**：通过精确间距和表面色差异维持可读性

## Colors

### Brand & Accent
- **Sky Link Blue** (`{colors.primary}` — #2997ff): 暗色表面上的主交互色。所有链接、高亮、CTA。
- **Focus Blue** (`{colors.primary-focus}` — #0a84ff): 键盘聚焦环。
- **Deep Blue** (`{colors.primary-deep}` — #0071e3): 渐变终点、深层强调。

### Surface
- **Canvas Dark** (`{colors.canvas-dark}` — #1d1d1f): 主画布背景。
- **Tile 1** (`{colors.surface-tile-1}` — #272729): 主暗色 tile 表面。
- **Tile 2** (`{colors.surface-tile-2}` — #2a2a2c): 微亮一步的暗色 tile。
- **Tile 3** (`{colors.surface-tile-3}` — #252527): 微暗一步的暗色 tile。
- **Elevated** (`{colors.surface-elevated}` — #2c2c2e): 提升 surface，用于按钮、输入框。
- **Elevated 2** (`{colors.surface-elevated-2}` — #3a3a3c): 二级提升 surface。
- **Card** (`{colors.surface-card}` — #1c1c1e): 卡片背景，比画布略深。
- **Pure Black** (`{colors.surface-black}` — #000000): 代码块、视频播放器。

### Text
- **On Dark** (`{colors.ink-on-dark}` — #ffffff): 暗色表面上的主文字。
- **On Dark Secondary** (`{colors.ink-on-dark-secondary}` — #cccccc): 次要文字。
- **On Dark Muted** (`{colors.ink-on-dark-muted}` — #8e8e93): 辅助文字、说明。
- **On Dark Faint** (`{colors.ink-on-dark-faint}` — #48484a): 极淡文字、分隔线填充。

### Accent Colors
- **Lime** (`{colors.accent-lime}` — #30d158): 关键数据高亮、健康指标。
- **Green** (`{colors.accent-green}` — #34c759): 通过状态。
- **Orange** (`{colors.accent-orange}` — #ff9f0a): 警告状态。
- **Pink** (`{colors.accent-pink}` — #ff375f): 错误/告警。
- **Violet** (`{colors.accent-violet}` — #bf5af2): 标签芯片、辅助强调。
- **Indigo** (`{colors.accent-indigo}` — #5e5ce6): 图表点、辅助色。
- **Teal** (`{colors.accent-teal}` — #64d2ff): 信息辅助色。

### Hairlines
- **Hairline Dark** (`{colors.hairline-dark}` — #38383a): 暗色卡片 1px 边框。
- **Hairline Dark Subtle** (`{colors.hairline-dark-subtle}` — #2c2c2e): 更微妙的分隔线。

### Semantic
- **PASS** — 文字 `{colors.semantic-pass}` #30d158 / 背景 `{colors.semantic-pass-bg}` rgba(48,209,88,0.15)
- **WARN** — 文字 `{colors.semantic-warn}` #ff9f0a / 背景 `{colors.semantic-warn-bg}` rgba(255,159,10,0.15)
- **BLOCK** — 文字 `{colors.semantic-block}` #ff453a / 背景 `{colors.semantic-block-bg}` rgba(255,69,58,0.15)
- **INFO** — 文字 `{colors.semantic-info}` #2997ff / 背景 `{colors.semantic-info-bg}` rgba(41,151,255,0.15)

## Typography

### Font Family
- **Display**: `SF Pro Display, system-ui, -apple-system, sans-serif` — 标题。
- **Body / UI**: `SF Pro Text, system-ui, -apple-system, sans-serif` — 正文、说明、按钮。
- **Code**: `SF Mono, Monaco, Menlo, Courier New, monospace` — 日志、规则代码。
- **中文回退**: PingFang SC → Microsoft YaHei → sans-serif

### Hierarchy

| Token | Size | Weight | Line Height | Letter Spacing | Use |
|---|---|---|---|---|---|
| `{typography.display-lg}` | 40px | 600 | 1.10 | 0 | 大屏标题 |
| `{typography.display-md}` | 34px | 600 | 1.47 | -0.374px | 区段标题 |
| `{typography.headline}` | 28px | 600 | 1.14 | 0.196px | 卡片标题 |
| `{typography.subhead}` | 21px | 600 | 1.19 | 0.231px | 子标题 |
| `{typography.body-strong}` | 17px | 600 | 1.24 | -0.374px | 强调正文 |
| `{typography.body}` | 17px | 400 | 1.47 | -0.374px | 默认正文 |
| `{typography.caption}` | 14px | 400 | 1.43 | -0.224px | 说明文字 |
| `{typography.caption-strong}` | 14px | 600 | 1.29 | -0.224px | 状态标签 |
| `{typography.stat-value}` | 48px | 600 | 1.0 | -0.28px | 统计数值 |
| `{typography.stat-label}` | 14px | 400 | 1.43 | -0.224px | 统计标签 |
| `{typography.code}` | 14px | 400 | 1.50 | 0 | 代码/日志 |
| `{typography.code-strong}` | 14px | 700 | 1.50 | 0 | 高亮代码关键词 |

## Layout

### Spacing System
- **Base unit**: 4px
- **Tokens**: 与管理后台一致，4px 基准网格。
- **Card padding**: 24px 在监控卡片；16px 在告警卡片。
- **Section padding**: 80px 在主要区段之间。

### Grid & Container
- 监控大屏使用全幅布局，内容区域最大宽度 1440px。
- 统计卡片网格在桌面端 4-up，平板 2-up。
- 图表卡片占据 2 列宽度。

### Whitespace Philosophy
暗色表面上的留白与亮色不同 — 暗色画布"吸收"留白，需要更明确的间距来维持可读性。Apple 的暗色美学通过微妙的表面色差异（而非大间距）传达层次。卡片之间使用 16-24px 间距，区段之间使用 48-80px。

## Elevation & Depth

| Level | Treatment | Use |
|---|---|---|
| 0 (flat) | 无阴影，无边框 | 默认暗色表面 |
| 1 (hairline) | 1px `{colors.hairline-dark}` 边框 | 监控卡片、代码块 |
| 2 (surface lift) | `{colors.surface-elevated}` 在 `{colors.canvas-dark}` 上 | 按钮、输入框 |
| 3 (glow) | `rgba(41, 151, 255, 0.12) 0 0 20px` | 关键数据高亮光晕 |

暗色表面上的深度通过表面色差异传达，而非投影。唯一的"光"效果是关键数据指标的微弱蓝色光晕。

## Components

### Monitor Cards
- **monitor-card**: 暗色卡片，`{colors.surface-card}` 背景 + `{colors.hairline-dark}` 边框 + `{rounded.lg}` 18px 圆角。
- **monitor-stat-card**: 统计卡片，数值使用 `{typography.stat-value}` 48px + `{colors.accent-lime}` 绿色。
- **monitor-code-block**: 代码块，`{colors.surface-black}` 背景 + `{typography.code}` SF Mono。
- **monitor-alert-card**: 告警卡片，`{colors.semantic-block-bg}` 半透明红色背景。
- **monitor-chart-card**: 图表卡片，与 monitor-card 相同结构。

### Status Tags (Pill)
- **pill-tag-pass**: `{colors.semantic-pass-bg}` + `{colors.semantic-pass}` + pill 圆角。
- **pill-tag-warn**: `{colors.semantic-warn-bg}` + `{colors.semantic-warn}` + pill 圆角。
- **pill-tag-block**: `{colors.semantic-block-bg}` + `{colors.semantic-block}` + pill 圆角。
- **pill-tag-info**: `{colors.semantic-info-bg}` + `{colors.semantic-info}` + pill 圆角。

### Buttons
- **button-primary**: `{colors.primary}` Sky Link Blue + pill 圆角。
- **button-secondary-pill**: `{colors.surface-elevated}` + `{colors.primary}` 文字 + pill 圆角。
- **button-ghost-on-dark**: 透明背景 + `{colors.primary}` 文字 + pill 圆角。

### Navigation
- **global-nav-dark**: `{colors.surface-black}` 纯黑背景，44px 高度。
- **search-input-dark**: `{colors.surface-elevated}` 背景 + pill 圆角。

---
version: "1.0"
name: Apple-Settlement-design-reference
description: >
  HIS 结算页面设计参考 — 基于 Apple 精致金融美学。
  Apple 的 Parchment 灰白画布 + SF Pro Display 负字距标题 + tabular 等宽数字排版。
  单一 Action Blue 交互色，pill 胶囊 CTA，18px 圆角卡片带来金融级精致感。
  金额展示使用 SF Pro Display + tnum 特性确保数字对齐。
  适用于结算管理、DRG费用、费用明细。

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
  canvas-soft: "#fafafc"
  surface-pearl: "#fafafc"
  surface-tile-1: "#272729"
  surface-black: "#000000"
  hairline: "#e0e0e0"
  divider-soft: "#f0f0f0"
  body-on-dark: "#ffffff"
  body-muted-on-dark: "#cccccc"
  settlement-accent: "#0066cc"
  settlement-accent-deep: "#004999"
  settlement-accent-soft: "#3399ff"
  settlement-ink: "#1d1d1f"
  settlement-ink-secondary: "#333333"
  settlement-canvas-soft: "#f5f5f7"
  settlement-hairline: "#e0e0e0"
  semantic-pass: "#34c759"
  semantic-pass-bg: "#e8f9ed"
  semantic-warn: "#ff9f0a"
  semantic-warn-bg: "#fff5e5"
  semantic-block: "#ff3b30"
  semantic-block-bg: "#ffe5e3"
  semantic-info: "#0066cc"
  semantic-info-bg: "#e5f0ff"

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
    typography: "{typography.headline}"
    rounded: "{rounded.lg}"
    padding: 32px
  settlement-detail-row:
    backgroundColor: "{colors.canvas}"
    textColor: "{colors.settlement-ink}"
    typography: "{typography.tabular}"
    rounded: "{rounded.none}"
    padding: 12px 0
    border: "1px solid {colors.divider-soft}"
  settlement-summary-card:
    backgroundColor: "{colors.canvas-parchment}"
    textColor: "{colors.settlement-ink}"
    typography: "{typography.body}"
    rounded: "{rounded.lg}"
    padding: 24px
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
  global-nav:
    backgroundColor: "{colors.surface-black}"
    textColor: "{colors.body-on-dark}"
    typography: "{typography.button-utility}"
    height: 44px
  sub-nav-frosted:
    backgroundColor: "{colors.canvas-parchment}"
    textColor: "{colors.ink}"
    typography: "{typography.subhead}"
    height: 52px
  footer:
    backgroundColor: "{colors.canvas-parchment}"
    textColor: "{colors.ink-secondary}"
    typography: "{typography.fine-print}"
    padding: 64px
---

## Overview

Apple 的金融级精致感来自极致的克制。结算页面延续 Apple 的核心美学 — Parchment 灰白画布、SF Pro Display 负字距标题、单一 Action Blue 交互色 — 但在数字展示上做了专门的精细化处理：`tabular` 等宽数字排版（`font-feature-settings: "tnum"`）确保金额列对齐，`tabular-large` 34px 大号等宽字体用于总金额展示。

与 Stripe 的渐变头部不同，Apple 的结算页面使用近黑色 (`{colors.surface-tile-1}`) 头部区域，创造沉稳的金融权威感。卡片使用 `{rounded.lg}` 18px 圆角 — Apple 标志性的柔和圆角 — 配合 1px 发丝线边框。金额行使用 `{typography.tabular}` 确保所有数字在垂直方向完美对齐。

**Key Characteristics:**
- **Parchment 灰白画布**：`#f5f5f7` 作为结算页面默认背景，比纯白更温暖
- **tabular 等宽数字**：所有金额使用 `tnum` OpenType 特性，确保数字列对齐
- **近黑色头部**：`#272729` 沉稳头部区域，金融权威感
- **18px 圆角卡片**：Apple 标志性柔和圆角，金融级精致
- **pill 胶囊 CTA**：Action Blue 胶囊按钮，与全系统一致
- **SF Pro Display 负字距**：标题使用 -0.374px 字距，Apple 紧凑感
- **单一蓝色交互**：`#0066cc` 承载所有交互，没有第二品牌色

## Colors

### Brand & Accent
- **Action Blue** (`{colors.primary}` — #0066cc): 主交互色。CTA、链接、聚焦环。
- **Focus Blue** (`{colors.primary-focus}` — #0071e3): 键盘聚焦环。
- **Settlement Accent** (`{colors.settlement-accent}` — #0066cc): 与主色一致，金融主色。
- **Settlement Accent Deep** (`{colors.settlement-accent-deep}` — #004999): 深层强调。
- **Settlement Accent Soft** (`{colors.settlement-accent-soft}` — #3399ff): 柔和强调。

### Surface
- **Canvas** (`{colors.canvas}` — #ffffff): 卡片背景。
- **Canvas Parchment** (`{colors.canvas-parchment}` — #f5f5f7): 页面背景、摘要卡片。
- **Canvas Soft** (`{colors.canvas-soft}` — #fafafc): 柔和表面。
- **Surface Pearl** (`{colors.surface-pearl}` — #fafafc): 次要按钮填充。
- **Surface Tile 1** (`{colors.surface-tile-1}` — #272729): 头部区域背景。
- **Surface Black** (`{colors.surface-black}` — #000000): 导航栏。

### Text
- **Ink** (`{colors.ink}` — #1d1d1f): 主文字色，近黑而非纯黑。
- **Ink Secondary** (`{colors.ink-secondary}` — #333333): 次要文字。
- **Ink Muted** (`{colors.ink-muted}` — #7a7a7a): 辅助文字、标签。
- **Body On Dark** (`{colors.body-on-dark}` — #ffffff): 暗色头部上的文字。

### Hairlines
- **Hairline** (`{colors.hairline}` — #e0e0e0): 卡片边框。
- **Divider Soft** (`{colors.divider-soft}` — #f0f0f0): 金额行分隔线。

### Semantic
- **PASS** — 文字 `{colors.semantic-pass}` #34c759 / 背景 `{colors.semantic-pass-bg}` #e8f9ed
- **WARN** — 文字 `{colors.semantic-warn}` #ff9f0a / 背景 `{colors.semantic-warn-bg}` #fff5e5
- **BLOCK** — 文字 `{colors.semantic-block}` #ff3b30 / 背景 `{colors.semantic-block-bg}` #ffe5e3

## Typography

### Font Family
- **Display**: `SF Pro Display, system-ui, -apple-system, sans-serif` — 标题、大号金额。
- **Body / UI**: `SF Pro Text, system-ui, -apple-system, sans-serif` — 正文、按钮。
- **Code**: `SF Mono, Monaco, Menlo, Courier New, monospace` — 公式代码。
- **中文回退**: PingFang SC → Microsoft YaHei → sans-serif

### Hierarchy

| Token | Size | Weight | Line Height | Letter Spacing | Feature | Use |
|---|---|---|---|---|---|---|
| `{typography.display-lg}` | 40px | 600 | 1.10 | 0 | — | 页面标题 |
| `{typography.display-md}` | 34px | 600 | 1.47 | -0.374px | — | 区段标题 |
| `{typography.headline}` | 28px | 600 | 1.14 | 0.196px | — | 卡片标题 |
| `{typography.subhead}` | 21px | 600 | 1.19 | 0.231px | — | 子标题 |
| `{typography.body-strong}` | 17px | 600 | 1.24 | -0.374px | — | 强调正文 |
| `{typography.body}` | 17px | 400 | 1.47 | -0.374px | — | 默认正文 |
| `{typography.tabular}` | 17px | 400 | 1.47 | -0.374px | tnum | 金额、数字（等宽） |
| `{typography.tabular-large}` | 34px | 600 | 1.0 | -0.374px | tnum | 总金额展示 |
| `{typography.caption}` | 14px | 400 | 1.43 | -0.224px | — | 说明文字 |
| `{typography.caption-strong}` | 14px | 600 | 1.29 | -0.224px | — | 状态标签 |
| `{typography.button}` | 17px | 400 | 1.0 | 0 | — | 按钮标签 |
| `{typography.fine-print}` | 12px | 400 | 1.0 | -0.12px | — | 法律细文 |

### Principles
- **tabular 等宽数字是金融签名**：所有金额单元格必须使用 `font-feature-settings: "tnum"` 确保数字列对齐。
- **tabular-large 用于总金额**：34px / weight 600 / tnum — 大号等宽金额是结算页面的视觉焦点。
- **负字距标题**：SF Pro Display 在 34px 使用 -0.374px 字距，Apple 紧凑感。
- **17px 正文**：比常见的 14px 更大更舒适，适合金融数据阅读。

## Components

### Settlement Cards
- **settlement-card**: 白色卡片，`{rounded.lg}` 18px 圆角，1px `{colors.hairline}` 边框，32px 内边距。
- **settlement-header**: 近黑色头部，`{colors.surface-tile-1}` 背景，白色文字，`{rounded.lg}` 圆角。
- **settlement-summary-card**: 羊皮纸色摘要卡片，`{colors.canvas-parchment}` 背景。
- **settlement-detail-row**: 金额行，`{typography.tabular}` 等宽字体，`{colors.divider-soft}` 分隔线。

### Amount Display
- **settlement-amount**: `{typography.tabular-large}` 34px + tnum，用于总金额。
- **settlement-amount-label**: `{typography.caption}` 14px，用于金额标签。

### Status Tags
- **result-level-tag-pass/warn/block**: pill 胶囊形态，半透明背景，与全系统一致。

### Buttons
- **button-primary**: Action Blue + pill 圆角，主操作。
- **button-secondary-pill**: 白色 + Action Blue 文字 + pill 圆角，次要操作。
- **button-ghost**: 透明 + Action Blue 文字 + pill 圆角，第三级操作。

### Navigation
- **global-nav**: 纯黑背景，44px 高度。
- **sub-nav-frosted**: 羊皮纸色背景，52px 高度。

# vite-plugin-auto-testid

> 构建期自动为 Vue SFC 模板中的 Element Plus / 项目自定义组件注入 `test-id` 属性，用于 UI 自动化录制与定位。

---

## 1. 文件结构

```
frontend/scripts/plugins/auto-testid/
├── index.js          # 插件入口（createAutoTestIdPlugin 工厂 + Vite 钩子）
├── tag-map.js        # 默认 tag → 元素类型映射表
├── transformer.js    # 核心：AST 遍历 + testid 注入 + tab-pane label slot 补全
├── helpers.js        # 工具函数：字符串规范化、AST props 解析、语义键解析
└── README.md         # 本文档
```

---

## 2. 快速使用

已在 `frontend/vite.config.js` 默认注册。本地 `npm run dev` 后，浏览器 F12 即可看到 DOM 上的 `test-id` 属性。

### 2.1 关闭插件

```bash
# 临时关闭一次
VITE_ENABLE_AUTO_TESTID=false npm run dev

# 或在 .env.local 内永久关闭
# VITE_ENABLE_AUTO_TESTID=false
```

### 2.2 打开调试日志

修改 `vite.config.js`：

```js
createAutoTestIdPlugin({
  enabled: enableAutoTestId,
  verbose: true,   // ← 打开
})
```

终端会输出形如：

```
[auto-testid] /xxx/views/ui-automation/test-cases/TestCaseManager.vue: 注入 42 个 test-id
```

---

## 3. testid 生成规则

格式：`{pageName}-{elementType}-{semantic|counter}`

| 段 | 来源 | 示例 |
|----|------|------|
| `pageName` | 文件路径（去掉 `src/views/`、`src/components/`、`index.vue`、`.vue` 后缀） | `ui-automation-test-cases-test-case-manager` |
| `elementType` | `tag-map.js` 中的映射（如 `el-button → btn`、`el-input → input`） | `btn` / `input` / `select` / `dialog` ... |
| `semantic` | 按元素类型优先级提取的语义键 | `submit-form` / `zh-a1b2c3` |
| `counter` | 当无语义键时使用的类型计数器（同类型内自增） | `1` / `2` / `3` |

### 3.1 各类元素的语义键提取优先级

| 元素类型 | 优先级（从高到低） |
|---------|-------------------|
| 表单控件（input/select/datepicker/switch/check/radio/tree 等） | `prop` → `field` → `name` → `v-model` 末段 → `@*-change` |
| 表单容器（form） | `:model` 末段 → 子文本 |
| 表单字段（formitem） | `label`（中文走哈希） → `prop` |
| 下拉选项（option） | `label`（中文走哈希） → `value` |
| 表格列（tablecol） | `prop` → `label`（中文走哈希） → `#header` slot 文本 |
| 按钮（btn） | `code` → `icon` → 文本（≤30 字符，中文走哈希） → `@click` 处理函数名 |
| 对话框/抽屉/气泡确认（dialog/drawer/popover/popconfirm） | `title`（中文走哈希） |
| 气泡提示（tooltip） | `content`（中文走哈希） |
| 表格（table） | `name` → `field` → `:table-data` 表达式末段 |
| 分页（pagination） | （无稳定语义，用计数器） |
| Tabs | `v-model` 末段 |
| Tab Pane | `label`（中文走哈希） → `name` |
| 菜单项（menuitem/submenu） | `index` → 子文本（中文走哈希） |
| 菜单容器（menu） | 子文本 → `v-model` 末段 |
| 下拉项（dropdownitem） | `command` → 子文本（中文走哈希） |
| 卡片（card） | `header`（中文走哈希） → `#header` slot → `title` → 子文本 |
| 描述列表（descriptions） | `title` → `col-{column}` |
| 描述项（descriptionsitem） | `label`（中文走哈希） → `prop` |
| 标签（tag） | 子文本（中文走哈希） → `type` |
| 折叠（collapse） | `v-model` 末段 |
| 折叠项（collapseitem） | `title`（中文走哈希） → `name` → `#title` slot |
| 面包屑项（breadcrumbitem） | `to` 末段 → 子文本 |
| 警告（alert） | `title`（中文走哈希） → `type` → 子文本 |
| 链接（link） | 子文本 → `href` 末段 |
| 文本（text） | 子文本 → `type` |
| 图标（icon） | `size-{N}` → `color` |
| 复选按钮（checkbutton）/ 单选按钮（radiobutton） | `label` → `value` |
| 列（col） | `span-{N}` → 子文本 |
| 行（row） | `gutter-{N}` |
| 进度条（progress） | `{N}pct` → `status` |
| 分隔线（divider） | `direction` → `content-position` |
| 空状态（empty） | `description` → 子文本 |
| 动态组件（dynamic，`<component :is>`） | `:is` 末段 → `is` 静态值 |
| 纯容器（dropdown/dropdownmenu/breadcrumb/scrollbar/layout/pagination） | （无稳定语义，用计数器） |

### 3.2 中文哈希

中文字符串通过 `md5(text).slice(0, 6)` 生成 `zh-{6位hex}` 稳定键。同一段中文每次构建产出同一个 testid，UI 自动化脚本可以放心复用。

### 3.3 冲突处理

同 SFC 内重复的 testid：第二次起追加 `-2`、`-3` 后缀，保证页面内唯一。

### 3.4 已有 testid 不覆盖

业务代码已经手写 `test-id="custom"` 的元素会被跳过，插件不会覆盖。

---

## 4. 配置项

| 选项 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | `boolean` | `true` | 关闭后 transform 直接返回 null，不修改任何文件 |
| `tagTypeMap` | `Record<string, string>` | `{}` | 与默认表合并的额外映射（用户优先） |
| `signName` | `string` | `'test-id'` | 注入的属性名 |
| `include` | `(RegExp\|string)[]` | `[/\.vue($\|\?)/]` | 仅处理匹配的文件 |
| `exclude` | `(RegExp\|string)[]` | `[/node_modules/]` | 跳过匹配的文件 |
| `verbose` | `boolean` | `false` | 打印每文件注入数量 |

---

## 5. 扩展标签映射

### 5.1 全项目通用（推荐）

直接编辑 `tag-map.js`，加新条目（**只需 kebab-case**，PascalCase 由 `normalizeTagName` 自动转换）：

```js
// tag-map.js
export const DEFAULT_TAG_TYPE_MAP = {
  // ... 既有条目
  'my-special-picker': 'select',
}
```

### 5.2 单项目临时

在 `vite.config.js` 传 `tagTypeMap`，会与默认表合并（同样只需 kebab-case）：

```js
createAutoTestIdPlugin({
  tagTypeMap: {
    'my-special-picker': 'select',
  },
})
```

### 5.3 元素类型选择建议

| 业务语义 | 建议类型 |
|---------|---------|
| 任何点击触发动作的组件 | `btn` |
| 文本输入框、数值输入框 | `input` |
| 任何单选下拉（普通、字典、自定义弹层） | `select` |
| 任何多级/树形下拉 | `tree-select` |
| 任何独立树形组件 | `tree` |
| 表格/列表 | `table` |
| 弹窗/抽屉 | `dialog` / `drawer` |

> 同语义组件用同一类型，UI 录制器可以统一识别。

---

## 6. 调试常见问题

### Q1：DOM 上没看到 `test-id`？

按顺序排查：
1. `.env` 是否设了 `VITE_ENABLE_AUTO_TESTID=false`？
2. 重启 dev server（Vite 加载 vite.config.js 是启动时一次性的，改插件配置必须重启）
3. 临时把 `verbose: true` 打开，看终端是否有 `[auto-testid] xxx: 注入 N 个` 日志
4. 检查该组件是否在 `tag-map.js` 里（没有就加进去）
5. 检查业务代码是否已经手写了 `test-id`（手写的不会被覆盖）

### Q2：testid 名字看起来怪（一长串 `zh-xxx`）？

中文文本被自动哈希了，这是预期行为，保证跨语言/换文案后 testid 仍稳定。如要可读 testid，给元素加上 `prop` / `name` / `code` 等英文标识。

### Q3：性能影响？

只在 SFC 模板上跑 AST 遍历，每个文件一次，dev 模式下首次解析才触发，HMR 不会重复。生产构建 testid 字面量会进 chunk，但体积可忽略（每个 testid ~30-50 字节）。

---

## 7. 不在本插件范围

- 不处理 JSX / TSX
- 不处理 render 函数返回的 VNode
- 不处理 Web Component / Custom Element
- 不动业务代码已手写的 `test-id`（约定大于代码）
- 不为 `<template #header><span>...</span></template>` 这类用户自定义具名 slot 内的元素自动包一层

如需上述能力，请单独提需求。

---

## 8. 内部模块边界

```
index.js          ─ 工厂函数 + Vite Plugin（transform 钩子、文件过滤、verbose 日志）
   ↓ 调用
transformer.js    ─ processTemplate / injectTabPaneLabelTestIds / collectTargetNodes / buildTestId
   ↓ 依赖
helpers.js        ─ 字符串规范化 / AST props 解析 / 语义键解析 / extractPageName / 中文哈希
   ↓ 依赖
tag-map.js        ─ DEFAULT_TAG_TYPE_MAP 常量（独立维护）
```

修改时建议：
- 加新组件 → 改 `tag-map.js`
- 改语义键提取逻辑 → 改 `helpers.js` 中的 `resolveSemanticKey`
- 改注入位置/格式 → 改 `transformer.js`
- 加全局开关 / 新选项 → 改 `index.js`

---

最后更新: 2026-06-11 | v1.4 (TestHub Platform) — 移除全部 jc- 项目自定义组件映射（12 项），只保留 Element Plus + 通用业务组件（dept-select / user-select / list-tree / v-tree-select）

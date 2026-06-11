# auto-testid Vite 插件 - 设计方案与使用手册

> **目标读者**：研发负责人 / 测试架构师 / 平台架构组
> **场景**：UI 自动化录制与定位环节中，自动为前端组件注入稳定 test-id 属性
> **状态**：✅ 已在本项目（TestHub Platform）落地并通过端到端验证

---

## 一、TL;DR

**auto-testid** 是一个 Vue 3 + Vite 7 专属的构建期插件。它在不改业务代码的前提下，自动扫描所有 `.vue` 模板的 AST，为 Element Plus 等组件注入语义稳定的 `test-id` 属性，让 UI 自动化脚本能用「按 test-id 定位」替代脆弱的 CSS 选择器或文本匹配。

核心数据（在本项目实测）：

| 指标 | 数值 |
|------|------|
| 覆盖组件类型 | 75 种（Element Plus 全集 + 通用业务组件） |
| 语义键类型 | 35+ 种（含 label / prop / v-model 末段 / 中文哈希 / 具名 slot 等） |
| 单页平均 testid 注入量 | 30-120 个（按页面密度差异） |
| 单页面改动 | 0 行业务代码 |
| 构建期耗时 | < 1ms / 文件（AST 遍历一次） |
| 生产构建体积影响 | 忽略不计（每个 testid ~30-50 字节字面量） |

---

## 二、背景与目标

### 2.1 业务痛点

UI 自动化录制/回放脚本长期面临三个老大难：

| 痛点 | 现象 | 根因 |
|------|------|------|
| **定位不稳定** | 改一次样式文案，用例全挂 | CSS 选择器 / XPath 依赖样式结构 / 文本值 |
| **录制噪声大** | 一打开页面录了几百个无关元素 | 缺少稳定的「测试专用锚点」 |
| **跨项目复用难** | 每个项目要重新训练录制器 | 每套前端组件库都需要一套定位约定 |

### 2.2 目标

1. **业务代码零侵入**——插件在构建期注入，业务代码不写一行
2. **test-id 稳定**——生成规则基于 `prop` / `label` / `v-model` 等语义属性，与样式/文案解耦
3. **跨项目通用**——同一插件可在任何 Vue 3 + Element Plus 项目复用

---

## 三、核心方案

### 3.1 架构定位

```
┌─────────────────────────────────────────────────────────────┐
│                     构建期（Vite transform 钩子）                │
│                                                             │
│   .vue 源文件 ──► parseSfc ──► 遍历 <template> AST             │
│                                       │                     │
│                                       ▼                     │
│                          collectTargetNodes                 │
│                          （按 tag-map 过滤）                  │
│                                       │                     │
│                                       ▼                     │
│                          resolveSemanticKey                 │
│                          （按元素类型提取 prop/label/...）      │
│                                       │                     │
│                                       ▼                     │
│                          在开标签内插入  test-id="..."        │
│                                       │                     │
│                                       ▼                     │
│   .vue 输出文件（含 test-id） ──► vite 后续 pipeline           │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼ 浏览器渲染后
┌─────────────────────────────────────────────────────────────┐
│                  后端 Playwright 定位                        │
│                                                             │
│   locator_strategy == 'test-id'                             │
│       └─► page.locator('[test-id="..."]')                   │
└─────────────────────────────────────────────────────────────┘
```

**关键特性：构建期一次性完成，不进运行时**——无需在 main.ts / App.vue 引入任何 runtime 库。

### 3.2 文件结构

```
frontend/scripts/plugins/auto-testid/
├── index.js          # 插件工厂 createAutoTestIdPlugin(options) + Vite 钩子
├── tag-map.js        # 默认 tag → 元素类型映射表（75 条）
├── transformer.js    # 核心：AST 遍历 + testid 注入 + tab-pane label slot 补全
├── helpers.js        # 工具函数：字符串规范化、AST props 解析、语义键解析
└── README.md         # 开发者文档（详细 API、配置项、扩展方式）
```

---

## 四、testid 生成规则

### 4.1 格式

```
{pageName}-{elementType}-{semantic|counter}
```

| 段 | 来源 | 示例 |
|----|------|------|
| `pageName` | 文件路径（去掉 `src/views/`、`src/components/`、`index.vue`、`.vue` 后缀） | `ui-automation-test-cases-test-case-manager` |
| `elementType` | `tag-map.js` 映射 | `btn` / `input` / `select` / `dialog` |
| `semantic` | 按元素类型优先级提取的语义键 | `submit-form` / `zh-a1b2c3` |
| `counter` | 同一类型无语义时按出现顺序计数 | `1` / `2` / `3` |

### 4.2 各元素类型语义键提取优先级

> 完整表格见 `frontend/scripts/plugins/auto-testid/README.md` 第 3.1 节

| 元素类型 | 优先级（从高到低） |
|---------|-------------------|
| 表单控件（input / select / datepicker / switch / radio / tree …） | `prop` → `field` → `name` → `v-model` 末段 → `@*-change` 事件 |
| 按钮（btn） | `code` → `icon` → 文本（≤30 字符，中文走哈希） → `@click` 处理函数名 |
| 对话框 / 抽屉 / 气泡确认（dialog / drawer / popover / popconfirm） | `title`（中文走哈希） |
| 表格列（tablecol） | `prop` → `label` → `#header` slot 文本 |
| 菜单项（menuitem / submenu） | `index` → 子文本（中文走哈希） |
| 折叠项（collapseitem） | `title` → `name` → `#title` slot |
| 卡片（card） | `header` attr → `#header` slot → `title` → 子文本 |
| 标签（tag） | 子文本（中文走哈希） → `type` |
| 警告（alert） | `title`（中文走哈希） → `type` → 子文本 |
| 动态组件（`<component :is>`） | `:is` 末段 → `is` 静态值 |
| 纯容器（dropdown / breadcrumb / scrollbar / layout / pagination） | （无稳定语义，用计数器） |

### 4.3 中文稳定哈希

中文字符串通过 `md5(text).slice(0, 6)` 生成 `zh-{6位hex}` 稳定键。同段中文每次构建产出同一个 testid，UI 自动化脚本可放心复用：

```html
<!-- 源文件 -->
<el-button>提交</el-button>

<!-- 构建后 -->
<el-button test-id="page-btn-zh-819767">提交</el-button>
```

### 4.4 冲突处理

- 同一 SFC 内重复 testid：第二次起追加 `-2`、`-3` 后缀
- 业务代码手写 `test-id="custom"` 的元素：插件跳过不覆盖
- 跨文件不冲突：`pageName` 含文件路径，天然唯一

---

## 五、现有项目接入情况（本项目 TestHub Platform）

### 5.1 集成步骤（已完成）

1. ✅ 插件文件已落地到 `frontend/scripts/plugins/auto-testid/`
2. ✅ `frontend/vite.config.js` 已注册插件（默认开启，`VITE_ENABLE_AUTO_TESTID=false` 可关闭）
3. ✅ 后端 `backend/apps/ui_automation/playwright_engine.py` 已对齐：策略 `test-id` 用 CSS 选择器 `[test-id="..."]` 定位
4. ✅ 后端 `backend/apps/core/management/commands/init_locator_strategies.py` 策略描述已同步

### 5.2 实际注入效果（端到端验证）

| 页面 | 注入 testid 数 |
|------|----------------|
| `views/ui-automation/test-cases/TestCaseManager.vue` | 121 |
| `views/projects/ProjectList.vue` | 40 |
| `views/reports/AiTestReport.vue` | 19 |
| `views/testcases/TestCaseDetail.vue` | 16 |
| `views/requirement-analysis/TaskDetail.vue` | 13 |
| `components/project/DynamicFormRenderer.vue` | 11 |
| `views/configuration/ConfigurationCenter.vue` | 1（仅 `<component :is>` wrapper） |

---

## 六、快速上手（5 分钟）

### 6.1 新项目接入清单

> 适用于任何 Vue 3 + Vite 7 + Element Plus 项目。

#### 前提条件

- Node.js ≥ 18（Vite 7 要求）
- Vue ≥ 3.3
- Vite ≥ 5
- Element Plus ≥ 2.0（可选；不引 Element Plus 也能用，只是 tag-map 里 `el-*` 条目都不生效）

#### Step 1：复制插件目录

```bash
# 在目标项目根目录下
mkdir -p frontend/scripts/plugins/auto-testid
cp -r <源项目>/frontend/scripts/plugins/auto-testid/* \
      frontend/scripts/plugins/auto-testid/
```

文件清单：

```
frontend/scripts/plugins/auto-testid/
├── index.js
├── tag-map.js
├── transformer.js
├── helpers.js
└── README.md
```

#### Step 2：注册到 vite.config.js

```js
// frontend/vite.config.js
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import createAutoTestIdPlugin from './scripts/plugins/auto-testid/index.js'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const enableAutoTestId = env.VITE_ENABLE_AUTO_TESTID !== 'false'

  return {
    plugins: [
      vue(),
      createAutoTestIdPlugin({
        enabled: enableAutoTestId,
        // verbose: true,    // 调试时打开，终端会刷每文件注入数
      }),
    ],
    // ... 其他配置
  }
})
```

#### Step 3：本地验证

```bash
cd frontend
npm run dev
```

打开任意页面 → F12 → 在 DOM 上应能看到 `test-id="xxx-btn-xxx"` 这样的属性。

#### Step 4：与后端联动（可选）

如果后端使用 Playwright 定位元素，在策略映射处加：

```python
elif locator_strategy.lower() == 'test-id':
    # 用 CSS 属性选择器（get_by_test_id 默认找 data-testid，我们用 test-id）
    safe_value = locator_value.replace("\\", "\\\\").replace('"', '\\"')
    locator = self.page.locator(f'[test-id="{safe_value}"]')
```

#### Step 5：关闭（任何时刻）

```bash
# 临时关闭一次
VITE_ENABLE_AUTO_TESTID=false npm run dev

# 或在 .env / .env.local 永久关闭
echo 'VITE_ENABLE_AUTO_TESTID=false' >> .env.local
```

### 6.2 验证清单

- [ ] 终端无报错
- [ ] dev 启动后 F12 看到 Element Plus 组件有 `test-id`
- [ ] 中文按钮的 testid 末段是 `zh-{6位hex}` 稳定键
- [ ] `VITE_ENABLE_AUTO_TESTID=false` 后无 `test-id` 注入
- [ ] `npm run build` 无报错，dist/ 产物含 `test-id` 字面量

---

## 七、新项目快速迁移（落地到其他项目）

### 7.1 迁移路径分类

| 目标项目情况 | 迁移策略 |
|--------------|----------|
| 全新 Vue 3 + Element Plus 项目 | 直接按第六章 5 步接入 |
| 已有 UI 自动化 + 旧定位策略 | 并行启用，旧策略保留，新用例用 `test-id` |
| 已用 `data-testid` 的项目 | 通过 `signName: 'data-testid'` 选项保持兼容（见 7.3） |
| 非 Element Plus（如 Ant Design Vue） | 改 `tag-map.js` 的 key，重新映射元素类型（见 7.4） |
| 多套前端组件库混用 | 在 `tag-map.js` 追加对应库的 tag（互不影响） |

### 7.2 团队级协作建议

1. **约定「不要手写 test-id」**——除非有特殊需求
2. **新组件开发时同步登记到 tag-map.js**——避免插件装好后组件没 testid
3. **CI 加一道卡**：`grep -r 'test-id="' src/views/ --include='*.vue' | wc -l` 应 > 0，确保没有页面被遗漏
4. **tag-map.js 进 Git review**——任何组件映射变更都要评审

### 7.3 切换属性名（如 `data-testid`）

如项目已有用 `data-testid` 的存量代码，可指定不同的属性名：

```js
createAutoTestIdPlugin({
  signName: 'data-testid',  // 默认 'test-id'
})
```

下游定位器也要同步：

```python
elif locator_strategy.lower() == 'data-testid':
    safe_value = locator_value.replace("\\", "\\\\").replace('"', '\\"')
    locator = self.page.locator(f'[data-testid="{safe_value}"]')
```

### 7.4 切换组件库（以 Ant Design Vue 为例）

编辑 `frontend/scripts/plugins/auto-testid/tag-map.js`，把 Element Plus 的 tag 替换/追加：

```js
export const DEFAULT_TAG_TYPE_MAP = {
  // Ant Design Vue
  'a-button': 'btn',
  'a-input': 'input',
  'a-select': 'select',
  'a-select-option': 'option',
  'a-form': 'form',
  'a-form-item': 'formitem',
  'a-table': 'table',
  'a-table-column': 'tablecol',
  'a-modal': 'dialog',
  'a-drawer': 'drawer',
  // ...
}
```

`helpers.js` 的语义键提取逻辑（按 `label` / `prop` / `v-model` 等）跨组件库通用，**无需修改**。

### 7.5 项目级临时扩展（不改 tag-map.js）

如只想在某个项目临时加几个组件映射，可在 `vite.config.js` 传 `tagTypeMap`：

```js
createAutoTestIdPlugin({
  tagTypeMap: {
    'my-special-picker': 'select',
    'MySpecialPicker': 'select',  // 也支持 PascalCase（依赖 normalizeTagName fallback）
  },
})
```

`tagTypeMap` 与默认表合并，用户优先。

---

## 八、配置项速查

| 选项 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | `boolean` | `true` | 关闭后 transform 直接返回 null，不修改任何文件 |
| `tagTypeMap` | `Record<string, string>` | `{}` | 与默认表合并的额外映射（用户优先） |
| `signName` | `string` | `'test-id'` | 注入的属性名 |
| `include` | `(RegExp\|string)[]` | `[/\.vue($|\?)/]` | 仅处理匹配的文件 |
| `exclude` | `(RegExp\|string)[]` | `[/node_modules/]` | 跳过匹配的文件 |
| `verbose` | `boolean` | `false` | 终端打印每文件注入数（调试用） |

完整配置参考 `frontend/scripts/plugins/auto-testid/index.js` 顶部 JSDoc。

---

## 九、调试与排障

### 9.1 打开调试日志

修改 `frontend/vite.config.js`：

```js
createAutoTestIdPlugin({
  enabled: enableAutoTestId,
  verbose: true,
})
```

终端会输出形如：

```
[auto-testid] /xxx/views/ui-automation/test-cases/TestCaseManager.vue: 注入 121 个 test-id
[auto-testid] /xxx/views/projects/ProjectList.vue: 注入 40 个 test-id
```

### 9.2 常见问题

| 问题 | 排查顺序 |
|------|---------|
| DOM 上看不到 test-id | ① 检查 `.env` 是否设了 `VITE_ENABLE_AUTO_TESTID=false` → ② 重启 dev server → ③ 打开 `verbose: true` 看终端 → ④ 检查组件是否在 `tag-map.js` → ⑤ 检查业务代码是否手写了 test-id |
| test-id 一长串 `zh-xxx` | 中文文本被自动哈希了，是预期行为；如要可读 testid，给元素加 `prop` / `name` / `code` 等英文标识 |
| 性能影响 | 仅在 SFC 模板上跑 AST 遍历，每个文件一次，dev 首次解析触发，HMR 不重复；生产构建 testid 字面量进 chunk，体积可忽略 |
| `<component :is>` 内部组件没有 test-id | 构建期只能给 wrapper 加，运行时 DOM 增强是另一个 PR 的范围（不在 v1.4） |
| 某个组件从未出现 test-id | 该组件不在 `tag-map.js`，加一行 kebab-case 条目即可；PascalCase 由 fallback 兜底，不需要重复登记 |

---

## 十、不在 v1.4 范围

| 项 | 说明 |
|----|------|
| JSX / TSX | 不处理 |
| Render 函数返回的 VNode | 不处理 |
| Web Component / Custom Element | 不处理 |
| `<component :is>` 内部组件 | 仅 wrapper 有 test-id，内部组件需运行时增强 |
| 给 `_is_truly_disabled` 加单测 | 当前以手动验证为主 |

---

## 十一、参考资源

- 源码：`frontend/scripts/plugins/auto-testid/`
- 开发者文档（详细 API）：`frontend/scripts/plugins/auto-testid/README.md`
- Vite Plugin API：https://vitejs.dev/guide/api-plugin.html
- Vue SFC AST：`vue/compiler-sfc` parse 输出的 `descriptor.template.ast`

---

最后更新: 2026-06-11 | v1.4 (TestHub Platform)
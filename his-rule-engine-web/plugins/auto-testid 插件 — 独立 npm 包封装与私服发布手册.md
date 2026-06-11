# auto-testid 插件 — 独立 npm 包封装与私服发布手册

> **目标读者**：前端架构组 / 平台基建负责人 / DevOps
> **场景**：把 `vite-plugin-auto-testid` 从本项目（TestHub Platform）抽成独立 npm 包，发布到团队私服，让其他 Vue 3 + Element Plus 项目能像装第三方包一样接入
> **状态**：✅ 方案已就绪、代码改动可在本仓库落地

---

## 一、TL;DR

把当前位于 `frontend/scripts/plugins/auto-testid/` 的插件**整体上移**到仓库根的 `packages/vite-plugin-auto-testid/`，加 `package.json` + `.npmrc` 即可发到 npm 私服。下游项目一行 `npm install -D @testhub/vite-plugin-auto-testid` 接入，业务代码不动。

三种发布路径，按团队基建完备度任选其一：

| 路径 | 适用场景 | 接入复杂度 | 团队成本 |
|------|---------|-----------|---------|
| **方案 A：npm 私服**（推荐） | 多项目复用 + 正式版本管理 | 低（用户） / 中（基建） | 需运维搭私服 |
| **方案 B：Git 路径安装** | 还没建私服，先在 2-3 个项目用起来 | 极低 | 零 |
| **方案 C：本地 file 协议** | 同一 monorepo 跨子包联调 | 极低 | 零 |

---

## 二、背景与目标

### 2.1 当前状态（v1.4）

```
testhub_platform/frontend/scripts/plugins/auto-testid/
├── index.js
├── tag-map.js
├── transformer.js
├── helpers.js
└── README.md
```

**问题**：

| 痛点 | 现象 |
|------|------|
| 物理位置嵌套深 | 其他项目想复用只能 `cp -r`，无法升级、无法锁定版本 |
| 无版本号 | 改了一行就污染下游所有用 `cp` 拉过去的项目 |
| 无 `package.json` | 没法走 `npm install` 链路，IDE 跳转、类型提示全断 |
| 无 changelog | 出问题难以回滚 |

### 2.2 目标

1. **抽离为独立 npm 包**——`@testhub/vite-plugin-auto-testid`
2. **支持私服发布**——团队内可走私有 npm registry
3. **保留项目内即开即用**——本项目（TestHub Platform）继续用本地路径，不强制走私服
4. **源码零改动**——纯位置/包元数据层面的调整，业务逻辑一行不动

---

## 三、方案 A：独立 npm 包 + 私服发布（推荐）

### 3.1 目录调整

将插件从 `frontend/scripts/plugins/auto-testid/` 上移到仓库根的 `packages/vite-plugin-auto-testid/`：

```
testhub_platform/
├── frontend/
│   └── scripts/
│       └── plugins/
│           └── auto-testid/             ← 删除（迁移后）
├── backend/
└── packages/                            ← 新增
    └── vite-plugin-auto-testid/         ← 新增包根
        ├── package.json                 ← 新增
        ├── .npmrc                       ← 新增（私服地址，不提交公共仓库）
        ├── README.md
        ├── index.js
        ├── tag-map.js
        ├── transformer.js
        └── helpers.js
```

源码文件**逐字搬迁**，无需改任何 import/export（已经是 ESM）。

### 3.2 `package.json` 模板

```json
{
  "name": "@testhub/vite-plugin-auto-testid",
  "version": "1.4.0",
  "description": "Vite 构建期自动为 Vue SFC 模板中的 Element Plus 等组件注入 test-id 属性，用于 UI 自动化录制与定位",
  "type": "module",
  "main": "./index.js",
  "exports": {
    ".": "./index.js",
    "./tag-map": "./tag-map.js"
  },
  "files": [
    "index.js",
    "tag-map.js",
    "transformer.js",
    "helpers.js",
    "README.md"
  ],
  "scripts": {
    "lint": "echo 'no lint yet'",
    "test": "echo 'no test yet'"
  },
  "keywords": [
    "vite",
    "vite-plugin",
    "vue",
    "vue3",
    "element-plus",
    "testid",
    "ui-automation",
    "playwright"
  ],
  "license": "MIT",
  "peerDependencies": {
    "vite": ">=5.0.0",
    "vue": ">=3.3.0"
  },
  "engines": {
    "node": ">=18"
  },
  "sideEffects": false
}
```

**关键字段说明**：

| 字段 | 作用 | 为什么这样写 |
|------|------|------------|
| `name` 用 `@testhub/` scope | 与社区包隔离 | 避免和 `vite-plugin-testid` 等同名包冲突 |
| `type: "module"` | 声明 ESM | 下游是 Vite 7，全部走 ESM |
| `exports` 双路径 | 暴露主入口 + 单独的 `tag-map` | 进阶用户可 `import { DEFAULT_TAG_TYPE_MAP } from '@testhub/vite-plugin-auto-testid/tag-map'` |
| `files` 白名单 | 决定 `npm publish` 上传哪些 | 排除 `.npmrc`、`.DS_Store`、`node_modules`、本地调试文件 |
| `peerDependencies` | 只声明 vue/vite | vite 自身传递依赖 `vue/compiler-sfc`，不重复打包 |
| `engines.node >= 18` | Vite 7 硬性要求 | 提前挡住 node 16/14 用户 |
| `sideEffects: false` | 让打包器放心 tree-shake | 本插件是纯构建期代码，运行时 0 副作用 |

### 3.3 `.npmrc`（私服地址，**包级**）

```ini
# packages/vite-plugin-auto-testid/.npmrc
registry=https://nexus.example.com/repository/npm-private/
```

> `.npmrc` 不应提交到公共 Git 仓库。仓库内放 `.npmrc.example` 模板，开发者本地复制为 `.npmrc`。

### 3.4 私服选型

| 私服 | 适用规模 | 部署成本 | 推荐场景 |
|------|---------|---------|---------|
| **Verdaccio** | 小团队（≤50 人） | 极低（一条 docker run） | 内部试水、不想运维重组件 |
| **Nexus Repository** | 中大型企业 | 中 | 已有 Nexus 全家桶、多语言制品统一管理 |
| **Artifactory** | 大型企业 | 高 | 已有 JFrog 平台、需细粒度权限 |
| **GitHub Packages / GitLab npm registry** | 远程团队 | 低 | 不想自建，CI 推 package registry 即可 |

**Verdaccio 最小启动**（本地自测用）：

```bash
npm i -g verdaccio
verdaccio
# 默认监听 0.0.0.0:4873
# 把 .npmrc 里 registry 改成 http://localhost:4873/ 即可联调
```

### 3.5 发版流程

```bash
# 1. 登录私服（首次）
cd packages/vite-plugin-auto-testid
npm login --registry=https://nexus.example.com/repository/npm-private/
# 输入 username / password / email
# 凭证写入 ~/.npmrc（全局，与项目内 .npmrc 分离）

# 2. 检查名字是否被占
npm view @testhub/vite-plugin-auto-testid --registry=https://nexus.example.com/repository/npm-private/
# 期望：返回 404 / E404，未发布过

# 3. 升版本（必须严格走 semver）
npm version patch   # 1.4.0 → 1.4.1（修 bug）
npm version minor   # 1.4.0 → 1.5.0（新功能、向下兼容）
npm version major   # 1.4.0 → 2.0.0（破坏性变更）
# npm version 会自动改 package.json + 打 git tag

# 4. 发布
npm publish --registry=https://nexus.example.com/repository/npm-private/
# scope 包首次发布需要：
npm publish --access public --registry=https://nexus.example.com/repository/npm-private/
```

### 3.6 下游项目接入

#### Step 1：私服 `.npmrc`（**项目根**）

```ini
# 下游项目 frontend/.npmrc
registry=https://nexus.example.com/repository/npm-private/
@testhub:registry=https://nexus.example.com/repository/npm-private/
```

第二行**关键**——告诉 npm：所有 `@testhub/*` 包**只**走私服，不要去公网 `registry.npmjs.org` 查。

#### Step 2：安装

```bash
cd <下游项目>/frontend
npm install -D @testhub/vite-plugin-auto-testid
```

#### Step 3：`vite.config.js` 接入

```js
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import createAutoTestIdPlugin from '@testhub/vite-plugin-auto-testid'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const enableAutoTestId = env.VITE_ENABLE_AUTO_TESTID !== 'false'

  return {
    plugins: [
      vue(),
      createAutoTestIdPlugin({
        enabled: enableAutoTestId,
        // tagTypeMap: { 'my-picker': 'select' },  // 项目级临时扩展
      }),
    ],
  }
})
```

#### Step 4：验证

```bash
# dev 启动后 F12 应能看到 test-id
npm run dev

# 关掉开关验证
VITE_ENABLE_AUTO_TESTID=false npm run dev
# F12 确认无 test-id
```

### 3.7 本项目（TestHub Platform）的过渡

本仓库已经用本地路径写进 `vite.config.js`，**保持不动**最简单：

```js
// frontend/vite.config.js
import createAutoTestIdPlugin from '../packages/vite-plugin-auto-testid/index.js'
```

> 把 import 路径从 `frontend/scripts/plugins/auto-testid/` 改成仓库根的 `packages/vite-plugin-auto-testid/`，等插件上移完成即可。

或走 **本地 file 协议**（方案 C）：

```json
// frontend/package.json
{
  "devDependencies": {
    "@testhub/vite-plugin-auto-testid": "file:../packages/vite-plugin-auto-testid"
  }
}
```

```js
// frontend/vite.config.js
import createAutoTestIdPlugin from '@testhub/vite-plugin-auto-testid'
```

这样本项目既能验证包能不能用，又不强制要求私服立刻可用。

---

## 四、方案 B：Git 路径安装（无私服）

适合：**团队还没建私服，2-3 个项目急着用**。

### 4.1 把插件推到独立 Git 仓库

```bash
# 假设推到 GitLab
cd packages/vite-plugin-auto-testid
git init
git remote add origin git@gitlab.example.com:testhub/vite-plugin-auto-testid.git
git add .
git commit -m "feat: initial release v1.4.0"
git tag v1.4.0
git push -u origin main --tags
```

### 4.2 下游项目 `package.json`

```json
{
  "devDependencies": {
    "@testhub/vite-plugin-auto-testid": "git+ssh://git@gitlab.example.com:testhub/vite-plugin-auto-testid.git#v1.4.0"
  }
}
```

```bash
npm install
```

**优点**：零私服基建成本，CI/CD 友好（CI runner 上有 SSH key 即可）
**缺点**：

- 没有版本元数据（`npm view` 用不了）
- 缓存命中靠 Git LFS / npm 自带 git cache，比 npm 私服慢
- tag 删了/分支被 force-push 会断

---

## 五、方案 C：本地 file 协议（monorepo）

适合：**几个子包都在同一仓库**，需要联调。

### 5.1 仓库根 `package.json`（可选 workspaces）

```json
{
  "name": "testhub-platform",
  "private": true,
  "workspaces": [
    "frontend",
    "packages/*"
  ]
}
```

### 5.2 本地引用

```json
// frontend/package.json
{
  "devDependencies": {
    "@testhub/vite-plugin-auto-testid": "*"
  }
}
```

```bash
# 仓库根
npm install
# npm 会自动创建 packages/vite-plugin-auto-testid → frontend/node_modules/@testhub/vite-plugin-auto-testid 的软链
```

> file 协议不写版本号时锁死为 `*`，本地开发最灵活；正式发版前切到私服。

---

## 六、方案选型决策表

| 团队现状 | 推荐方案 | 理由 |
|---------|---------|------|
| 已经搭了 Nexus / Artifactory | **A 私服** | 复用现有基建，权限/审计齐全 |
| 私服还在规划、≤3 个项目要接入 | **B Git 路径** | 零成本，1 天就能用 |
| 同一 monorepo 内多个 `frontend` 子项目 | **C file 协议 + workspaces** | 软链最快，热更新友好 |
| 多团队跨仓库 + 严格版本管理 | **A 私服 + semantic-release** | 正式合规，自动化 |
| 单个项目内部工具、不打算开源 | **B Git 路径** | 最简，私有仓库不暴露 |

**演进路径**：B → C → A（先用 Git 路径跑通，搬 monorepo 时用 file 协议，团队规模上来再上私服）。

---

## 七、进阶：CD 自动发版

私服到位后，可以接 `semantic-release` 让 CI 自动 `npm version` + `npm publish`：

### 7.1 安装（包内）

```bash
npm i -D semantic-release @semantic-release/changelog @semantic-release/git @semantic-release/npm
```

### 7.2 `release.config.cjs`

```js
module.exports = {
  branches: ['main'],
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/release-notes-generator',
    '@semantic-release/changelog',
    [
      '@semantic-release/npm',
      {
        npmPublish: true,
        tarballDir: 'dist',
      },
    ],
    [
      '@semantic-release/git',
      {
        assets: ['package.json', 'CHANGELOG.md'],
      },
    ],
  ],
}
```

### 7.3 GitLab CI 片段

```yaml
publish-npm:
  stage: release
  image: node:20
  script:
    - cd packages/vite-plugin-auto-testid
    - npm ci
    - npx semantic-release
  only:
    - main
  environment:
    name: npm-private
```

提交信息遵守 Conventional Commits 后即可全自动：

- `fix: xxx` → 自动 `1.4.0 → 1.4.1`
- `feat: xxx` → 自动 `1.4.0 → 1.5.0`
- `feat!: xxx` / `BREAKING CHANGE:` → 自动 `1.4.0 → 2.0.0`

---

## 八、发布前 checklist

### 8.1 包元数据

- [ ] `name` 用 `@testhub/` scope
- [ ] `version` 与当前代码状态一致（v1.4.0 起步）
- [ ] `description` 简洁明了（< 200 字）
- [ ] `keywords` 包含 `vite / vue / element-plus / testid / ui-automation`
- [ ] `license` 字段填了
- [ ] `repository` 字段指向 GitLab/GitHub 仓库（可选但推荐）
- [ ] `homepage` 指向内部文档（可选）

### 8.2 文件清单

- [ ] `index.js / tag-map.js / transformer.js / helpers.js` 全部在 `files` 内
- [ ] `README.md` 在 `files` 内
- [ ] `.npmrc` **不**在 `files` 内（避免泄露私服地址）
- [ ] 没有 `node_modules / dist / .DS_Store` 被误提交

### 8.3 依赖

- [ ] `peerDependencies` 写 `vite >= 5` 和 `vue >= 3.3`
- [ ] 没有把 `vue / vite` 写到 `dependencies`
- [ ] 没有把 `vue/compiler-sfc` 写到任何 deps（Vite 自带）

### 8.4 私服配置

- [ ] `.npmrc` 在 `packages/vite-plugin-auto-testid/` 内（不放在仓库根）
- [ ] `.npmrc` 在 `.gitignore` 或仅本地保留
- [ ] `.npmrc.example` 模板进了仓库，给后来人参考

### 8.5 发布

- [ ] 私服 `npm view @testhub/vite-plugin-auto-testid` 返回 404（未被占）
- [ ] `npm login` 成功，凭证在 `~/.npmrc`
- [ ] 本地 `npm pack --dry-run` 检查 tarball 内容
- [ ] 第一次发 `npm publish --access public`
- [ ] 后续发 `npm publish`

### 8.6 下游验证

- [ ] 私服 `.npmrc` 在下游项目根
- [ ] `npm install -D @testhub/vite-plugin-auto-testid` 成功
- [ ] `vite.config.js` 改用包名 import，本地 dev 服务可启动
- [ ] F12 检查 DOM 有 `test-id`
- [ ] `VITE_ENABLE_AUTO_TESTID=false` 可关闭

---

## 九、常见问题

### Q1：scope 包 `@testhub/` 必须 `--access public` 吗？

私服的 npm-private 仓库不强制 access 模式，但首次 publish 建议带上 `--access public` 显式声明，等私服管理员确认权限配置后再省掉。

### Q2：包内能不能带 TypeScript 类型？

可以，但需要先编译。当前 v1.4 是纯 JS + JSDoc，下游项目用 Vite 7 时 `import` 路径会被 Vite 自动解析，IDE 通过 JSDoc 给提示也够用。**真要 `.d.ts` 的话**：

1. 把源码改 `.ts`（5 个文件）
2. 配 `tsc --declaration --emitDeclarationOnly` 出 `.d.ts`
3. `package.json` 加 `"types": "./index.d.ts"`

但 v1.4 不在范围内，不建议现在做。

### Q3：本项目（TestHub Platform）一定要切到私服吗？

**不必要**。本地 file 协议或 `../packages/...` 相对路径都行。本项目作为插件的**原产地/上游**，不需要自己也去私服拉自己发布的包。

### Q4：私服地址要不要写进 README？

**不要**。README 是给所有人看的，私服地址是企业内部信息。放在 `.npmrc.example` 模板里更合适。

### Q5：上游改完插件，下游怎么升级？

下游项目 `package.json` 改版本号 → `npm install`。如果是 Git 路径（方案 B），改 tag 后下游重装：

```json
"@testhub/vite-plugin-auto-testid": "git+ssh://...#v1.5.0"
```

```bash
rm -rf node_modules/@testhub/vite-plugin-auto-testid
npm install
```

### Q6：能否在不改 package.json 的情况下给所有项目统一加 test-id 注入？

可以，走 **monorepo + 共享 vite.config.base.js**：

```js
// 仓库根 /config/vite.base.js
import createAutoTestIdPlugin from '@testhub/vite-plugin-auto-testid'

export const basePlugins = ({ env }) => [
  createAutoTestIdPlugin({ enabled: env.VITE_ENABLE_AUTO_TESTID !== 'false' }),
]
```

```js
// 各子项目 vite.config.js
import { basePlugins } from '../../../config/vite.base.js'
// 拼接到自己 plugins 数组里
```

---

## 十、不在本文档范围

| 项 | 说明 |
| --- | --- |
| 把插件改 TypeScript + 出 `.d.ts` | 留作 v2.0 重构；v1.4 纯 JS 够用 |
| 单元测试 / E2E | v1.4 范围外（前端工程暂无单测基建） |
| 私服高可用 / 权限模型 / 审计 | 走运维侧的 Nexus/Artifactory 规范 |
| 浏览器运行时增强（`<component :is>` 内部组件自动打 test-id） | 插件 v1.4 不支持，需独立 PR |
| 跨版本兼容（如 Vite 4、Vite 6） | 当前只承诺 Vite 5/6/7 |

---

## 十一、参考资源

- **本文档关联**：
  - 插件设计文档：[`auto-testid-vite-plugin.md`](./auto-testid-vite-plugin.md)
  - 插件开发者文档：`frontend/scripts/plugins/auto-testid/README.md`
- **官方文档**：
  - npm scope: https://docs.npmjs.com/cli/v10/using-npm/scope
  - npm peerDependencies: https://docs.npmjs.com/cli/v10/configuring-npm/package-json#peerdependencies
  - Verdaccio: https://verdaccio.org/docs/what-is-verdaccio
- **私服对比**：
  - Nexus Repository: https://www.sonatype.com/products/nexus-repository
  - Artifactory: https://jfrog.com/artifactory/
  - GitLab npm registry: https://docs.gitlab.com/ee/user/packages/npm_registry/

---

最后更新: 2026-06-11 | v1.0 (TestHub Platform)
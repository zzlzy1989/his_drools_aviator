# PLAN-20260522-001: 前端性能优化计划

> 计划 ID: PLAN-20260522-001
> 创建时间: 2026-05-22
> 状态: completed
> 完成时间: 2026-05-22
> 优先级: P1

---

## 1. 问题分析

### 1.1 当前症状

- 点击模块时短暂白屏
- 首次访问模块响应慢
- 路由切换时页面闪烁

### 1.2 根本原因

| 问题 | 原因 | 影响 |
|------|------|------|
| AntV X6 (419KB) | 规则流设计器组件过大 | 首屏加载延迟 |
| Monaco Editor | 代码编辑器体积大 | 懒加载时间长 |
| Element Plus | 全量导入未按需加载 | 初始包体积大 |
| 路由懒加载 | 每个模块独立 chunk | 首次访问需下载 |
| CSS 导入 | 全量 CSS 加载 | 样式加载慢 |

### 1.3 当前包体积分析

| 文件 | 大小 | 说明 |
|------|------|------|
| element-plus-CRaK-_FX.js | 1.1MB | Element Plus 全量 |
| Dashboard-C5wupIy8.js | 1.0MB | Dashboard 懒加载 |
| antv-x6-Bxmg6NS4.js | 419KB | 图编辑库 |
| FlowEditor-Dz99YWCo.js | 125KB | 规则流编辑器 |
| request-CvIn4Zu6.js | 43KB | API 请求 |

---

## 2. 优化方案

### 2.1 路由懒加载优化

**当前问题**: 每个路由组件独立打包，首次访问需下载

**优化方案**: 使用 `Promise.all` 并行预加载关键路由

```typescript
// router/index.ts
// 预加载关键组件
const preloadPromises = [
  import('@/views/dashboard/Dashboard.vue'),
  import('@/views/rule/RuleList.vue'),
]

// 路由保持懒加载，但利用空闲时间预加载
```

### 2.2 组件按需加载

**Element Plus 按需导入**
```typescript
// main.ts
import { ElButton, ElInput, ElTable, ElPagination } from 'element-plus'
// 只导入实际使用的组件
```

**AntV X6 按需导入**
```typescript
// FlowEditor 使用时再 import
const { Graph } = await import('@antv/x6')
```

### 2.3 路由预加载策略

```typescript
// 空闲时预加载下一模块
router.beforeEach((to) => {
  // 识别下一个可能访问的路由
  if (to.path === '/flow') {
    // 空闲时预加载 flow 相关组件
    requestIdleCallback(() => {
      import('@/views/flow/FlowEditor.vue')
    })
  }
})
```

### 2.4 加载状态优化

在路由切换时显示加载指示器，减少白屏感知：

```vue
<!-- App.vue -->
<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <div v-if="isLoading" class="loading-skeleton" />
      <component v-else :is="Component" />
    </transition>
  </router-view>
</template>
```

### 2.5 分层打包策略

优化 Rollup chunk 分割：

```typescript
// vite.config.ts
manualChunks: {
  'element-plus': ['element-plus'],           // Element Plus 单独
  'antv-x6': ['@antv/x6', '@antv/x6-vue-shape'], // 图编辑单独
  'monaco': ['monaco-editor'],                 // 编辑器单独
  'vue-core': ['vue', 'vue-router', 'pinia'],  // Vue 核心
}
```

### 2.6 预连接关键域名

```html
<!-- index.html -->
<link rel="preconnect" href="http://localhost:9000" />
```

---

## 3. 实施步骤

### Step 1: Element Plus 按需导入 ✅

- [x] 修改 `main.ts` 只导入使用的组件
- [x] 更新组件注册
- [x] 构建验证通过

### Step 2: 路由预加载配置 ✅

- [x] 添加 requestIdleCallback 预加载
- [x] 配置关键路由预加载
- [x] 路由守卫添加预加载逻辑

### Step 3: 加载体验优化 ✅

- [x] 添加路由切换过渡动画 (fade 0.2s ease-in-out)
- [x] 配置 CSS transition 效果
- [x] 使用 route.path 作为 key 避免重渲染

### Step 4: 构建优化 ✅

- [x] 配置 chunk 分割
- [x] Element Plus 单独 chunk (355KB CSS → 47KB gzip)
- [x] AntV X6 单独 chunk (426KB)

---

## 4. 预期效果

| 指标 | 优化前 | 优化后 |
|------|--------|--------|
| 首屏加载 | ~2s | <1s |
| 模块切换 | 500-800ms 白屏 | <200ms |
| Element Plus CSS | 1.1MB | ~355KB (gzip 47KB) |
| AntV X6 按需 | 419KB 首次 | 按需 |

---

## 5. 验证方法

1. 使用 Chrome DevTools Performance 面板录制切换操作
2. Lighthouse 审计首屏加载时间
3. Network 面板查看各 chunk 加载时间

---

*计划结束*
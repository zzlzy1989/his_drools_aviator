import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/components/Layout/Layout.vue'

// 路由守卫：检查登录状态
function authGuard(to: { path: string }) {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    return '/login'
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'rule-group',
        name: 'RuleGroup',
        component: () => import('@/views/rule-group/RuleGroup.vue'),
        meta: { title: '规则组管理' },
      },
      {
        path: 'rule',
        name: 'Rule',
        component: () => import('@/views/rule/RuleList.vue'),
        meta: { title: '规则定义' },
      },
      {
        path: 'formula',
        name: 'Formula',
        component: () => import('@/views/formula/FormulaList.vue'),
        meta: { title: '公式管理' },
      },
      {
        path: 'flow',
        name: 'Flow',
        component: () => import('@/views/flow/FlowList.vue'),
        meta: { title: '规则流管理' },
      },
      {
        path: 'flow/editor/:id?',
        name: 'FlowEditor',
        component: () => import('@/views/flow/FlowEditor.vue'),
        meta: { title: '规则流设计器' },
      },
      {
        path: 'flow/history/:id',
        name: 'FlowHistory',
        component: () => import('@/views/flow/FlowHistory.vue'),
        meta: { title: '版本历史' },
      },
      {
        path: 'settlement',
        name: 'Settlement',
        component: () => import('@/views/settlement/SettlementList.vue'),
        meta: { title: '结算管理' },
      },
      {
        path: 'drug',
        name: 'Drug',
        component: () => import('@/views/drug/DrugList.vue'),
        meta: { title: '用药审核' },
      },
      {
        path: 'quality',
        name: 'Quality',
        component: () => import('@/views/quality/QualityList.vue'),
        meta: { title: '质量控制' },
      },
      {
        path: 'drg',
        name: 'Drg',
        component: () => import('@/views/drg/DrgList.vue'),
        meta: { title: 'DRG管理' },
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/monitor/Dashboard.vue'),
        meta: { title: '监控大屏' },
      },
      {
        path: 'sandbox',
        name: 'Sandbox',
        component: () => import('@/views/sandbox/SandboxPage.vue'),
        meta: { title: '测试沙箱' },
      },
      {
        path: 'sandbox/logs',
        name: 'SandboxLogs',
        component: () => import('@/views/sandbox/ExecutionLogList.vue'),
        meta: { title: '执行记录' },
      },
      {
        path: 'market',
        name: 'Market',
        component: () => import('@/views/market/MarketPage.vue'),
        meta: { title: '规则市场' },
      },
      {
        path: 'audit',
        name: 'Audit',
        component: () => import('@/views/audit/AuditLogList.vue'),
        meta: { title: '操作日志' },
      },
      {
        path: 'cache',
        name: 'Cache',
        component: () => import('@/views/system/cache/CacheManage.vue'),
        meta: { title: '缓存管理' },
      },
    ],
  },
]

// 预加载关键组件（空闲时）
const preloadRoutes = [
  () => import('@/views/rule/RuleList.vue'),
  () => import('@/views/formula/FormulaList.vue'),
  () => import('@/views/settlement/SettlementList.vue'),
]

// 空闲时预加载
if ('requestIdleCallback' in window) {
  requestIdleCallback(() => {
    preloadRoutes.forEach(route => route())
  })
} else {
  // 兼容不支持 requestIdleCallback 的浏览器
  setTimeout(() => {
    preloadRoutes.forEach(route => route())
  }, 3000)
}

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由预加载守卫
router.beforeEach((to) => {
  // 识别下一可能访问的路由并预加载
  const currentPath = to.path
  if (currentPath === '/') {
    // 首页加载后预加载仪表板
    requestIdleCallback(() => import('@/views/dashboard/Dashboard.vue'))
  }
  if (currentPath.startsWith('/flow')) {
    // 预加载规则流相关组件
    requestIdleCallback(() => import('@/views/flow/FlowList.vue'))
  }
  return true
})

export default router
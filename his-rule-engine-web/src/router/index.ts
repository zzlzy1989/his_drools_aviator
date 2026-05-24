import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { defineAsyncComponent } from 'vue'
import Layout from '@/components/Layout/Layout.vue'

function authGuard(to: { path: string }) {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    return '/login'
  }
}

function asyncView(loader: () => Promise<any>) {
  return defineAsyncComponent({
    loader,
    timeout: 10000,
  })
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
        component: asyncView(() => import('@/views/dashboard/Dashboard.vue')),
        meta: { title: '首页', preload: true },
      },
      {
        path: 'rule-group',
        name: 'RuleGroup',
        component: asyncView(() => import('@/views/rule-group/RuleGroup.vue')),
        meta: { title: '规则组管理' },
      },
      {
        path: 'rule',
        name: 'Rule',
        component: asyncView(() => import('@/views/rule/RuleList.vue')),
        meta: { title: '规则定义', preload: true },
      },
      {
        path: 'formula',
        name: 'Formula',
        component: asyncView(() => import('@/views/formula/FormulaList.vue')),
        meta: { title: '公式管理', preload: true },
      },
      {
        path: 'flow',
        name: 'Flow',
        component: asyncView(() => import('@/views/flow/FlowList.vue')),
        meta: { title: '规则流管理' },
      },
      {
        path: 'flow/editor/:id?',
        name: 'FlowEditor',
        component: asyncView(() => import('@/views/flow/FlowEditor.vue')),
        meta: { title: '规则流设计器' },
      },
      {
        path: 'flow/history/:id',
        name: 'FlowHistory',
        component: asyncView(() => import('@/views/flow/FlowHistory.vue')),
        meta: { title: '版本历史' },
      },
      {
        path: 'settlement',
        name: 'Settlement',
        component: asyncView(() => import('@/views/settlement/SettlementList.vue')),
        meta: { title: '结算管理', preload: true },
      },
      {
        path: 'drug',
        name: 'Drug',
        component: asyncView(() => import('@/views/drug/DrugList.vue')),
        meta: { title: '用药审核' },
      },
      {
        path: 'quality',
        name: 'Quality',
        component: asyncView(() => import('@/views/quality/QualityList.vue')),
        meta: { title: '质量控制' },
      },
      {
        path: 'drg',
        name: 'Drg',
        component: asyncView(() => import('@/views/drg/DrgList.vue')),
        meta: { title: 'DRG管理' },
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: asyncView(() => import('@/views/monitor/Dashboard.vue')),
        meta: { title: '监控大屏' },
      },
      {
        path: 'sandbox',
        name: 'Sandbox',
        component: asyncView(() => import('@/views/sandbox/SandboxPage.vue')),
        meta: { title: '测试沙箱' },
      },
      {
        path: 'sandbox/logs',
        name: 'SandboxLogs',
        component: asyncView(() => import('@/views/sandbox/ExecutionLogList.vue')),
        meta: { title: '执行记录' },
      },
      {
        path: 'market',
        name: 'Market',
        component: asyncView(() => import('@/views/market/MarketPage.vue')),
        meta: { title: '规则市场' },
      },
      {
        path: 'audit',
        name: 'Audit',
        component: asyncView(() => import('@/views/audit/AuditLogList.vue')),
        meta: { title: '操作日志' },
      },
      {
        path: 'cache',
        name: 'Cache',
        component: asyncView(() => import('@/views/system/cache/CacheManage.vue')),
        meta: { title: '缓存管理' },
      },
    ],
  },
]

const routePrefetchMap: Record<string, string[]> = {
  '/dashboard': ['/rule', '/formula', '/settlement', '/flow'],
  '/rule-group': ['/rule'],
  '/rule': ['/formula', '/rule-group'],
  '/formula': ['/rule', '/flow'],
  '/flow': ['/rule', '/formula'],
  '/settlement': ['/drug', '/quality'],
  '/drug': ['/quality', '/drg'],
  '/quality': ['/drug', '/drg'],
  '/drg': ['/quality', '/drug'],
  '/monitor': ['/sandbox'],
  '/sandbox': ['/audit'],
  '/market': ['/rule', '/formula'],
}

const prefetchedRoutes = new Set<string>()

function prefetchRoute(path: string) {
  if (prefetchedRoutes.has(path)) return
  prefetchedRoutes.add(path)
  const matched = router.resolve(path)
  if (matched?.matched?.length) {
    const routeRecord = matched.matched[matched.matched.length - 1]
    const components = routeRecord.components
    if (components?.default && typeof components.default === 'function') {
      ;(components.default as () => Promise<any>)().catch(() => {})
    }
  }
}

function prefetchAdjacentRoutes(currentPath: string) {
  const adjacentPaths = routePrefetchMap[currentPath]
  if (!adjacentPaths) return
  if ('requestIdleCallback' in window) {
    requestIdleCallback(() => {
      adjacentPaths.forEach(prefetchRoute)
    })
  } else {
    setTimeout(() => {
      adjacentPaths.forEach(prefetchRoute)
    }, 1000)
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    return '/login'
  }
})

router.afterEach((to) => {
  prefetchAdjacentRoutes(to.path)
})

export default router
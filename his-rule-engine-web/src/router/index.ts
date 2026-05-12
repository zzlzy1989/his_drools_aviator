import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/components/Layout/Layout.vue'

const routes: RouteRecordRaw[] = [
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
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
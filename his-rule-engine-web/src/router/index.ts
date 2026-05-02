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
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
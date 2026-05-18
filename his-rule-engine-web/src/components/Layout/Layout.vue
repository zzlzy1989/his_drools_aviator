<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">
        <h3>HIS规则中台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="/rule">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>规则管理</span>
          </template>
          <el-menu-item index="/rule-group">规则组</el-menu-item>
          <el-menu-item index="/rule">规则定义</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/formula">
          <el-icon><Document /></el-icon>
          <span>公式管理</span>
        </el-menu-item>
        <el-menu-item index="/flow">
          <el-icon><Connection /></el-icon>
          <span>规则流</span>
        </el-menu-item>
        <el-menu-item index="/settlement">
          <el-icon><Coin /></el-icon>
          <span>结算管理</span>
        </el-menu-item>
        <el-menu-item index="/drug">
          <el-icon><Tools /></el-icon>
          <span>用药审核</span>
        </el-menu-item>
        <el-menu-item index="/quality">
          <el-icon><Star /></el-icon>
          <span>质量控制</span>
        </el-menu-item>
        <el-menu-item index="/drg">
          <el-icon><DataAnalysis /></el-icon>
          <span>DRG管理</span>
        </el-menu-item>
        <el-menu-item index="/monitor">
          <el-icon><DataLine /></el-icon>
          <span>监控大屏</span>
        </el-menu-item>
        <el-menu-item index="/sandbox">
          <el-icon><Box /></el-icon>
          <span>测试沙箱</span>
        </el-menu-item>
        <el-menu-item index="/market">
          <el-icon><Shop /></el-icon>
          <span>规则市场</span>
        </el-menu-item>
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/audit">操作日志</el-menu-item>
          <el-menu-item index="/cache">缓存管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="header-actions">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-icon><User /></el-icon>
                <span>{{ currentUser }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  HomeFilled, Setting, Document, Coin, Tools,
  Star, DataAnalysis, User, Connection, DataLine, Box, Shop
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)
const currentUser = computed(() => localStorage.getItem('userId') || 'admin')

function handleCommand(command: string) {
  if (command === 'logout') {
    localStorage.clear()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.el-aside {
  background-color: #304156;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #263445;

    h3 {
      color: #fff;
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }
  }

  .el-menu {
    border-right: none;
  }
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 16px;

  .header-content {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .header-actions {
    display: flex;
    align-items: center;
  }

  .user-info {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    color: #333;
  }
}

.el-main {
  background-color: #f0f2f5;
  padding: 16px;
}
</style>
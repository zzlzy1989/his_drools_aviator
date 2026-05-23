<template>
  <el-container class="his-layout">
    <el-aside :width="sidebarWidth" class="his-layout__sidebar">
      <div class="his-layout__logo">
        <span class="his-layout__logo-icon">HIS</span>
        <h3 class="his-layout__logo-text">规则中台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="his-layout__menu"
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
    <el-container class="his-layout__main-container">
      <el-header class="his-layout__header" height="44px">
        <div class="his-layout__header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="his-layout__header-right">
          <el-dropdown @command="handleCommand">
            <span class="his-layout__user">
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
      </el-header>
      <el-main class="his-layout__content">
        <router-view v-slot="{ Component, route }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
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

const sidebarWidth = '220px'
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

.his-layout {
  width: 100%;
  height: 100vh;
}

.his-layout__sidebar {
  background-color: $color-inverse-canvas;
  width: $sidebar-width;
  overflow-y: auto;
  overflow-x: hidden;

  .el-menu {
    border-right: none;
    background-color: $color-inverse-canvas;
    color: $color-inverse-ink-muted;

    --el-menu-bg-color: #{$color-inverse-canvas};
    --el-menu-text-color: #{$color-inverse-ink-muted};
    --el-menu-active-color: #{$color-on-primary};
    --el-menu-hover-bg-color: #{$color-inverse-surface-1};

    .el-menu-item {
      height: 40px;
      line-height: 40px;
      font-size: $font-size-small;
      letter-spacing: $letter-spacing-caption;
      border-radius: $radius-md;
      margin: 2px $spacing-xs;
      transition: all 0.2s ease;

      &:hover {
        background-color: $color-inverse-surface-1;
        color: $color-inverse-ink;
      }

      &.is-active {
        background-color: $color-primary;
        color: $color-on-primary;
      }
    }

    .el-sub-menu {
      .el-sub-menu__title {
        height: 40px;
        line-height: 40px;
        color: $color-inverse-ink-muted;
        border-radius: $radius-md;
        margin: 2px $spacing-xs;

        &:hover {
          background-color: $color-inverse-surface-1;
          color: $color-inverse-ink;
        }
      }
    }
  }
}

.his-layout__logo {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  background-color: $color-inverse-surface-1;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.his-layout__logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background-color: $color-primary;
  color: $color-on-primary;
  font-size: $font-size-caption;
  font-weight: $font-weight-semibold;
  letter-spacing: 0.5px;
  border-radius: $radius-xs;
}

.his-layout__logo-text {
  color: $color-inverse-ink;
  margin: 0;
  font-size: $font-size-body-strong;
  font-weight: $font-weight-semibold;
  letter-spacing: $letter-spacing-caption;
}

.his-layout__main-container {
  flex-direction: column;
}

.his-layout__header {
  background-color: $color-canvas;
  border-bottom: 1px solid $color-hairline;
  padding: 0 $spacing-lg;
  height: $header-height;
  display: flex;
  align-items: center;

  .his-layout__header-left,
  .his-layout__header-right {
    display: flex;
    align-items: center;
  }

  .his-layout__header-left {
    flex: 1;
  }
}

.his-layout__user {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: $spacing-xxs;
  color: $color-ink-secondary;
  font-size: $font-size-small;
  letter-spacing: $letter-spacing-caption;

  &:hover {
    color: $color-ink;
  }
}

.his-layout__content {
  background-color: $color-surface-1;
  padding: $spacing-md;
  overflow-y: auto;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
}

.fade-slide-leave-to {
  opacity: 0;
}
</style>

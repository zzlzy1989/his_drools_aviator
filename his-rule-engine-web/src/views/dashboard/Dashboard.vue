<template>
  <div class="his-dashboard">
    <div class="his-dashboard__header">
      <h2 class="his-dashboard__title">首页</h2>
      <p class="his-dashboard__subtitle">HIS 规则中台运行概览</p>
    </div>

    <div class="his-dashboard__stats">
      <StatCard
        label="规则总数"
        :value="stats.ruleCount"
        variant="primary"
        description="已注册规则"
      />
      <StatCard
        label="公式总数"
        :value="stats.formulaCount"
        variant="success"
        description="Aviator 表达式"
      />
      <StatCard
        label="规则流"
        :value="stats.flowCount"
        variant="warning"
        description="Drools 流程"
      />
      <StatCard
        label="今日执行"
        :value="stats.executeCount"
        variant="danger"
        trend="up"
        trend-text="+12.3%"
      />
    </div>

    <div class="his-dashboard__content">
      <div class="his-dashboard__main">
        <div class="his-dashboard__card">
          <div class="his-dashboard__card-header">
            <h3 class="his-dashboard__card-title">执行趋势</h3>
          </div>
          <div class="his-dashboard__card-body">
            <div class="his-dashboard__chart-placeholder">
              <el-empty description="图表加载中..." />
            </div>
          </div>
        </div>
      </div>

      <div class="his-dashboard__side">
        <div class="his-dashboard__card">
          <div class="his-dashboard__card-header">
            <h3 class="his-dashboard__card-title">快捷入口</h3>
          </div>
          <div class="his-dashboard__card-body">
            <div class="his-dashboard__quick-links">
              <el-button type="primary" @click="router.push('/flow/editor')">
                <el-icon><Plus /></el-icon>
                新建规则流
              </el-button>
              <el-button @click="router.push('/rule')">
                <el-icon><Document /></el-icon>
                规则管理
              </el-button>
              <el-button @click="router.push('/formula')">
                <el-icon><Connection /></el-icon>
                公式管理
              </el-button>
              <el-button @click="router.push('/settlement')">
                <el-icon><Coin /></el-icon>
                结算管理
              </el-button>
            </div>
          </div>
        </div>

        <div class="his-dashboard__card">
          <div class="his-dashboard__card-header">
            <h3 class="his-dashboard__card-title">系统状态</h3>
          </div>
          <div class="his-dashboard__card-body">
            <div class="his-dashboard__system-status">
              <div class="his-dashboard__status-item">
                <span class="his-dashboard__status-label">规则引擎</span>
                <StatusTag type="pass" show-dot size="small" />
              </div>
              <div class="his-dashboard__status-item">
                <span class="his-dashboard__status-label">结算服务</span>
                <StatusTag type="pass" show-dot size="small" />
              </div>
              <div class="his-dashboard__status-item">
                <span class="his-dashboard__status-label">监控服务</span>
                <StatusTag type="pass" show-dot size="small" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Connection, Plus, Coin } from '@element-plus/icons-vue'
import { StatCard, StatusTag } from '@/components/HIS'

const router = useRouter()

const stats = ref({
  ruleCount: 0,
  formulaCount: 0,
  flowCount: 0,
  executeCount: 0,
})

onMounted(() => {
  stats.value = {
    ruleCount: 13,
    formulaCount: 18,
    flowCount: 0,
    executeCount: 0,
  }
})
</script>

<style lang="scss" scoped>

.his-dashboard {
  &__header {
    margin-bottom: $spacing-lg;
  }

  &__title {
    font-size: $font-size-headline;
    font-weight: $font-weight-regular;
    color: $color-ink;
    margin: 0;
  }

  &__subtitle {
    font-size: $font-size-body;
    color: $color-ink-muted;
    margin: $spacing-xxs 0 0 0;
  }

  &__stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-md;
    margin-bottom: $spacing-lg;
  }

  &__content {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: $spacing-md;
  }

  &__card {
    background-color: $color-canvas;
    border: 1px solid $color-hairline;
    margin-bottom: $spacing-md;
  }

  &__card-header {
    padding: $spacing-md $spacing-lg;
    border-bottom: 1px solid $color-hairline;
  }

  &__card-title {
    font-size: $font-size-body-lg;
    font-weight: $font-weight-regular;
    color: $color-ink;
    margin: 0;
  }

  &__card-body {
    padding: $spacing-lg;
  }

  &__chart-placeholder {
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__quick-links {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;

    .el-button {
      width: 100%;
      justify-content: flex-start;
    }
  }

  &__system-status {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }

  &__status-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-xs 0;
    border-bottom: 1px solid $color-hairline;

    &:last-child {
      border-bottom: none;
    }
  }

  &__status-label {
    font-size: $font-size-body;
    color: $color-ink-secondary;
  }
}
</style>

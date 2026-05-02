<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" color="#409EFF"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.ruleCount }}</div>
              <div class="stat-label">规则总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" color="#67C23A"><Connection /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.formulaCount }}</div>
              <div class="stat-label">公式总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" color="#E6A23C"><Memo /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.flowCount }}</div>
              <div class="stat-label">规则流</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" color="#F56C6C"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.executeCount }}</div>
              <div class="stat-label">今日执行</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>执行趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表加载中..." />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷入口</span>
          </template>
          <div class="quick-links">
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
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Connection, Memo, Clock, Plus } from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref({
  ruleCount: 0,
  formulaCount: 0,
  flowCount: 0,
  executeCount: 0,
})

onMounted(() => {
  // Load stats
  stats.value = {
    ruleCount: 13,
    formulaCount: 18,
    flowCount: 0,
    executeCount: 0,
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    font-size: 48px;
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #333;
    }

    .stat-label {
      color: #999;
      font-size: 14px;
      margin-top: 4px;
    }
  }

  .chart-placeholder {
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .quick-links {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .el-button {
      width: 100%;
      justify-content: flex-start;
    }
  }
}
</style>
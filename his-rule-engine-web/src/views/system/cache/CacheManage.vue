<template>
  <div class="cache-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>缓存管理</span>
          <el-button type="primary" :icon="Refresh" @click="loadStats" :loading="loading">
            刷新
          </el-button>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="cacheStats" class="stats-container">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-label">缓存状态</div>
              <div class="stat-value">
                <el-tag :type="cacheStats.enabled ? 'success' : 'danger'">
                  {{ cacheStats.enabled ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-label">缓存条目数</div>
              <div class="stat-value">{{ cacheStats.size || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-label">命中率</div>
              <div class="stat-value">
                <span :class="hitRateClass">{{ hitRate }}%</span>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-label">淘汰次数</div>
              <div class="stat-value">{{ evictionCount || 0 }}</div>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <div class="stats-detail">
          <h4>详细统计</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="命中次数">{{ hitCount }}</el-descriptions-item>
            <el-descriptions-item label="未命中次数">{{ missCount }}</el-descriptions-item>
            <el-descriptions-item label="缓存命中率">{{ hitRate }}%</el-descriptions-item>
            <el-descriptions-item label="淘汰条数">{{ evictionCount }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="actions">
          <h4>缓存操作</h4>
          <el-space>
            <el-button type="primary" @click="refreshAll" :loading="refreshing">
              刷新所有缓存
            </el-button>
            <el-button type="warning" @click="loadStats">
              刷新统计
            </el-button>
          </el-space>
        </div>
      </div>

      <el-empty v-else description="缓存统计加载失败" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getCacheStats, refreshCache } from '@/api/cache'

const loading = ref(false)
const refreshing = ref(false)
const cacheStats = ref<any>(null)

interface CacheStatDetail {
  hitCount: number
  missCount: number
  hitRate: number
  evictionCount: number
}

const parsedStats = computed<CacheStatDetail | null>(() => {
  if (!cacheStats.value?.stats) return null
  const parts = cacheStats.value.stats.split(', ')
  const result: any = {}
  parts.forEach((p: string) => {
    const [k, v] = p.split('=')
    if (k === 'hitRate') {
      result.hitRate = parseFloat(v.replace('%', ''))
    } else {
      result[k] = parseInt(v, 10)
    }
  })
  return result as CacheStatDetail
})

const hitCount = computed(() => parsedStats.value?.hitCount ?? 0)
const missCount = computed(() => parsedStats.value?.missCount ?? 0)
const hitRate = computed(() => parsedStats.value?.hitRate?.toFixed(2) ?? '0.00')
const evictionCount = computed(() => parsedStats.value?.evictionCount ?? 0)

const hitRateClass = computed(() => {
  const rate = parsedStats.value?.hitRate ?? 0
  if (rate >= 90) return 'rate-high'
  if (rate >= 70) return 'rate-medium'
  return 'rate-low'
})

async function loadStats() {
  loading.value = true
  try {
    const res = await getCacheStats()
    if (res.code === '0') {
      cacheStats.value = res.data
    } else {
      ElMessage.error(res.message || '加载缓存统计失败')
    }
  } catch (e: any) {
    ElMessage.error('加载缓存统计失败: ' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

async function refreshAll() {
  refreshing.value = true
  try {
    const res = await refreshCache()
    if (res.code === '0') {
      ElMessage.success('缓存刷新成功')
      await loadStats()
    } else {
      ElMessage.error(res.message || '刷新缓存失败')
    }
  } catch (e: any) {
    ElMessage.error('刷新缓存失败: ' + (e.message || ''))
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style lang="scss" scoped>
.cache-manage {
  padding: 16px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .loading {
    padding: 20px;
  }

  .stats-container {
    .stat-card {
      background: #f5f7fa;
      border-radius: 8px;
      padding: 20px;
      text-align: center;

      .stat-label {
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #333;

        .rate-high {
          color: #67c23a;
        }

        .rate-medium {
          color: #e6a23c;
        }

        .rate-low {
          color: #f56c6c;
        }
      }
    }

    .stats-detail {
      margin-top: 20px;

      h4 {
        margin-bottom: 12px;
        color: #333;
      }
    }

    .actions {
      margin-top: 20px;

      h4 {
        margin-bottom: 12px;
        color: #333;
      }
    }
  }
}
</style>
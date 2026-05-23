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
        <!-- Aviator 缓存 -->
        <div class="cache-section">
          <h4>Aviator 表达式缓存</h4>
          <el-row :gutter="20" v-if="cacheStats.aviatorCache">
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-label">缓存条目数</div>
                <div class="stat-value">{{ cacheStats.aviatorCache.size || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="9">
              <div class="stat-card">
                <div class="stat-label">命中率</div>
                <div class="stat-value">
                  <span :class="getHitRateClass('aviator')">{{ getHitRate('aviator') }}%</span>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-label">淘汰次数</div>
                <div class="stat-value">{{ getEvictionCount('aviator') || 0 }}</div>
              </div>
            </el-col>
          </el-row>
          <el-descriptions v-if="cacheStats.aviatorCache" :column="2" border class="mt-16">
            <el-descriptions-item label="命中次数">{{ getHitCount('aviator') }}</el-descriptions-item>
            <el-descriptions-item label="未命中次数">{{ getMissCount('aviator') }}</el-descriptions-item>
            <el-descriptions-item label="命中率">{{ getHitRate('aviator') }}%</el-descriptions-item>
            <el-descriptions-item label="淘汰条数">{{ getEvictionCount('aviator') }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider />

        <!-- 规则定义缓存 -->
        <div class="cache-section" v-if="cacheStats.ruleDefinitionCache">
          <h4>规则定义缓存</h4>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-label">缓存条目数</div>
                <div class="stat-value">{{ cacheStats.ruleDefinitionCache.size || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="9">
              <div class="stat-card">
                <div class="stat-label">命中率</div>
                <div class="stat-value">
                  <span :class="getHitRateClass('rule')">{{ getHitRate('rule') }}%</span>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-label">淘汰次数</div>
                <div class="stat-value">{{ getEvictionCount('rule') || 0 }}</div>
              </div>
            </el-col>
          </el-row>
          <el-descriptions :column="2" border class="mt-16">
            <el-descriptions-item label="命中次数">{{ getHitCount('rule') }}</el-descriptions-item>
            <el-descriptions-item label="未命中次数">{{ getMissCount('rule') }}</el-descriptions-item>
            <el-descriptions-item label="命中率">{{ getHitRate('rule') }}%</el-descriptions-item>
            <el-descriptions-item label="淘汰条数">{{ getEvictionCount('rule') }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="actions">
          <h4>缓存操作</h4>
          <el-space>
            <el-button type="primary" @click="refreshAll" :loading="refreshing">
              刷新所有缓存
            </el-button>
            <el-button type="warning" @click="invalidateAviator" :loading="refreshingAviator">
              刷新 Aviator 缓存
            </el-button>
          </el-space>
        </div>
      </div>

      <el-empty v-else description="缓存统计加载失败" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getCacheStats, refreshCache, invalidateCache } from '@/api/cache'

const loading = ref(false)
const refreshing = ref(false)
const refreshingAviator = ref(false)
const cacheStats = ref<any>(null)

interface CacheStatDetail {
  hitCount: number
  missCount: number
  hitRate: number
  evictionCount: number
}

function parseStats(statsStr: string): CacheStatDetail {
  const parts = statsStr.split(', ')
  const result: CacheStatDetail = {
    hitCount: 0,
    missCount: 0,
    hitRate: 0,
    evictionCount: 0
  }
  parts.forEach((p: string) => {
    const [k, v] = p.split('=')
    if (k === 'hitRate') {
      result.hitRate = parseFloat(v.replace('%', ''))
    } else {
      result[k] = parseInt(v, 10)
    }
  })
  return result
}

function getAviatorStats(): CacheStatDetail | null {
  if (!cacheStats.value?.aviatorCache?.stats) return null
  return parseStats(cacheStats.value.aviatorCache.stats)
}

function getRuleStats(): CacheStatDetail | null {
  if (!cacheStats.value?.ruleDefinitionCache?.stats) return null
  return parseStats(cacheStats.value.ruleDefinitionCache.stats)
}

function getHitCount(type: 'aviator' | 'rule'): number {
  const stats = type === 'aviator' ? getAviatorStats() : getRuleStats()
  return stats?.hitCount ?? 0
}

function getMissCount(type: 'aviator' | 'rule'): number {
  const stats = type === 'aviator' ? getAviatorStats() : getRuleStats()
  return stats?.missCount ?? 0
}

function getHitRate(type: 'aviator' | 'rule'): string {
  const stats = type === 'aviator' ? getAviatorStats() : getRuleStats()
  return (stats?.hitRate ?? 0).toFixed(2)
}

function getEvictionCount(type: 'aviator' | 'rule'): number {
  const stats = type === 'aviator' ? getAviatorStats() : getRuleStats()
  return stats?.evictionCount ?? 0
}

function getHitRateClass(type: 'aviator' | 'rule'): string {
  const rate = parseFloat(getHitRate(type))
  if (rate >= 90) return 'rate-high'
  if (rate >= 70) return 'rate-medium'
  return 'rate-low'
}

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

async function invalidateAviator() {
  refreshingAviator.value = true
  try {
    const res = await invalidateCache('aviator')
    if (res.code === '0') {
      ElMessage.success('Aviator缓存已刷新')
      await loadStats()
    } else {
      ElMessage.error(res.message || '刷新缓存失败')
    }
  } catch (e: any) {
    ElMessage.error('刷新缓存失败: ' + (e.message || ''))
  } finally {
    refreshingAviator.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style lang="scss" scoped>
.cache-manage {
  @include page-container;

  .card-header {
    @include flex-between;
  }

  .loading {
    padding: $spacing-md;
  }

  .stats-container {
    .cache-section {
      h4 {
        margin-bottom: $spacing-sm;
        color: $color-ink;
        font-weight: $font-weight-semibold;
      }

      .mt-16 {
        margin-top: $spacing-md;
      }
    }

    .stat-card {
      background: $color-surface-1;
      border-radius: $radius-md;
      padding: $spacing-md;
      text-align: center;

      .stat-label {
        font-size: $font-size-caption;
        color: $color-ink-muted;
        margin-bottom: $spacing-xs;
      }

      .stat-value {
        font-size: $font-size-headline;
        font-weight: $font-weight-semibold;
        color: $color-ink;
        font-feature-settings: "tnum";

        .rate-high {
          color: $color-semantic-pass;
        }

        .rate-medium {
          color: $color-semantic-warn;
        }

        .rate-low {
          color: $color-semantic-block;
        }
      }
    }

    .actions {
      margin-top: $spacing-md;

      h4 {
        margin-bottom: $spacing-sm;
        color: $color-ink;
        font-weight: $font-weight-semibold;
      }
    }
  }
}
</style>
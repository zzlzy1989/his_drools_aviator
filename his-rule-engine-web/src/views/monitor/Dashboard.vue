<template>
  <div class="monitor-dashboard">
    <div class="dashboard-header">
      <h2>HIS 规则引擎监控大屏</h2>
      <div class="header-actions">
        <el-select v-model="refreshInterval" size="default" style="width: 120px">
          <el-option :value="0" label="关闭自动刷新" />
          <el-option :value="5000" label="5秒" />
          <el-option :value="10000" label="10秒" />
          <el-option :value="30000" label="30秒" />
          <el-option :value="60000" label="1分钟" />
        </el-select>
        <el-button @click="loadMetrics" :loading="loading">刷新</el-button>
      </div>
    </div>

    <!-- 指标卡片 -->
    <div class="metrics-cards">
      <div class="metric-card">
        <div class="metric-label">规则执行总次数</div>
        <div class="metric-value">{{ formatNumber(metrics?.executionTotal || 0) }}</div>
        <div class="metric-change positive">今日 +12.3%</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">成功率</div>
        <div class="metric-value">{{ metrics?.successRate || 0 }}%</div>
        <div class="metric-change positive">目标 ≥99%</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">P99 执行耗时</div>
        <div class="metric-value">{{ metrics?.p99DurationMs || 0 }} ms</div>
        <div class="metric-change" :class="(metrics?.p99DurationMs || 0) > 100 ? 'negative' : 'positive'">
          目标 &lt;100ms
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-label">活跃规则数</div>
        <div class="metric-value">{{ metrics?.activeRuleCount || 0 }}</div>
        <div class="metric-change positive">已注册规则</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-panel">
        <h3>执行耗时分布</h3>
        <div ref="durationChartRef" class="chart-container"></div>
      </div>
      <div class="chart-panel">
        <h3>TOP 10 高频规则</h3>
        <div ref="topRulesChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 下方区域 -->
    <div class="bottom-row">
      <div class="alert-panel">
        <h3>最近告警</h3>
        <div class="alert-list">
          <div v-if="!metrics?.recentAlerts?.length" class="empty-state">
            暂无告警
          </div>
          <div v-for="alert in metrics?.recentAlerts" :key="alert.alertId" class="alert-item">
            <span class="alert-icon" :class="'level-' + alert.level">{{ alert.level }}</span>
            <span class="alert-message">{{ alert.message }}</span>
            <span class="alert-time">{{ formatTime(alert.alertTime) }}</span>
          </div>
        </div>
      </div>
      <div class="stats-panel">
        <h3>执行统计</h3>
        <div class="stats-content">
          <div class="stat-item">
            <span class="stat-label">成功</span>
            <span class="stat-value success">{{ formatNumber(metrics?.executionSuccess || 0) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">失败</span>
            <span class="stat-value danger">{{ formatNumber(metrics?.executionFailed || 0) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">公式命中率</span>
            <span class="stat-value">{{ metrics?.formulaHitRate || 0 }}%</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">P95 耗时</span>
            <span class="stat-value">{{ metrics?.p95DurationMs || 0 }} ms</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { monitorApi } from '@/api/monitor'

const loading = ref(false)
const refreshInterval = ref(10000)
const metrics = ref<any>(null)
const durationChartRef = ref<HTMLElement>()
const topRulesChartRef = ref<HTMLElement>()

let durationChart: echarts.ECharts | null = null
let topRulesChart: echarts.ECharts | null = null
let refreshTimer: ReturnType<typeof setInterval> | null = null

function formatNumber(num: number): string {
  return num.toLocaleString()
}

function formatTime(time: string): string {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString()
}

async function loadMetrics() {
  try {
    loading.value = true
    const res = await monitorApi.getMetrics()
    if (res.code === '0') {
      metrics.value = res.data
      updateCharts()
    }
  } catch (error) {
    console.error('加载监控指标失败:', error)
  } finally {
    loading.value = false
  }
}

function updateCharts() {
  if (durationChartRef.value) {
    durationChart?.dispose()
    durationChart = echarts.init(durationChartRef.value)
    durationChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['0ms', '10ms', '30ms', '50ms', '100ms', '200ms', '500ms', '1000ms+']
      },
      yAxis: { type: 'value', name: '执行次数' },
      series: [{
        name: '执行次数',
        type: 'bar',
        data: [120, 340, 560, 780, 450, 230, 120, 40],
        itemStyle: { color: '#409EFF' }
      }]
    })
  }

  if (topRulesChartRef.value && metrics.value?.topRules) {
    topRulesChart?.dispose()
    topRulesChart = echarts.init(topRulesChartRef.value)
    const topRules = metrics.value.topRules.slice(0, 10)
    topRulesChart.setOption({
      tooltip: { trigger: 'item' },
      yAxis: {
        type: 'category',
        data: topRules.map((r: any) => r.ruleKey).reverse()
      },
      xAxis: { type: 'value', name: '命中次数' },
      series: [{
        name: '命中次数',
        type: 'bar',
        data: topRules.map((r: any) => r.hitCount).reverse(),
        itemStyle: { color: '#67C23A' }
      }]
    })
  }
}

function startRefreshTimer() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  if (refreshInterval.value > 0) {
    refreshTimer = setInterval(loadMetrics, refreshInterval.value)
  }
}

watch(refreshInterval, () => {
  startRefreshTimer()
})

onMounted(() => {
  loadMetrics()
  startRefreshTimer()
  window.addEventListener('resize', () => {
    durationChart?.resize()
    topRulesChart?.resize()
  })
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  durationChart?.dispose()
  topRulesChart?.dispose()
})
</script>

<style scoped>
.monitor-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.metrics-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.metric-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.metric-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.metric-change {
  font-size: 12px;
  color: #909399;
}

.metric-change.positive {
  color: #67C23A;
}

.metric-change.negative {
  color: #F56C6C;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-panel {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart-panel h3 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.chart-container {
  height: 250px;
}

.bottom-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.alert-panel,
.stats-panel {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.alert-panel h3,
.stats-panel h3 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.alert-list {
  max-height: 300px;
  overflow-y: auto;
}

.empty-state {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.alert-icon {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 10px;
}

.level-WARN {
  background: #E6A23C;
  color: white;
}

.level-ERROR {
  background: #F56C6C;
  color: white;
}

.alert-message {
  flex: 1;
  color: #606266;
}

.alert-time {
  color: #909399;
  font-size: 12px;
}

.stats-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 6px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.danger {
  color: #F56C6C;
}
</style>
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

    <!-- 历史数据查询 -->
    <div class="history-section">
      <h3>历史数据查询</h3>
      <div class="history-filters">
        <el-select v-model="historyQuery.metricName" placeholder="选择指标" size="default" style="width: 180px">
          <el-option value="rule_hit" label="规则触发" />
          <el-option value="execution_time" label="执行耗时" />
          <el-option value="success_rate" label="成功率" />
        </el-select>
        <el-date-picker
          v-model="historyDateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          size="default"
          style="width: 340px"
        />
        <el-button @click="loadHistory" :loading="historyLoading">查询</el-button>
      </div>
      <div class="history-content">
        <div class="history-summary">
          <div class="summary-item">
            <span class="summary-label">查询条数:</span>
            <span class="summary-value">{{ historySummary.count }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">平均值:</span>
            <span class="summary-value">{{ historySummary.avg }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">最大值:</span>
            <span class="summary-value">{{ historySummary.max }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">最小值:</span>
            <span class="summary-value">{{ historySummary.min }}</span>
          </div>
        </div>
        <div ref="historyChartRef" class="history-chart"></div>
      </div>
    </div>

    <!-- 规则触发热力图 -->
    <div class="heatmap-section">
      <h3>规则触发热力图</h3>
      <div class="heatmap-filters">
        <el-select v-model="heatmapDays" placeholder="查询天数" size="default" style="width: 120px">
          <el-option :value="7" label="近7天" />
          <el-option :value="14" label="近14天" />
          <el-option :value="30" label="近30天" />
        </el-select>
        <el-button @click="loadHeatmap" :loading="heatmapLoading">加载热力图</el-button>
      </div>
      <div ref="heatmapChartRef" class="heatmap-chart"></div>
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

// History state
const historyLoading = ref(false)
const historyChartRef = ref<HTMLElement>()
const historyDateRange = ref<[Date, Date]>([
  new Date(Date.now() - 24 * 60 * 60 * 1000),
  new Date()
])
const historyQuery = ref({
  metricName: 'rule_hit'
})
const historySummary = ref<any>({ count: 0, avg: '-', max: '-', min: '-' })
let historyChart: echarts.ECharts | null = null

// Heatmap state
const heatmapLoading = ref(false)
const heatmapChartRef = ref<HTMLElement>()
const heatmapDays = ref(7)
let heatmapChart: echarts.ECharts | null = null

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

async function loadHistory() {
  try {
    historyLoading.value = true
    const [startTime, endTime] = historyDateRange.value
    const res = await monitorApi.getHistory({
      metricName: historyQuery.value.metricName,
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString()
    })
    if (res.code === '0') {
      const data = res.data
      historySummary.value = data.summary || { count: 0, avg: '-', max: '-', min: '-' }
      updateHistoryChart(data.dataPoints || [])
    }
  } catch (error) {
    console.error('加载历史数据失败:', error)
  } finally {
    historyLoading.value = false
  }
}

function updateHistoryChart(dataPoints: any[]) {
  if (!historyChartRef.value) return
  historyChart?.dispose()
  historyChart = echarts.init(historyChartRef.value)

  const times = dataPoints.map(p => p.time)
  const values = dataPoints.map(p => p.value)

  historyChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: times, name: '时间' },
    yAxis: { type: 'value', name: '指标值' },
    series: [{
      name: '指标值',
      type: 'line',
      data: values,
      smooth: true,
      itemStyle: { color: '#409EFF' },
      areaStyle: { color: 'rgba(64, 158, 255, 0.2)' }
    }]
  })
}

async function loadHeatmap() {
  try {
    heatmapLoading.value = true
    const res = await monitorApi.getHeatmap(heatmapDays.value)
    if (res.code === '0') {
      updateHeatmapChart(res.data)
    }
  } catch (error) {
    console.error('加载热力图失败:', error)
  } finally {
    heatmapLoading.value = false
  }
}

function updateHeatmapChart(data: any) {
  if (!heatmapChartRef.value) return
  heatmapChart?.dispose()
  heatmapChart = echarts.init(heatmapChartRef.value)

  const hours = data.hours || []
  const rules = data.rules || []

  const heatmapData: [number, number, number][] = []
  rules.forEach((rule: any, ruleIdx: number) => {
    hours.forEach((hour: string, hourIdx: number) => {
      const value = rule[hour] || 0
      if (value > 0) {
        heatmapData.push([hourIdx, ruleIdx, value])
      }
    })
  })

  heatmapChart.setOption({
    tooltip: { position: 'top' },
    xAxis: { type: 'category', data: hours, name: '小时' },
    yAxis: { type: 'category', data: rules.map((r: any) => r.ruleKey), name: '规则' },
    visualMap: {
      min: 0,
      max: Math.max(...heatmapData.map(d => d[2]), 1),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '0%',
      inRange: { color: ['#e6f7ff', '#1890ff', '#f5222d'] }
    },
    series: [{
      name: '触发次数',
      type: 'heatmap',
      data: heatmapData,
      label: { show: false },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
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
    historyChart?.resize()
    heatmapChart?.resize()
  })
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  durationChart?.dispose()
  topRulesChart?.dispose()
  historyChart?.dispose()
  heatmapChart?.dispose()
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

.history-section,
.heatmap-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.history-section h3,
.heatmap-section h3 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.history-filters,
.heatmap-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.history-content {
  display: flex;
  flex-direction: column;
}

.history-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.summary-label {
  color: #909399;
  font-size: 12px;
}

.summary-value {
  color: #303133;
  font-weight: bold;
}

.history-chart {
  height: 250px;
}

.heatmap-chart {
  height: 400px;
}
</style>
<template>
  <div class="his-monitor">
    <div class="his-monitor__header">
      <h2 class="his-monitor__title">HIS 规则引擎监控大屏</h2>
      <div class="his-monitor__actions">
        <el-select v-model="refreshInterval" size="default">
          <el-option :value="0" label="关闭自动刷新" />
          <el-option :value="5000" label="5秒" />
          <el-option :value="10000" label="10秒" />
          <el-option :value="30000" label="30秒" />
          <el-option :value="60000" label="1分钟" />
        </el-select>
        <el-button @click="loadMetrics" :loading="loading">刷新</el-button>
      </div>
    </div>

    <div class="his-monitor__metrics">
      <StatCard label="规则执行总次数" :value="metrics?.executionTotal || 0" variant="primary" description="今日 +12.3%" />
      <StatCard label="成功率" :value="`${metrics?.successRate || 0}%`" variant="success" description="目标 ≥99%" />
      <StatCard label="P99 执行耗时" :value="`${metrics?.p99DurationMs || 0} ms`" :variant="(metrics?.p99DurationMs || 0) > 100 ? 'danger' : 'success'" description="目标 <100ms" />
      <StatCard label="活跃规则数" :value="metrics?.activeRuleCount || 0" variant="warning" description="已注册规则" />
    </div>

    <div class="his-monitor__charts">
      <div class="his-monitor__panel">
        <h3 class="his-monitor__panel-title">执行耗时分布</h3>
        <div ref="durationChartRef" class="his-monitor__chart" />
      </div>
      <div class="his-monitor__panel">
        <h3 class="his-monitor__panel-title">TOP 10 高频规则</h3>
        <div ref="topRulesChartRef" class="his-monitor__chart" />
      </div>
    </div>

    <div class="his-monitor__bottom">
      <div class="his-monitor__panel his-monitor__panel--wide">
        <h3 class="his-monitor__panel-title">最近告警</h3>
        <div class="his-monitor__alert-list">
          <div v-if="!metrics?.recentAlerts?.length" class="his-monitor__empty">
            暂无告警
          </div>
          <div v-for="alert in metrics?.recentAlerts" :key="alert.alertId" class="his-monitor__alert-item">
            <StatusTag
              :type="alert.level === 'ERROR' ? 'error' : 'warning'"
              :label="alert.level"
              show-dot
              size="small"
            />
            <span class="his-monitor__alert-msg">{{ alert.message }}</span>
            <span class="his-monitor__alert-time">{{ formatTime(alert.alertTime) }}</span>
          </div>
        </div>
      </div>
      <div class="his-monitor__panel">
        <h3 class="his-monitor__panel-title">执行统计</h3>
        <div class="his-monitor__stats">
          <div class="his-monitor__stat-item">
            <span class="his-monitor__stat-label">成功</span>
            <span class="his-monitor__stat-value his-monitor__stat-value--success">{{ formatNumber(metrics?.executionSuccess || 0) }}</span>
          </div>
          <div class="his-monitor__stat-item">
            <span class="his-monitor__stat-label">失败</span>
            <span class="his-monitor__stat-value his-monitor__stat-value--danger">{{ formatNumber(metrics?.executionFailed || 0) }}</span>
          </div>
          <div class="his-monitor__stat-item">
            <span class="his-monitor__stat-label">公式命中率</span>
            <span class="his-monitor__stat-value">{{ metrics?.formulaHitRate || 0 }}%</span>
          </div>
          <div class="his-monitor__stat-item">
            <span class="his-monitor__stat-label">P95 耗时</span>
            <span class="his-monitor__stat-value">{{ metrics?.p95DurationMs || 0 }} ms</span>
          </div>
        </div>
      </div>
    </div>

    <div class="his-monitor__panel his-monitor__panel--full">
      <h3 class="his-monitor__panel-title">历史数据查询</h3>
      <div class="his-monitor__filters">
        <el-select v-model="historyQuery.metricName" placeholder="选择指标" size="default">
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
        />
        <el-button @click="loadHistory" :loading="historyLoading">查询</el-button>
      </div>
      <div class="his-monitor__history-summary">
        <div class="his-monitor__summary-item">
          <span class="his-monitor__summary-label">查询条数:</span>
          <span class="his-monitor__summary-value">{{ historySummary.count }}</span>
        </div>
        <div class="his-monitor__summary-item">
          <span class="his-monitor__summary-label">平均值:</span>
          <span class="his-monitor__summary-value">{{ historySummary.avg }}</span>
        </div>
        <div class="his-monitor__summary-item">
          <span class="his-monitor__summary-label">最大值:</span>
          <span class="his-monitor__summary-value">{{ historySummary.max }}</span>
        </div>
        <div class="his-monitor__summary-item">
          <span class="his-monitor__summary-label">最小值:</span>
          <span class="his-monitor__summary-value">{{ historySummary.min }}</span>
        </div>
      </div>
      <div ref="historyChartRef" class="his-monitor__chart" />
    </div>

    <div class="his-monitor__panel his-monitor__panel--full">
      <h3 class="his-monitor__panel-title">规则触发热力图</h3>
      <div class="his-monitor__filters">
        <el-select v-model="heatmapDays" placeholder="查询天数" size="default">
          <el-option :value="7" label="近7天" />
          <el-option :value="14" label="近14天" />
          <el-option :value="30" label="近30天" />
        </el-select>
        <el-button @click="loadHeatmap" :loading="heatmapLoading">加载热力图</el-button>
      </div>
      <div ref="heatmapChartRef" class="his-monitor__chart his-monitor__chart--tall" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, HeatmapChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  VisualMapComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  BarChart,
  LineChart,
  HeatmapChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  VisualMapComponent,
  CanvasRenderer,
])
import { monitorApi } from '@/api/monitor'
import { StatCard, StatusTag } from '@/components/HIS'

const loading = ref(false)
const refreshInterval = ref(10000)
const metrics = ref<any>(null)
const durationChartRef = ref<HTMLElement>()
const topRulesChartRef = ref<HTMLElement>()

const historyLoading = ref(false)
const historyChartRef = ref<HTMLElement>()
const historyDateRange = ref<[Date, Date]>([
  new Date(Date.now() - 24 * 60 * 60 * 1000),
  new Date()
])
const historyQuery = ref({ metricName: 'rule_hit' })
const historySummary = ref<any>({ count: 0, avg: '-', max: '-', min: '-' })
let historyChart: echarts.ECharts | null = null

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
  return new Date(time).toLocaleTimeString()
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
      itemStyle: { color: '#0f62fe' },
      areaStyle: { color: 'rgba(15, 98, 254, 0.15)' }
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
      if (value > 0) heatmapData.push([hourIdx, ruleIdx, value])
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
      inRange: { color: ['#e6f7ff', '#0f62fe', '#da1e28'] }
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
        itemStyle: { color: '#0f62fe' }
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
        itemStyle: { color: '#198038' }
      }]
    })
  }
}

function startRefreshTimer() {
  if (refreshTimer) clearInterval(refreshTimer)
  if (refreshInterval.value > 0) {
    refreshTimer = setInterval(loadMetrics, refreshInterval.value)
  }
}

watch(refreshInterval, () => { startRefreshTimer() })

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
  if (refreshTimer) clearInterval(refreshTimer)
  durationChart?.dispose()
  topRulesChart?.dispose()
  historyChart?.dispose()
  heatmapChart?.dispose()
})
</script>

<style lang="scss" scoped>

.his-monitor {
  padding: $spacing-lg;
  background-color: $color-surface-1;
  min-height: 100%;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;
  }

  &__title {
    margin: 0;
    font-size: $font-size-headline;
    font-weight: $font-weight-bold;
    color: $color-ink;
    letter-spacing: $letter-spacing-headline;
  }

  &__actions {
    display: flex;
    gap: $spacing-sm;
    align-items: center;
  }

  &__metrics {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-md;
    margin-bottom: $spacing-lg;
  }

  &__charts {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-md;
    margin-bottom: $spacing-lg;
  }

  &__panel {
    @include apple-card;
    border-radius: $radius-lg;

    &--wide {
      grid-column: span 2;
    }

    &--full {
      margin-top: $spacing-lg;
    }
  }

  &__panel-title {
    margin: 0 0 $spacing-md 0;
    font-size: $font-size-subheading;
    font-weight: $font-weight-semibold;
    color: $color-ink;
  }

  &__chart {
    height: 250px;

    &--tall {
      height: 400px;
    }
  }

  &__bottom {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: $spacing-md;
  }

  &__alert-list {
    max-height: 300px;
    overflow-y: auto;
  }

  &__empty {
    color: $color-ink-muted;
    text-align: center;
    padding: $spacing-xxl 0;
  }

  &__alert-item {
    display: flex;
    align-items: center;
    padding: $spacing-sm 0;
    border-bottom: 1px solid $color-hairline;
  }

  &__alert-msg {
    flex: 1;
    color: $color-ink-secondary;
    font-size: $font-size-body;
    margin-left: $spacing-sm;
  }

  &__alert-time {
    color: $color-ink-muted;
    font-size: $font-size-small;
  }

  &__stats {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-md;
  }

  &__stat-item {
    display: flex;
    flex-direction: column;
    padding: $spacing-md;
    background-color: $color-surface-1;
    border-radius: $radius-sm;
  }

  &__stat-label {
    font-size: $font-size-small;
    color: $color-ink-muted;
    margin-bottom: $spacing-xs;
  }

  &__stat-value {
    font-size: $font-size-subheading;
    font-weight: $font-weight-bold;
    color: $color-ink;
    font-family: $font-family-mono;

    &--success {
      color: $color-semantic-pass;
    }

    &--danger {
      color: $color-semantic-fail;
    }
  }

  &__filters {
    display: flex;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;
  }

  &__history-summary {
    display: flex;
    gap: $spacing-lg;
    margin-bottom: $spacing-md;
    padding: $spacing-sm $spacing-md;
    background-color: $color-surface-1;
    border-radius: $radius-sm;
  }

  &__summary-item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__summary-label {
    color: $color-ink-muted;
    font-size: $font-size-small;
  }

  &__summary-value {
    color: $color-ink;
    font-weight: $font-weight-semibold;
    font-family: $font-family-mono;
  }
}
</style>

import request from './request'

export interface MonitorMetricsVO {
  executionTotal: number
  executionSuccess: number
  executionFailed: number
  successRate: string
  p99DurationMs: number
  p95DurationMs: number
  avgDurationMs: number
  activeRuleCount: number
  formulaHitRate: string
  lastUpdateTime: string
  topRules: RuleStatItem[]
  recentAlerts: AlertItem[]
}

export interface RuleStatItem {
  ruleKey: string
  ruleName: string
  hitCount: number
  hitRate: string
}

export interface AlertItem {
  alertId: string
  alertType: string
  message: string
  level: string
  alertTime: string
}

export const monitorApi = {
  getMetrics(): Promise<any> {
    return request.get('/api/v1/monitor/metrics')
  },

  getAlerts(): Promise<any> {
    return request.get('/api/v1/monitor/alerts')
  },

  getTopRules(): Promise<any> {
    return request.get('/api/v1/monitor/top-rules')
  },

  recordExecution(success: boolean, durationMs: number, ruleKey?: string): Promise<any> {
    return request.post('/api/v1/monitor/record', null, {
      params: { success, durationMs, ruleKey }
    })
  },

  recordAlert(alertType: string, message: string, level: string): Promise<any> {
    return request.post('/api/v1/monitor/alert', null, {
      params: { alertType, message, level }
    })
  }
}
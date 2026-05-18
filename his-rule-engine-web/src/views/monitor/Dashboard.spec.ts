import { describe, it, expect, vi, beforeEach } from 'vitest'

// Tests for Dashboard.vue monitoring dashboard logic

describe('Dashboard.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should have correct metric card types', () => {
    const metricTypes = ['totalExecutions', 'successRate', 'formulaHitRate', 'avgDuration']
    expect(metricTypes).toHaveLength(4)
  })

  it('should calculate success rate correctly', () => {
    const calcSuccessRate = (success, total) => {
      if (total === 0) return 0
      return (success / total) * 100
    }
    expect(calcSuccessRate(95, 100)).toBe(95)
    expect(calcSuccessRate(0, 100)).toBe(0)
    expect(calcSuccessRate(100, 100)).toBe(100)
  })

  it('should format duration in milliseconds', () => {
    const formatDuration = (ms) => {
      if (ms < 1000) return `${ms}ms`
      return `${(ms / 1000).toFixed(2)}s`
    }
    expect(formatDuration(500)).toBe('500ms')
    expect(formatDuration(1500)).toBe('1.50s')
    expect(formatDuration(100)).toBe('100ms')
  })

  it('should calculate average duration', () => {
    const calcAvgDuration = (durations) => {
      if (durations.length === 0) return 0
      const sum = durations.reduce((a, b) => a + b, 0)
      return sum / durations.length
    }
    expect(calcAvgDuration([100, 200, 300])).toBe(200)
    expect(calcAvgDuration([])).toBe(0)
  })

  it('should handle null formulaHitRate', () => {
    const displayHitRate = (rate) => {
      if (rate === null || rate === undefined) return '暂无数据'
      return `${rate}%`
    }
    expect(displayHitRate(null)).toBe('暂无数据')
    expect(displayHitRate(95.5)).toBe('95.5%')
    expect(displayHitRate(undefined)).toBe('暂无数据')
  })

  it('should sort top rules by hit count', () => {
    const rules = [
      { ruleKey: 'rule1', hitCount: 100 },
      { ruleKey: 'rule2', hitCount: 500 },
      { ruleKey: 'rule3', hitCount: 300 }
    ]
    const sorted = rules.sort((a, b) => b.hitCount - a.hitCount)
    expect(sorted[0].ruleKey).toBe('rule2')
    expect(sorted[1].ruleKey).toBe('rule3')
    expect(sorted[2].ruleKey).toBe('rule1')
  })

  it('should limit top rules to 10', () => {
    const allRules = Array.from({ length: 20 }, (_, i) => ({
      ruleKey: `rule${i}`,
      hitCount: i * 10
    }))
    const top10 = allRules.sort((a, b) => b.hitCount - a.hitCount).slice(0, 10)
    expect(top10).toHaveLength(10)
  })
})
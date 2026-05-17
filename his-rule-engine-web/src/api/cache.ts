import request from './request'
import type { AxiosPromise } from 'axios'

export interface CacheStatsVO {
  enabled: boolean
  size: number
  stats: string
  message?: string
}

export const getCacheStats = (): AxiosPromise<CacheStatsVO> => {
  return request({
    url: '/api/v1/formulas/cache/stats',
    method: 'GET',
  })
}

export const refreshCache = (expression?: string): AxiosPromise<void> => {
  return request({
    url: '/api/v1/formulas/cache/refresh',
    method: 'POST',
    params: expression ? { expression } : {},
  })
}
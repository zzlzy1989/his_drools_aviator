import request from './request'
import type { AxiosPromise } from 'axios'

export interface CacheStatsVO {
  aviatorCache?: {
    size: number
    stats: string
  }
  ruleDefinitionCache?: {
    size: number
    stats: string
  }
  timestamp?: number
}

export const getCacheStats = (): AxiosPromise<CacheStatsVO> => {
  return request({
    url: '/api/v1/cache/stats',
    method: 'GET',
  })
}

export const refreshCache = (expression?: string): AxiosPromise<any> => {
  return request({
    url: '/api/v1/cache/refresh',
    method: 'POST',
  })
}

export const getCacheKeys = (cacheName: string): AxiosPromise<any> => {
  return request({
    url: '/api/v1/cache/keys',
    method: 'GET',
    params: { cacheName },
  })
}

export const invalidateCache = (cacheName: string, key?: string): AxiosPromise<any> => {
  return request({
    url: '/api/v1/cache/invalidate',
    method: 'POST',
    params: { cacheName, key },
  })
}
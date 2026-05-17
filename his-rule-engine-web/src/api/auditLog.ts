import request from './request'
import type { AxiosPromise } from 'axios'

export interface AuditLogVO {
  id: number
  tenantId: string
  action: string
  targetType: string
  targetId: string
  targetKey: string
  operator: string
  detail: string
  ipAddress: string
  userAgent: string
  createTime: string
}

export interface AuditLogQueryDTO {
  action?: string
  targetType?: string
  targetKey?: string
  operator?: string
  startDate?: string
  endDate?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getAuditLogPage = (
  page: number,
  pageSize: number,
  query?: AuditLogQueryDTO
): AxiosPromise<PageResult<AuditLogVO>> => {
  return request({
    url: '/api/v1/audit-logs',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}
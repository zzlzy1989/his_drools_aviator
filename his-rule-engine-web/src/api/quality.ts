import request from './request'
import type { AxiosPromise } from 'axios'

export interface QualityVO {
  id: number
  itemKey: string
  itemName: string
  category: string
  level: 'info' | 'warning' | 'error'
  description?: string
  status: 'active' | 'inactive'
  tenantId: string
  createTime: string
  updateTime: string
}

export interface CreateQualityDTO {
  itemKey: string
  itemName: string
  category: string
  level: string
  description?: string
  status?: string
}

export interface UpdateQualityDTO {
  itemName?: string
  category?: string
  level?: string
  description?: string
  status?: string
}

export interface QualityQueryDTO {
  itemName?: string
  category?: string
  level?: string
  status?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export const getQualityPage = (
  page: number,
  pageSize: number,
  query?: QualityQueryDTO
): AxiosPromise<PageResult<QualityVO>> => {
  return request({
    url: '/api/v1/quality',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const createQuality = (data: CreateQualityDTO): AxiosPromise<number> => {
  return request({ url: '/api/v1/quality', method: 'POST', data })
}

export const updateQuality = (id: number, data: UpdateQualityDTO): AxiosPromise<void> => {
  return request({ url: `/api/v1/quality/${id}`, method: 'PUT', data })
}

export const deleteQuality = (id: number): AxiosPromise<void> => {
  return request({ url: `/api/v1/quality/${id}`, method: 'DELETE' })
}

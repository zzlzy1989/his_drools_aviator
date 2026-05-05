import request from './request'
import type { AxiosPromise } from 'axios'

export interface DrgVO {
  id: number
  drgCode: string
  drgName: string
  category: string
  weight: number
  description?: string
  status: 'active' | 'inactive'
  tenantId: string
  createTime: string
  updateTime: string
}

export interface CreateDrgDTO {
  drgCode: string
  drgName: string
  category: string
  weight: number
  description?: string
  status?: string
}

export interface UpdateDrgDTO {
  drgName?: string
  category?: string
  weight?: number
  description?: string
  status?: string
}

export interface DrgQueryDTO {
  drgName?: string
  category?: string
  status?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export const getDrgPage = (
  page: number,
  pageSize: number,
  query?: DrgQueryDTO
): AxiosPromise<PageResult<DrgVO>> => {
  return request({
    url: '/api/v1/drg',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const createDrg = (data: CreateDrgDTO): AxiosPromise<number> => {
  return request({ url: '/api/v1/drg', method: 'POST', data })
}

export const updateDrg = (id: number, data: UpdateDrgDTO): AxiosPromise<void> => {
  return request({ url: `/api/v1/drg/${id}`, method: 'PUT', data })
}

export const deleteDrg = (id: number): AxiosPromise<void> => {
  return request({ url: `/api/v1/drg/${id}`, method: 'DELETE' })
}

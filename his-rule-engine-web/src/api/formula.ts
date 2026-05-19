import request from './request'
import type { AxiosPromise } from 'axios'

export interface FormulaVO {
  id: number
  formulaKey: string
  formulaName: string
  expression: string
  category: string
  returnType: string
  description?: string
  status: 'draft' | 'active' | 'inactive'
  tenantId: string
  createBy?: string
  createTime: string
  updateTime: string
}

export interface CreateFormulaDTO {
  formulaKey: string
  formulaName: string
  expression: string
  category: string
  returnType: string
  description?: string
  status?: string
}

export interface UpdateFormulaDTO {
  formulaName?: string
  expression?: string
  category?: string
  returnType?: string
  description?: string
  status?: string
}

export interface FormulaQueryDTO {
  formulaName?: string
  category?: string
  status?: string
  tenantId?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface FormulaTestResult {
  result: any
  executionTime: number
  success: boolean
}

export const getFormulaPage = (
  page: number,
  pageSize: number,
  query?: FormulaQueryDTO
): AxiosPromise<PageResult<FormulaVO>> => {
  return request({
    url: '/api/v1/formulas',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const getFormulaById = (id: number): AxiosPromise<FormulaVO> => {
  return request({
    url: `/api/v1/formulas/${id}`,
    method: 'GET',
  })
}

export const createFormula = (data: CreateFormulaDTO): AxiosPromise<number> => {
  return request({
    url: '/api/v1/formulas',
    method: 'POST',
    data,
  })
}

export const updateFormula = (id: number, data: UpdateFormulaDTO): AxiosPromise<void> => {
  return request({
    url: `/api/v1/formulas/${id}`,
    method: 'PUT',
    data,
  })
}

export const deleteFormula = (id: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/formulas/${id}`,
    method: 'DELETE',
  })
}

export const testFormula = (
  id: number,
  params: Record<string, any>
): AxiosPromise<FormulaTestResult> => {
  return request({
    url: `/api/v1/formulas/${id}/test`,
    method: 'POST',
    data: params,
  })
}

export const validateFormula = (id: number): AxiosPromise<{ valid: boolean; errors: string[] }> => {
  return request({
    url: `/api/v1/formulas/${id}/validate`,
    method: 'GET',
  })
}

// ===== 缓存管理 =====
export const cacheApi = {
  getStats(): AxiosPromise<any> {
    return request.get('/api/v1/cache/stats')
  },

  getKeys(cacheName: string): AxiosPromise<any> {
    return request.get('/api/v1/cache/keys', { params: { cacheName } })
  },

  invalidate(cacheName: string, key?: string): AxiosPromise<any> {
    return request.post('/api/v1/cache/invalidate', null, { params: { cacheName, key } })
  },

  refreshAll(): AxiosPromise<any> {
    return request.post('/api/v1/cache/refresh')
  }
}

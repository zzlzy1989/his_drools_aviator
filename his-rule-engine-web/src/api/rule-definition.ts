import request from './request'
import type { AxiosPromise } from 'axios'

export interface RuleDefinitionVO {
  id: number
  ruleKey: string
  ruleName: string
  groupId: number
  groupName?: string
  ruleText: string
  category: string
  priority: number
  status: 'draft' | 'active' | 'inactive'
  description?: string
  tenantId: string
  createBy?: string
  createTime: string
  updateTime: string
}

export interface CreateRuleDTO {
  ruleKey: string
  ruleName: string
  groupId: number
  ruleText: string
  category: string
  priority?: number
  status?: string
  description?: string
}

export interface UpdateRuleDTO {
  ruleName?: string
  groupId?: number
  ruleText?: string
  category?: string
  priority?: number
  status?: string
  description?: string
}

export interface RuleQueryDTO {
  ruleName?: string
  groupId?: number
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

export const getRulePage = (
  page: number,
  pageSize: number,
  query?: RuleQueryDTO
): AxiosPromise<PageResult<RuleDefinitionVO>> => {
  return request({
    url: '/api/v1/rules',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const getRuleById = (id: number): AxiosPromise<RuleDefinitionVO> => {
  return request({
    url: `/api/v1/rules/${id}`,
    method: 'GET',
  })
}

export const createRule = (data: CreateRuleDTO): AxiosPromise<number> => {
  return request({
    url: '/api/v1/rules',
    method: 'POST',
    data,
  })
}

export const updateRule = (id: number, data: UpdateRuleDTO): AxiosPromise<void> => {
  return request({
    url: `/api/v1/rules/${id}`,
    method: 'PUT',
    data,
  })
}

export const deleteRule = (id: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/rules/${id}`,
    method: 'DELETE',
  })
}

export const publishRule = (id: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/rules/${id}/publish`,
    method: 'POST',
  })
}

export const validateRule = (id: number): AxiosPromise<{ valid: boolean; errors: string[] }> => {
  return request({
    url: `/api/v1/rules/${id}/validate`,
    method: 'GET',
  })
}

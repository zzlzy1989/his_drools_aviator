import request from './request'
import type { AxiosPromise } from 'axios'

export interface RuleGroupVO {
  id: number
  groupKey: string
  groupName: string
  description?: string
  category?: string
  sortOrder?: number
  status: 'active' | 'inactive'
  tenantId: string
  createBy?: string
  createTime: string
  updateTime: string
}

export interface CreateRuleGroupDTO {
  groupCode: string
  groupName: string
  description?: string
  priority?: number
}

export interface UpdateRuleGroupDTO {
  groupName?: string
  description?: string
  priority?: number
}

export interface RuleGroupQueryDTO {
  groupName?: string
  status?: string
  tenantId?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export const getRuleGroupPage = (
  page: number,
  pageSize: number,
  query?: RuleGroupQueryDTO
): AxiosPromise<PageResult<RuleGroupVO>> => {
  return request({
    url: '/api/v1/rule-groups/page',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const getRuleGroupById = (id: number): AxiosPromise<RuleGroupVO> => {
  return request({
    url: `/api/v1/rule-groups/${id}`,
    method: 'GET',
  })
}

export const createRuleGroup = (data: CreateRuleGroupDTO): AxiosPromise<RuleGroupVO> => {
  return request({
    url: '/api/v1/rule-groups',
    method: 'POST',
    data,
  })
}

export const updateRuleGroup = (id: number, data: UpdateRuleGroupDTO): AxiosPromise<RuleGroupVO> => {
  return request({
    url: `/api/v1/rule-groups/${id}`,
    method: 'PUT',
    data,
  })
}

export const deleteRuleGroup = (id: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/rule-groups/${id}`,
    method: 'DELETE',
  })
}

export const toggleRuleGroupStatus = (id: number, enabled: boolean): AxiosPromise<void> => {
  return request({
    url: `/api/v1/rule-groups/${id}/enabled`,
    method: 'PUT',
    params: { enabled },
  })
}

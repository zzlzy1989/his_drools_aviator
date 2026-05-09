import request from './request'
import type { AxiosPromise } from 'axios'

// Node type definition
export interface NodeDTO {
  nodeId: string
  type: 'start' | 'end' | 'condition' | 'action' | 'formula' | 'subflow'
  label: string
  expression?: string
  ruleKey?: string
  formulaKey?: string
  flowId?: string
  branches?: Record<string, string>
  params?: Record<string, any>
  timeout?: number
  async?: boolean
  x?: number
  y?: number
}

// Edge type definition
export interface EdgeDTO {
  source: string
  target: string
  label?: string
}

// Flow definition
export interface FlowDefinitionDTO {
  nodes: NodeDTO[]
  edges: EdgeDTO[]
}

// Create flow DTO
export interface CreateFlowDTO {
  flowKey: string
  flowName: string
  category?: string
  flowDefinition: FlowDefinitionDTO
  description?: string
}

// Update flow DTO
export interface UpdateFlowDTO {
  flowName?: string
  category?: string
  flowDefinition?: FlowDefinitionDTO
  description?: string
  changeDescription?: string
}

// Flow query DTO
export interface FlowQueryDTO {
  flowName?: string
  category?: string
  status?: string
  tenantId?: string
}

// Flow VO
export interface RuleFlowVO {
  id: number
  flowKey: string
  flowName: string
  flowDefinition: FlowDefinitionDTO
  version: number
  status: 'draft' | 'active' | 'inactive'
  category?: string
  tenantId: string
  createBy?: string
  createTime: string
  updateBy?: string
  updateTime: string
}

// Flow version VO
export interface FlowVersionVO {
  id: number
  flowId: number
  version: number
  flowDefinition: FlowDefinitionDTO
  changeDesc?: string
  changeBy: string
  changeTime: string
}

// Page result
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export type FlowStatus = 'draft' | 'active' | 'inactive'

// API functions
export const getFlowPage = (page: number, pageSize: number, query?: FlowQueryDTO): AxiosPromise<PageResult<RuleFlowVO>> => {
  return request({
    url: '/api/v1/flows',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const getFlowById = (id: number): AxiosPromise<RuleFlowVO> => {
  return request({
    url: `/api/v1/flows/${id}`,
    method: 'GET',
  })
}

export const createFlow = (data: CreateFlowDTO): AxiosPromise<number> => {
  return request({
    url: '/api/v1/flows',
    method: 'POST',
    data,
  })
}

export const updateFlow = (id: number, data: UpdateFlowDTO): AxiosPromise<void> => {
  return request({
    url: `/api/v1/flows/${id}`,
    method: 'PUT',
    data,
  })
}

export const deleteFlow = (id: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/flows/${id}`,
    method: 'DELETE',
  })
}

export const publishFlow = (id: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/flows/${id}/publish`,
    method: 'POST',
  })
}

export const rollbackFlow = (id: number, targetVersion: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/flows/${id}/rollback`,
    method: 'POST',
    params: { targetVersion },
  })
}

export const getFlowVersions = (id: number): AxiosPromise<FlowVersionVO[]> => {
  return request({
    url: `/api/v1/flows/${id}/versions`,
    method: 'GET',
  })
}

export const exportFlow = (id: number): AxiosPromise<string> => {
  return request({
    url: `/api/v1/flows/${id}/export`,
    method: 'GET',
  })
}

export const importFlow = (flowJson: string, tenantId?: string): AxiosPromise<number> => {
  return request({
    url: '/api/v1/flows/import',
    method: 'POST',
    data: flowJson,
    params: { tenantId },
  })
}
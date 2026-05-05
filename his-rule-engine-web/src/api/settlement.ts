import request from './request'
import type { AxiosPromise } from 'axios'

export interface SettlementVO {
  id: number
  settlementNo: string
  patientId: string
  patientName: string
  totalFee: number
  reimburseAmount: number
  selfPayAmount: number
  status: 'pending' | 'processing' | 'completed' | 'rejected'
  flowId?: number
  flowName?: string
  settlementResult?: string
  remarks?: string
  tenantId: string
  createBy?: string
  createTime: string
  updateTime: string
}

export interface CreateSettlementDTO {
  patientId: string
  patientName: string
  totalFee: number
  flowId?: number
  remarks?: string
  items?: Array<{
    itemName: string
    quantity: number
    unitPrice: number
  }>
}

export interface SettlementQueryDTO {
  settlementNo?: string
  patientId?: string
  status?: string
  startTime?: string
  endTime?: string
  tenantId?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface SettlementDetailVO {
  id: number
  settlementId: number
  itemId: string
  itemName: string
  quantity: number
  unitPrice: number
  totalPrice: number
  reimburseRatio: number
  reimburseAmount: number
  selfPayAmount: number
}

export const getSettlementPage = (
  page: number,
  pageSize: number,
  query?: SettlementQueryDTO
): AxiosPromise<PageResult<SettlementVO>> => {
  return request({
    url: '/api/v1/settlements',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const getSettlementById = (id: number): AxiosPromise<SettlementVO> => {
  return request({
    url: `/api/v1/settlements/${id}`,
    method: 'GET',
  })
}

export const createSettlement = (data: CreateSettlementDTO): AxiosPromise<number> => {
  return request({
    url: '/api/v1/settlements',
    method: 'POST',
    data,
  })
}

export const executeSettlement = (id: number): AxiosPromise<SettlementVO> => {
  return request({
    url: `/api/v1/settlements/${id}/execute`,
    method: 'POST',
  })
}

export const cancelSettlement = (id: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/settlements/${id}/cancel`,
    method: 'POST',
  })
}

export const getSettlementDetails = (id: number): AxiosPromise<SettlementDetailVO[]> => {
  return request({
    url: `/api/v1/settlements/${id}/details`,
    method: 'GET',
  })
}

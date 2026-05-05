import request from './request'
import type { AxiosPromise } from 'axios'

export interface DrugVO {
  id: number
  drugCode: string
  drugName: string
  genericName: string
  specification: string
  manufacturer: string
  drugType: 'western' | 'chinese' | 'biological' | 'device'
  prescriptionFlag: boolean
  insuranceType: 'jia' | 'yi' | 'bing' | 'self'
  unitPrice: number
  status: 'active' | 'inactive'
  description?: string
  tenantId: string
  createBy?: string
  createTime: string
  updateTime: string
}

export interface CreateDrugDTO {
  drugCode: string
  drugName: string
  genericName: string
  specification: string
  manufacturer: string
  drugType: string
  prescriptionFlag: boolean
  insuranceType: string
  unitPrice: number
  description?: string
}

export interface UpdateDrugDTO {
  drugName?: string
  genericName?: string
  specification?: string
  manufacturer?: string
  drugType?: string
  prescriptionFlag?: boolean
  insuranceType?: string
  unitPrice?: number
  description?: string
  status?: string
}

export interface DrugQueryDTO {
  drugName?: string
  drugType?: string
  insuranceType?: string
  status?: string
  tenantId?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface DrugInteractionCheckResult {
  hasInteraction: boolean
  interactions: Array<{
    drug1: string
    drug2: string
    level: 'warning' | 'danger'
    description: string
  }>
}

export const getDrugPage = (
  page: number,
  pageSize: number,
  query?: DrugQueryDTO
): AxiosPromise<PageResult<DrugVO>> => {
  return request({
    url: '/api/v1/drugs',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const getDrugById = (id: number): AxiosPromise<DrugVO> => {
  return request({
    url: `/api/v1/drugs/${id}`,
    method: 'GET',
  })
}

export const createDrug = (data: CreateDrugDTO): AxiosPromise<number> => {
  return request({
    url: '/api/v1/drugs',
    method: 'POST',
    data,
  })
}

export const updateDrug = (id: number, data: UpdateDrugDTO): AxiosPromise<void> => {
  return request({
    url: `/api/v1/drugs/${id}`,
    method: 'PUT',
    data,
  })
}

export const deleteDrug = (id: number): AxiosPromise<void> => {
  return request({
    url: `/api/v1/drugs/${id}`,
    method: 'DELETE',
  })
}

export const checkDrugInteraction = (
  drugIds: number[]
): AxiosPromise<DrugInteractionCheckResult> => {
  return request({
    url: '/api/v1/drugs/check-interaction',
    method: 'POST',
    data: { drugIds },
  })
}

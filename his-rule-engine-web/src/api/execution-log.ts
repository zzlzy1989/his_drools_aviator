import request from './request'
import type { AxiosPromise } from 'axios'

export interface TestExecutionLogVO {
  id: number
  testCaseId: number
  dataSetId: number
  caseName: string
  inputJson: string
  expectedJson: string
  actualJson: string
  diffJson: string
  status: string
  elapsedMs: number
  errorMessage?: string
  executedBy: string
  executeTime: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
  hasNext: boolean
}

export interface ExecutionLogQueryDTO {
  status?: string
  startDate?: string
  endDate?: string
}

export const getExecutionLogs = (page: number, pageSize: number, query?: ExecutionLogQueryDTO): AxiosPromise<PageResult<TestExecutionLogVO>> => {
  return request({
    url: '/api/v1/sandbox/execution-logs',
    method: 'GET',
    params: { page, pageSize, ...query },
  })
}

export const getExecutionLogById = (id: number): AxiosPromise<TestExecutionLogVO> => {
  return request({
    url: `/api/v1/sandbox/execution-logs/${id}`,
    method: 'GET',
  })
}
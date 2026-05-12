import request from './request'

export interface TestCaseDTO {
  id?: number
  caseId: string
  caseName: string
  fact?: Record<string, any>
  expected?: Record<string, any>
  status?: number
  lastResult?: string
}

export interface TestDataSetDTO {
  id?: number
  dataSetName: string
  description?: string
  category?: string
  testCases?: TestCaseDTO[]
}

export function listDatasets(category?: string) {
  return request.get<any, TestDataSetDTO[]>('/api/v1/sandbox/datasets', { params: { category } })
}

export function getDataset(id: number) {
  return request.get<any, TestDataSetDTO>(`/api/v1/sandbox/datasets/${id}`)
}

export function createDataset(data: TestDataSetDTO) {
  return request.post<any, TestDataSetDTO>('/api/v1/sandbox/datasets', data)
}

export function updateDataset(id: number, data: TestDataSetDTO) {
  return request.put<any, TestDataSetDTO>(`/api/v1/sandbox/datasets/${id}`, data)
}

export function deleteDataset(id: number) {
  return request.delete<any, void>(`/api/v1/sandbox/datasets/${id}`)
}

export function executeCase(caseId: number) {
  return request.post<any, Record<string, any>>(`/api/v1/sandbox/execute/${caseId}`, {})
}

export function batchExecute(dataSetId: number) {
  return request.post<any, Record<string, any>[]>(`/api/v1/sandbox/batch-execute/${dataSetId}`, {})
}
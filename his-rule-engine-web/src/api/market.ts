import request from './request'

export interface TemplateContentDTO {
  rules?: Record<string, any>[]
  formulas?: Record<string, any>[]
  flows?: Record<string, any>[]
}

export interface RuleTemplateDTO {
  id?: number
  templateKey?: string
  name: string
  category: string
  tags?: string
  description?: string
  version?: string
  content?: TemplateContentDTO
  rules?: Record<string, any>[]
  formulas?: Record<string, any>[]
  flows?: Record<string, any>[]
  providerId?: string
  providerName?: string
  publishedBy?: string
  status?: string
  installCount?: number
  tenantId?: string
  createBy?: string
  createTime?: string
}

export const marketApi = {
  getTemplates(params: {
    page?: number
    pageSize?: number
    category?: string
    keyword?: string
  }) {
    return request.get<any, any>('/api/v1/market/templates', { params })
  },

  getTemplate(id: number) {
    return request.get<any, RuleTemplateDTO>(`/api/v1/market/templates/${id}`)
  },

  publishTemplate(data: RuleTemplateDTO) {
    return request.post<any, RuleTemplateDTO>('/api/v1/market/templates', data)
  },

  updateTemplate(id: number, data: RuleTemplateDTO) {
    return request.put<any, RuleTemplateDTO>(`/api/v1/market/templates/${id}`, data)
  },

  deleteTemplate(id: number) {
    return request.delete<any, void>(`/api/v1/market/templates/${id}`)
  },

  installTemplate(id: number) {
    return request.post<any, void>(`/api/v1/market/templates/${id}/install`, {})
  },

  uninstallTemplate(id: number) {
    return request.delete<any, void>(`/api/v1/market/templates/${id}/install`)
  },

  getMyTemplates() {
    return request.get<any, RuleTemplateDTO[]>('/api/v1/market/templates/my')
  },

  getSubscribedTemplates() {
    return request.get<any, RuleTemplateDTO[]>('/api/v1/market/templates/subscribed')
  }
}
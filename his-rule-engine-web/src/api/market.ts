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
  ratingSummary?: {
    count: number
    avgRating: number
    distribution: Record<string, number>
  }
}

export interface TemplateRatingDTO {
  rating: number
  comment?: string
}

export interface RatingSummaryVO {
  count: number
  avgRating: number
  distribution: Record<string, number>
}

export interface RatingItemVO {
  id: number
  userId: string
  rating: number
  comment: string
  createTime: string
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
  },

  getRatings(templateId: number) {
    return request.get<any, RatingItemVO[]>(`/api/v1/market/templates/${templateId}/ratings`)
  },

  getRatingSummary(templateId: number) {
    return request.get<any, RatingSummaryVO>(`/api/v1/market/templates/${templateId}/rating-summary`)
  },

  rateTemplate(templateId: number, data: TemplateRatingDTO) {
    return request.post<any, void>(`/api/v1/market/templates/${templateId}/ratings`, data)
  },

  getFavorites() {
    return request.get<any, any[]>('/api/v1/market/templates/favorites')
  },

  favorite(templateId: number) {
    return request.post<any, void>(`/api/v1/market/templates/${templateId}/favorite`, {})
  },

  unfavorite(templateId: number) {
    return request.delete<any, void>(`/api/v1/market/templates/${templateId}/favorite`)
  },

  getFavoriteStatus(templateId: number) {
    return request.get<any, { favorited: boolean }>(`/api/v1/market/templates/${templateId}/favorite-status`)
  },

  checkUpdate(templateId: number) {
    return request.get<any, { templateId: number, currentVersion: string, latestVersion: string, hasUpdate: boolean }>(`/api/v1/market/templates/${templateId}/check-update`)
  },

  upgrade(templateId: number) {
    return request.post<any, void>(`/api/v1/market/templates/${templateId}/upgrade`, {})
  }
}
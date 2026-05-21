import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userId: string
  username: string
  realName: string
  tenantId: string
  role: string
}

export function loginApi(data: LoginParams): Promise<LoginResult> {
  return request({
    url: '/api/v1/auth/login',
    method: 'POST',
    data,
  })
}

export function getCurrentUser(): Promise<{ userId: string; tenantId: string }> {
  return request({
    url: '/api/v1/auth/me',
    method: 'GET',
  })
}

import request from './request'

export function login(data: { username: string; password: string }) {
  return request({
    url: '/api/v1/auth/login',
    method: 'post',
    data,
  })
}

export function getCurrentUser() {
  return request({
    url: '/api/v1/auth/me',
    method: 'get',
  })
}
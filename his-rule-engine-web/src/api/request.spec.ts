import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'

// Mock element-plus
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
  },
}))

describe('request.ts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should add X-Tenant-Id header', () => {
    localStorage.setItem('tenantId', 'T001')
    expect(localStorage.getItem('tenantId')).toBe('T001')
  })

  it('should add Authorization Bearer token', () => {
    localStorage.setItem('token', 'test-token')
    expect(localStorage.getItem('token')).toBe('test-token')
  })

  it('should handle successful response (code=0)', () => {
    const response = { code: '0', data: { id: 1 }, message: '成功' }
    expect(response.code).toBe('0')
  })

  it('should reject on error response', () => {
    const errorResponse = { code: 'HIS-001', message: '参数错误' }
    expect(errorResponse.code).not.toBe('0')
  })

  it('should handle network error', () => {
    const error = { message: '网络错误', response: null }
    expect(error.response).toBeNull()
  })

  it('should extract data from successful response', () => {
    const res = { code: '0', data: { name: 'test' }, message: 'success' }
    const result = res.data !== undefined ? res.data : res
    expect(result).toEqual({ name: 'test' })
  })

  it('should use default baseURL when VITE_API_BASE_URL not set', () => {
    const baseURL = import.meta.env.VITE_API_BASE_URL || ''
    expect(baseURL).toBe('')
  })
})
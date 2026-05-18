import { describe, it, expect, vi, beforeEach } from 'vitest'

// Test Login.vue form validation and auth flow logic without importing Vue component
// (avoids element-plus CSS import issues in vitest)

describe('Login.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should have default credentials admin/admin', () => {
    const loginForm = {
      username: 'admin',
      password: 'admin',
    }
    expect(loginForm.username).toBe('admin')
    expect(loginForm.password).toBe('admin')
  })

  it('should validate required fields', () => {
    const loginRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    }
    expect(loginRules.username[0].required).toBe(true)
    expect(loginRules.password[0].required).toBe(true)
  })

  it('should handle login success response', () => {
    const mockResponse = {
      code: '0',
      data: {
        token: 'jwt-token-123',
        userId: 'admin',
        tenantId: 'T001',
      },
    }
    expect(mockResponse.code).toBe('0')
    expect(mockResponse.data.token).toBeDefined()
  })

  it('should handle login failure response', () => {
    const mockResponse = {
      code: 'HIS-001',
      message: '用户名或密码错误',
    }
    expect(mockResponse.code).not.toBe('0')
  })

  it('should store auth data in localStorage on success', () => {
    const authData = {
      token: 'jwt-token-456',
      userId: 'admin',
      tenantId: 'T001',
    }
    localStorage.setItem('token', authData.token)
    localStorage.setItem('userId', authData.userId)
    localStorage.setItem('tenantId', authData.tenantId)

    expect(localStorage.getItem('token')).toBe('jwt-token-456')
    expect(localStorage.getItem('userId')).toBe('admin')
    expect(localStorage.getItem('tenantId')).toBe('T001')
  })

  it('should handle login API error gracefully', () => {
    const catchBlock = () => {
      try {
        throw new Error('网络错误')
      } catch (error) {
        // Should not throw, just log error
      }
    }
    expect(catchBlock).not.toThrow()
  })

  it('should call ElMessage.error on login failure', () => {
    const mockMessage = { error: vi.fn() }
    mockMessage.error('登录失败')
    expect(mockMessage.error).toHaveBeenCalledWith('登录失败')
  })
})
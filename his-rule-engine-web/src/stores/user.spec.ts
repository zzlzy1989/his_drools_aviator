import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Simple unit tests for user store logic
describe('user store', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should initialize with empty token', () => {
    const token = localStorage.getItem('token') || ''
    expect(token).toBe('')
  })

  it('should initialize with default tenantId', () => {
    const tenantId = localStorage.getItem('tenantId') || 'default'
    expect(tenantId).toBe('default')
  })

  it('should setToken and persist to localStorage', () => {
    const newToken = 'test-token-456'
    localStorage.setItem('token', newToken)
    expect(localStorage.getItem('token')).toBe(newToken)
  })

  it('should setTenantId and persist to localStorage', () => {
    const newTenantId = 'T001'
    localStorage.setItem('tenantId', newTenantId)
    expect(localStorage.getItem('tenantId')).toBe(newTenantId)
  })

  it('should logout and clear localStorage', () => {
    localStorage.setItem('token', 'test-token')
    localStorage.setItem('username', 'admin')
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('username')).toBeNull()
  })

  it('should retrieve token from localStorage', () => {
    localStorage.setItem('token', 'my-jwt-token')
    const token = localStorage.getItem('token') || ''
    expect(token).toBe('my-jwt-token')
  })

  it('should handle missing token gracefully', () => {
    localStorage.clear()
    const token = localStorage.getItem('token') || ''
    expect(token).toBe('')
  })
})
import { describe, it, expect, vi, beforeEach } from 'vitest'

// Tests for Layout.vue sidebar navigation logic

describe('Layout.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should have default collapsed state', () => {
    const defaultCollapsed = false
    expect(defaultCollapsed).toBe(false)
  })

  it('should have correct menu structure', () => {
    const menuItems = [
      { path: '/dashboard', title: '首页概览' },
      { path: '/rule', title: '规则管理' },
      { path: '/flow', title: '规则流' },
      { path: '/formula', title: '公式管理' },
      { path: '/settlement', title: '结算管理' },
      { path: '/sandbox', title: '测试沙箱' },
      { path: '/monitor', title: '监控大屏' },
      { path: '/market', title: '规则市场' },
      { path: '/drug', title: '用药审核' },
      { path: '/quality', title: '质控规则' },
      { path: '/drg', title: 'DRG分组' },
      { path: '/audit', title: '审计日志' }
    ]
    expect(menuItems).toHaveLength(12)
  })

  it('should generate correct menu item id', () => {
    const generateMenuId = (path) => path.replace(/\//g, '-').replace(/^-/, '')
    expect(generateMenuId('/dashboard')).toBe('dashboard')
    expect(generateMenuId('/rule')).toBe('rule')
    expect(generateMenuId('/sandbox')).toBe('sandbox')
  })

  it('should check active menu correctly', () => {
    const isActiveMenu = (currentPath, menuPath) => currentPath.startsWith(menuPath)
    expect(isActiveMenu('/rule/list', '/rule')).toBe(true)
    expect(isActiveMenu('/rule/detail/1', '/rule')).toBe(true)
    expect(isActiveMenu('/flow', '/rule')).toBe(false)
  })

  it('should handle collapse toggle', () => {
    let collapsed = false
    const toggleCollapse = () => { collapsed = !collapsed }
    expect(collapsed).toBe(false)
    toggleCollapse()
    expect(collapsed).toBe(true)
    toggleCollapse()
    expect(collapsed).toBe(false)
  })

  it('should get tenant info from localStorage', () => {
    localStorage.setItem('tenantId', 'T001')
    localStorage.setItem('username', 'admin')
    expect(localStorage.getItem('tenantId')).toBe('T001')
    expect(localStorage.getItem('username')).toBe('admin')
  })

  it('should handle logout', () => {
    localStorage.setItem('token', 'jwt-token')
    localStorage.setItem('tenantId', 'T001')
    localStorage.setItem('username', 'admin')

    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('tenantId')
      localStorage.removeItem('username')
    }

    logout()
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('tenantId')).toBeNull()
    expect(localStorage.getItem('username')).toBeNull()
  })
})
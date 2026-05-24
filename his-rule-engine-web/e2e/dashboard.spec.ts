import { test, expect } from '@playwright/test'

test.describe('首页/仪表盘', () => {
  // 需要先登录
  test.beforeEach(async ({ page }) => {
    // 访问登录页
    await page.goto('/login')

    // 输入账号密码
    const usernameInput = page.locator('input[type="text"], input[placeholder*="账号"]')
    await usernameInput.fill('admin')

    const passwordInput = page.locator('input[type="password"]')
    await passwordInput.fill('admin123')

    // 点击登录
    const loginButton = page.locator('button[type="submit"], button:has-text("登录")')
    await loginButton.click()

    // 等待跳转到 dashboard
    await page.waitForURL('**/dashboard**', { timeout: 10000 })
  })

  test('Dashboard 页面加载正常', async ({ page }) => {
    // 验证页面标题或面包屑
    await expect(page.locator('.his-layout, [class*="layout"]')).toBeVisible()

    // 验证侧边栏菜单存在
    await expect(page.locator('.el-menu, [class*="menu"]').first()).toBeVisible()
  })

  test('点击菜单可以切换到规则定义页', async ({ page }) => {
    // 点击规则管理子菜单
    const ruleMenuItem = page.locator('.el-sub-menu:has-text("规则管理"), .el-menu-item:has-text("规则定义")')
    await ruleMenuItem.first().click()

    // 等待页面切换
    await page.waitForURL('**/rule**', { timeout: 5000 })

    // 验证规则列表页面加载
    await expect(page.locator('.el-table, [class*="table"]').first()).toBeVisible({ timeout: 5000 })
  })
})
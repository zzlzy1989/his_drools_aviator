import { test, expect } from '@playwright/test'

test.describe('规则定义管理', () => {
  // 需要先登录
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    const usernameInput = page.locator('input[type="text"], input[placeholder*="账号"]')
    await usernameInput.fill('admin')
    const passwordInput = page.locator('input[type="password"]')
    await passwordInput.fill('admin123')
    const loginButton = page.locator('.el-button').first()
    await loginButton.click()
    await page.waitForURL('**/dashboard**', { timeout: 10000 })
  })

  test('进入规则定义页面', async ({ page }) => {
    // 直接导航到规则页面
    await page.goto('/rule')
    await page.waitForLoadState('networkidle')

    // 验证表格存在
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 5000 })
  })

  test('规则列表显示正常', async ({ page }) => {
    await page.goto('/rule')
    await page.waitForLoadState('networkidle')

    // 等待表格加载
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 8000 })

    // 验证表格有列头
    await expect(page.locator('.el-table__header th').first()).toBeVisible()
  })
})
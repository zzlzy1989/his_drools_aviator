import { test, expect } from '@playwright/test'

test.describe('结算管理', () => {
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

  test('进入结算管理页面', async ({ page }) => {
    await page.goto('/settlement')
    await page.waitForLoadState('networkidle')

    // 验证页面加载
    await expect(page.locator('.el-table, [class*="card"]').first()).toBeVisible({ timeout: 5000 })
  })

  test('结算列表显示正常', async ({ page }) => {
    await page.goto('/settlement')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.el-table, [class*="content"]').first()).toBeVisible({ timeout: 8000 })
  })
})
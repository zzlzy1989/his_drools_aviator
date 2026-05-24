import { test, expect } from '@playwright/test'

test.describe('规则流设计', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    const usernameInput = page.locator('input[type="text"], input[placeholder*="账号"]')
    await usernameInput.fill('admin')
    const passwordInput = page.locator('input[type="password"]')
    await passwordInput.fill('admin123')
    const loginButton = page.locator('button[type="submit"], button:has-text("登录")')
    await loginButton.click()
    await page.waitForURL('**/dashboard**', { timeout: 10000 })
  })

  test('进入规则流列表页面', async ({ page }) => {
    const flowMenu = page.locator('.el-menu-item:has-text("规则流")')
    await flowMenu.click()
    await page.waitForURL('**/flow**', { timeout: 5000 })

    // 验证规则流列表加载
    await expect(page.locator('.el-table, [class*="flow"]').first()).toBeVisible({ timeout: 5000 })
  })

  test('规则流列表显示正常', async ({ page }) => {
    await page.goto('/flow')
    await page.waitForLoadState('networkidle')

    // 验证表格或卡片列表
    await expect(page.locator('.el-table, [class*="list"]').first()).toBeVisible({ timeout: 8000 })
  })

  test('点击新增按钮打开创建弹窗', async ({ page }) => {
    await page.goto('/flow')
    await page.waitForLoadState('networkidle')

    // 点击新增按钮
    const addButton = page.locator('button:has-text("新增"), button:has-text("新建"), .el-button:has-text("新增")')
    await addButton.first().click()

    // 验证弹窗出现
    await expect(page.locator('.el-dialog, [class*="dialog"]').first()).toBeVisible({ timeout: 3000 })
  })
})
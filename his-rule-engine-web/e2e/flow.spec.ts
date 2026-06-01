import { expect, test } from '@playwright/test'

test.describe('规则流设计', () => {
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

  test('进入规则流列表页面', async ({ page }) => {
    await page.goto('/flow')
    await page.waitForLoadState('networkidle')

    // 验证规则流列表加载
    await expect(page.locator('.el-table, [class*="flow"]').first()).toBeVisible({ timeout: 5000 })
  })

  test('规则流列表显示正常', async ({ page }) => {
    await page.goto('/flow')
    await page.waitForLoadState('networkidle')

    // 验证表格或卡片列表
    await expect(page.locator('.el-table, [class*="list"]').first()).toBeVisible({ timeout: 8000 })
  })

  test('点击新建按钮进入规则流编辑器', async ({ page }) => {
    await page.goto('/flow')
    await page.waitForLoadState('networkidle')

    // 点击新建规则流按钮 - 应该跳转到编辑器页面
    const addButton = page.locator('.el-button:has-text("新建规则流")')
    await addButton.click()

    // 等待跳转到编辑器页面
    await page.waitForURL('**/flow/editor**', { timeout: 5000 })

    // 验证编辑器页面加载
    await expect(page.locator('.flow-editor, [class*="editor"]').first()).toBeVisible({ timeout: 5000 })
  })
})
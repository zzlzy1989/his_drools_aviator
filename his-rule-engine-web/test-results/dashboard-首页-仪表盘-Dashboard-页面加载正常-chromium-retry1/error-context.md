# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: dashboard.spec.ts >> 首页/仪表盘 >> Dashboard 页面加载正常
- Location: e2e/dashboard.spec.ts:24:7

# Error details

```
Test timeout of 60000ms exceeded while running "beforeEach" hook.
```

```
Error: locator.click: Test timeout of 60000ms exceeded.
Call log:
  - waiting for locator('button[type="submit"], button:has-text("登录")')

```

# Page snapshot

```yaml
- generic [ref=e3]:
  - generic [ref=e4]:
    - generic [ref=e5]:
      - generic [ref=e6]: HIS
      - heading "规则中台" [level=1] [ref=e7]
    - paragraph [ref=e8]: Dynamic Rule Engine for Healthcare Intelligence
    - generic [ref=e9]:
      - generic [ref=e12]: Drools 规则引擎 · 实时决策
      - generic [ref=e15]: Aviator 公式计算 · 精准结算
      - generic [ref=e18]: DRG 分组 · 质量控制 · 用药审核
  - generic [ref=e20]:
    - heading "登录" [level=2] [ref=e21]
    - paragraph [ref=e22]: 登录到 HIS 规则中台管理后台
    - generic [ref=e23]:
      - generic [ref=e27]:
        - img [ref=e30]
        - textbox "用户名" [ref=e32]: admin
      - generic [ref=e36]:
        - img [ref=e39]
        - textbox "密码" [active] [ref=e42]: admin123
        - img [ref=e45] [cursor=pointer]
      - button "登 录" [ref=e50] [cursor=pointer]:
        - generic [ref=e51]: 登 录
    - generic [ref=e52]: "默认账号: admin / admin"
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test'
  2  | 
  3  | test.describe('首页/仪表盘', () => {
  4  |   // 需要先登录
  5  |   test.beforeEach(async ({ page }) => {
  6  |     // 访问登录页
  7  |     await page.goto('/login')
  8  | 
  9  |     // 输入账号密码
  10 |     const usernameInput = page.locator('input[type="text"], input[placeholder*="账号"]')
  11 |     await usernameInput.fill('admin')
  12 | 
  13 |     const passwordInput = page.locator('input[type="password"]')
  14 |     await passwordInput.fill('admin123')
  15 | 
  16 |     // 点击登录
  17 |     const loginButton = page.locator('button[type="submit"], button:has-text("登录")')
> 18 |     await loginButton.click()
     |                       ^ Error: locator.click: Test timeout of 60000ms exceeded.
  19 | 
  20 |     // 等待跳转到 dashboard
  21 |     await page.waitForURL('**/dashboard**', { timeout: 10000 })
  22 |   })
  23 | 
  24 |   test('Dashboard 页面加载正常', async ({ page }) => {
  25 |     // 验证页面标题或面包屑
  26 |     await expect(page.locator('.his-layout, [class*="layout"]')).toBeVisible()
  27 | 
  28 |     // 验证侧边栏菜单存在
  29 |     await expect(page.locator('.el-menu, [class*="menu"]').first()).toBeVisible()
  30 |   })
  31 | 
  32 |   test('点击菜单可以切换到规则定义页', async ({ page }) => {
  33 |     // 点击规则管理子菜单
  34 |     const ruleMenuItem = page.locator('.el-sub-menu:has-text("规则管理"), .el-menu-item:has-text("规则定义")')
  35 |     await ruleMenuItem.first().click()
  36 | 
  37 |     // 等待页面切换
  38 |     await page.waitForURL('**/rule**', { timeout: 5000 })
  39 | 
  40 |     // 验证规则列表页面加载
  41 |     await expect(page.locator('.el-table, [class*="table"]').first()).toBeVisible({ timeout: 5000 })
  42 |   })
  43 | })
```
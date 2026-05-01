import { test, expect } from '@playwright/test'
import { LayoutPage } from '../page-objects/LayoutPage'

test.describe('Layout', () => {
  test('navigates all sidebar items', async ({ page }) => {
        const layout = new LayoutPage(page)

    const menuItems = [
      '数据概览',
      '实时报警',
      '历史报警',
      'PDI报表',
      '盒子管理',
      '通道管理',
      '用户管理',
      '角色管理',
      '规则配置',
      '阈值配置',
      '通用配置',
      '操作日志',
    ]

    for (const title of menuItems) {
      const item = layout.getMenuItem(title)
      const visible = await item.isVisible().catch(() => false)
      if (visible) {
        await item.click()
        await page.locator('.el-main, .app-main, .page-container').first().waitFor({ state: 'visible' })
      }
    }
  })

  test('logout redirects to login', async ({ page }) => {
        const layout = new LayoutPage(page)
    await page.goto('/')

    await layout.logout()
    await page.waitForURL('/login')
    await expect(page).toHaveURL('/login')
  })
})

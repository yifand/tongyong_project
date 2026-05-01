import { type Page, type Locator } from '@playwright/test'

export class DashboardPage {
  readonly statOnlineBoxes: Locator
  readonly statOfflineBoxes: Locator
  readonly statTodayAlarms: Locator
  readonly statTodaySessions: Locator
  readonly pieChartCanvas: Locator
  readonly lineChartCanvas: Locator

  constructor(readonly page: Page) {
    this.statOnlineBoxes = page.locator('.stat-title').filter({ hasText: '在线盒子' }).locator('..').locator('.stat-value')
    this.statOfflineBoxes = page.locator('.stat-title').filter({ hasText: '离线盒子' }).locator('..').locator('.stat-value')
    this.statTodayAlarms = page.locator('.stat-title').filter({ hasText: '今日报警' }).locator('..').locator('.stat-value')
    this.statTodaySessions = page.locator('.stat-title').filter({ hasText: '今日PDI作业' }).locator('..').locator('.stat-value')
    this.pieChartCanvas = page.locator('div').filter({ has: page.locator('.stat-title') }).filter({ hasText: '设备在线状态' }).locator('div[style*="height: 300px"]').first()
    this.lineChartCanvas = page.locator('div').filter({ has: page.locator('.stat-title') }).filter({ hasText: '最近24小时报警趋势' }).locator('div[style*="height: 300px"]').first()
  }

  async goto() {
    await this.page.goto('/dashboard')
  }
}

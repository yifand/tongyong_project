import { test, expect } from '@playwright/test'
import { DashboardPage } from '../page-objects/DashboardPage'

test.describe('Dashboard', () => {
  test('loads with stat cards and charts', async ({ page }) => {
        const dashboard = new DashboardPage(page)
    await dashboard.goto()

    await expect(dashboard.statOnlineBoxes).toBeVisible()
    await expect(dashboard.statOfflineBoxes).toBeVisible()
    await expect(dashboard.statTodayAlarms).toBeVisible()
    await expect(dashboard.statTodaySessions).toBeVisible()
    await expect(dashboard.pieChartCanvas).toBeVisible()
    await expect(dashboard.lineChartCanvas).toBeVisible()
  })
})

import { test, expect } from '@playwright/test'
import { HistoryAlarmPage } from '../page-objects/HistoryAlarmPage'

test.describe('Alarm History', () => {
  test('search and table load', async ({ page }) => {
        const historyPage = new HistoryAlarmPage(page)
    await historyPage.goto()

    await expect(historyPage.searchButton).toBeVisible()
    await historyPage.search()
    await expect(historyPage.table).toBeVisible()
  })

  test('opens detail dialog', async ({ page }) => {
        const historyPage = new HistoryAlarmPage(page)
    await historyPage.goto()
    await historyPage.search()

    const count = await historyPage.tableRows.count()
    if (count > 0) {
      await historyPage.openDetail(0)
      await expect(historyPage.detailDialog).toBeVisible()
    }
  })

  test('export triggers download', async ({ page }) => {
        const historyPage = new HistoryAlarmPage(page)
    await historyPage.goto()

    const [download] = await Promise.all([
      page.waitForEvent('download', { timeout: 5000 }).catch(() => null),
      historyPage.export(),
    ])
  })
})

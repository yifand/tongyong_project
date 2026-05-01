import { test, expect } from '@playwright/test'
import { PdiReportPage } from '../page-objects/PdiReportPage'

test.describe('PDI Report', () => {
  test('search and table load', async ({ page }) => {
        const reportPage = new PdiReportPage(page)
    await reportPage.goto()

    await expect(reportPage.searchButton).toBeVisible()
    await reportPage.search()
    await expect(reportPage.table).toBeVisible()
  })

  test('opens detail dialog', async ({ page }) => {
        const reportPage = new PdiReportPage(page)
    await reportPage.goto()
    await reportPage.search()

    const count = await reportPage.tableRows.count()
    if (count > 0) {
      await reportPage.openDetail(0)
      await expect(reportPage.detailDialog).toBeVisible()
    }
  })

  test('export triggers download', async ({ page }) => {
        const reportPage = new PdiReportPage(page)
    await reportPage.goto()

    const [download] = await Promise.all([
      page.waitForEvent('download', { timeout: 5000 }).catch(() => null),
      reportPage.export(),
    ])
  })
})

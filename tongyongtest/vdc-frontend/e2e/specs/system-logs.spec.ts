import { test, expect } from '@playwright/test'
import { OperationLogPage } from '../page-objects/OperationLogPage'

test.describe('System Logs', () => {
  test('page loads with table', async ({ page }) => {
        const logPage = new OperationLogPage(page)
    await logPage.goto()
    await expect(logPage.table).toBeVisible()
  })

  test('filter by operation type', async ({ page }) => {
        const logPage = new OperationLogPage(page)
    await logPage.goto()

    await logPage.operationTypeSelect.click()
    await page.locator('.el-select-dropdown__item').first().click()
    await logPage.search()
    await expect(logPage.table).toBeVisible()
  })

  test('filter by date range', async ({ page }) => {
        const logPage = new OperationLogPage(page)
    await logPage.goto()

    await logPage.dateRangePicker.click()
    await page.locator('.el-picker-panel__shortcut').first().click()
    await logPage.search()
    await expect(logPage.table).toBeVisible()
  })
})

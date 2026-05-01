import { test, expect } from '@playwright/test'
import { ThresholdConfigPage } from '../page-objects/ThresholdConfigPage'

test.describe('System Thresholds', () => {
  test('page loads', async ({ page }) => {
        const thresholdPage = new ThresholdConfigPage(page)
    await thresholdPage.goto()

    const hasEmpty = await thresholdPage.emptyState.isVisible().catch(() => false)
    const hasForm = await thresholdPage.formItems.first().isVisible().catch(() => false)
    expect(hasEmpty || hasForm).toBe(true)
  })

  test('update and save', async ({ page }) => {
        const thresholdPage = new ThresholdConfigPage(page)
    await thresholdPage.goto()

    const hasForm = await thresholdPage.formItems.first().isVisible().catch(() => false)
    if (hasForm) {
      const firstInput = thresholdPage.page.locator('.el-form-item input').first()
      await firstInput.fill('10')
      await thresholdPage.save()
    }
  })
})

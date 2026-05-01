import { test, expect } from '@playwright/test'
import { GeneralConfigPage } from '../page-objects/GeneralConfigPage'

test.describe('System General', () => {
  test('page loads', async ({ page }) => {
        const generalPage = new GeneralConfigPage(page)
    await generalPage.goto()

    const hasEmpty = await generalPage.emptyState.isVisible().catch(() => false)
    const hasForm = await generalPage.formItems.first().isVisible().catch(() => false)
    expect(hasEmpty || hasForm).toBe(true)
  })

  test('update and save', async ({ page }) => {
        const generalPage = new GeneralConfigPage(page)
    await generalPage.goto()

    const hasForm = await generalPage.formItems.first().isVisible().catch(() => false)
    if (hasForm) {
      const firstInput = generalPage.page.locator('.el-form-item input').first()
      await firstInput.fill('E2E Updated Value')
      await generalPage.save()
    }
  })
})

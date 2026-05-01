import { test, expect } from '@playwright/test'
import { RuleConfigPage } from '../page-objects/RuleConfigPage'

test.describe('System Rules', () => {
  test('list loads table', async ({ page }) => {
        const rulePage = new RuleConfigPage(page)
    await rulePage.goto()
    await expect(rulePage.table).toBeVisible()
  })

  test('edit and save first rule', async ({ page }) => {
        const rulePage = new RuleConfigPage(page)
    await rulePage.goto()

    const count = await rulePage.tableRows.count()
    if (count > 0) {
      await rulePage.openEditDialog(0)
      await expect(rulePage.editDialog).toBeVisible()
      await rulePage.fillForm({ standardDuration: 120 })
      await rulePage.confirmSave()
    }
  })
})

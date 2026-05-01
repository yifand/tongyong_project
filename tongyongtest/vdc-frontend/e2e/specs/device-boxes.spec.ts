import { test, expect } from '@playwright/test'
import { BoxListPage } from '../page-objects/BoxListPage'

test.describe.serial('Device Boxes', () => {
  test('search loads table', async ({ page }) => {
        const boxPage = new BoxListPage(page)
    await boxPage.goto()
    await boxPage.search()
    await expect(boxPage.table).toBeVisible()
  })

  test('create box', async ({ page }) => {
        const boxPage = new BoxListPage(page)
    await boxPage.goto()
    await boxPage.openAddDialog()
    await expect(boxPage.addDialog).toBeVisible()

    await boxPage.fillAddForm({
      boxId: 'E2E-BOX-001',
      boxName: 'E2E Test Box',
      ipAddress: '192.168.1.100',
    })
    await boxPage.confirmAdd()
  })

  test('reboot first box', async ({ page }) => {
        const boxPage = new BoxListPage(page)
    await boxPage.goto()
    await boxPage.search()

    const count = await boxPage.tableRows.count()
    if (count > 0) {
      await boxPage.rebootBox(0)
    }
  })

  test('delete created box', async ({ page }) => {
        const boxPage = new BoxListPage(page)
    await boxPage.goto()
    await boxPage.search()

    const count = await boxPage.tableRows.count()
    for (let i = 0; i < count; i++) {
      const text = await boxPage.tableRows.nth(i).textContent()
      if (text?.includes('E2E-BOX-001')) {
        await boxPage.deleteBox(i)
        break
      }
    }
  })
})

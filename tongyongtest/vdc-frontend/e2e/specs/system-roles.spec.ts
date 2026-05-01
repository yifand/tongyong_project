import { test, expect } from '@playwright/test'
import { RoleManagePage } from '../page-objects/RoleManagePage'

test.describe.serial('System Roles', () => {
  test('list loads table', async ({ page }) => {
        const rolePage = new RoleManagePage(page)
    await rolePage.goto()
    await expect(rolePage.table).toBeVisible()
  })

  test('create role', async ({ page }) => {
        const rolePage = new RoleManagePage(page)
    await rolePage.goto()
    await rolePage.openAddDialog()
    await expect(rolePage.dialog).toBeVisible()

    await rolePage.fillForm({
      roleCode: 'e2e_role',
      roleName: 'E2E Role',
      dataScope: '全部站点',
    })
    await rolePage.confirmSave()
  })

  test('edit created role', async ({ page }) => {
        const rolePage = new RoleManagePage(page)
    await rolePage.goto()

    const count = await rolePage.tableRows.count()
    for (let i = 0; i < count; i++) {
      const text = await rolePage.tableRows.nth(i).textContent()
      if (text?.includes('e2e_role')) {
        await rolePage.openEditDialog(i)
        await expect(rolePage.dialog).toBeVisible()
        await rolePage.fillForm({ roleName: 'E2E Role Updated' })
        await rolePage.confirmSave()
        break
      }
    }
  })

  test('delete created role', async ({ page }) => {
        const rolePage = new RoleManagePage(page)
    await rolePage.goto()

    const count = await rolePage.tableRows.count()
    for (let i = 0; i < count; i++) {
      const text = await rolePage.tableRows.nth(i).textContent()
      if (text?.includes('e2e_role')) {
        await rolePage.deleteRole(i)
        break
      }
    }
  })
})

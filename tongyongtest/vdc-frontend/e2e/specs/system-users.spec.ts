import { test, expect } from '@playwright/test'
import { UserManagePage } from '../page-objects/UserManagePage'

test.describe.serial('System Users', () => {
  test('search loads table', async ({ page }) => {
        const userPage = new UserManagePage(page)
    await userPage.goto()
    await userPage.search()
    await expect(userPage.table).toBeVisible()
  })

  test('create user', async ({ page }) => {
        const userPage = new UserManagePage(page)
    await userPage.goto()
    await userPage.openAddDialog()
    await expect(userPage.dialog).toBeVisible()

    await userPage.fillForm({
      username: 'e2euser',
      password: 'e2ePass123',
      realName: 'E2E User',
      phone: '13800138000',
      email: 'e2e@example.com',
      status: '启用',
    })
    await userPage.confirmSave()
  })

  test('edit created user', async ({ page }) => {
        const userPage = new UserManagePage(page)
    await userPage.goto()
    await userPage.search()

    const count = await userPage.tableRows.count()
    for (let i = 0; i < count; i++) {
      const text = await userPage.tableRows.nth(i).textContent()
      if (text?.includes('e2euser')) {
        await userPage.openEditDialog(i)
        await expect(userPage.dialog).toBeVisible()
        await userPage.fillForm({ realName: 'E2E User Updated' })
        await userPage.confirmSave()
        break
      }
    }
  })

  test('delete created user', async ({ page }) => {
        const userPage = new UserManagePage(page)
    await userPage.goto()
    await userPage.search()

    const count = await userPage.tableRows.count()
    for (let i = 0; i < count; i++) {
      const text = await userPage.tableRows.nth(i).textContent()
      if (text?.includes('e2euser')) {
        await userPage.deleteUser(i)
        break
      }
    }
  })
})

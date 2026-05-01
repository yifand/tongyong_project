import { test as setup } from '@playwright/test'

const authFile = 'playwright/.auth/admin.json'
const adminUsername = process.env.E2E_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.E2E_ADMIN_PASSWORD || 'admin123'

setup('authenticate as admin', async ({ page }) => {
  await page.goto('/login')

  await page.getByPlaceholder('请输入用户名').fill(adminUsername)
  await page.getByPlaceholder('请输入密码').fill(adminPassword)
  await page.getByRole('button', { name: '登录' }).click()

  await page.waitForURL('/')

  await page.context().storageState({ path: authFile })
})

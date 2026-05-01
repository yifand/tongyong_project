import { test, expect } from '@playwright/test'
import { LoginPage } from '../page-objects/LoginPage'

test.describe('Login', () => {
  test('redirects to dashboard with valid credentials', async ({ page }) => {
    const loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.login('admin', 'admin123')
    await page.waitForURL('/')
    await expect(page).toHaveURL('/')
  })

  test('shows error with invalid credentials', async ({ page }) => {
    const loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.login('admin', 'wrongpassword')
    await loginPage.expectErrorMessage('用户名或密码错误')
  })

  test('shows validation error with empty fields', async ({ page }) => {
    const loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.loginButton.click()
    await loginPage.expectValidationError('请输入用户名')
  })
})

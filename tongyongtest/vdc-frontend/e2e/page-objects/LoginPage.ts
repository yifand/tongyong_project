import { expect, type Page, type Locator } from '@playwright/test'

export class LoginPage {
  readonly usernameInput: Locator
  readonly passwordInput: Locator
  readonly loginButton: Locator

  constructor(readonly page: Page) {
    this.usernameInput = page.getByPlaceholder('请输入用户名')
    this.passwordInput = page.getByPlaceholder('请输入密码')
    this.loginButton = page.getByRole('button', { name: '登录' })
  }

  async goto() {
    await this.page.goto('/login')
  }

  async login(username: string, password: string) {
    await this.usernameInput.fill(username)
    await this.passwordInput.fill(password)
    await this.loginButton.click()
  }

  async expectErrorMessage(message: string) {
    await expect(this.page.locator('.el-message--error')).toBeVisible()
    await expect(this.page.getByText(message)).toBeVisible()
  }

  async expectValidationError(message: string) {
    await expect(this.page.getByText(message)).toBeVisible()
  }
}

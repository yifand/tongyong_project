import { type Page, type Locator } from '@playwright/test'

export class LayoutPage {
  readonly sidebar: Locator
  readonly logoutButton: Locator
  readonly userName: Locator

  constructor(readonly page: Page) {
    this.sidebar = page.locator('.sidebar')
    this.logoutButton = page.getByRole('button', { name: '退出' })
    this.userName = page.locator('.user-name')
  }

  getMenuItem(title: string): Locator {
    return this.page.locator('.el-menu-item').filter({ hasText: title })
  }

  async navigateTo(title: string) {
    await this.getMenuItem(title).click()
  }

  async logout() {
    await this.logoutButton.click()
  }
}

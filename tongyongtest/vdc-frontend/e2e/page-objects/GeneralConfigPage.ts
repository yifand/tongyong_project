import { type Page, type Locator } from '@playwright/test'

export class GeneralConfigPage {
  readonly emptyState: Locator
  readonly saveButton: Locator
  readonly formItems: Locator

  constructor(readonly page: Page) {
    this.emptyState = page.locator('.el-empty')
    this.saveButton = page.getByRole('button', { name: '保存' })
    this.formItems = page.locator('.el-form-item')
  }

  async goto() {
    await this.page.goto('/system/general')
  }

  getConfigInput(label: string): Locator {
    return this.page.locator('.el-form-item').filter({ hasText: label }).locator('input').first()
  }

  async setConfigValue(label: string, value: string) {
    await this.getConfigInput(label).fill(value)
  }

  async save() {
    await this.saveButton.click()
  }
}

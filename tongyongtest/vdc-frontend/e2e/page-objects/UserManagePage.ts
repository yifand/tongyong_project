import { type Page, type Locator } from '@playwright/test'
import {
  getFormItem,
  getTable,
  getTableRows,
  getPagination,
  getDialog,
  getDialogConfirmButton,
  waitForMessage
} from '../utils/element-plus'

export class UserManagePage {
  readonly usernameInput: Locator
  readonly searchButton: Locator
  readonly addButton: Locator
  readonly table: Locator
  readonly tableRows: Locator
  readonly pagination: Locator
  readonly dialog: Locator

  constructor(readonly page: Page) {
    this.usernameInput = getFormItem(page, '用户名').locator('input')
    this.searchButton = page.getByRole('button', { name: '查询' }).first()
    this.addButton = page.getByRole('button', { name: '添加用户' })
    this.table = getTable(page)
    this.tableRows = getTableRows(page)
    this.pagination = getPagination(page)
    this.dialog = getDialog(page)
  }

  async goto() {
    await this.page.goto('/system/users')
  }

  async search() {
    await this.searchButton.click()
  }

  async openAddDialog() {
    await this.addButton.click()
  }

  async openEditDialog(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '编辑' }).click()
  }

  async fillForm(fields: {
    username?: string
    password?: string
    realName?: string
    phone?: string
    email?: string
    roleName?: string
    siteName?: string
    status?: '启用' | '禁用'
  }) {
    const container = this.dialog
    if (fields.username != null) {
      const input = container.locator('.el-form-item').filter({ hasText: '用户名' }).locator('input')
      const isDisabled = await input.isDisabled().catch(() => false)
      if (!isDisabled) await input.fill(fields.username)
    }
    if (fields.password != null) {
      const input = container.locator('.el-form-item').filter({ hasText: '密码' }).locator('input')
      const isVisible = await input.isVisible().catch(() => false)
      if (isVisible) await input.fill(fields.password)
    }
    if (fields.realName != null) {
      await container.locator('.el-form-item').filter({ hasText: '姓名' }).locator('input').fill(fields.realName)
    }
    if (fields.phone != null) {
      await container.locator('.el-form-item').filter({ hasText: '手机号' }).locator('input').fill(fields.phone)
    }
    if (fields.email != null) {
      await container.locator('.el-form-item').filter({ hasText: '邮箱' }).locator('input').fill(fields.email)
    }
    if (fields.roleName != null) {
      await container.locator('.el-form-item').filter({ hasText: '角色' }).locator('.el-select').click()
      await this.page.locator('.el-select-dropdown__item', { hasText: fields.roleName }).click()
    }
    if (fields.siteName != null) {
      await container.locator('.el-form-item').filter({ hasText: '所属站点' }).locator('.el-select').click()
      await this.page.locator('.el-select-dropdown__item', { hasText: fields.siteName }).click()
    }
    if (fields.status != null) {
      await container.locator('.el-radio-group').getByText(fields.status).click()
    }
  }

  async confirmSave() {
    await getDialogConfirmButton(this.page).click()
    await waitForMessage(this.page, 'success')
  }

  async deleteUser(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '删除' }).click()
    await this.page.locator('.el-message-box').getByRole('button', { name: '确定' }).click()
    await waitForMessage(this.page, 'success')
  }
}

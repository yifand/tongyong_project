import { type Page, type Locator } from '@playwright/test'
import {
  getTable,
  getTableRows,
  getPagination,
  getDialog,
  getDialogConfirmButton,
  waitForMessage
} from '../utils/element-plus'

export class RoleManagePage {
  readonly addButton: Locator
  readonly table: Locator
  readonly tableRows: Locator
  readonly pagination: Locator
  readonly dialog: Locator

  constructor(readonly page: Page) {
    this.addButton = page.getByRole('button', { name: '添加角色' })
    this.table = getTable(page)
    this.tableRows = getTableRows(page)
    this.pagination = getPagination(page)
    this.dialog = getDialog(page)
  }

  async goto() {
    await this.page.goto('/system/roles')
  }

  async openAddDialog() {
    await this.addButton.click()
  }

  async openEditDialog(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '编辑' }).click()
  }

  async fillForm(fields: {
    roleCode?: string
    roleName?: string
    dataScope?: '全部站点' | '站点隔离'
    permissions?: string[]
  }) {
    const container = this.dialog
    if (fields.roleCode != null) {
      const input = container.locator('.el-form-item').filter({ hasText: '角色编码' }).locator('input')
      const isDisabled = await input.isDisabled().catch(() => false)
      if (!isDisabled) await input.fill(fields.roleCode)
    }
    if (fields.roleName != null) {
      await container.locator('.el-form-item').filter({ hasText: '角色名称' }).locator('input').fill(fields.roleName)
    }
    if (fields.dataScope != null) {
      await container.locator('.el-radio-group').getByText(fields.dataScope).click()
    }
    if (fields.permissions != null) {
      for (const permission of fields.permissions) {
        const checkbox = container.locator('.el-checkbox-group').getByText(permission)
        await checkbox.check()
      }
    }
  }

  async confirmSave() {
    await getDialogConfirmButton(this.page).click()
    await waitForMessage(this.page, 'success')
  }

  async deleteRole(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '删除' }).click()
    await this.page.locator('.el-message-box').getByRole('button', { name: '确定' }).click()
    await waitForMessage(this.page, 'success')
  }
}

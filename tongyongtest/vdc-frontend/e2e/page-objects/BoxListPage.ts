import { type Page, type Locator } from '@playwright/test'
import {
  getFormSelectByLabel,
  getTable,
  getTableRows,
  getPagination,
  getDialog,
  getDialogConfirmButton,
  waitForMessage
} from '../utils/element-plus'

export class BoxListPage {
  readonly siteSelect: Locator
  readonly statusSelect: Locator
  readonly searchButton: Locator
  readonly addButton: Locator
  readonly table: Locator
  readonly tableRows: Locator
  readonly pagination: Locator
  readonly addDialog: Locator

  constructor(readonly page: Page) {
    this.siteSelect = getFormSelectByLabel(page, '站点')
    this.statusSelect = getFormSelectByLabel(page, '状态')
    this.searchButton = page.getByRole('button', { name: '查询' }).first()
    this.addButton = page.getByRole('button', { name: '添加盒子' })
    this.table = getTable(page)
    this.tableRows = getTableRows(page)
    this.pagination = getPagination(page)
    this.addDialog = getDialog(page, '添加盒子')
  }

  async goto() {
    await this.page.goto('/device/boxes')
  }

  async search() {
    await this.searchButton.click()
  }

  async openAddDialog() {
    await this.addButton.click()
  }

  async fillAddForm(fields: { boxId?: string; boxName?: string; siteId?: string; ipAddress?: string }) {
    if (fields.boxId != null) {
      await this.addDialog.locator('.el-form-item').filter({ hasText: '盒子编号' }).locator('input').fill(fields.boxId)
    }
    if (fields.boxName != null) {
      await this.addDialog.locator('.el-form-item').filter({ hasText: '名称' }).locator('input').fill(fields.boxName)
    }
    if (fields.siteId != null) {
      await this.addDialog.locator('.el-form-item').filter({ hasText: '站点' }).locator('.el-select').click()
      await this.page.locator('.el-select-dropdown__item', { hasText: fields.siteId }).click()
    }
    if (fields.ipAddress != null) {
      await this.addDialog.locator('.el-form-item').filter({ hasText: 'IP地址' }).locator('input').fill(fields.ipAddress)
    }
  }

  async confirmAdd() {
    await getDialogConfirmButton(this.page).click()
    await waitForMessage(this.page, 'success')
  }

  async rebootBox(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '重启' }).click()
    await this.page.locator('.el-message-box').getByRole('button', { name: '确定' }).click()
    await waitForMessage(this.page, 'success')
  }

  async deleteBox(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '删除' }).click()
    await this.page.locator('.el-message-box').getByRole('button', { name: '确定' }).click()
    await waitForMessage(this.page, 'success')
  }
}

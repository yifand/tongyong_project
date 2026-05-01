import { type Page, type Locator } from '@playwright/test'
import {
  getFormSelectByLabel,
  getTable,
  getTableRows,
  getPagination,
  getDialog,
  getDialogConfirmButton,
  waitForMessage,
} from '../utils/element-plus'

export class ChannelListPage {
  readonly boxSelect: Locator
  readonly statusSelect: Locator
  readonly searchButton: Locator
  readonly table: Locator
  readonly tableRows: Locator
  readonly pagination: Locator
  readonly editDialog: Locator
  readonly previewDialog: Locator

  constructor(readonly page: Page) {
    this.boxSelect = getFormSelectByLabel(page, '所属盒子')
    this.statusSelect = getFormSelectByLabel(page, '状态')
    this.searchButton = page.getByRole('button', { name: '查询' }).first()
    this.table = getTable(page)
    this.tableRows = getTableRows(page)
    this.pagination = getPagination(page)
    this.editDialog = getDialog(page, '编辑通道')
    this.previewDialog = getDialog(page, '实时预览')
  }

  async goto() {
    await this.page.goto('/device/channels')
  }

  async search() {
    await this.searchButton.click()
  }

  async openEditDialog(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '编辑' }).click()
  }

  async fillEditForm(fields: { channelName?: string; rtspUrl?: string; username?: string; password?: string }) {
    if (fields.channelName != null) {
      await this.editDialog.locator('.el-form-item').filter({ hasText: '通道名称' }).locator('input').fill(fields.channelName)
    }
    if (fields.rtspUrl != null) {
      await this.editDialog.locator('.el-form-item').filter({ hasText: 'RTSP地址' }).locator('input').fill(fields.rtspUrl)
    }
    if (fields.username != null) {
      await this.editDialog.locator('.el-form-item').filter({ hasText: '用户名' }).locator('input').fill(fields.username)
    }
    if (fields.password != null) {
      await this.editDialog.locator('.el-form-item').filter({ hasText: '密码' }).locator('input').fill(fields.password)
    }
  }

  async confirmEdit() {
    await getDialogConfirmButton(this.page).click()
    await waitForMessage(this.page, 'success')
  }

  async openPreview(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '预览' }).click()
  }
}

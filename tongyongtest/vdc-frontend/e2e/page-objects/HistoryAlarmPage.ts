import { type Page, type Locator } from '@playwright/test'
import {
  getFormSelectByLabel,
  getFormItem,
  getDialog,
  getTable,
  getTableRows,
  getPagination,
} from '../utils/element-plus'

export class HistoryAlarmPage {
  readonly siteSelect: Locator
  readonly typeSelect: Locator
  readonly statusSelect: Locator
  readonly dateRangePicker: Locator
  readonly searchButton: Locator
  readonly exportButton: Locator
  readonly table: Locator
  readonly tableRows: Locator
  readonly pagination: Locator
  readonly detailDialog: Locator

  constructor(readonly page: Page) {
    this.siteSelect = getFormSelectByLabel(page, '站点')
    this.typeSelect = getFormSelectByLabel(page, '类型')
    this.statusSelect = getFormSelectByLabel(page, '状态')
    this.dateRangePicker = getFormItem(page, '时间').locator('.el-date-editor')
    this.searchButton = page.getByRole('button', { name: '查询' })
    this.exportButton = page.getByRole('button', { name: '导出' })
    this.table = getTable(page)
    this.tableRows = getTableRows(page)
    this.pagination = getPagination(page)
    this.detailDialog = getDialog(page, '报警详情')
  }

  async goto() {
    await this.page.goto('/alarm/history')
  }

  async search() {
    await this.searchButton.click()
  }

  async export() {
    await this.exportButton.click()
  }

  async openDetail(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '详情' }).click()
  }

  async markProcessed() {
    await this.detailDialog.getByRole('button', { name: '标记已处理' }).click()
  }

  async markFalseAlarm() {
    await this.detailDialog.getByRole('button', { name: '标记误报' }).click()
  }
}

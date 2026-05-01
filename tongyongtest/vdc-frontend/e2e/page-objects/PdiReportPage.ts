import { type Page, type Locator } from '@playwright/test'
import {
  getFormSelectByLabel,
  getFormItem,
  getTable,
  getTableRows,
  getPagination,
  getDialog,
} from '../utils/element-plus'

export class PdiReportPage {
  readonly siteSelect: Locator
  readonly channelSelect: Locator
  readonly resultSelect: Locator
  readonly dateRangePicker: Locator
  readonly searchButton: Locator
  readonly exportButton: Locator
  readonly table: Locator
  readonly tableRows: Locator
  readonly pagination: Locator
  readonly detailDialog: Locator

  constructor(readonly page: Page) {
    this.siteSelect = getFormSelectByLabel(page, '站点')
    this.channelSelect = getFormSelectByLabel(page, '工位')
    this.resultSelect = getFormSelectByLabel(page, '结果')
    this.dateRangePicker = getFormItem(page, '时间').locator('.el-date-editor')
    this.searchButton = page.getByRole('button', { name: '查询' })
    this.exportButton = page.getByRole('button', { name: '导出' })
    this.table = getTable(page)
    this.tableRows = getTableRows(page)
    this.pagination = getPagination(page)
    this.detailDialog = getDialog(page, '作业详情')
  }

  async goto() {
    await this.page.goto('/report/pdi')
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
}

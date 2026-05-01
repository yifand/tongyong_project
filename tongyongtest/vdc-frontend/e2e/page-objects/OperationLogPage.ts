import { type Page, type Locator } from '@playwright/test'
import {
  getFormSelectByLabel,
  getFormItem,
  getTable,
  getTableRows,
  getPagination,
} from '../utils/element-plus'

export class OperationLogPage {
  readonly operationTypeSelect: Locator
  readonly dateRangePicker: Locator
  readonly searchButton: Locator
  readonly table: Locator
  readonly tableRows: Locator
  readonly pagination: Locator

  constructor(readonly page: Page) {
    this.operationTypeSelect = getFormSelectByLabel(page, '操作类型')
    this.dateRangePicker = getFormItem(page, '时间').locator('.el-date-editor')
    this.searchButton = page.getByRole('button', { name: '查询' })
    this.table = getTable(page)
    this.tableRows = getTableRows(page)
    this.pagination = getPagination(page)
  }

  async goto() {
    await this.page.goto('/system/logs')
  }

  async search() {
    await this.searchButton.click()
  }
}

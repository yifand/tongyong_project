import { type Page, type Locator } from '@playwright/test'

export function getDialog(page: Page, title?: string): Locator {
  const dialog = page.locator('.el-dialog').filter({ has: page.locator('.el-dialog__body') })
  return title ? dialog.filter({ hasText: title }) : dialog
}

export function getDialogHeader(page: Page, title?: string): Locator {
  const header = page.locator('.el-dialog__header')
  return title ? header.filter({ hasText: title }) : header
}

export function getDialogBody(page: Page): Locator {
  return page.locator('.el-dialog__body')
}

export function getDialogFooter(page: Page): Locator {
  return page.locator('.el-dialog__footer')
}

export function getDialogConfirmButton(page: Page): Locator {
  return getDialogFooter(page).getByRole('button', { name: '确 定' })
}

export function getDialogCancelButton(page: Page): Locator {
  return getDialogFooter(page).getByRole('button', { name: '取 消' })
}

export function getTable(page: Page): Locator {
  return page.locator('.el-table')
}

export function getTableRows(page: Page): Locator {
  return page.locator('.el-table__body-wrapper tbody tr')
}

export function getTableCell(page: Page, rowIndex: number, columnIndex: number): Locator {
  return page.locator(
    `.el-table__body-wrapper tbody tr:nth-child(${rowIndex + 1}) td:nth-child(${columnIndex + 1})`
  )
}

export function getMessage(page: Page, type: 'success' | 'warning' | 'info' | 'error'): Locator {
  return page.locator(`.el-message--${type}`)
}

export async function waitForMessage(
  page: Page,
  type: 'success' | 'warning' | 'info' | 'error',
  options?: { timeout?: number }
): Promise<Locator> {
  const locator = getMessage(page, type)
  await locator.waitFor({ state: 'visible', timeout: options?.timeout })
  return locator
}

export function getFormItem(page: Page, label: string): Locator {
  return page.locator('.el-form-item').filter({ hasText: label })
}

export function getFormInputByLabel(page: Page, label: string): Locator {
  return getFormItem(page, label).locator('.el-input__inner')
}

export function getFormSelectByLabel(page: Page, label: string): Locator {
  return getFormItem(page, label).locator('.el-select')
}

export function getPagination(page: Page): Locator {
  return page.locator('.el-pagination')
}

export function getPaginationTotal(page: Page): Locator {
  return getPagination(page).locator('.el-pagination__total')
}

export async function selectPageSize(page: Page, size: number): Promise<void> {
  const select = getPagination(page).locator('.el-pagination__sizes .el-select')
  await select.click()
  await page.locator('.el-select-dropdown__item', { hasText: String(size) }).click()
}

export async function clickPaginationButton(page: Page, name: '上一页' | '下一页'): Promise<void> {
  const btn = getPagination(page).getByRole('button', { name })
  await btn.click()
}

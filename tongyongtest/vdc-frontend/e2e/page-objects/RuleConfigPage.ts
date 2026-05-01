import { type Page, type Locator } from '@playwright/test'
import {
  getTable,
  getTableRows,
  getDialog,
  getDialogConfirmButton,
  waitForMessage
} from '../utils/element-plus'

export class RuleConfigPage {
  readonly table: Locator
  readonly tableRows: Locator
  readonly editDialog: Locator

  constructor(readonly page: Page) {
    this.table = getTable(page)
    this.tableRows = getTableRows(page)
    this.editDialog = getDialog(page, '编辑规则')
  }

  async goto() {
    await this.page.goto('/system/rules')
  }

  async openEditDialog(rowIndex: number) {
    await this.tableRows.nth(rowIndex).getByRole('button', { name: '编辑' }).click()
  }

  async fillForm(fields: {
    requireVehicle?: boolean
    enterPattern?: string
    exitPattern?: string
    standardDuration?: number
    criticalThresholdPct?: number
    personAbsentTimeout?: number
    isEnabled?: boolean
  }) {
    const container = this.editDialog
    if (fields.requireVehicle != null) {
      const switchEl = container.locator('.el-form-item').filter({ hasText: '需车辆在场' }).locator('.el-switch')
      const isChecked = await switchEl.locator('input').isChecked().catch(() => false)
      if (isChecked !== fields.requireVehicle) await switchEl.click()
    }
    if (fields.enterPattern != null) {
      await container.locator('.el-form-item').filter({ hasText: '进入序列(JSON)' }).locator('textarea').fill(fields.enterPattern)
    }
    if (fields.exitPattern != null) {
      await container.locator('.el-form-item').filter({ hasText: '离开序列(JSON)' }).locator('textarea').fill(fields.exitPattern)
    }
    if (fields.standardDuration != null) {
      await container.locator('.el-form-item').filter({ hasText: '标准工时(秒)' }).locator('input').fill(String(fields.standardDuration))
    }
    if (fields.criticalThresholdPct != null) {
      await container.locator('.el-form-item').filter({ hasText: '临界阈值%' }).locator('input').fill(String(fields.criticalThresholdPct))
    }
    if (fields.personAbsentTimeout != null) {
      await container.locator('.el-form-item').filter({ hasText: '人员消失超时(秒)' }).locator('input').fill(String(fields.personAbsentTimeout))
    }
    if (fields.isEnabled != null) {
      const switchEl = container.locator('.el-form-item').filter({ hasText: '启用' }).last().locator('.el-switch')
      const isChecked = await switchEl.locator('input').isChecked().catch(() => false)
      if (isChecked !== fields.isEnabled) await switchEl.click()
    }
  }

  async confirmSave() {
    await getDialogConfirmButton(this.page).click()
    await waitForMessage(this.page, 'success')
  }
}

import { type Page, type Locator } from '@playwright/test'
import { getDialog } from '../utils/element-plus'

export class RealtimeAlarmPage {
  readonly soundToggle: Locator
  readonly emptyState: Locator
  readonly alarmGrid: Locator
  readonly alarmCards: Locator
  readonly detailDialog: Locator

  constructor(readonly page: Page) {
    this.soundToggle = page.locator('.el-switch')
    this.emptyState = page.locator('.el-empty')
    this.alarmGrid = page.locator('.alarm-grid')
    this.alarmCards = page.locator('.alarm-card')
    this.detailDialog = getDialog(page, '报警详情')
  }

  async goto() {
    await this.page.goto('/alarm/realtime')
  }

  getAlarmCardByText(text: string): Locator {
    return this.alarmCards.filter({ hasText: text })
  }

  async openAlarmDetail(text: string) {
    await this.getAlarmCardByText(text).click()
  }

  async markProcessed() {
    await this.detailDialog.getByRole('button', { name: '标记已处理' }).click()
  }

  async markFalseAlarm() {
    await this.detailDialog.getByRole('button', { name: '标记误报' }).click()
  }
}

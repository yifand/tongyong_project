import { test, expect } from '@playwright/test'
import { RealtimeAlarmPage } from '../page-objects/RealtimeAlarmPage'

test.describe('Realtime Alarm', () => {
  test('page loads with toggle and grid', async ({ page }) => {
        const alarmPage = new RealtimeAlarmPage(page)
    await alarmPage.goto()

    await expect(alarmPage.soundToggle).toBeVisible()
    await expect(alarmPage.alarmGrid).toBeVisible()
  })

  test('shows empty state or alarm cards', async ({ page }) => {
        const alarmPage = new RealtimeAlarmPage(page)
    await alarmPage.goto()

    const hasEmpty = await alarmPage.emptyState.isVisible().catch(() => false)
    const hasCards = await alarmPage.alarmCards.first().isVisible().catch(() => false)
    expect(hasEmpty || hasCards).toBe(true)
  })

  test('toggles sound switch', async ({ page }) => {
        const alarmPage = new RealtimeAlarmPage(page)
    await alarmPage.goto()

    await alarmPage.soundToggle.click()
    await expect(alarmPage.alarmGrid.or(alarmPage.emptyState)).toBeVisible()
    await alarmPage.soundToggle.click()
  })
})

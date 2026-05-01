import { test, expect } from '@playwright/test'
import { ChannelListPage } from '../page-objects/ChannelListPage'

test.describe.serial('Device Channels', () => {
  test('search loads table', async ({ page }) => {
        const channelPage = new ChannelListPage(page)
    await channelPage.goto()
    await channelPage.search()
    await expect(channelPage.table).toBeVisible()
  })

  test('edit first channel', async ({ page }) => {
        const channelPage = new ChannelListPage(page)
    await channelPage.goto()
    await channelPage.search()

    const count = await channelPage.tableRows.count()
    if (count > 0) {
      await channelPage.openEditDialog(0)
      await expect(channelPage.editDialog).toBeVisible()
      await channelPage.fillEditForm({ channelName: 'E2E Updated Channel' })
      await channelPage.confirmEdit()
    }
  })

  test('preview first channel', async ({ page }) => {
        const channelPage = new ChannelListPage(page)
    await channelPage.goto()
    await channelPage.search()

    const count = await channelPage.tableRows.count()
    if (count > 0) {
      await channelPage.openPreview(0)
      await expect(channelPage.previewDialog).toBeVisible()
    }
  })
})

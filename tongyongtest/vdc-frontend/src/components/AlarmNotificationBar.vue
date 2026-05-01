<template>
  <div class="alarm-bar" @click="drawerVisible = true">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="alarm-badge">
      <el-icon :size="22" :class="{ 'flashing': hasNewAlarm }"><BellFilled /></el-icon>
    </el-badge>
    <span class="alarm-text" :class="{ 'flashing-text': hasNewAlarm }">
      实时预警 {{ unreadCount > 0 ? `(${unreadCount}条未读)` : '' }}
    </span>
  </div>

  <el-drawer v-model="drawerVisible" title="实时预警" size="500px" destroy-on-close>
    <div class="alarm-filter">
      <el-select v-model="filterType" placeholder="报警类型" clearable style="width: 140px">
        <el-option label="抽烟检测" value="SMOKE" />
        <el-option label="PDI违规" value="PDI_UNQUALIFIED" />
      </el-select>
      <el-select v-model="filterSiteId" placeholder="站点" clearable style="width: 140px; margin-left: 8px">
        <el-option label="金桥库" :value="1" />
        <el-option label="凯迪库" :value="2" />
      </el-select>
    </div>
    <el-empty v-if="filteredAlarms.length === 0" description="暂无实时报警" />
    <div v-else class="alarm-list">
      <div
        v-for="alarm in filteredAlarms"
        :key="alarm.id"
        class="alarm-item"
        :class="{ unread: !alarm.read }"
        @click="openDetail(alarm)"
      >
        <div class="alarm-header">
          <el-tag :type="alarmTypeColor(alarm.alarmType)" size="small">
            {{ alarmTypeText(alarm.alarmType) }}
          </el-tag>
          <span class="alarm-time">{{ formatTime(alarm.alarmTime) }}</span>
        </div>
        <div class="alarm-body">
          <p>站点: {{ siteName(alarm.siteId) }} | 通道: {{ channelName(alarm.channelId) }}</p>
        </div>
      </div>
    </div>
  </el-drawer>

  <el-dialog v-model="detailVisible" title="报警详情" width="600px">
    <div v-if="selectedAlarm" class="alarm-detail">
      <p><strong>报警类型:</strong> {{ alarmTypeText(selectedAlarm.alarmType) }}</p>
      <p><strong>站点:</strong> {{ siteName(selectedAlarm.siteId) }}</p>
      <p><strong>通道:</strong> {{ channelName(selectedAlarm.channelId) }}</p>
      <p><strong>报警时间:</strong> {{ formatTime(selectedAlarm.alarmTime) }}</p>
      <p><strong>处理状态:</strong> {{ processStatusText(selectedAlarm.processStatus) }}</p>
      <div v-if="imageUrls.targetImageUrl" class="image-row">
        <div class="image-block">
          <p>目标截图</p>
          <el-image :src="imageUrls.targetImageUrl" fit="contain" style="height: 160px" />
        </div>
        <div class="image-block">
          <p>场景截图</p>
          <el-image :src="imageUrls.sceneImageUrl" fit="contain" style="height: 160px" />
        </div>
      </div>
      <div class="detail-actions">
        <el-button type="primary" @click="markProcessed('PROCESSED')">标记已处理</el-button>
        <el-button @click="markProcessed('FALSE_ALARM')">标记误报</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { BellFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getAlarms, processAlarm, getAlarmDetail } from '@/api/alarm'
import type { Alarm } from '@/types'

const drawerVisible = ref(false)
const detailVisible = ref(false)
const selectedAlarm = ref<Alarm | null>(null)
const imageUrls = ref({ targetImageUrl: '', sceneImageUrl: '' })
const alarms = ref<Alarm[]>([])
const filterType = ref('')
const filterSiteId = ref<number | ''>('')
const hasNewAlarm = ref(false)
let flashTimer: any = null
let stompClient: Client | null = null

const unreadCount = computed(() => alarms.value.filter(a => !a.read).length)

const filteredAlarms = computed(() => {
  return alarms.value.filter(a => {
    if (filterType.value && a.alarmType !== filterType.value) return false
    if (filterSiteId.value !== '' && a.siteId !== filterSiteId.value) return false
    return true
  })
})

function alarmTypeText(type: string) {
  return type === 'SMOKE' ? '抽烟检测' : 'PDI违规'
}

function alarmTypeColor(type: string) {
  return type === 'SMOKE' ? 'danger' : 'warning'
}

function siteName(siteId: number) {
  return siteId === 1 ? '金桥库' : siteId === 2 ? '凯迪库' : '未知'
}

function channelName(channelId: number) {
  return 'CH_' + channelId
}

function processStatusText(status: string) {
  const map: Record<string, string> = {
    UNPROCESSED: '未处理',
    PROCESSED: '已处理',
    FALSE_ALARM: '误报',
  }
  return map[status] || status
}

function formatTime(time: string) {
  if (!time) return '-'
  const d = new Date(time)
  return d.toLocaleString('zh-CN')
}

async function fetchRecentAlarms() {
  const res = await getAlarms({ page: 1, size: 20, processStatus: 'UNPROCESSED' })
  alarms.value = (res.records || []).map(a => ({ ...a, read: false } as Alarm & { read: boolean }))
}

async function openDetail(alarm: Alarm & { read?: boolean }) {
  selectedAlarm.value = alarm
  alarm.read = true
  detailVisible.value = true
  try {
    const detail = await getAlarmDetail(alarm.id)
    selectedAlarm.value = detail.alarm || detail
    imageUrls.value = {
      targetImageUrl: detail.targetImageUrl || '',
      sceneImageUrl: detail.sceneImageUrl || ''
    }
  } catch {
    imageUrls.value = { targetImageUrl: '', sceneImageUrl: '' }
  }
}

async function markProcessed(status: string) {
  if (!selectedAlarm.value) return
  await processAlarm(selectedAlarm.value.id, status)
  ElMessage.success('操作成功')
  detailVisible.value = false
  await fetchRecentAlarms()
}

function triggerFlash() {
  hasNewAlarm.value = true
  if (flashTimer) clearTimeout(flashTimer)
  flashTimer = setTimeout(() => {
    hasNewAlarm.value = false
  }, 3000)
}

function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    onConnect: () => {
      stompClient?.subscribe('/topic/alarms', (message) => {
        const payload = JSON.parse(message.body)
        alarms.value.unshift({ ...payload, read: false } as any)
        if (alarms.value.length > 50) alarms.value.pop()
        triggerFlash()
      })
    },
    onDisconnect: () => {},
    onStompError: () => {},
  })
  stompClient.activate()
}

onMounted(() => {
  fetchRecentAlarms()
  connectWebSocket()
})

onUnmounted(() => {
  stompClient?.deactivate()
  if (flashTimer) clearTimeout(flashTimer)
})
</script>

<style scoped>
.alarm-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 4px;
  transition: background 0.2s;
}
.alarm-bar:hover {
  background: #f5f7fa;
}
.alarm-text {
  font-size: 14px;
  color: #606266;
}
.flashing {
  animation: flash-icon 1s infinite;
  color: #f56c6c;
}
.flashing-text {
  animation: flash-text 1s infinite;
  color: #f56c6c;
  font-weight: bold;
}
@keyframes flash-icon {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
@keyframes flash-text {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.alarm-filter {
  margin-bottom: 12px;
}
.alarm-list {
  max-height: calc(100vh - 160px);
  overflow-y: auto;
}
.alarm-item {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
}
.alarm-item:hover {
  background: #f5f7fa;
}
.alarm-item.unread {
  background: #fdf6ec;
}
.alarm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.alarm-time {
  font-size: 12px;
  color: #909399;
}
.alarm-body p {
  font-size: 13px;
  color: #606266;
  margin: 0;
}
.image-row {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}
.image-block {
  flex: 1;
}
.image-block p {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.detail-actions {
  margin-top: 16px;
  text-align: right;
}
</style>

<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实时预警</span>
          <el-switch v-model="soundEnabled" active-text="声音提示" />
        </div>
      </template>
      <el-empty v-if="alarms.length === 0" description="暂无实时报警" />
      <div v-else class="alarm-grid">
        <div v-for="alarm in alarms" :key="alarm.id" class="alarm-card" @click="openDetail(alarm)">
          <div class="alarm-card-header">
            <el-tag :type="alarmTypeColor(alarm.alarmType)" size="small">{{ alarmTypeText(alarm.alarmType) }}</el-tag>
            <span class="time">{{ formatTime(alarm.alarmTime) }}</span>
          </div>
          <div class="alarm-card-body">
            <p>站点: {{ siteName(alarm.siteId) }}</p>
            <p>通道: {{ channelName(alarm.channelId) }}</p>
            <p>状态: {{ processStatusText(alarm.processStatus) }}</p>
          </div>
          <div class="alarm-card-images" v-if="alarm.targetImage || alarm.sceneImage">
            <el-image v-if="alarm.targetImage" :src="alarm.targetImage" fit="cover" style="height: 100px; width: 50%" />
            <el-image v-if="alarm.sceneImage" :src="alarm.sceneImage" fit="cover" style="height: 100px; width: 50%" />
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="报警详情" width="700px">
      <AlarmDetail :alarm="selectedAlarm" @processed="onProcessed" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getAlarms, processAlarm } from '@/api/alarm'
import type { Alarm } from '@/types'
import AlarmDetail from './AlarmDetail.vue'

const alarms = ref<(Alarm & { read?: boolean })[]>([])
const soundEnabled = ref(false)
const detailVisible = ref(false)
const selectedAlarm = ref<Alarm | null>(null)
let stompClient: Client | null = null

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
  const map: Record<string, string> = { UNPROCESSED: '未处理', PROCESSED: '已处理', FALSE_ALARM: '误报' }
  return map[status] || status
}
function formatTime(time: string) {
  return time ? new Date(time).toLocaleString('zh-CN') : '-'
}

async function fetchAlarms() {
  const res = await getAlarms({ page: 1, size: 50, processStatus: 'UNPROCESSED' })
  alarms.value = res.records || []
}

function openDetail(alarm: Alarm & { read?: boolean }) {
  selectedAlarm.value = alarm
  alarm.read = true
  detailVisible.value = true
}

async function onProcessed({ id, status }: { id: number; status: string }) {
  await processAlarm(id, status)
  detailVisible.value = false
  await fetchAlarms()
}

function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    onConnect: () => {
      stompClient?.subscribe('/topic/alarms', (message) => {
        const payload = JSON.parse(message.body)
        alarms.value.unshift(payload)
        if (alarms.value.length > 50) alarms.value.pop()
        if (soundEnabled.value) {
          const audio = new Audio('/alarm.mp3')
          audio.play().catch(() => {})
        }
      })
    },
  })
  stompClient.activate()
}

onMounted(() => {
  fetchAlarms()
  connectWebSocket()
})
onUnmounted(() => {
  stompClient?.deactivate()
})
</script>

<style scoped>
.page {
  padding-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.alarm-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
.alarm-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  background: #fff;
}
.alarm-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.alarm-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.time {
  font-size: 12px;
  color: #909399;
}
.alarm-card-body p {
  margin: 4px 0;
  font-size: 13px;
  color: #606266;
}
.alarm-card-images {
  display: flex;
  gap: 4px;
  margin-top: 8px;
}
</style>

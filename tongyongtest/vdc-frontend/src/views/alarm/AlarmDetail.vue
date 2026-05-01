<template>
  <div v-if="alarmDetail" class="detail">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="报警类型">{{ alarmTypeText(alarmDetail.alarmType) }}</el-descriptions-item>
      <el-descriptions-item label="处理状态">
        <el-tag :type="statusColor(alarmDetail.processStatus)">{{ processStatusText(alarmDetail.processStatus) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="站点">{{ siteName(alarmDetail.siteId) }}</el-descriptions-item>
      <el-descriptions-item label="通道">{{ channelName(alarmDetail.channelId) }}</el-descriptions-item>
      <el-descriptions-item label="报警时间" :span="2">{{ formatTime(alarmDetail.alarmTime) }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ alarmDetail.description || '-' }}</el-descriptions-item>
    </el-descriptions>

    <div class="images">
      <div v-if="imageUrls.targetImageUrl" class="img-wrap">
        <p>目标截图</p>
        <el-image :src="imageUrls.targetImageUrl" fit="contain" style="height: 200px" />
      </div>
      <div v-if="imageUrls.sceneImageUrl" class="img-wrap">
        <p>场景截图</p>
        <el-image :src="imageUrls.sceneImageUrl" fit="contain" style="height: 200px" />
      </div>
    </div>

    <div class="actions">
      <el-button type="primary" @click="mark('PROCESSED')">标记已处理</el-button>
      <el-button @click="mark('FALSE_ALARM')">标记误报</el-button>
      <el-button v-if="imageUrls.targetImageUrl" type="success" @click="downloadImage">下载图片</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getAlarmDetail } from '@/api/alarm'
import type { Alarm } from '@/types'

const props = defineProps<{ alarm: Alarm | null }>()
const emit = defineEmits<{ (e: 'processed', payload: { id: number; status: string }): void }>()

const alarmDetail = ref<Alarm | null>(null)
const imageUrls = ref({ targetImageUrl: '', sceneImageUrl: '' })

watch(() => props.alarm, async (val) => {
  if (!val) return
  try {
    const detail = await getAlarmDetail(val.id)
    alarmDetail.value = detail.alarm || detail
    imageUrls.value = {
      targetImageUrl: detail.targetImageUrl || '',
      sceneImageUrl: detail.sceneImageUrl || ''
    }
  } catch {
    alarmDetail.value = val
    imageUrls.value = { targetImageUrl: '', sceneImageUrl: '' }
  }
}, { immediate: true })

function alarmTypeText(type: string) {
  return type === 'SMOKE' ? '抽烟检测' : 'PDI违规'
}
function processStatusText(status: string) {
  const map: Record<string, string> = { UNPROCESSED: '未处理', PROCESSED: '已处理', FALSE_ALARM: '误报' }
  return map[status] || status
}
function statusColor(status: string) {
  const map: Record<string, any> = { UNPROCESSED: 'danger', PROCESSED: 'success', FALSE_ALARM: 'info' }
  return map[status] || ''
}
function siteName(siteId: number) {
  return siteId === 1 ? '金桥库' : siteId === 2 ? '凯迪库' : '未知'
}
function channelName(channelId: number) {
  return 'CH_' + channelId
}
function formatTime(time: string) {
  return time ? new Date(time).toLocaleString('zh-CN') : '-'
}
function mark(status: string) {
  if (!alarmDetail.value) return
  emit('processed', { id: alarmDetail.value.id, status })
}
function downloadImage() {
  if (imageUrls.value.targetImageUrl) {
    window.open(imageUrls.value.targetImageUrl, '_blank')
  }
}
</script>

<style scoped>
.detail {
  padding: 8px;
}
.images {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}
.img-wrap {
  flex: 1;
}
.img-wrap p {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.actions {
  margin-top: 16px;
  text-align: right;
}
</style>

<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card>
          <div class="stat-title">在线盒子</div>
          <div class="stat-value" style="color: #67c23a">{{ monitorData?.onlineBoxes || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-title">离线盒子</div>
          <div class="stat-value" style="color: #f56c6c">{{ monitorData?.offlineBoxes || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-title">在线通道</div>
          <div class="stat-value" style="color: #409eff">{{ onlineChannels || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-title">离线通道</div>
          <div class="stat-value" style="color: #e6a23c">{{ offlineChannels || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>盒子在线状态</span>
          </template>
          <div ref="boxPieChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近24小时报警趋势</span>
          </template>
          <div ref="alarmLineChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>盒子列表状态</span>
          </template>
          <el-table :data="boxList" v-loading="loading" stripe size="small" :max-height="300">
            <el-table-column prop="boxId" label="盒子编号" width="120" />
            <el-table-column prop="boxName" label="名称" width="120" />
            <el-table-column prop="siteId" label="站点" width="100">
              <template #default="{ row }">{{ row.siteId === 1 ? '金桥库' : '凯迪库' }}</template>
            </el-table-column>
            <el-table-column prop="ipAddress" label="IP地址" width="130" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '在线' : '离线' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastHeartbeat" label="最后心跳" width="160" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>通道状态</span>
          </template>
          <el-table :data="channelList" v-loading="loading" stripe size="small" :max-height="300">
            <el-table-column prop="channelId" label="通道编号" width="120" />
            <el-table-column prop="channelName" label="名称" width="120" />
            <el-table-column prop="channelType" label="类型" width="100" />
            <el-table-column prop="algorithmType" label="算法" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '在线' : '离线' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getDeviceMonitor, getBoxes, getChannels } from '@/api/device'
import type { EdgeBox, Channel } from '@/types'

const boxPieChart = ref<HTMLDivElement>()
const alarmLineChart = ref<HTMLDivElement>()
const monitorData = ref<any>({})
const boxList = ref<EdgeBox[]>([])
const channelList = ref<Channel[]>([])
const loading = ref(false)
let boxPieInstance: echarts.ECharts | null = null
let alarmLineInstance: echarts.ECharts | null = null

const onlineChannels = computed(() => {
  return channelList.value.filter(c => c.status === 1).length
})
const offlineChannels = computed(() => {
  return channelList.value.filter(c => c.status === 0).length
})

async function loadData() {
  loading.value = true
  try {
    monitorData.value = await getDeviceMonitor().catch(() => ({}))
    const boxRes = await getBoxes({ page: 1, size: 100 })
    boxList.value = boxRes.records || []
    const chRes = await getChannels({ page: 1, size: 100 })
    channelList.value = chRes.records || []
  } finally {
    loading.value = false
  }
}

function initCharts() {
  boxPieInstance = echarts.init(boxPieChart.value!)
  boxPieInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [
      {
        name: '盒子状态',
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: monitorData.value.onlineBoxes || 0, name: '在线' },
          { value: monitorData.value.offlineBoxes || 0, name: '离线' },
        ],
      },
    ],
  })

  alarmLineInstance = echarts.init(alarmLineChart.value!)
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  alarmLineInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value' },
    series: [
      {
        name: '报警数',
        type: 'line',
        smooth: true,
        data: monitorData.value.alarmTrend || Array(24).fill(0),
      },
    ],
  })
}

function handleResize() {
  boxPieInstance?.resize()
  alarmLineInstance?.resize()
}

onMounted(() => {
  loadData().then(() => initCharts())
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  boxPieInstance?.dispose()
  alarmLineInstance?.dispose()
})
</script>

<style scoped>
.page {
  padding-bottom: 16px;
}
.stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
}
</style>

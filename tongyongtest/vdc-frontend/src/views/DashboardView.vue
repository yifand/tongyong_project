<template>
  <div class="dashboard">
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
          <div class="stat-title">今日报警</div>
          <div class="stat-value" style="color: #e6a23c">{{ monitorData?.todayAlarms || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-title">今日PDI作业</div>
          <div class="stat-value" style="color: #409eff">{{ monitorData?.todaySessions || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card title="设备在线状态">
          <div ref="pieChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="最近24小时报警趋势">
          <div ref="lineChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDeviceMonitor } from '@/api/device'

const pieChart = ref<HTMLDivElement>()
const lineChart = ref<HTMLDivElement>()
const monitorData = ref<any>({})
let pieInstance: echarts.ECharts | null = null
let lineInstance: echarts.ECharts | null = null

async function initCharts() {
  monitorData.value = await getDeviceMonitor().catch(() => ({}))

  pieInstance = echarts.init(pieChart.value!)
  pieInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [
      {
        name: '设备状态',
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: monitorData.value.onlineBoxes || 0, name: '在线' },
          { value: monitorData.value.offlineBoxes || 0, name: '离线' },
        ],
      },
    ],
  })

  lineInstance = echarts.init(lineChart.value!)
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  lineInstance.setOption({
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
  pieInstance?.resize()
  lineInstance?.resize()
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieInstance?.dispose()
  lineInstance?.dispose()
})
</script>

<style scoped>
.dashboard {
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

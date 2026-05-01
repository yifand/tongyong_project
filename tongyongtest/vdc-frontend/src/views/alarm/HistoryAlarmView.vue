<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header"><span>历史预警查询</span></div>
      </template>
      <el-form :model="query" inline>
        <el-form-item label="站点">
          <el-select v-model="query.siteId" placeholder="全部" clearable style="width: 140px">
            <el-option label="金桥库" :value="1" />
            <el-option label="凯迪库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.alarmType" placeholder="全部" clearable style="width: 140px">
            <el-option label="抽烟检测" value="SMOKE" />
            <el-option label="PDI违规" value="PDI_UNQUALIFIED" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.processStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="未处理" value="UNPROCESSED" />
            <el-option label="已处理" value="PROCESSED" />
            <el-option label="误报" value="FALSE_ALARM" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleExport('csv')">导出CSV</el-button>
          <el-button @click="handleExport('xlsx')">导出Excel</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="alarmType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.alarmType === 'SMOKE' ? 'danger' : 'warning'" size="small">
              {{ row.alarmType === 'SMOKE' ? '抽烟检测' : 'PDI违规' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="siteId" label="站点" width="120">
          <template #default="{ row }">
            {{ row.siteId === 1 ? '金桥库' : row.siteId === 2 ? '凯迪库' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="channelId" label="通道" width="120" />
        <el-table-column prop="alarmTime" label="时间" width="180" />
        <el-table-column prop="processStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusColor(row.processStatus)" size="small">{{ statusText(row.processStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="handleSearch" />
    </el-card>

    <el-dialog v-model="detailVisible" title="报警详情" width="700px">
      <AlarmDetail :alarm="selectedAlarm" @processed="onProcessed" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlarms, exportAlarms, processAlarm } from '@/api/alarm'
import type { Alarm, AlarmQueryParams } from '@/types'
import AlarmDetail from './AlarmDetail.vue'

const query = reactive<AlarmQueryParams>({ page: 1, size: 10 })
const dateRange = ref<string[]>([])
const tableData = ref<Alarm[]>([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const selectedAlarm = ref<Alarm | null>(null)

function statusText(status: string) {
  const map: Record<string, string> = { UNPROCESSED: '未处理', PROCESSED: '已处理', FALSE_ALARM: '误报' }
  return map[status] || status
}
function statusColor(status: string) {
  const map: Record<string, any> = { UNPROCESSED: 'danger', PROCESSED: 'success', FALSE_ALARM: 'info' }
  return map[status] || ''
}

async function handleSearch() {
  loading.value = true
  try {
    const params = { ...query }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getAlarms(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handleExport(format: 'csv' | 'xlsx') {
  const params = { ...query }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  const blob = await exportAlarms(params, format)
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const ext = format === 'xlsx' ? 'xlsx' : 'csv'
  a.download = `alarms_${new Date().toISOString().slice(0,10)}.${ext}`
  a.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

function openDetail(row: Alarm) {
  selectedAlarm.value = row
  detailVisible.value = true
}

async function onProcessed({ id, status }: { id: number; status: string }) {
  await processAlarm(id, status)
  detailVisible.value = false
  await handleSearch()
}

handleSearch()
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { font-weight: bold; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>

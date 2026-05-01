<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header"><span>PDI 作业报表</span></div>
      </template>
      <el-form :model="query" inline>
        <el-form-item label="站点">
          <el-select v-model="query.siteId" placeholder="全部" clearable style="width: 140px">
            <el-option label="金桥库" :value="1" />
            <el-option label="凯迪库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="工位">
          <el-select v-model="query.channelId" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.channelName" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="query.result" placeholder="全部" clearable style="width: 140px">
            <el-option label="合格" value="QUALIFIED" />
            <el-option label="临界" value="CRITICAL" />
            <el-option label="不合格" value="UNQUALIFIED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="siteId" label="站点" width="100">
          <template #default="{ row }">{{ row.siteId === 1 ? '金桥库' : '凯迪库' }}</template>
        </el-table-column>
        <el-table-column prop="channelId" label="工位" width="120" />
        <el-table-column prop="vehicleInfo" label="车辆信息" width="140" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="actualDuration" label="实际时长" width="120">
          <template #default="{ row }">{{ formatDuration(row.actualDuration) }}</template>
        </el-table-column>
        <el-table-column prop="standardDuration" label="标准工时" width="120">
          <template #default="{ row }">{{ formatDuration(row.standardDuration) }}</template>
        </el-table-column>
        <el-table-column prop="deviationPct" label="偏差" width="100">
          <template #default="{ row }">{{ row.deviationPct ?? '-' }}%</template>
        </el-table-column>
        <el-table-column prop="result" label="判定结果" width="100">
          <template #default="{ row }">
            <el-tag :type="resultColor(row.result)" size="small">{{ resultText(row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="handleSearch" />
    </el-card>

    <el-dialog v-model="exportVisible" title="导出报表" width="400px">
      <el-form :model="exportForm" label-width="100px">
        <el-form-item label="导出格式">
          <el-radio-group v-model="exportForm.format">
            <el-radio label="xlsx">Excel</el-radio>
            <el-radio label="pdf">PDF</el-radio>
            <el-radio label="zip">ZIP (含报表+截图)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="包含截图" v-if="exportForm.format === 'zip'">
          <el-switch v-model="exportForm.includeImages" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExport">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="作业详情" width="700px">
      <el-descriptions :column="2" border v-if="selectedRow">
        <el-descriptions-item label="站点">{{ selectedRow.siteId === 1 ? '金桥库' : '凯迪库' }}</el-descriptions-item>
        <el-descriptions-item label="工位">{{ selectedRow.channelId }}</el-descriptions-item>
        <el-descriptions-item label="车辆信息">{{ selectedRow.vehicleInfo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="判定结果">
          <el-tag :type="resultColor(selectedRow.result)">{{ resultText(selectedRow.result) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ selectedRow.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ selectedRow.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际时长">{{ formatDuration(selectedRow.actualDuration) }}</el-descriptions-item>
        <el-descriptions-item label="标准工时">{{ formatDuration(selectedRow.standardDuration) }}</el-descriptions-item>
        <el-descriptions-item label="偏差百分比">{{ selectedRow.deviationPct ?? '-' }}%</el-descriptions-item>
      </el-descriptions>
      <div class="images" v-if="selectedRow">
        <div v-if="selectedRow.snapshotHead" class="img-wrap">
          <p>头帧截图</p>
          <el-image :src="selectedRow.snapshotHead" fit="contain" style="height: 180px" />
        </div>
        <div v-if="selectedRow.snapshotMid" class="img-wrap">
          <p>中间帧</p>
          <el-image :src="selectedRow.snapshotMid" fit="contain" style="height: 180px" />
        </div>
        <div v-if="selectedRow.snapshotTail" class="img-wrap">
          <p>尾帧截图</p>
          <el-image :src="selectedRow.snapshotTail" fit="contain" style="height: 180px" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPdiReports, getPdiReportDetail, exportPdiReports } from '@/api/report'
import { getChannels } from '@/api/device'
import type { WorkSession, Channel, ReportQueryParams } from '@/types'

const query = reactive<ReportQueryParams>({ page: 1, size: 10 })
const dateRange = ref<string[]>([])
const tableData = ref<WorkSession[]>([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const selectedRow = ref<WorkSession | null>(null)
const channels = ref<Channel[]>([])
const exportVisible = ref(false)
const exportForm = reactive({ format: 'xlsx', includeImages: false })

function resultText(r: string) {
  const map: Record<string, string> = { QUALIFIED: '合格', CRITICAL: '临界', UNQUALIFIED: '不合格' }
  return map[r] || r
}
function resultColor(r: string) {
  const map: Record<string, any> = { QUALIFIED: 'success', CRITICAL: 'warning', UNQUALIFIED: 'danger' }
  return map[r] || ''
}
function formatDuration(s?: number) {
  if (s == null) return '-'
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}分${sec}秒`
}

async function handleSearch() {
  loading.value = true
  try {
    const params = { ...query }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getPdiReports(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleExport() {
  exportForm.format = 'xlsx'
  exportForm.includeImages = false
  exportVisible.value = true
}

async function confirmExport() {
  const params: any = { ...query }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  params.format = exportForm.format
  params.includeImages = exportForm.includeImages
  const blob = await exportPdiReports(params)
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const ext = exportForm.format === 'zip' ? 'zip' : exportForm.format
  a.download = `pdi_report_${new Date().toISOString().slice(0,10)}.${ext}`
  a.click()
  window.URL.revokeObjectURL(url)
  exportVisible.value = false
  ElMessage.success('导出成功')
}

async function openDetail(row: WorkSession) {
  detailVisible.value = true
  selectedRow.value = row
  try {
    const detail = await getPdiReportDetail(row.id)
    if (detail && detail.session) {
      selectedRow.value = {
        ...row,
        snapshotHead: detail.snapshotHeadUrl || row.snapshotHead,
        snapshotMid: detail.snapshotMidUrl || row.snapshotMid,
        snapshotTail: detail.snapshotTailUrl || row.snapshotTail,
      }
    }
  } catch (e) {
    // fallback to row data
  }
}

onMounted(async () => {
  const chRes = await getChannels({ size: 1000 })
  channels.value = chRes.records || []
  handleSearch()
})
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { font-weight: bold; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.images { display: flex; gap: 12px; margin-top: 16px; }
.img-wrap { flex: 1; }
.img-wrap p { font-size: 12px; color: #909399; margin-bottom: 4px; }
</style>

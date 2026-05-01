<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header"><span>操作日志</span></div>
      </template>
      <el-form :model="query" inline>
        <el-form-item label="操作类型">
          <el-select v-model="query.operationType" placeholder="全部" clearable style="width: 160px">
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="配置修改" value="CONFIG_CHANGE" />
            <el-option label="导出" value="EXPORT" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="createdAt" label="操作时间" width="180" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="operationType" label="操作类型" width="120" />
        <el-table-column prop="operationContent" label="操作内容" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small">{{ row.result === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="handleSearch" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { getOperationLogs } from '@/api/user'
import type { OperationLog, LogQueryParams } from '@/types'

const query = reactive<LogQueryParams>({ page: 1, size: 10 })
const dateRange = ref<string[]>([])
const tableData = ref<OperationLog[]>([])
const total = ref(0)
const loading = ref(false)

async function handleSearch() {
  loading.value = true
  try {
    const params = { ...query }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getOperationLogs(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

handleSearch()
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { font-weight: bold; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>

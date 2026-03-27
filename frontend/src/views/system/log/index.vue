<script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { getLogList } from '@/api/modules/system'
  import { useTable } from '@/composables/useTable'
  import type { LogItem, LogQueryParams } from '@/api/types'

  const queryForm = reactive<LogQueryParams>({
    page: 1,
    size: 10,
    username: '',
    operation: '',
    status: '',
    startTime: '',
    endTime: ''
  })

  const { loading, list, total, pagination, handlePageChange, handleSizeChange } = useTable<
    LogItem,
    LogQueryParams
  >({
    fetchFn: getLogList,
    initialQuery: queryForm
  })

  const statusOptions = [
    { value: 'success', label: '成功' },
    { value: 'error', label: '失败' }
  ]

  function handleSearch() {
    queryForm.page = 1
    // 重新加载数据
  }

  function handleReset() {
    queryForm.username = ''
    queryForm.operation = ''
    queryForm.status = ''
    queryForm.startTime = ''
    queryForm.endTime = ''
    handleSearch()
  }

  function getStatusType(status: string) {
    return status === 'success' ? 'success' : 'danger'
  }
</script>

<template>
  <div class="system-log-page">
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="queryForm.operation" placeholder="请输入操作类型" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.startTime"
            type="datetime"
            placeholder="开始时间"
            style="width: 180px"
          />
          <span style="margin: 0 8px">至</span>
          <el-date-picker
            v-model="queryForm.endTime"
            type="datetime"
            placeholder="结束时间"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="operation" label="操作类型" min-width="120" />
        <el-table-column prop="method" label="请求方法" min-width="150" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" min-width="120" />
        <el-table-column prop="duration" label="耗时(ms)" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" min-width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
  .system-log-page {
    .search-card {
      margin-bottom: 20px;
    }

    .table-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .pagination-wrapper {
        display: flex;
        justify-content: flex-end;
        margin-top: 20px;
      }
    }
  }
</style>

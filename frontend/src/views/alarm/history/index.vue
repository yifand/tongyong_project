<script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { getAlarmHistory } from '@/api/modules/alarm'
  import { useTable } from '@/composables/useTable'
  import type { AlarmItem, AlarmQueryParams } from '@/api/types'

  const queryForm = reactive<AlarmQueryParams>({
    page: 1,
    size: 10,
    alarmType: '',
    level: undefined,
    startTime: '',
    endTime: '',
    isConfirmed: undefined
  })

  const { loading, list, total, pagination, handlePageChange, handleSizeChange } = useTable<
    AlarmItem,
    AlarmQueryParams
  >({
    fetchFn: getAlarmHistory,
    initialQuery: queryForm
  })

  const levelOptions = [
    { value: 1, label: '紧急' },
    { value: 2, label: '重要' },
    { value: 3, label: '一般' }
  ]

  const statusOptions = [
    { value: true, label: '已确认' },
    { value: false, label: '未确认' }
  ]

  function handleSearch() {
    queryForm.page = 1
    // 重新加载数据
  }

  function handleReset() {
    queryForm.alarmType = ''
    queryForm.level = undefined
    queryForm.startTime = ''
    queryForm.endTime = ''
    queryForm.isConfirmed = undefined
    handleSearch()
  }

  function handleExport() {
    // 导出数据
  }
</script>

<template>
  <div class="alarm-history-page">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="报警类型">
          <el-input v-model="queryForm.alarmType" placeholder="请输入报警类型" clearable />
        </el-form-item>
        <el-form-item label="报警级别">
          <el-select v-model="queryForm.level" placeholder="请选择级别" clearable style="width: 120px">
            <el-option v-for="item in levelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="queryForm.isConfirmed" placeholder="请选择状态" clearable style="width: 120px">
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

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>报警记录</span>
          <el-button type="primary" @click="handleExport">
            <el-icon><download /></el-icon>
            导出
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="alarmType" label="报警类型" min-width="120" />
        <el-table-column prop="level" label="级别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.level === 1 ? 'danger' : row.level === 2 ? 'warning' : 'info'" effect="dark">
              {{ row.level === 1 ? '紧急' : row.level === 2 ? '重要' : '一般' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备名称" min-width="120" />
        <el-table-column prop="channelLocation" label="通道位置" min-width="150" />
        <el-table-column prop="createTime" label="报警时间" min-width="160" />
        <el-table-column prop="isConfirmed" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isConfirmed ? 'success' : 'info'">
              {{ row.isConfirmed ? '已确认' : '未确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">详情</el-button>
            <el-button v-if="!row.isConfirmed" type="success" link size="small">确认</el-button>
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
  .alarm-history-page {
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

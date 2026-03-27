<script setup lang="ts">
  import { ref, reactive } from 'vue'

  const loading = ref(false)
  const queryForm = reactive({
    personName: '',
    alarmType: '',
    dateRange: []
  })

  const archiveList = ref([
    {
      id: '1',
      personName: '张三',
      personId: 'P001',
      alarmType: '安全帽检测',
      alarmCount: 5,
      firstAlarmTime: '2024-01-10 08:30:00',
      lastAlarmTime: '2024-01-15 16:45:00'
    },
    {
      id: '2',
      personName: '李四',
      personId: 'P002',
      alarmType: '反光衣检测',
      alarmCount: 3,
      firstAlarmTime: '2024-01-12 09:15:00',
      lastAlarmTime: '2024-01-14 14:20:00'
    }
  ])

  function handleSearch() {
    // 搜索逻辑
  }

  function handleReset() {
    queryForm.personName = ''
    queryForm.alarmType = ''
    queryForm.dateRange = []
  }

  function handleViewDetail(row: any) {
    // 查看详情
    console.log('View detail:', row)
  }

  function handleExport() {
    // 导出档案
  }
</script>

<template>
  <div class="archive-page">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="人员姓名">
          <el-input v-model="queryForm.personName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="报警类型">
          <el-select v-model="queryForm.alarmType" placeholder="请选择类型" clearable style="width: 160px">
            <el-option label="安全帽检测" value="helmet" />
            <el-option label="反光衣检测" value="vest" />
            <el-option label="吸烟检测" value="smoke" />
            <el-option label="烟火检测" value="fire" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
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
          <span>行为档案列表</span>
          <el-button type="primary" @click="handleExport">
            <el-icon><download /></el-icon>
            导出档案
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="archiveList" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="personName" label="人员姓名" min-width="100" />
        <el-table-column prop="personId" label="人员编号" min-width="100" />
        <el-table-column prop="alarmType" label="报警类型" min-width="120" />
        <el-table-column prop="alarmCount" label="报警次数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="danger" effect="dark">{{ row.alarmCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="firstAlarmTime" label="首次报警" min-width="160" />
        <el-table-column prop="lastAlarmTime" label="最近报警" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
  .archive-page {
    .search-card {
      margin-bottom: 20px;
    }

    .table-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }
</style>

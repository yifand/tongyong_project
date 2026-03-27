<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { getChannelList } from '@/api/modules/device'
  import { useTable } from '@/composables/useTable'
  import type { Channel, ChannelQueryParams } from '@/api/types'

  const queryForm = ref<ChannelQueryParams>({
    page: 1,
    size: 10,
    name: '',
    deviceId: ''
  })

  const { loading, list, total, pagination, handlePageChange, handleSizeChange, fetchData } =
    useTable<Channel, ChannelQueryParams>({
      fetchFn: getChannelList,
      initialQuery: queryForm.value,
      immediate: false
    })

  const deviceOptions = ref([
    { label: '盒子 01', value: '1' },
    { label: '盒子 02', value: '2' }
  ])

  const aiModels = ref([
    { label: '安全帽检测', value: 'helmet' },
    { label: '反光衣检测', value: 'vest' },
    { label: '吸烟检测', value: 'smoke' },
    { label: '烟火检测', value: 'fire' }
  ])

  onMounted(() => {
    fetchData()
  })
</script>

<template>
  <div class="device-channel-page">
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="通道名称">
          <el-input v-model="queryForm.name" placeholder="请输入通道名称" clearable />
        </el-form-item>
        <el-form-item label="所属设备">
          <el-select v-model="queryForm.deviceId" placeholder="请选择设备" clearable style="width: 180px">
            <el-option
              v-for="item in deviceOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">
            <el-icon><search /></el-icon>
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>通道列表</span>
          <el-button type="primary">
            <el-icon><plus /></el-icon>
            新增通道
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="通道名称" min-width="120" />
        <el-table-column prop="code" label="通道编号" min-width="100" />
        <el-table-column prop="deviceName" label="所属设备" min-width="120" />
        <el-table-column prop="location" label="安装位置" min-width="150" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'online' ? 'success' : 'info'">
              {{ row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiEnabled" label="AI检测" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.aiEnabled" />
          </template>
        </el-table-column>
        <el-table-column prop="aiModels" label="检测模型" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="model in row.aiModels" :key="model" size="small" class="model-tag">
              {{ model }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">配置</el-button>
            <el-button type="success" link size="small">预览</el-button>
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
  .device-channel-page {
    .search-card {
      margin-bottom: 20px;
    }

    .table-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .model-tag {
        margin-right: 4px;
        margin-bottom: 4px;
      }

      .pagination-wrapper {
        display: flex;
        justify-content: flex-end;
        margin-top: 20px;
      }
    }
  }
</style>

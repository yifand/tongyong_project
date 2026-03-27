<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { getDeviceList, createDevice, updateDevice, deleteDevice } from '@/api/modules/device'
  import { useDeviceStore } from '@/stores/modules/device'
  import { useTable } from '@/composables/useTable'
  import type { Device, DeviceQueryParams } from '@/api/types'

  const deviceStore = useDeviceStore()
  const dialogVisible = ref(false)
  const dialogTitle = ref('新增设备')
  const formRef = ref()
  const isEdit = ref(false)

  const queryForm = ref<DeviceQueryParams>({
    page: 1,
    size: 10,
    name: '',
    status: ''
  })

  const { loading, list, total, pagination, handlePageChange, handleSizeChange, fetchData } =
    useTable<Device, DeviceQueryParams>({
      fetchFn: getDeviceList,
      initialQuery: queryForm.value,
      immediate: false
    })

  const formData = ref<Partial<Device>>({
    name: '',
    code: '',
    ip: '',
    port: 8080,
    description: ''
  })

  const rules = {
    name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
    code: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
    ip: [{ required: true, message: '请输入IP地址', trigger: 'blur' }],
    port: [{ required: true, message: '请输入端口号', trigger: 'blur' }]
  }

  const statusOptions = [
    { value: 'online', label: '在线' },
    { value: 'offline', label: '离线' },
    { value: 'error', label: '故障' }
  ]

  onMounted(() => {
    fetchData()
  })

  function handleAdd() {
    isEdit.value = false
    dialogTitle.value = '新增设备'
    formData.value = {
      name: '',
      code: '',
      ip: '',
      port: 8080,
      description: ''
    }
    dialogVisible.value = true
  }

  function handleEdit(row: Device) {
    isEdit.value = true
    dialogTitle.value = '编辑设备'
    formData.value = { ...row }
    dialogVisible.value = true
  }

  async function handleDelete(row: Device) {
    try {
      await ElMessageBox.confirm('确认删除该设备吗？', '提示', {
        type: 'warning'
      })
      await deleteDevice(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch {
      // 取消删除
    }
  }

  async function handleSubmit() {
    await formRef.value.validate()
    try {
      if (isEdit.value && formData.value.id) {
        await updateDevice(formData.value.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createDevice(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }

  function getStatusType(status: string) {
    const map: Record<string, string> = {
      online: 'success',
      offline: 'info',
      error: 'danger'
    }
    return map[status] || 'info'
  }
</script>

<template>
  <div class="device-box-page">
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="设备名称">
          <el-input v-model="queryForm.name" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          <span>边缘盒子列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><plus /></el-icon>
            新增设备
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="设备名称" min-width="120" />
        <el-table-column prop="code" label="设备编号" min-width="100" />
        <el-table-column prop="ip" label="IP地址" min-width="120" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status === 'online' ? '在线' : row.status === 'offline' ? '离线' : '故障' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="channelCount" label="通道数" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备编号" prop="code">
          <el-input v-model="formData.code" placeholder="请输入设备编号" />
        </el-form-item>
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="formData.ip" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="formData.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
  .device-box-page {
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

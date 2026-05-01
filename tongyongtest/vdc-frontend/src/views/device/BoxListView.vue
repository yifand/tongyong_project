<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>边缘盒子管理</span>
          <el-button type="primary" @click="openAdd">添加盒子</el-button>
        </div>
      </template>
      <el-form :model="query" inline>
        <el-form-item label="站点">
          <el-select v-model="query.siteId" placeholder="全部" clearable style="width: 140px">
            <el-option label="金桥库" :value="1" />
            <el-option label="凯迪库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="离线" :value="0" />
            <el-option label="在线" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="boxId" label="盒子编号" width="140" />
        <el-table-column prop="boxName" label="名称" width="140" />
        <el-table-column prop="siteId" label="站点" width="100">
          <template #default="{ row }">{{ row.siteId === 1 ? '金桥库' : '凯迪库' }}</template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '在线' : '离线' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="180" />
        <el-table-column prop="version" label="版本" width="120" />
        <el-table-column label="资源" width="220">
          <template #default="{ row }">
            <span v-if="row.cpuUsage != null">CPU:{{ row.cpuUsage }}%</span>
            <span v-if="row.memUsage != null" style="margin-left:8px">Mem:{{ row.memUsage }}%</span>
            <span v-if="row.diskUsage != null" style="margin-left:8px">Disk:{{ row.diskUsage }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="reboot(row)">重启</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="handleSearch" />
    </el-card>

    <el-dialog v-model="addVisible" title="添加盒子" width="500px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="盒子编号">
          <el-input v-model="addForm.boxId" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="addForm.boxName" />
        </el-form-item>
        <el-form-item label="站点">
          <el-select v-model="addForm.siteId" style="width: 100%">
            <el-option label="金桥库" :value="1" />
            <el-option label="凯迪库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="addForm.ipAddress" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBoxes, addBox, deleteBox, rebootBox } from '@/api/device'
import type { EdgeBox, BoxQueryParams } from '@/types'

const query = reactive<BoxQueryParams>({ page: 1, size: 10 })
const tableData = ref<EdgeBox[]>([])
const total = ref(0)
const loading = ref(false)
const addVisible = ref(false)
const addForm = reactive<Partial<EdgeBox>>({})

async function handleSearch() {
  loading.value = true
  try {
    const res = await getBoxes(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  Object.assign(addForm, { boxId: '', boxName: '', siteId: 1, ipAddress: '' })
  addVisible.value = true
}

async function confirmAdd() {
  await addBox(addForm)
  ElMessage.success('添加成功')
  addVisible.value = false
  handleSearch()
}

async function reboot(row: EdgeBox) {
  await ElMessageBox.confirm(`确定重启盒子 ${row.boxName} 吗？`, '提示', { type: 'warning' })
  await rebootBox(row.id)
  ElMessage.success('重启指令已发送')
}

async function remove(row: EdgeBox) {
  await ElMessageBox.confirm(`确定删除盒子 ${row.boxName} 吗？`, '提示', { type: 'warning' })
  await deleteBox(row.id)
  ElMessage.success('删除成功')
  handleSearch()
}

handleSearch()
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>

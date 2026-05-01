<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="openAdd">添加角色</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="roleName" label="角色名称" width="140" />
        <el-table-column prop="dataScope" label="数据权限" width="120">
          <template #default="{ row }">{{ row.dataScope === 'ALL' ? '全部' : '站点隔离' }}</template>
        </el-table-column>
        <el-table-column prop="permissions" label="功能权限">
          <template #default="{ row }">
            <el-tag v-for="p in row.permissions" :key="p" size="small" style="margin-right: 4px">{{ p }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="handleSearch" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '添加角色'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色编码">
          <el-input v-model="form.roleCode" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="数据权限">
          <el-radio-group v-model="form.dataScope">
            <el-radio label="ALL">全部站点</el-radio>
            <el-radio label="SITE_SPECIFIC">站点隔离</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="功能权限">
          <el-checkbox-group v-model="form.permissions">
            <el-checkbox label="user:read">用户查看</el-checkbox>
            <el-checkbox label="user:write">用户管理</el-checkbox>
            <el-checkbox label="role:read">角色查看</el-checkbox>
            <el-checkbox label="role:write">角色管理</el-checkbox>
            <el-checkbox label="alarm:read">预警查看</el-checkbox>
            <el-checkbox label="alarm:write">预警处理</el-checkbox>
            <el-checkbox label="report:read">报表查看</el-checkbox>
            <el-checkbox label="device:read">设备查看</el-checkbox>
            <el-checkbox label="device:write">设备管理</el-checkbox>
            <el-checkbox label="config:read">配置查看</el-checkbox>
            <el-checkbox label="config:write">配置管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole } from '@/api/user'
import type { SysRole, RoleQueryParams } from '@/types'

const query = reactive<RoleQueryParams>({ page: 1, size: 10 })
const tableData = ref<SysRole[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<Partial<SysRole> & { permissions?: string[] }>({ permissions: [] })

async function handleSearch() {
  loading.value = true
  try {
    const res = await getRoles(query)
    if (Array.isArray(res)) {
      tableData.value = res
      total.value = res.length
    } else {
      const paginated = res as unknown as { records?: SysRole[]; total?: number }
      tableData.value = paginated.records || []
      total.value = paginated.total || 0
    }
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editingId.value = null
  Object.assign(form, { roleCode: '', roleName: '', dataScope: 'SITE_SPECIFIC', permissions: [] })
  dialogVisible.value = true
}

function edit(row: SysRole) {
  editingId.value = row.id
  Object.assign(form, { ...row, permissions: row.permissions || [] })
  dialogVisible.value = true
}

async function confirmSave() {
  const payload = { ...form, permissions: form.permissions || [] }
  if (editingId.value) {
    await updateRole(editingId.value, payload)
  } else {
    await createRole(payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  handleSearch()
}

async function remove(row: SysRole) {
  await ElMessageBox.confirm(`确定删除角色 ${row.roleName} 吗？`, '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  handleSearch()
}

onMounted(() => handleSearch())
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>

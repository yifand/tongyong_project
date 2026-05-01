<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="openAdd">添加用户</el-button>
        </div>
      </template>
      <el-form :model="query" inline>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="roleId" label="角色" width="120">
          <template #default="{ row }">{{ roleName(row.roleId) }}</template>
        </el-table-column>
        <el-table-column prop="siteId" label="所属站点" width="120">
          <template #default="{ row }">{{ row.siteId === 1 ? '金桥库' : row.siteId === 2 ? '凯迪库' : '全部' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '添加用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="密码" v-if="!editingId">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属站点">
          <el-select v-model="form.siteId" clearable placeholder="全部站点" style="width: 100%">
            <el-option label="金桥库" :value="1" />
            <el-option label="凯迪库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { getUsers, createUser, updateUser, deleteUser } from '@/api/user'
import { getRoles } from '@/api/user'
import type { SysUser, SysRole, UserQueryParams } from '@/types'

const query = reactive<UserQueryParams>({ page: 1, size: 10 })
const tableData = ref<SysUser[]>([])
const total = ref(0)
const loading = ref(false)
const roles = ref<SysRole[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<Partial<SysUser>>({})

function roleName(id: number) {
  const r = roles.value.find(x => x.id === id)
  return r ? r.roleName : id
}

async function handleSearch() {
  loading.value = true
  try {
    const res = await getUsers(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editingId.value = null
  Object.assign(form, { username: '', password: '', realName: '', phone: '', email: '', roleId: undefined, siteId: undefined, status: 1 })
  dialogVisible.value = true
}

function edit(row: SysUser) {
  editingId.value = row.id
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function confirmSave() {
  if (editingId.value) {
    await updateUser(editingId.value, form)
  } else {
    await createUser(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  handleSearch()
}

async function remove(row: SysUser) {
  await ElMessageBox.confirm(`确定删除用户 ${row.username} 吗？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  handleSearch()
}

onMounted(async () => {
  const r = await getRoles({ size: 1000 })
  roles.value = Array.isArray(r) ? r : (r as unknown as { records?: SysRole[] }).records || []
  handleSearch()
})
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>

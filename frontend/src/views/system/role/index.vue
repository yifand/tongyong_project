<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { getRoleList, createRole, updateRole, deleteRole, getAllPermissions } from '@/api/modules/system'
  import { useTable } from '@/composables/useTable'
  import type { Role, RoleQueryParams } from '@/api/types'

  const queryForm = ref<RoleQueryParams>({
    page: 1,
    size: 10,
    name: '',
    code: ''
  })

  const { loading, list, total, pagination, handlePageChange, handleSizeChange, fetchData } =
    useTable<Role, RoleQueryParams>({
      fetchFn: getRoleList,
      initialQuery: queryForm.value,
      immediate: false
    })

  const dialogVisible = ref(false)
  const permissionDialogVisible = ref(false)
  const dialogTitle = ref('新增角色')
  const formRef = ref()
  const isEdit = ref(false)

  const formData = ref<Partial<Role>>({
    name: '',
    code: '',
    description: '',
    permissions: []
  })

  const allPermissions = ref<string[]>([
    'alarm:realtime:view',
    'alarm:history:view',
    'archive:view',
    'device:box:view',
    'device:channel:view',
    'system:user:view',
    'system:role:view',
    'system:config:view',
    'system:log:view'
  ])

  const rules = {
    name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
    code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
  }

  onMounted(() => {
    fetchData()
    loadPermissions()
  })

  async function loadPermissions() {
    try {
      const { data } = await getAllPermissions()
      allPermissions.value = data
    } catch {
      // 使用默认权限
    }
  }

  function handleAdd() {
    isEdit.value = false
    dialogTitle.value = '新增角色'
    formData.value = {
      name: '',
      code: '',
      description: '',
      permissions: []
    }
    dialogVisible.value = true
  }

  function handleEdit(row: Role) {
    isEdit.value = true
    dialogTitle.value = '编辑角色'
    formData.value = { ...row }
    dialogVisible.value = true
  }

  function handlePermission(row: Role) {
    formData.value = { ...row }
    permissionDialogVisible.value = true
  }

  async function handleDelete(row: Role) {
    try {
      await ElMessageBox.confirm('确认删除该角色吗？', '提示', {
        type: 'warning'
      })
      await deleteRole(row.id)
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
        await updateRole(formData.value.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createRole(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }
</script>

<template>
  <div class="system-role-page">
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="角色名称">
          <el-input v-model="queryForm.name" placeholder="请输入角色名称" clearable />
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
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="角色名称" min-width="120" />
        <el-table-column prop="code" label="角色编码" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userCount" label="用户数" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handlePermission(row)">权限</el-button>
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
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入角色编码" />
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

    <!-- 权限配置弹窗 -->
    <el-dialog v-model="permissionDialogVisible" title="权限配置" width="600px">
      <el-form label-width="0">
        <el-form-item>
          <el-checkbox-group v-model="formData.permissions">
            <el-checkbox v-for="perm in allPermissions" :key="perm" :label="perm">
              {{ perm }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
  .system-role-page {
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

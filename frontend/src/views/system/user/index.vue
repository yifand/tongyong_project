<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { getUserList, createUser, updateUser, deleteUser, resetPassword } from '@/api/modules/system'
  import { useTable } from '@/composables/useTable'
  import type { User, UserQueryParams } from '@/api/types'

  const queryForm = ref<UserQueryParams>({
    page: 1,
    size: 10,
    username: '',
    nickname: '',
    status: ''
  })

  const { loading, list, total, pagination, handlePageChange, handleSizeChange, fetchData } =
    useTable<User, UserQueryParams>({
      fetchFn: getUserList,
      initialQuery: queryForm.value,
      immediate: false
    })

  const dialogVisible = ref(false)
  const dialogTitle = ref('新增用户')
  const formRef = ref()
  const isEdit = ref(false)

  const formData = ref<Partial<User>>({
    username: '',
    nickname: '',
    email: '',
    phone: '',
    status: 'enabled',
    roleIds: []
  })

  const roleOptions = ref([
    { label: '管理员', value: '1' },
    { label: '操作员', value: '2' },
    { label: '访客', value: '3' }
  ])

  const statusOptions = [
    { value: 'enabled', label: '启用' },
    { value: 'disabled', label: '禁用' }
  ]

  const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
    email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
  }

  onMounted(() => {
    fetchData()
  })

  function handleAdd() {
    isEdit.value = false
    dialogTitle.value = '新增用户'
    formData.value = {
      username: '',
      nickname: '',
      email: '',
      phone: '',
      status: 'enabled',
      roleIds: []
    }
    dialogVisible.value = true
  }

  function handleEdit(row: User) {
    isEdit.value = true
    dialogTitle.value = '编辑用户'
    formData.value = { ...row }
    dialogVisible.value = true
  }

  async function handleDelete(row: User) {
    try {
      await ElMessageBox.confirm('确认删除该用户吗？', '提示', {
        type: 'warning'
      })
      await deleteUser(row.id)
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
        await updateUser(formData.value.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createUser(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }

  function handleResetPwd(row: User) {
    ElMessageBox.prompt('请输入新密码', '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'password'
    }).then(({ value }) => {
      resetPassword(row.id, value)
      ElMessage.success('密码重置成功')
    })
  }
</script>

<template>
  <div class="system-user-page">
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
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
          <span>用户列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="nickname" label="昵称" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="siteName" label="所属站点" min-width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'enabled' ? 'success' : 'danger'">
              {{ row.status === 'enabled' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleNames" label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="role in row.roleNames" :key="role" size="small" class="role-tag">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio label="enabled">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="formData.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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
  .system-user-page {
    .search-card {
      margin-bottom: 20px;
    }

    .table-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .role-tag {
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

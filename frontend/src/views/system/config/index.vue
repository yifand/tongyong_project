<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { getSystemConfig, updateSystemConfig } from '@/api/modules/system'
  import type { SystemConfig } from '@/api/types'

  const loading = ref(false)
  const saving = ref(false)

  const config = ref<SystemConfig>({
    siteName: 'PDI智能监测平台',
    logo: '',
    alarmSoundEnabled: true,
    alarmAutoPopup: true,
    dataRetentionDays: 30,
    videoRetentionDays: 7
  })

  onMounted(async () => {
    loading.value = true
    try {
      const { data } = await getSystemConfig()
      config.value = data
    } finally {
      loading.value = false
    }
  })

  async function handleSave() {
    saving.value = true
    try {
      await updateSystemConfig(config.value)
      ElMessage.success('保存成功')
    } catch {
      ElMessage.error('保存失败')
    } finally {
      saving.value = false
    }
  }
</script>

<template>
  <div class="system-config-page">
    <el-card v-loading="loading" class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
        </div>
      </template>

      <el-form label-width="150px">
        <el-divider>基础配置</el-divider>
        <el-form-item label="站点名称">
          <el-input v-model="config.siteName" placeholder="请输入站点名称" style="width: 300px" />
        </el-form-item>
        <el-form-item label="站点Logo">
          <el-upload class="logo-uploader" action="/api/upload" :show-file-list="false">
            <img v-if="config.logo" :src="config.logo" class="logo-preview" />
            <el-icon v-else class="logo-uploader-icon"><plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-divider>报警配置</el-divider>
        <el-form-item label="报警音效">
          <el-switch v-model="config.alarmSoundEnabled" />
        </el-form-item>
        <el-form-item label="报警自动弹窗">
          <el-switch v-model="config.alarmAutoPopup" />
        </el-form-item>

        <el-divider>存储配置</el-divider>
        <el-form-item label="数据保留天数">
          <el-input-number v-model="config.dataRetentionDays" :min="7" :max="365" />
          <span class="form-tip">报警数据和档案数据的保留天数</span>
        </el-form-item>
        <el-form-item label="视频保留天数">
          <el-input-number v-model="config.videoRetentionDays" :min="1" :max="30" />
          <span class="form-tip">报警抓拍图片和视频录像的保留天数</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>系统信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="前端版本">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="后端版本">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="数据库版本">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="License" :span="2">Enterprise Edition</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
  .system-config-page {
    .config-card {
      margin-bottom: 20px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .form-tip {
        margin-left: 12px;
        color: var(--el-text-color-secondary);
        font-size: 13px;
      }

      .logo-uploader {
        border: 1px dashed var(--el-border-color);
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: var(--el-transition-duration-fast);
        width: 120px;
        height: 120px;

        &:hover {
          border-color: var(--el-color-primary);
        }

        .logo-uploader-icon {
          font-size: 28px;
          color: #8c939d;
          width: 120px;
          height: 120px;
          text-align: center;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .logo-preview {
          width: 120px;
          height: 120px;
          display: block;
          object-fit: contain;
        }
      }
    }
  }
</style>

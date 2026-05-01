<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header"><span>阈值配置</span></div>
      </template>
      <el-empty v-if="configs.length === 0" description="暂无阈值配置项" />
      <el-form v-else label-width="180px">
        <el-form-item v-for="item in configs" :key="item.id" :label="item.configKey">
          <el-input v-model="item.configValue" style="width: 300px" />
          <span style="margin-left: 12px; color: #909399; font-size: 13px">{{ item.description }}</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveAll">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getThresholdConfigs, updateThresholdConfigs } from '@/api/config'
import type { SystemConfig } from '@/types'

const configs = ref<SystemConfig[]>([])

async function loadData() {
  configs.value = await getThresholdConfigs()
}

async function saveAll() {
  await updateThresholdConfigs(configs.value)
  ElMessage.success('保存成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { font-weight: bold; }
</style>

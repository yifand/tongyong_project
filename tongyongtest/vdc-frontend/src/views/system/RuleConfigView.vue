<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header"><span>业务规则配置</span></div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="ruleName" label="规则名称" width="160" />
        <el-table-column prop="channelType" label="适用工位" width="140" />
        <el-table-column prop="requireVehicle" label="需车辆在场" width="120">
          <template #default="{ row }">{{ row.requireVehicle ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="enterPattern" label="进入序列" show-overflow-tooltip />
        <el-table-column prop="exitPattern" label="离开序列" show-overflow-tooltip />
        <el-table-column prop="standardDuration" label="标准工时(秒)" width="130" />
        <el-table-column prop="criticalThresholdPct" label="临界阈值%" width="110" />
        <el-table-column prop="personAbsentTimeout" label="人员消失超时(秒)" width="150" />
        <el-table-column prop="isEnabled" label="启用" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isEnabled ? 'success' : 'info'" size="small">{{ row.isEnabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑规则" width="600px">
      <el-form :model="form" label-width="140px">
        <el-form-item label="规则名称">
          <el-input v-model="form.ruleName" disabled />
        </el-form-item>
        <el-form-item label="适用工位">
          <el-input v-model="form.channelType" disabled />
        </el-form-item>
        <el-form-item label="需车辆在场">
          <el-switch v-model="form.requireVehicle" />
        </el-form-item>
        <el-form-item label="进入序列(JSON)">
          <el-input v-model="enterPatternStr" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="离开序列(JSON)">
          <el-input v-model="exitPatternStr" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="标准工时(秒)">
          <el-input-number v-model="form.standardDuration" :min="1" />
        </el-form-item>
        <el-form-item label="临界阈值%">
          <el-input-number v-model="form.criticalThresholdPct" :min="0" :max="100" :precision="2" />
        </el-form-item>
        <el-form-item label="人员消失超时(秒)">
          <el-input-number v-model="form.personAbsentTimeout" :min="1" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.isEnabled" />
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
import { ElMessage } from 'element-plus'
import { getRuleConfigs, updateRuleConfigs } from '@/api/config'
import type { RuleConfig } from '@/types'

const tableData = ref<RuleConfig[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive<Partial<RuleConfig>>({})
const enterPatternStr = ref('')
const exitPatternStr = ref('')

async function loadData() {
  loading.value = true
  try {
    tableData.value = await getRuleConfigs()
  } finally {
    loading.value = false
  }
}

function edit(row: RuleConfig) {
  Object.assign(form, { ...row })
  enterPatternStr.value = JSON.stringify(row.enterPattern)
  exitPatternStr.value = JSON.stringify(row.exitPattern)
  dialogVisible.value = true
}

async function confirmSave() {
  try {
    form.enterPattern = JSON.parse(enterPatternStr.value)
    form.exitPattern = JSON.parse(exitPatternStr.value)
  } catch {
    ElMessage.error('JSON格式错误')
    return
  }
  await updateRuleConfigs([form as RuleConfig])
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { font-weight: bold; }
</style>

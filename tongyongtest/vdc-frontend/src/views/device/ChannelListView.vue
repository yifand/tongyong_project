<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通道管理</span>
          <el-button type="primary" @click="openAdd">添加通道</el-button>
        </div>
      </template>
      <el-form :model="query" inline>
        <el-form-item label="所属盒子">
          <el-select v-model="query.boxId" placeholder="全部" clearable style="width: 180px">
            <el-option v-for="box in boxes" :key="box.id" :label="box.boxName" :value="box.id" />
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
        <el-table-column prop="channelId" label="通道编号" width="120" />
        <el-table-column prop="channelName" label="名称" width="140" />
        <el-table-column prop="boxId" label="所属盒子" width="140">
          <template #default="{ row }">{{ boxName(row.boxId) }}</template>
        </el-table-column>
        <el-table-column prop="channelType" label="类型" width="120" />
        <el-table-column prop="algorithmType" label="算法" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '在线' : '离线' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rtspUrl" label="RTSP地址" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="success" @click="preview(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="handleSearch" />
    </el-card>

    <el-dialog v-model="editVisible" title="编辑通道" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="通道名称">
          <el-input v-model="editForm.channelName" />
        </el-form-item>
        <el-form-item label="RTSP地址">
          <el-input v-model="editForm.rtspUrl" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="editForm.password" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addVisible" title="添加通道" width="500px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="通道编号">
          <el-input v-model="addForm.channelId" />
        </el-form-item>
        <el-form-item label="通道名称">
          <el-input v-model="addForm.channelName" />
        </el-form-item>
        <el-form-item label="所属盒子">
          <el-select v-model="addForm.boxId" style="width: 100%">
            <el-option v-for="box in boxes" :key="box.id" :label="box.boxName" :value="box.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="通道类型">
          <el-select v-model="addForm.channelType" style="width: 100%">
            <el-option label="视频流" value="VIDEO_STREAM" />
            <el-option label="抓拍" value="SNAPSHOT" />
          </el-select>
        </el-form-item>
        <el-form-item label="算法类型">
          <el-select v-model="addForm.algorithmType" style="width: 100%">
            <el-option label="PDI前工位" value="PDI_FRONT" />
            <el-option label="PDI后工位" value="PDI_REAR" />
            <el-option label="PDI侧滑门" value="PDI_SLIDING" />
            <el-option label="吸烟检测" value="SMOKE" />
            <el-option label="混合检测" value="BOTH" />
          </el-select>
        </el-form-item>
        <el-form-item label="RTSP地址">
          <el-input v-model="addForm.rtspUrl" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="addForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="addForm.password" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="实时预览" width="700px">
      <div v-if="previewUrl" class="preview-box">
        <video v-if="isVideo" :src="previewUrl" controls autoplay style="width: 100%" />
        <div v-else>
          <p>预览地址: {{ previewUrl }}</p>
          <el-button type="primary" @click="openInNewTab">新窗口打开</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getChannels, updateChannel, getChannelPreview, getBoxes, addChannel } from '@/api/device'
import type { Channel, EdgeBox, ChannelQueryParams } from '@/types'

const query = reactive<ChannelQueryParams>({ page: 1, size: 10 })
const tableData = ref<Channel[]>([])
const total = ref(0)
const loading = ref(false)
const boxes = ref<EdgeBox[]>([])
const addVisible = ref(false)
const addForm = reactive<Partial<Channel>>({})
const editVisible = ref(false)
const editForm = reactive<Partial<Channel>>({})
let editingId = 0
const previewVisible = ref(false)
const previewUrl = ref('')
const isVideo = ref(false)

function boxName(id: number) {
  const b = boxes.value.find(x => x.id === id)
  return b ? b.boxName : id
}

async function handleSearch() {
  loading.value = true
  try {
    const res = await getChannels(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function edit(row: Channel) {
  editingId = row.id
  Object.assign(editForm, { channelName: row.channelName, rtspUrl: row.rtspUrl, username: row.username, password: row.password })
  editVisible.value = true
}

async function confirmEdit() {
  await updateChannel(editingId, editForm)
  ElMessage.success('保存成功')
  editVisible.value = false
  handleSearch()
}

function openAdd() {
  Object.assign(addForm, {
    channelId: '', channelName: '', boxId: undefined,
    channelType: 'VIDEO_STREAM', algorithmType: '',
    rtspUrl: '', username: '', password: ''
  })
  addVisible.value = true
}

async function confirmAdd() {
  await addChannel(addForm)
  ElMessage.success('添加成功')
  addVisible.value = false
  handleSearch()
}

async function preview(row: Channel) {
  try {
    const res = await getChannelPreview(row.id)
    previewUrl.value = res.streamUrl
    isVideo.value = /\.(mp4|m3u8|flv)(\?|$)/i.test(res.streamUrl)
    previewVisible.value = true
  } catch {
    ElMessage.error('获取预览地址失败')
  }
}

function openInNewTab() {
  window.open(previewUrl.value, '_blank')
}

onMounted(async () => {
  const b = await getBoxes({ size: 1000 })
  boxes.value = b.records || []
  handleSearch()
})
</script>

<style scoped>
.page { padding-bottom: 16px; }
.card-header { font-weight: bold; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.preview-box { text-align: center; }
</style>

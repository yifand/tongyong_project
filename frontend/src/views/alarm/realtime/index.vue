<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useAlarmStore } from '@/stores/modules/alarm'
  import { useWebSocket } from '@/composables/useWebSocket'

  const alarmStore = useAlarmStore()

  // WebSocket连接
  const { isConnected } = useWebSocket({
    url: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',
    onMessage: (data) => {
      if (data.type === 'alarm') {
        alarmStore.addRealtimeAlarm(data.payload)
      }
    }
  })

  const videoList = ref([
    { id: 1, name: '通道 01', status: 'online', url: '' },
    { id: 2, name: '通道 02', status: 'online', url: '' },
    { id: 3, name: '通道 03', status: 'offline', url: '' },
    { id: 4, name: '通道 04', status: 'online', url: '' }
  ])
</script>

<template>
  <div class="realtime-alarm-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">实时预警</h2>
        <el-tag :type="isConnected ? 'success' : 'danger'" effect="dark">
          {{ isConnected ? '实时连接中' : '连接断开' }}
        </el-tag>
      </div>
      <div class="header-right">
        <el-button type="primary">
          <el-icon><video-play /></el-icon>
          开始监测
        </el-button>
      </div>
    </div>

    <div class="video-grid">
      <div v-for="video in videoList" :key="video.id" class="video-item">
        <div class="video-header">
          <span class="video-name">{{ video.name }}</span>
          <el-tag size="small" :type="video.status === 'online' ? 'success' : 'danger'">
            {{ video.status === 'online' ? '在线' : '离线' }}
          </el-tag>
        </div>
        <div class="video-player">
          <div class="video-placeholder">
            <el-icon :size="48"><video-camera /></el-icon>
            <p>视频播放区域</p>
          </div>
        </div>
        <div class="video-toolbar">
          <el-button type="primary" link size="small">
            <el-icon><full-screen /></el-icon>
            全屏
          </el-button>
          <el-button type="primary" link size="small">
            <el-icon><camera /></el-icon>
            截图
          </el-button>
        </div>
      </div>
    </div>

    <!-- 最新报警弹窗 -->
    <el-dialog
      v-model="alarmStore.isAlarmDialogVisible"
      title="新报警"
      width="800px"
      destroy-on-close
    >
      <div v-if="alarmStore.currentAlarm" class="alarm-popup">
        <div class="alarm-image">
          <img :src="alarmStore.currentAlarm.imageUrl" alt="报警图片" />
        </div>
        <div class="alarm-info">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="报警类型">
              {{ alarmStore.currentAlarm.alarmType }}
            </el-descriptions-item>
            <el-descriptions-item label="报警级别">
              <el-tag :type="alarmStore.currentAlarm.level === 1 ? 'danger' : 'warning'">
                {{ alarmStore.currentAlarm.level === 1 ? '紧急' : '重要' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="通道位置">
              {{ alarmStore.currentAlarm.channelLocation }}
            </el-descriptions-item>
            <el-descriptions-item label="报警时间">
              {{ alarmStore.currentAlarm.createTime }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="alarmStore.hideAlarmDialog">忽略</el-button>
        <el-button type="primary" @click="alarmStore.hideAlarmDialog">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
  .realtime-alarm-page {
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .page-title {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
        }
      }
    }

    .video-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 20px;

      .video-item {
        background: var(--el-bg-color-overlay);
        border-radius: 8px;
        overflow: hidden;
        border: 1px solid var(--el-border-color);

        .video-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 16px;
          background: var(--el-bg-color-page);
          border-bottom: 1px solid var(--el-border-color);

          .video-name {
            font-weight: 500;
          }
        }

        .video-player {
          aspect-ratio: 16/9;
          background: #000;

          .video-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: rgba(255, 255, 255, 0.5);

            p {
              margin-top: 8px;
            }
          }
        }

        .video-toolbar {
          display: flex;
          justify-content: flex-end;
          padding: 8px 16px;
          border-top: 1px solid var(--el-border-color);
        }
      }
    }

    .alarm-popup {
      display: flex;
      gap: 20px;

      .alarm-image {
        flex: 1;
        max-width: 400px;

        img {
          width: 100%;
          border-radius: 8px;
        }
      }

      .alarm-info {
        flex: 1;
      }
    }
  }
</style>

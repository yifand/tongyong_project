<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { getDeviceList } from '@/api/modules/device'
  import { useDeviceStore } from '@/stores/modules/device'
  import { formatDateTime } from '@/utils/date'

  const deviceStore = useDeviceStore()
  const loading = ref(false)
  const recentAlarms = ref([
    { id: '1', type: '安全帽检测', level: 1, time: '2024-01-15 10:30:00', status: '未确认' },
    { id: '2', type: '反光衣检测', level: 2, time: '2024-01-15 09:15:00', status: '已确认' },
    { id: '3', type: '烟火检测', level: 1, time: '2024-01-15 08:45:00', status: '未确认' }
  ])

  const statistics = ref([
    { title: '今日报警', value: 24, icon: 'bell', color: '#ef4444' },
    { title: '设备在线', value: 86, unit: '%', icon: 'monitor', color: '#22c55e' },
    { title: '边缘盒子', value: 12, icon: 'box', color: '#3b82f6' },
    { title: '监控通道', value: 48, icon: 'video-camera', color: '#f97316' }
  ])

  onMounted(async () => {
    loading.value = true
    try {
      const { data } = await getDeviceList()
      deviceStore.setDevices(data.list)
    } finally {
      loading.value = false
    }
  })
</script>

<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="statistics-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="item in statistics" :key="item.title">
        <el-card class="statistic-card" shadow="hover">
          <div class="statistic-content">
            <div class="statistic-icon" :style="{ backgroundColor: item.color + '20', color: item.color }">
              <el-icon :size="28">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="statistic-info">
              <div class="statistic-value">
                {{ item.value }}
                <span class="unit" v-if="item.unit">{{ item.unit }}</span>
              </div>
              <div class="statistic-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>报警趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-placeholder">
            <p>报警趋势图表区域</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>设备状态</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <p>设备状态饼图区域</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近报警 -->
    <el-card class="alarm-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>最近报警</span>
          <el-button type="primary" text>查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentAlarms" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="报警类型" />
        <el-table-column prop="level" label="级别">
          <template #default="{ row }">
            <el-tag :type="row.level === 1 ? 'danger' : 'warning'">
              {{ row.level === 1 ? '紧急' : '重要' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === '已确认' ? 'success' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default>
            <el-button type="primary" link>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
  .dashboard-page {
    .statistics-row {
      margin-bottom: 20px;

      .statistic-card {
        margin-bottom: 20px;

        .statistic-content {
          display: flex;
          align-items: center;

          .statistic-icon {
            width: 60px;
            height: 60px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 16px;
          }

          .statistic-info {
            .statistic-value {
              font-size: 28px;
              font-weight: 600;
              color: var(--el-text-color-primary);
              line-height: 1.2;

              .unit {
                font-size: 16px;
                margin-left: 4px;
              }
            }

            .statistic-title {
              font-size: 14px;
              color: var(--el-text-color-secondary);
              margin-top: 4px;
            }
          }
        }
      }
    }

    .chart-row {
      margin-bottom: 20px;

      .chart-card {
        margin-bottom: 20px;

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .chart-placeholder {
          height: 300px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: var(--el-bg-color-page);
          border-radius: 8px;
          color: var(--el-text-color-secondary);
        }
      }
    }

    .alarm-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }
</style>

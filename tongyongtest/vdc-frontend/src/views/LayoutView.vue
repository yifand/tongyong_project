<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">VDC 业务平台</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409EFF"
        class="menu"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <AlarmNotificationBar />
        <div class="header-right">
          <span class="user-name">{{ authStore.userInfo?.realName || authStore.userInfo?.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  HomeFilled,
  BellFilled,
  WarningFilled,
  Document,
  Box,
  VideoCamera,
  UserFilled,
  User,
  SetUp,
  Operation,
  Tools,
  List,
  Monitor,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import AlarmNotificationBar from '@/components/AlarmNotificationBar.vue'

const router = useRouter()
const authStore = useAuthStore()

const menuItems = computed(() => [
  { path: '/dashboard', title: '首页', icon: HomeFilled },
  { path: '/alarm/realtime', title: '实时预警', icon: BellFilled },
  { path: '/alarm/history', title: '历史预警', icon: WarningFilled },
  { path: '/report/pdi', title: 'PDI报表', icon: Document },
  { path: '/device/monitor', title: '设备监控', icon: Monitor },
  { path: '/device/boxes', title: '盒子管理', icon: Box },
  { path: '/device/channels', title: '通道管理', icon: VideoCamera },
  { path: '/system/users', title: '用户管理', icon: UserFilled },
  { path: '/system/roles', title: '角色管理', icon: User },
  { path: '/system/rules', title: '规则配置', icon: SetUp },
  { path: '/system/thresholds', title: '阈值配置', icon: Operation },
  { path: '/system/general', title: '通用配置', icon: Tools },
  { path: '/system/logs', title: '操作日志', icon: List },
])

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.sidebar {
  background: #001529;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.menu {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  z-index: 10;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-name {
  font-size: 14px;
  color: #606266;
}
.main {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
}
</style>

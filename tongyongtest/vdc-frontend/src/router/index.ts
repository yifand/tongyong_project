import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/views/LayoutView.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '首页', icon: 'HomeFilled' },
        },
        {
          path: '/alarm/realtime',
          name: 'RealtimeAlarm',
          component: () => import('@/views/alarm/RealtimeAlarmView.vue'),
          meta: { title: '实时预警', icon: 'BellFilled' },
        },
        {
          path: '/alarm/history',
          name: 'HistoryAlarm',
          component: () => import('@/views/alarm/HistoryAlarmView.vue'),
          meta: { title: '历史预警', icon: 'WarningFilled' },
        },
        {
          path: '/report/pdi',
          name: 'PdiReport',
          component: () => import('@/views/report/PdiReportView.vue'),
          meta: { title: 'PDI报表', icon: 'Document' },
        },
        {
          path: '/device/monitor',
          name: 'DeviceMonitor',
          component: () => import('@/views/device/DeviceMonitorView.vue'),
          meta: { title: '设备监控', icon: 'Monitor' },
        },
        {
          path: '/device/boxes',
          name: 'BoxList',
          component: () => import('@/views/device/BoxListView.vue'),
          meta: { title: '盒子管理', icon: 'Box' },
        },
        {
          path: '/device/channels',
          name: 'ChannelList',
          component: () => import('@/views/device/ChannelListView.vue'),
          meta: { title: '通道管理', icon: 'VideoCamera' },
        },
        {
          path: '/system/users',
          name: 'UserManage',
          component: () => import('@/views/system/UserManageView.vue'),
          meta: { title: '用户管理', icon: 'UserFilled' },
        },
        {
          path: '/system/roles',
          name: 'RoleManage',
          component: () => import('@/views/system/RoleManageView.vue'),
          meta: { title: '角色管理', icon: 'User' },
        },
        {
          path: '/system/rules',
          name: 'RuleConfig',
          component: () => import('@/views/system/RuleConfigView.vue'),
          meta: { title: '规则配置', icon: 'SetUp' },
        },
        {
          path: '/system/thresholds',
          name: 'ThresholdConfig',
          component: () => import('@/views/system/ThresholdConfigView.vue'),
          meta: { title: '阈值配置', icon: 'Operation' },
        },
        {
          path: '/system/general',
          name: 'GeneralConfig',
          component: () => import('@/views/system/GeneralConfigView.vue'),
          meta: { title: '通用配置', icon: 'Tools' },
        },
        {
          path: '/system/logs',
          name: 'OperationLog',
          component: () => import('@/views/system/OperationLogView.vue'),
          meta: { title: '操作日志', icon: 'List' },
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (to.path !== '/login' && !authStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router

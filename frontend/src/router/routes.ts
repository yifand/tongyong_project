import type { RouteRecordRaw } from 'vue-router'

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true, title: '登录', layout: 'FullscreenLayout' }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { hidden: true, title: '页面不存在', layout: 'FullscreenLayout' }
  }
]

export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '工作台',
          icon: 'dashboard',
          affix: true
        }
      }
    ]
  },
  {
    path: '/alarm',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/alarm/realtime',
    meta: { title: '预警中心', icon: 'alarm' },
    children: [
      {
        path: 'realtime',
        name: 'AlarmRealtime',
        component: () => import('@/views/alarm/realtime/index.vue'),
        meta: {
          title: '实时预警',
          icon: 'video',
          permissions: ['alarm:realtime:view']
        }
      },
      {
        path: 'history',
        name: 'AlarmHistory',
        component: () => import('@/views/alarm/history/index.vue'),
        meta: {
          title: '历史预警',
          icon: 'history',
          permissions: ['alarm:history:view']
        }
      }
    ]
  },
  {
    path: '/archive',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'Archive',
        component: () => import('@/views/archive/index.vue'),
        meta: {
          title: '行为档案',
          icon: 'archive',
          permissions: ['archive:view']
        }
      }
    ]
  },
  {
    path: '/device',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/device/box',
    meta: { title: '设备管理', icon: 'device' },
    children: [
      {
        path: 'box',
        name: 'DeviceBox',
        component: () => import('@/views/device/box/index.vue'),
        meta: {
          title: '边缘盒子',
          icon: 'box',
          permissions: ['device:box:view']
        }
      },
      {
        path: 'channel',
        name: 'DeviceChannel',
        component: () => import('@/views/device/channel/index.vue'),
        meta: {
          title: '通道管理',
          icon: 'channel',
          permissions: ['device:channel:view']
        }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'system' },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'user',
          permissions: ['system:user:view']
        }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'role',
          permissions: ['system:role:view']
        }
      },
      {
        path: 'config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: {
          title: '系统配置',
          icon: 'config',
          permissions: ['system:config:view']
        }
      },
      {
        path: 'log',
        name: 'SystemLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: {
          title: '日志审计',
          icon: 'log',
          permissions: ['system:log:view']
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

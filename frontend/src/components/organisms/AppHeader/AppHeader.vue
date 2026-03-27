<script setup lang="ts">
  import { computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useAppStore } from '@/stores/modules/app'
  import { useUserStore } from '@/stores/modules/user'
  import { useAlarmStore } from '@/stores/modules/alarm'

  const route = useRoute()
  const router = useRouter()
  const appStore = useAppStore()
  const userStore = useUserStore()
  const alarmStore = useAlarmStore()

  const breadcrumbs = computed(() => {
    const matched = route.matched.filter(item => item.meta?.title)
    return matched.map(item => ({
      path: item.path,
      title: item.meta?.title as string
    }))
  })

  const hasUnreadAlarms = computed(() => alarmStore.hasUnread)
  const unreadCount = computed(() => alarmStore.unreadCount)

  function toggleSidebar() {
    appStore.toggleSidebar()
  }

  function toggleTheme() {
    appStore.toggleTheme()
  }

  function handleCommand(command: string) {
    switch (command) {
      case 'profile':
        router.push('/profile')
        break
      case 'settings':
        router.push('/settings')
        break
      case 'logout':
        userStore.logout()
        router.push('/login')
        break
    }
  }

  function viewAlarms() {
    router.push('/alarm/realtime')
  }
</script>

<template>
  <el-header class="app-header">
    <div class="header-left">
      <el-icon class="toggle-icon" @click="toggleSidebar">
        <fold v-if="!appStore.sidebarCollapsed" />
        <expand v-else />
      </el-icon>
      <breadcrumb class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="{ path: item.path }">
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </breadcrumb>
    </div>
    <div class="header-right">
      <el-badge :value="unreadCount" :hidden="!hasUnreadAlarms" class="alarm-badge">
        <el-icon class="header-icon" @click="viewAlarms">
          <bell />
        </el-icon>
      </el-badge>
      <el-icon class="header-icon" @click="toggleTheme">
        <sunny v-if="appStore.isDark" />
        <moon v-else />
      </el-icon>
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="userStore.avatar">
            <el-icon><user /></el-icon>
          </el-avatar>
          <span class="username">{{ userStore.nickname || userStore.username }}</span>
          <el-icon><arrow-down /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="settings">系统设置</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<style scoped lang="scss">
  .app-header {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: var(--el-bg-color-overlay);
    border-bottom: 1px solid var(--el-border-color);
    padding: 0 20px;

    .header-left {
      display: flex;
      align-items: center;

      .toggle-icon {
        font-size: 20px;
        cursor: pointer;
        margin-right: 16px;
        color: var(--el-text-color-primary);

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .header-icon {
        font-size: 20px;
        cursor: pointer;
        color: var(--el-text-color-primary);

        &:hover {
          color: var(--el-color-primary);
        }
      }

      .alarm-badge {
        :deep(.el-badge__content) {
          background-color: var(--el-color-danger);
        }
      }

      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        gap: 8px;

        .username {
          font-size: 14px;
          color: var(--el-text-color-primary);
        }
      }
    }
  }
</style>

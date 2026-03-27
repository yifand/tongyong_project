<script setup lang="ts">
  import { computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useAppStore } from '@/stores/modules/app'
  import { useUserStore } from '@/stores/modules/user'

  const route = useRoute()
  const router = useRouter()
  const appStore = useAppStore()
  const userStore = useUserStore()

  const activeMenu = computed(() => {
    const { meta, path } = route
    if (meta?.activeMenu) {
      return meta.activeMenu as string
    }
    return path
  })

  const isCollapse = computed(() => appStore.sidebarCollapsed)

  const menuRoutes = computed(() => {
    // 从路由配置中过滤出有权限的路由
    return router.getRoutes().filter(r => {
      return r.meta && !r.meta.hidden && r.children && r.children.length > 0
    })
  })

  function handleSelect(path: string) {
    router.push(path)
  }
</script>

<template>
  <el-aside :width="appStore.sidebarWidth" class="app-sidebar">
    <div class="sidebar-header">
      <img src="/logo.png" alt="logo" class="logo" v-if="!isCollapse" />
      <span class="title" v-if="!isCollapse">PDI监测平台</span>
      <el-icon v-else class="collapse-logo">
        <monitor />
      </el-icon>
    </div>
    <el-scrollbar class="sidebar-menu">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="transparent"
        text-color="#94a3b8"
        active-text-color="#3b82f6"
        @select="handleSelect"
      >
        <el-sub-menu v-for="route in menuRoutes" :key="route.path" :index="route.path">
          <template #title>
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <span>{{ route.meta?.title }}</span>
          </template>
          <el-menu-item
            v-for="child in route.children"
            :key="child.path"
            :index="route.path + '/' + child.path"
          >
            <el-icon v-if="child.meta?.icon">
              <component :is="child.meta.icon" />
            </el-icon>
            <span>{{ child.meta?.title }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>
  </el-aside>
</template>

<style scoped lang="scss">
  .app-sidebar {
    background-color: var(--pdi-nav-bg);
    transition: width 0.3s;

    .sidebar-header {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 16px;
      border-bottom: 1px solid var(--pdi-border);

      .logo {
        width: 32px;
        height: 32px;
        margin-right: 12px;
      }

      .title {
        font-size: 18px;
        font-weight: 600;
        color: var(--pdi-text-primary);
        white-space: nowrap;
      }

      .collapse-logo {
        font-size: 24px;
        color: var(--pdi-primary);
      }
    }

    .sidebar-menu {
      height: calc(100% - 60px);
    }
  }
</style>

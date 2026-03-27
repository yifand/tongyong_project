<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from '@/components/organisms/AppSidebar/AppSidebar.vue'
import AppHeader from '@/components/organisms/AppHeader/AppHeader.vue'
import { useAppStore } from '@/stores/modules/app'

const route = useRoute()
const appStore = useAppStore()

const cachedViews = computed(() => {
  return []
})

const showTagsView = computed(() => {
  return route.meta.tagsView !== false
})
</script>

<template>
  <el-container class="default-layout">
    <AppSidebar />
    <el-container class="main-container">
      <AppHeader />
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.default-layout {
  width: 100%;
  height: 100vh;

  .main-container {
    flex-direction: column;
    height: 100%;
    overflow: hidden;

    .main-content {
      padding: 20px;
      background-color: var(--el-bg-color-page);
      overflow-y: auto;
    }
  }
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>

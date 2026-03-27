<script setup lang="ts">
// 全屏布局，用于登录页和实时监控页面
</script>

<template>
  <div class="fullscreen-layout">
    <router-view />
  </div>
</template>

<style scoped lang="scss">
.fullscreen-layout {
  width: 100%;
  height: 100vh;
  background-color: var(--el-bg-color-page);
}
</style>

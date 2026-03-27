<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const layout = computed(() => {
  const layoutName = route.meta.layout || 'DefaultLayout'
  return layoutName
})
</script>

<template>
  <component :is="layout">
    <router-view />
  </component>
</template>

<style scoped>
html,
body,
#app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}
</style>

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // State
  const sidebarCollapsed = ref(false)
  const isDark = ref(false)
  const language = ref('zh-CN')
  const size = ref<'default' | 'large' | 'small'>('default')
  const visitedViews = ref<{ name: string; path: string; title: string }[]>([])

  // Getters
  const sidebarWidth = computed(() => (sidebarCollapsed.value ? '64px' : '220px'))

  // Actions
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    // 切换 Element Plus 暗黑模式
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  function setTheme(dark: boolean) {
    isDark.value = dark
    if (dark) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  function setLanguage(lang: string) {
    language.value = lang
  }

  function setSize(s: 'default' | 'large' | 'small') {
    size.value = s
  }

  function addVisitedView(view: { name: string; path: string; title: string }) {
    if (!visitedViews.value.some(v => v.path === view.path)) {
      visitedViews.value.push(view)
    }
  }

  function removeVisitedView(path: string) {
    const index = visitedViews.value.findIndex(v => v.path === path)
    if (index > -1) {
      visitedViews.value.splice(index, 1)
    }
  }

  return {
    sidebarCollapsed,
    isDark,
    language,
    size,
    visitedViews,
    sidebarWidth,
    toggleSidebar,
    setSidebarCollapsed,
    toggleTheme,
    setTheme,
    setLanguage,
    setSize,
    addVisitedView,
    removeVisitedView
  }
})

import { ref, watch } from 'vue'

interface UseThemeOptions {
  storageKey?: string
  defaultDark?: boolean
}

export function useTheme(options: UseThemeOptions = {}) {
  const { storageKey = 'theme-dark', defaultDark = false } = options

  const isDark = ref(false)

  // 初始化主题
  function initTheme() {
    const stored = localStorage.getItem(storageKey)
    if (stored !== null) {
      isDark.value = stored === 'true'
    } else {
      // 检查系统偏好
      isDark.value =
        defaultDark || window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
  }

  // 应用主题
  function applyTheme() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 切换主题
  function toggleTheme() {
    isDark.value = !isDark.value
  }

  // 设置主题
  function setTheme(dark: boolean) {
    isDark.value = dark
  }

  // 监听主题变化并持久化
  watch(isDark, (newValue) => {
    applyTheme()
    localStorage.setItem(storageKey, String(newValue))
  })

  // 初始化
  initTheme()

  return {
    isDark,
    toggleTheme,
    setTheme
  }
}

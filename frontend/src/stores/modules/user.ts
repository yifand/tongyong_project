import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getUserInfo } from '@/api/modules/auth'
import type { UserInfo, LoginForm } from '@/api/types'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const siteId = computed(() => userInfo.value?.siteId || '')
  const siteName = computed(() => userInfo.value?.siteName || '')

  // Actions
  async function loginAction(form: LoginForm) {
    const { data } = await login(form)
    token.value = data.token
    localStorage.setItem('token', data.token)
    return data
  }

  async function fetchUserInfo() {
    const { data } = await getUserInfo()
    userInfo.value = data
    permissions.value = data.permissions || []
    roles.value = data.roles || []
    return data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    roles.value = []
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    permissions,
    roles,
    isLoggedIn,
    username,
    nickname,
    avatar,
    siteId,
    siteName,
    loginAction,
    fetchUserInfo,
    logout
  }
})

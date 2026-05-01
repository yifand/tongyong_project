import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, refreshToken as refreshTokenApi } from '@/api/auth'
import type { LoginRequest, LoginResponse } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('vdc_token') || '')
  const refreshTokenValue = ref<string>(localStorage.getItem('vdc_refresh_token') || '')
  const userInfo = ref<LoginResponse | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value)

  async function login(payload: LoginRequest) {
    loading.value = true
    try {
      const res = await loginApi(payload)
      token.value = res.accessToken
      refreshTokenValue.value = res.refreshToken
      localStorage.setItem('vdc_token', res.accessToken)
      localStorage.setItem('vdc_refresh_token', res.refreshToken)
      userInfo.value = res
      return true
    } finally {
      loading.value = false
    }
  }

  async function refreshToken() {
    if (!refreshTokenValue.value) return false
    try {
      const res = await refreshTokenApi(refreshTokenValue.value)
      token.value = res.accessToken
      refreshTokenValue.value = res.refreshToken
      localStorage.setItem('vdc_token', res.accessToken)
      localStorage.setItem('vdc_refresh_token', res.refreshToken)
      userInfo.value = res
      return true
    } catch {
      logout()
      return false
    }
  }

  async function logout() {
    try {
      if (token.value) {
        await logoutApi(token.value)
      }
    } catch {
      // ignore backend errors during logout
    }
    token.value = ''
    refreshTokenValue.value = ''
    userInfo.value = null
    localStorage.removeItem('vdc_token')
    localStorage.removeItem('vdc_refresh_token')
  }

  return {
    token,
    refreshTokenValue,
    userInfo,
    loading,
    isLoggedIn,
    login,
    refreshToken,
    logout,
  }
})

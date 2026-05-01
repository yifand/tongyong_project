import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import type { ApiResult } from '@/types'

const request: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

let isRefreshing = false
let refreshSubscribers: Array<(token: string) => void> = []

function onRefreshed(token: string) {
  refreshSubscribers.forEach((cb) => cb(token))
  refreshSubscribers = []
}

function addRefreshSubscriber(cb: (token: string) => void) {
  refreshSubscribers.push(cb)
}

request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  async (error) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }
    const msg = error.response?.data?.message || error.message || '网络错误'

    if (error.response?.status === 401 && !originalRequest._retry) {
      const authStore = useAuthStore()
      if (!authStore.refreshTokenValue) {
        authStore.logout()
        router.push('/login')
        return Promise.reject(error)
      }

      if (isRefreshing) {
        return new Promise((resolve) => {
          addRefreshSubscriber((token: string) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`
            }
            resolve(request(originalRequest))
          })
        })
      }

      isRefreshing = true
      originalRequest._retry = true

      try {
        const success = await authStore.refreshToken()
        if (success) {
          onRefreshed(authStore.token)
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${authStore.token}`
          }
          isRefreshing = false
          return request(originalRequest)
        } else {
          isRefreshing = false
          authStore.logout()
          router.push('/login')
          return Promise.reject(error)
        }
      } catch {
        isRefreshing = false
        authStore.logout()
        router.push('/login')
        return Promise.reject(error)
      }
    }

    ElMessage.error(msg)
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export function get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request.get<ApiResult<T>>(url, config).then((res) => res.data.data)
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request.post<ApiResult<T>>(url, data, config).then((res) => res.data.data)
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request.put<ApiResult<T>>(url, data, config).then((res) => res.data.data)
}

export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request.delete<ApiResult<T>>(url, config).then((res) => res.data.data)
}

export default request

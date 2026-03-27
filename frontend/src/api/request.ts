import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type AxiosError
} from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求队列（用于取消重复请求）
const pendingMap = new Map<string, AbortController>()

// 生成请求key
function getRequestKey(config: AxiosRequestConfig): string {
  return `${config.method}_${config.url}_${JSON.stringify(config.params)}_${JSON.stringify(config.data)}`
}

// 添加请求到队列
function addPending(config: AxiosRequestConfig) {
  const key = getRequestKey(config)
  const controller = new AbortController()
  config.signal = controller.signal
  pendingMap.set(key, controller)
}

// 移除请求
function removePending(config: AxiosRequestConfig) {
  const key = getRequestKey(config)
  if (pendingMap.has(key)) {
    pendingMap.delete(key)
  }
}

// 请求拦截器
request.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 添加token
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    // 添加站点ID（数据权限）
    if (userStore.siteId) {
      config.headers = config.headers || {}
      config.headers['X-Site-Id'] = userStore.siteId
    }

    // 防止重复请求
    removePending(config)
    addPending(config)

    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    removePending(response.config)

    const { code, message, data } = response.data

    // 成功
    if (code === 200) {
      return data
    }

    // 业务错误
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error: AxiosError) => {
    if (error.config) {
      removePending(error.config)
    }

    const { response } = error

    if (response) {
      switch (response.status) {
        case 401:
          ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            const userStore = useUserStore()
            userStore.logout()
            window.location.href = '/login'
          })
          break
        case 403:
          ElMessage.error('没有权限执行此操作')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(response.data?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

// 封装请求方法
export function get<T>(url: string, params?: object): Promise<T> {
  return request.get(url, { params }) as Promise<T>
}

export function post<T>(url: string, data?: object): Promise<T> {
  return request.post(url, data) as Promise<T>
}

export function put<T>(url: string, data?: object): Promise<T> {
  return request.put(url, data) as Promise<T>
}

export function del<T>(url: string, params?: object): Promise<T> {
  return request.delete(url, { params }) as Promise<T>
}

export default request

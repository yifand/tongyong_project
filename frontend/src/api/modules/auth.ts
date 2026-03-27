import { post, get } from '../request'
import type { ApiResponse, LoginForm, LoginResult, UserInfo } from '../types'

// 用户登录
export function login(data: LoginForm): Promise<ApiResponse<LoginResult>> {
  return post('/auth/login', data)
}

// 获取用户信息
export function getUserInfo(): Promise<ApiResponse<UserInfo>> {
  return get('/auth/userInfo')
}

// 用户登出
export function logout(): Promise<ApiResponse<void>> {
  return post('/auth/logout')
}

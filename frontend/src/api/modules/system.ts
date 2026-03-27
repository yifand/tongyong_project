import { get, post, put, del } from '../request'
import type {
  ApiResponse,
  User,
  UserQueryParams,
  UserListResult,
  Role,
  RoleQueryParams,
  RoleListResult,
  SystemConfig,
  LogItem,
  LogQueryParams,
  LogListResult
} from '../types'

// 用户管理
export function getUserList(params: UserQueryParams): Promise<ApiResponse<UserListResult>> {
  return get('/system/user/list', params)
}

export function createUser(data: Partial<User>): Promise<ApiResponse<User>> {
  return post('/system/user', data)
}

export function updateUser(id: string, data: Partial<User>): Promise<ApiResponse<User>> {
  return put(`/system/user/${id}`, data)
}

export function deleteUser(id: string): Promise<ApiResponse<void>> {
  return del(`/system/user/${id}`)
}

export function resetPassword(id: string, password: string): Promise<ApiResponse<void>> {
  return post(`/system/user/${id}/resetPassword`, { password })
}

// 角色管理
export function getRoleList(params: RoleQueryParams): Promise<ApiResponse<RoleListResult>> {
  return get('/system/role/list', params)
}

export function createRole(data: Partial<Role>): Promise<ApiResponse<Role>> {
  return post('/system/role', data)
}

export function updateRole(id: string, data: Partial<Role>): Promise<ApiResponse<Role>> {
  return put(`/system/role/${id}`, data)
}

export function deleteRole(id: string): Promise<ApiResponse<void>> {
  return del(`/system/role/${id}`)
}

export function getAllPermissions(): Promise<ApiResponse<string[]>> {
  return get('/system/role/permissions')
}

// 系统配置
export function getSystemConfig(): Promise<ApiResponse<SystemConfig>> {
  return get('/system/config')
}

export function updateSystemConfig(data: Partial<SystemConfig>): Promise<ApiResponse<SystemConfig>> {
  return put('/system/config', data)
}

// 日志管理
export function getLogList(params: LogQueryParams): Promise<ApiResponse<LogListResult>> {
  return get('/system/log/list', params)
}

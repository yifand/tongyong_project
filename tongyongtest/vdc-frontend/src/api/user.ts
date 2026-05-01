import { get, post, put, del } from './request'
import type { SysUser, SysRole, OperationLog } from '@/types'

export interface UserQueryParams {
  page?: number
  size?: number
  username?: string
}

export interface RoleQueryParams {
  page?: number
  size?: number
}

export interface LogQueryParams {
  page?: number
  size?: number
  operationType?: string
  startTime?: string
  endTime?: string
}

export function getUsers(params?: UserQueryParams): Promise<{ records: SysUser[], total: number }> {
  return get('/api/v1/users', { params })
}

export function createUser(data: Partial<SysUser>): Promise<SysUser> {
  return post('/api/v1/users', data)
}

export function updateUser(id: number, data: Partial<SysUser>): Promise<SysUser> {
  return put(`/api/v1/users/${id}`, data)
}

export function deleteUser(id: number): Promise<void> {
  return del(`/api/v1/users/${id}`)
}

export function getRoles(params?: RoleQueryParams): Promise<SysRole[]> {
  return get('/api/v1/roles', { params })
}

export function createRole(data: Partial<SysRole>): Promise<SysRole> {
  return post('/api/v1/roles', data)
}

export function updateRole(id: number, data: Partial<SysRole>): Promise<SysRole> {
  return put(`/api/v1/roles/${id}`, data)
}

export function deleteRole(id: number): Promise<void> {
  return del(`/api/v1/roles/${id}`)
}

export function getOperationLogs(params?: LogQueryParams): Promise<{ records: OperationLog[], total: number }> {
  return get('/api/v1/system/logs', { params })
}

// API 通用响应类型
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页参数
export interface PaginationParams {
  page: number
  size: number
}

// 分页结果
export interface PaginationResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  pages: number
}

// 登录相关
export interface LoginForm {
  username: string
  password: string
  captcha?: string
  remember?: boolean
}

export interface LoginResult {
  token: string
  expiresIn: number
}

export interface UserInfo {
  id: string
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  siteId: string
  siteName: string
  roles: string[]
  permissions: string[]
}

// 预警相关
export interface AlarmItem {
  id: string
  alarmType: string
  level: number
  message: string
  deviceId: string
  deviceName: string
  channelId: string
  channelLocation: string
  imageUrl: string
  images: string[]
  confidence: number
  detectArea: string
  isConfirmed: boolean
  confirmedBy?: string
  confirmedTime?: string
  createTime: string
  typeText?: string
}

export interface AlarmQueryParams extends PaginationParams {
  alarmType?: string
  level?: number
  startTime?: string
  endTime?: string
  deviceId?: string
  isConfirmed?: boolean
}

export type AlarmListResult = PaginationResult<AlarmItem>

export interface ConfirmAlarmParams {
  id: string
  remark?: string
}

// 设备相关
export interface Device {
  id: string
  name: string
  code: string
  ip: string
  port: number
  status: 'online' | 'offline' | 'error'
  siteId: string
  siteName: string
  channelCount: number
  description?: string
  createTime: string
  updateTime: string
}

export interface DeviceQueryParams extends PaginationParams {
  name?: string
  status?: string
  siteId?: string
}

export type DeviceListResult = PaginationResult<Device>

export type DeviceStatus = 'online' | 'offline' | 'error'

// 通道相关
export interface Channel {
  id: string
  name: string
  code: string
  deviceId: string
  deviceName: string
  location: string
  status: 'online' | 'offline'
  streamUrl?: string
  aiEnabled: boolean
  aiModels: string[]
  createTime: string
  updateTime: string
}

export interface ChannelQueryParams extends PaginationParams {
  name?: string
  deviceId?: string
  status?: string
}

export type ChannelListResult = PaginationResult<Channel>

// 档案相关
export interface ArchiveItem {
  id: string
  personName: string
  personId: string
  alarmType: string
  alarmCount: number
  firstAlarmTime: string
  lastAlarmTime: string
  siteId: string
  siteName: string
}

export interface ArchiveDetail extends ArchiveItem {
  alarmList: AlarmItem[]
  timeline: ArchiveTimelineItem[]
}

export interface ArchiveTimelineItem {
  time: string
  type: 'alarm' | 'confirm'
  content: string
  imageUrl?: string
}

export interface ArchiveQueryParams extends PaginationParams {
  personName?: string
  alarmType?: string
  startTime?: string
  endTime?: string
}

export type ArchiveListResult = PaginationResult<ArchiveItem>

// 用户管理
export interface User {
  id: string
  username: string
  nickname: string
  email: string
  phone: string
  avatar?: string
  status: 'enabled' | 'disabled'
  siteId: string
  siteName: string
  roleIds: string[]
  roleNames: string[]
  createTime: string
  updateTime: string
}

export interface UserQueryParams extends PaginationParams {
  username?: string
  nickname?: string
  status?: string
  siteId?: string
}

export type UserListResult = PaginationResult<User>

// 角色管理
export interface Role {
  id: string
  name: string
  code: string
  description?: string
  permissions: string[]
  userCount: number
  createTime: string
  updateTime: string
}

export interface RoleQueryParams extends PaginationParams {
  name?: string
  code?: string
}

export type RoleListResult = PaginationResult<Role>

// 系统配置
export interface SystemConfig {
  siteName: string
  logo: string
  alarmSoundEnabled: boolean
  alarmAutoPopup: boolean
  dataRetentionDays: number
  videoRetentionDays: number
}

// 日志管理
export interface LogItem {
  id: string
  username: string
  operation: string
  method: string
  params: string
  ip: string
  duration: number
  status: 'success' | 'error'
  errorMsg?: string
  createTime: string
}

export interface LogQueryParams extends PaginationParams {
  username?: string
  operation?: string
  status?: string
  startTime?: string
  endTime?: string
}

export type LogListResult = PaginationResult<LogItem>

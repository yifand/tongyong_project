export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  userId: number
  username: string
  realName?: string
  roleCode: string
  siteId?: number
  permissions: string[]
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  phone?: string
  email?: string
  roleId: number
  siteId?: number
  status: number
  lastLoginAt?: string
}

export interface SysUser {
  id: number
  username: string
  password?: string
  realName: string
  phone?: string
  email?: string
  roleId: number
  siteId?: number
  status: number
  lastLoginAt?: string
  createdAt?: string
}

export interface SysRole {
  id: number
  roleCode: string
  roleName: string
  permissions: string[]
  dataScope: 'ALL' | 'SITE_SPECIFIC'
}

export interface Site {
  id: number
  siteCode: string
  siteName: string
}

export interface EdgeBox {
  id: number
  boxId: string
  boxName: string
  siteId: number
  ipAddress?: string
  status: number
  lastHeartbeat?: string
  version?: string
  cpuUsage?: number
  memUsage?: number
  diskUsage?: number
}

export interface Channel {
  id: number
  channelId: string
  channelName: string
  boxId: number
  channelType: string
  status: number
  algorithmType: string
  rtspUrl?: string
  username?: string
  password?: string
}

export interface Alarm {
  id: number
  alarmType: 'SMOKE' | 'PDI_UNQUALIFIED'
  siteId: number
  channelId: number
  workSessionId?: number
  alarmTime: string
  processStatus: 'UNPROCESSED' | 'PROCESSED' | 'FALSE_ALARM'
  processedBy?: number
  processedAt?: string
  targetImage?: string
  sceneImage?: string
  watermarkLogo?: string
  description?: string
  read?: boolean
}

export interface WorkSession {
  id: number
  siteId: number
  channelId: number
  vehicleInfo?: string
  startTime: string
  endTime?: string
  actualDuration?: number
  standardDuration: number
  deviationPct?: number
  result: 'QUALIFIED' | 'CRITICAL' | 'UNQUALIFIED'
  snapshotHead?: string
  snapshotTail?: string
  snapshotMid?: string
  status: number
}

export interface RuleConfig {
  id: number
  ruleName: string
  channelType: string
  requireVehicle: boolean
  enterPattern: any
  exitPattern: any
  standardDuration: number
  criticalThresholdPct: number
  personAbsentTimeout: number
  isEnabled: boolean
}

export interface SystemConfig {
  id: number
  configKey: string
  configValue: string
  description?: string
}

export interface OperationLog {
  id: number
  userId: number
  username: string
  ipAddress: string
  operationType: string
  operationContent: string
  result: number
  createdAt: string
}

export interface AlarmQueryParams {
  page?: number
  size?: number
  siteId?: number
  alarmType?: string
  processStatus?: string
  startTime?: string
  endTime?: string
  channelId?: number
}

export interface ReportQueryParams {
  page?: number
  size?: number
  siteId?: number
  channelId?: number
  startTime?: string
  endTime?: string
  result?: string
}

export interface BoxQueryParams {
  page?: number
  size?: number
  siteId?: number
  status?: number
}

export interface ChannelQueryParams {
  page?: number
  size?: number
  boxId?: number
  status?: number
}

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

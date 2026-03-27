import { get, post, del } from '../request'
import type {
  ApiResponse,
  AlarmItem,
  AlarmQueryParams,
  AlarmListResult,
  ConfirmAlarmParams
} from '../types'

// 获取实时预警列表
export function getRealtimeAlarms(): Promise<ApiResponse<AlarmItem[]>> {
  return get('/alarm/realtime')
}

// 获取历史预警列表
export function getAlarmHistory(
  params: AlarmQueryParams
): Promise<ApiResponse<AlarmListResult>> {
  return get('/alarm/history', params)
}

// 确认预警
export function confirmAlarm(data: ConfirmAlarmParams): Promise<ApiResponse<void>> {
  return post('/alarm/confirm', data)
}

// 批量确认预警
export function batchConfirmAlarm(ids: string[]): Promise<ApiResponse<void>> {
  return post('/alarm/batchConfirm', { ids })
}

// 删除预警记录
export function deleteAlarm(id: string): Promise<ApiResponse<void>> {
  return del(`/alarm/${id}`)
}

import { get, put, post } from './request'
import type { Alarm } from '@/types'

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

export function getAlarms(params: AlarmQueryParams): Promise<{ records: Alarm[], total: number }> {
  return get('/api/v1/alarms', { params })
}

export function getAlarmDetail(id: number): Promise<Alarm> {
  return get(`/api/v1/alarms/${id}`)
}

export function processAlarm(id: number, status: string): Promise<void> {
  return put(`/api/v1/alarms/${id}/process`, { processStatus: status })
}

export function exportAlarms(params: AlarmQueryParams, format: 'csv' | 'xlsx' = 'csv'): Promise<Blob> {
  return post(`/api/v1/alarms/export?format=${format}`, params, { responseType: 'blob' })
}

export function getAlarmImages(id: number): Promise<{ targetImageUrl: string, sceneImageUrl: string }> {
  return get(`/api/v1/alarms/${id}/images`)
}

import { get, post } from './request'
import type { WorkSession } from '@/types'

export interface ReportQueryParams {
  page?: number
  size?: number
  siteId?: number
  channelId?: number
  startTime?: string
  endTime?: string
  result?: string
  format?: string
  includeImages?: boolean
}

export function getPdiReports(params: ReportQueryParams): Promise<{ records: WorkSession[], total: number }> {
  return get('/api/v1/reports/pdi', { params })
}

export function getPdiReportDetail(id: number): Promise<WorkSession> {
  return get(`/api/v1/reports/pdi/${id}`)
}

export function exportPdiReports(params: ReportQueryParams): Promise<Blob> {
  return post('/api/v1/reports/pdi/export', params, { responseType: 'blob' })
}

import { get, post } from '../request'
import type {
  ApiResponse,
  ArchiveItem,
  ArchiveQueryParams,
  ArchiveListResult,
  ArchiveDetail
} from '../types'

// 获取档案列表
export function getArchiveList(
  params: ArchiveQueryParams
): Promise<ApiResponse<ArchiveListResult>> {
  return get('/archive/list', params)
}

// 获取档案详情
export function getArchiveDetail(id: string): Promise<ApiResponse<ArchiveDetail>> {
  return get(`/archive/${id}`)
}

// 导出档案
export function exportArchive(params: ArchiveQueryParams): Promise<Blob> {
  return post('/archive/export', params, { responseType: 'blob' })
}

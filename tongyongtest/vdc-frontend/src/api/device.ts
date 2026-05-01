import { get, post, del, put } from './request'
import type { EdgeBox, Channel } from '@/types'

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

export function getBoxes(params?: BoxQueryParams): Promise<{ records: EdgeBox[], total: number }> {
  return get('/api/v1/devices/boxes', { params })
}

export function addBox(data: Partial<EdgeBox>): Promise<EdgeBox> {
  return post('/api/v1/devices/boxes', data)
}

export function deleteBox(id: number): Promise<void> {
  return del(`/api/v1/devices/boxes/${id}`)
}

export function rebootBox(id: number): Promise<void> {
  return post(`/api/v1/devices/boxes/${id}/reboot`)
}

export function getChannels(params?: ChannelQueryParams): Promise<{ records: Channel[], total: number }> {
  return get('/api/v1/devices/channels', { params })
}

export function updateChannel(id: number, data: Partial<Channel>): Promise<Channel> {
  return put(`/api/v1/devices/channels/${id}`, data)
}

export function getChannelPreview(id: number): Promise<{ streamUrl: string }> {
  return get(`/api/v1/devices/channels/${id}/preview`)
}

export function addChannel(data: Partial<Channel>): Promise<Channel> {
  return post('/api/v1/devices/channels', data)
}

export function getDeviceMonitor(): Promise<any> {
  return get('/api/v1/devices/monitor')
}

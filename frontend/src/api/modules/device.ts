import { get, post, put, del } from '../request'
import type {
  ApiResponse,
  Device,
  DeviceQueryParams,
  DeviceListResult,
  Channel,
  ChannelQueryParams,
  ChannelListResult
} from '../types'

// 获取设备列表
export function getDeviceList(
  params?: DeviceQueryParams
): Promise<ApiResponse<DeviceListResult>> {
  return get('/device/box/list', params)
}

// 创建设备
export function createDevice(data: Partial<Device>): Promise<ApiResponse<Device>> {
  return post('/device/box', data)
}

// 更新设备
export function updateDevice(id: string, data: Partial<Device>): Promise<ApiResponse<Device>> {
  return put(`/device/box/${id}`, data)
}

// 删除设备
export function deleteDevice(id: string): Promise<ApiResponse<void>> {
  return del(`/device/box/${id}`)
}

// 获取设备状态
export function getDeviceStatus(id: string): Promise<ApiResponse<{ status: string }>> {
  return get(`/device/box/${id}/status`)
}

// 获取通道列表
export function getChannelList(
  params?: ChannelQueryParams
): Promise<ApiResponse<ChannelListResult>> {
  return get('/device/channel/list', params)
}

// 创建通道
export function createChannel(data: Partial<Channel>): Promise<ApiResponse<Channel>> {
  return post('/device/channel', data)
}

// 更新通道
export function updateChannel(id: string, data: Partial<Channel>): Promise<ApiResponse<Channel>> {
  return put(`/device/channel/${id}`, data)
}

// 删除通道
export function deleteChannel(id: string): Promise<ApiResponse<void>> {
  return del(`/device/channel/${id}`)
}

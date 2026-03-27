import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Device, DeviceStatus } from '@/api/types'

export const useDeviceStore = defineStore('device', () => {
  // State
  const devices = ref<Device[]>([])
  const onlineCount = ref(0)
  const offlineCount = ref(0)
  const loading = ref(false)

  // Getters
  const totalCount = computed(() => devices.value.length)
  const onlineRate = computed(() => {
    const total = devices.value.length
    return total > 0 ? Math.round((onlineCount.value / total) * 100) : 0
  })

  const deviceOptions = computed(() =>
    devices.value.map(d => ({ label: d.name, value: d.id }))
  )

  const onlineDevices = computed(() =>
    devices.value.filter(d => d.status === 'online')
  )

  const offlineDevices = computed(() =>
    devices.value.filter(d => d.status === 'offline')
  )

  // Actions
  function setDevices(data: Device[]) {
    devices.value = data
    onlineCount.value = data.filter(d => d.status === 'online').length
    offlineCount.value = data.filter(d => d.status === 'offline').length
  }

  function updateDeviceStatus(deviceId: string, status: DeviceStatus) {
    const device = devices.value.find(d => d.id === deviceId)
    if (device) {
      const oldStatus = device.status
      device.status = status

      // 更新在线/离线计数
      if (oldStatus !== status) {
        if (status === 'online') {
          onlineCount.value++
          offlineCount.value--
        } else {
          onlineCount.value--
          offlineCount.value++
        }
      }
    }
  }

  function addDevice(device: Device) {
    devices.value.push(device)
    if (device.status === 'online') {
      onlineCount.value++
    } else {
      offlineCount.value++
    }
  }

  function removeDevice(deviceId: string) {
    const index = devices.value.findIndex(d => d.id === deviceId)
    if (index > -1) {
      const device = devices.value[index]
      if (device.status === 'online') {
        onlineCount.value--
      } else {
        offlineCount.value--
      }
      devices.value.splice(index, 1)
    }
  }

  function setLoading(value: boolean) {
    loading.value = value
  }

  return {
    devices,
    onlineCount,
    offlineCount,
    loading,
    totalCount,
    onlineRate,
    deviceOptions,
    onlineDevices,
    offlineDevices,
    setDevices,
    updateDeviceStatus,
    addDevice,
    removeDevice,
    setLoading
  }
})

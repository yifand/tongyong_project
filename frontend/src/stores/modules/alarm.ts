import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AlarmItem } from '@/api/types'

export const useAlarmStore = defineStore('alarm', () => {
  // State
  const realtimeAlarms = ref<AlarmItem[]>([])
  const unreadCount = ref(0)
  const currentAlarm = ref<AlarmItem | null>(null)
  const isSoundEnabled = ref(true)
  const isAlarmDialogVisible = ref(false)

  // Getters
  const urgentAlarms = computed(() =>
    realtimeAlarms.value.filter(a => a.level === 1 && !a.isConfirmed)
  )
  const hasUnread = computed(() => unreadCount.value > 0)

  // Actions
  function addRealtimeAlarm(alarm: AlarmItem) {
    realtimeAlarms.value.unshift(alarm)
    unreadCount.value++

    // 限制列表长度
    if (realtimeAlarms.value.length > 100) {
      realtimeAlarms.value.pop()
    }

    // 播放音效
    if (isSoundEnabled.value && alarm.level === 1) {
      playAlarmSound()
    }
  }

  function showAlarmDialog(alarm: AlarmItem) {
    currentAlarm.value = alarm
    isAlarmDialogVisible.value = true
  }

  function hideAlarmDialog() {
    isAlarmDialogVisible.value = false
    currentAlarm.value = null
  }

  async function confirmAlarmAction(id: string) {
    const alarm = realtimeAlarms.value.find(a => a.id === id)
    if (alarm) {
      alarm.isConfirmed = true
      if (unreadCount.value > 0) {
        unreadCount.value--
      }
    }
  }

  function playAlarmSound() {
    const audio = new Audio('/alarm-sound.mp3')
    audio.play().catch(() => {
      // 浏览器可能阻止自动播放
    })
  }

  function toggleSound() {
    isSoundEnabled.value = !isSoundEnabled.value
  }

  function clearRealtimeAlarms() {
    realtimeAlarms.value = []
    unreadCount.value = 0
  }

  return {
    realtimeAlarms,
    unreadCount,
    currentAlarm,
    isSoundEnabled,
    isAlarmDialogVisible,
    urgentAlarms,
    hasUnread,
    addRealtimeAlarm,
    showAlarmDialog,
    hideAlarmDialog,
    confirmAlarmAction,
    playAlarmSound,
    toggleSound,
    clearRealtimeAlarms
  }
})

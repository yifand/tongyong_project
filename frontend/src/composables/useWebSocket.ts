<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface WebSocketOptions {
  url: string
  reconnectInterval?: number
  heartbeatInterval?: number
  maxReconnectAttempts?: number
  onMessage?: (data: any) => void
  onConnect?: () => void
  onDisconnect?: () => void
}

export function useWebSocket(options: WebSocketOptions) {
  const {
    url,
    reconnectInterval = 3000,
    heartbeatInterval = 30000,
    maxReconnectAttempts = 5,
    onMessage,
    onConnect,
    onDisconnect
  } = options

  const ws = ref<WebSocket | null>(null)
  const isConnected = ref(false)
  const reconnectAttempts = ref(0)
  const heartbeatTimer = ref<number | null>(null)
  const reconnectTimer = ref<number | null>(null)

  function connect() {
    if (ws.value?.readyState === WebSocket.OPEN) return

    const token = localStorage.getItem('token')
    const wsUrl = `${url}?token=${token}`

    ws.value = new WebSocket(wsUrl)

    ws.value.onopen = () => {
      console.log('WebSocket连接成功')
      isConnected.value = true
      reconnectAttempts.value = 0
      startHeartbeat()
      onConnect?.()
    }

    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        onMessage?.(data)
      } catch (error) {
        console.error('解析消息失败:', error)
      }
    }

    ws.value.onclose = () => {
      console.log('WebSocket连接关闭')
      isConnected.value = false
      stopHeartbeat()
      onDisconnect?.()
      attemptReconnect()
    }

    ws.value.onerror = (error) => {
      console.error('WebSocket错误:', error)
      isConnected.value = false
    }
  }

  function send(message: any) {
    if (ws.value?.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify(message))
    }
  }

  function startHeartbeat() {
    heartbeatTimer.value = window.setInterval(() => {
      send({ type: 'ping' })
    }, heartbeatInterval)
  }

  function stopHeartbeat() {
    if (heartbeatTimer.value) {
      clearInterval(heartbeatTimer.value)
      heartbeatTimer.value = null
    }
  }

  function attemptReconnect() {
    if (reconnectAttempts.value >= maxReconnectAttempts) {
      console.error('WebSocket重连次数已达上限')
      return
    }

    reconnectAttempts.value++
    console.log(`WebSocket第${reconnectAttempts.value}次重连...`)

    reconnectTimer.value = window.setTimeout(() => {
      connect()
    }, reconnectInterval)
  }

  function disconnect() {
    stopHeartbeat()
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value)
      reconnectTimer.value = null
    }
    ws.value?.close()
    ws.value = null
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    isConnected,
    connect,
    disconnect,
    send
  }
}

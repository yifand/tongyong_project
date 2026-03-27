// 报警级别
export const ALARM_LEVELS = [
  { value: 1, label: '紧急', type: 'danger', color: '#ef4444' },
  { value: 2, label: '重要', type: 'warning', color: '#f97316' },
  { value: 3, label: '一般', type: 'info', color: '#3b82f6' }
]

// 设备状态
export const DEVICE_STATUS = [
  { value: 'online', label: '在线', type: 'success' },
  { value: 'offline', label: '离线', type: 'info' },
  { value: 'error', label: '故障', type: 'danger' }
]

// 用户状态
export const USER_STATUS = [
  { value: 'enabled', label: '启用', type: 'success' },
  { value: 'disabled', label: '禁用', type: 'danger' }
]

// AI模型类型
export const AI_MODELS = [
  { value: 'helmet', label: '安全帽检测' },
  { value: 'vest', label: '反光衣检测' },
  { value: 'smoke', label: '吸烟检测' },
  { value: 'phone', label: '打电话检测' },
  { value: 'boundary', label: '越界检测' },
  { value: 'fire', label: '烟火检测' }
]

// 默认分页配置
export const DEFAULT_PAGE_SIZE = 10
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100]

/**
 * 数据格式化工具函数
 */

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化数字（千分位）
 */
export function formatNumber(num: number): string {
  return num.toLocaleString('zh-CN')
}

/**
 * 格式化百分比
 */
export function formatPercent(num: number, decimals = 2): string {
  return (num * 100).toFixed(decimals) + '%'
}

/**
 * 格式化时长（秒转时分秒）
 */
export function formatDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  const parts = []
  if (h > 0) parts.push(h.toString().padStart(2, '0'))
  parts.push(m.toString().padStart(2, '0'))
  parts.push(s.toString().padStart(2, '0'))
  return parts.join(':')
}

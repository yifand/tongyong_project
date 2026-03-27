import dayjs from 'dayjs'

/**
 * 格式化日期时间
 */
export function formatDateTime(date: string | number | Date, format = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化日期
 */
export function formatDate(date: string | number | Date, format = 'YYYY-MM-DD'): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化时间
 */
export function formatTime(date: string | number | Date, format = 'HH:mm:ss'): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 获取相对时间
 */
export function fromNow(date: string | number | Date): string {
  if (!date) return ''
  return dayjs(date).fromNow()
}

/**
 * 获取时间戳
 */
export function getTimestamp(date?: string | number | Date): number {
  return dayjs(date).valueOf()
}

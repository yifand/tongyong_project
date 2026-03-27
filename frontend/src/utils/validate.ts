/**
 * 表单验证工具函数
 */

/**
 * 验证手机号
 */
export function isMobile(phone: string): boolean {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 验证邮箱
 */
export function isEmail(email: string): boolean {
  const reg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return reg.test(email)
}

/**
 * 验证IP地址
 */
export function isIP(ip: string): boolean {
  const reg =
    /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/
  return reg.test(ip)
}

/**
 * 验证端口号
 */
export function isPort(port: number): boolean {
  return port > 0 && port <= 65535
}

/**
 * 验证是否为空
 */
export function isEmpty(value: unknown): boolean {
  if (value === null || value === undefined) return true
  if (typeof value === 'string') return value.trim() === ''
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}

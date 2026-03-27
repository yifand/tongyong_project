/**
 * 本地存储封装
 */

export const storage = {
  get<T>(key: string): T | null {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : null
    } catch {
      return null
    }
  },

  set(key: string, value: unknown): void {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error('Storage set error:', error)
    }
  },

  remove(key: string): void {
    localStorage.removeItem(key)
  },

  clear(): void {
    localStorage.clear()
  }
}

export const sessionStorage = {
  get<T>(key: string): T | null {
    try {
      const item = window.sessionStorage.getItem(key)
      return item ? JSON.parse(item) : null
    } catch {
      return null
    }
  },

  set(key: string, value: unknown): void {
    try {
      window.sessionStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error('SessionStorage set error:', error)
    }
  },

  remove(key: string): void {
    window.sessionStorage.removeItem(key)
  },

  clear(): void {
    window.sessionStorage.clear()
  }
}

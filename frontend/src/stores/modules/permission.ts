import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { asyncRoutes } from '@/router/routes'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from './user'

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref<RouteRecordRaw[]>([])
  const addRoutes = ref<RouteRecordRaw[]>([])

  // 判断是否有权限
  function hasPermission(route: RouteRecordRaw, permissions: string[]): boolean {
    if (route.meta?.permissions) {
      return (route.meta.permissions as string[]).some(p => permissions.includes(p))
    }
    return true
  }

  // 过滤有权限的路由
  function filterAsyncRoutes(
    routes: RouteRecordRaw[],
    permissions: string[]
  ): RouteRecordRaw[] {
    const res: RouteRecordRaw[] = []

    routes.forEach(route => {
      const tmp = { ...route }
      if (hasPermission(tmp, permissions)) {
        if (tmp.children) {
          tmp.children = filterAsyncRoutes(tmp.children, permissions)
        }
        res.push(tmp)
      }
    })

    return res
  }

  // 生成动态路由
  async function generateRoutes(): Promise<RouteRecordRaw[]> {
    const userStore = useUserStore()
    const permissions = userStore.permissions

    const accessedRoutes = filterAsyncRoutes(asyncRoutes, permissions)
    addRoutes.value = accessedRoutes
    routes.value = accessedRoutes

    return accessedRoutes
  }

  // 检查是否有指定权限
  function checkPermission(permission: string | string[]): boolean {
    const userStore = useUserStore()
    const perms = Array.isArray(permission) ? permission : [permission]
    return perms.some(p => userStore.permissions.includes(p))
  }

  return {
    routes,
    addRoutes,
    generateRoutes,
    checkPermission
  }
})

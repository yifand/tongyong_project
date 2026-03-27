import { useUserStore } from '@/stores/modules/user'

export function useAuth() {
  const userStore = useUserStore()

  // 检查是否有权限
  function hasPermission(permissions: string | string[]): boolean {
    const perms = Array.isArray(permissions) ? permissions : [permissions]
    return perms.some(p => userStore.permissions.includes(p))
  }

  // 检查是否有角色
  function hasRole(roles: string | string[]): boolean {
    const roleList = Array.isArray(roles) ? roles : [roles]
    return roleList.some(r => userStore.roles.includes(r))
  }

  // 检查是否为超级管理员
  function isSuperAdmin(): boolean {
    return userStore.roles.includes('super_admin')
  }

  // 检查站点权限
  function hasSiteAccess(siteId: string): boolean {
    if (isSuperAdmin()) return true
    return userStore.siteId === siteId
  }

  return {
    hasPermission,
    hasRole,
    isSuperAdmin,
    hasSiteAccess
  }
}

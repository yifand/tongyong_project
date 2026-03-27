import type { Router } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/modules/permission'

const whiteList = ['/login', '/404']

export function setupRouterGuards(router: Router) {
  // 前置守卫
  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - PDI监测平台` : 'PDI监测平台'

    const hasToken = userStore.isLoggedIn

    if (hasToken) {
      if (to.path === '/login') {
        next('/')
      } else {
        // 检查是否已获取用户信息
        if (!userStore.userInfo) {
          try {
            await userStore.fetchUserInfo()
            // 生成动态路由
            const accessRoutes = await permissionStore.generateRoutes()
            accessRoutes.forEach(route => router.addRoute(route))
            next({ ...to, replace: true })
          } catch (error) {
            await userStore.logout()
            ElMessage.error('获取用户信息失败，请重新登录')
            next(`/login?redirect=${to.path}`)
          }
        } else {
          next()
        }
      }
    } else {
      if (whiteList.includes(to.path)) {
        next()
      } else {
        next(`/login?redirect=${to.path}`)
      }
    }
  })

  // 后置守卫
  router.afterEach(() => {
    // 关闭loading等
  })

  // 错误处理
  router.onError(error => {
    console.error('路由错误:', error)
  })
}

// 全局类型定义

// Vue 模块声明
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 环境变量类型
declare interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_API_BASE_URL: string
  readonly VITE_WS_URL: string
  readonly VITE_UPLOAD_URL: string
}

declare interface ImportMeta {
  readonly env: ImportMetaEnv
}

// 通用类型
export {}

# PDI智能监测平台前端

基于 Vue 3 + TypeScript + Element Plus + Vite 开发的智能监测平台前端项目。

## 技术栈

- **Vue 3.4+** - 渐进式 JavaScript 框架
- **TypeScript 5.3+** - 类型安全的 JavaScript 超集
- **Element Plus 2.5+** - Vue 3 组件库
- **Vite 5.0+** - 下一代前端构建工具
- **Pinia 2.1+** - Vue 3 官方状态管理方案
- **Vue Router 4.2+** - 官方路由管理器
- **Axios 1.6+** - HTTP 客户端
- **ECharts 5.4+** - 数据可视化图表库

## 开发环境要求

- Node.js >= 18.0.0
- npm >= 9.0.0 或 yarn >= 1.22.0

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产环境

```bash
npm run build
```

### 代码检查

```bash
npm run lint
```

### 代码格式化

```bash
npm run format
```

## 项目结构

```
├── public/                     # 静态资源
├── src/
│   ├── api/                    # API 接口层
│   ├── assets/                 # 静态资源
│   ├── components/             # 公共组件（原子设计）
│   ├── composables/            # 组合式函数
│   ├── directives/             # 自定义指令
│   ├── layouts/                # 布局组件
│   ├── router/                 # 路由配置
│   ├── stores/                 # Pinia 状态管理
│   ├── styles/                 # 全局样式
│   ├── types/                  # TypeScript 类型
│   ├── utils/                  # 工具函数
│   ├── views/                  # 页面视图
│   ├── App.vue                 # 根组件
│   └── main.ts                 # 应用入口
└── vite.config.ts              # Vite 配置
```

## 开发规范

- 使用 Composition API + `<script setup lang="ts">` 编写组件
- 组件命名使用 PascalCase，多单词命名避免与 HTML 标签冲突
- Props 命名使用 camelCase
- 事件命名使用 kebab-case
- CSS 类名使用 BEM 命名规范

## 环境变量

- `.env.development` - 开发环境配置
- `.env.production` - 生产环境配置

## 功能模块

- **工作台** - 数据概览、统计图表
- **预警中心** - 实时预警、历史预警
- **行为档案** - 人员行为记录管理
- **设备管理** - 边缘盒子、通道管理
- **系统管理** - 用户管理、角色管理、系统配置、日志审计

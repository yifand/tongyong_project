# 金桥基地VDC智能算法项目业务平台——产品架构设计文档

**版本：** v1.1（开发就绪版）  
**日期：** 2026-04-14  
**编制依据：** 《金桥基地VDC智能算法项目业务平台需求规格说明书》、《PDI工位作业判定逻辑确认书》

> **开发就绪说明**：本版本在 v1.0 产品架构基础上，经技术细节澄清（标准工时、部署方式、服务边界、资源限制、盒子离线策略等），补充了 Dockerfile、docker-compose.yml、Nginx 配置及具体版本号，可直接用于开发落地。

---

## 1. 概述

### 1.1 项目背景
金桥基地VDC智能算法项目旨在通过视频智能分析技术，实现PDI（出厂前检测）作业规范性监测及违规抽烟行为实时报警。项目涉及两个独立物理站点（**金桥库**、**凯迪库**），每个站点部署边缘计算盒子接入现场摄像头。业务平台采用**中心统一部署**模式，部署于厂区内部服务器，实现站点级数据隔离与统一运维管理。

### 1.2 设计目标
- **实时性**：从边缘事件发生到平台页面展示报警，延迟 ≤ 3秒。
- **合规判定**：基于边缘盒子推送的"基础状态四元组"（车门开关、人员存在、进出动作、车辆存在）时间序列，在业务平台侧实现可配置的PDI作业时长判定与违规报警。
- **多站点隔离**：金桥、凯迪两库数据严格隔离，超级管理员可跨站点查看。
- **成本可控**：采用开源技术栈，降低许可费用；部署于单台或少量物理服务器，适合10并发用户规模。

### 1.3 核心设计原则
1. **边缘轻量、中心重业务**：视频流不经过平台中转，原始视频存于站点NVR；平台只接收状态流与抓拍图，专注业务规则计算与展示。
2. **状态驱动**：以`door_open`、`person_present`、`person_entering_exiting`、`vehicle_present`四个布尔状态的时间序列为核心输入，驱动规则引擎。
3. **规则可配置**：进入判定、离开判定、标准工时、偏差阈值均支持后台配置，适应后续工艺调整。
4. **分层解耦**：接入层、规则引擎层、业务服务层、展示层分离，便于独立迭代与扩展。

---

## 2. 总体架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              用 户 终 端                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  PC管理后台  │  │  监控大屏    │  │  手机端(H5)  │  │  边缘盒子管理界面    │  │
│  │  (Vue3)     │  │  (Vue3)     │  │             │  │  (盒子自带Web)      │  │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └─────────────────────┘  │
└─────────┼────────────────┼────────────────┼───────────────────────────────────┘
          │                │                │
          └────────────────┴────────────────┘
                           │ HTTPS / WebSocket
┌──────────────────────────▼──────────────────────────────────────────────────┐
│                           中 心 业 务 平 台                                  │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                        Nginx 反向代理 / 静态资源                          │  │
│  │                    (负载均衡、SSL终结、前端资源托管)                       │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                    Spring Boot 3 业务服务层                               │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │  │
│  │  │ 预警服务  │ │ 报表服务  │ │ 设备服务  │ │ 权限服务  │ │ 系统配置服务  │  │  │
│  │  │ Alert    │ │ Report   │ │ Device   │ │ Auth     │ │ Config       │  │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │  │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │  │
│  │  │                    业务规则引擎 (Rule Engine)                      │  │  │
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │  │  │
│  │  │  │ 状态流处理器 │  │ 模式匹配器   │  │ 事件生成器 (进入/离开/违规)│  │  │  │
│  │  │  │ (State      │  │ (Pattern    │  │ (Event Generator)       │  │  │  │
│  │  │  │  Processor) │  │  Matcher)   │  │                         │  │  │  │
│  │  │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │  │  │
│  │  └──────────────────────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                    数据持久层 / 中间件                                    │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │  │
│  │  │  PostgreSQL  │  │    Redis     │  │    MinIO     │  │ RabbitMQ  │  │  │
│  │  │  (主数据库 +  │  │  (缓存/会话/  │  │  (对象存储/   │  │ (消息队列/  │  │  │
│  │  │   时序数据)   │  │   实时推送)   │  │   图片存储)   │  │  可选)     │  │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────┘  │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
          ▲                                                ▲
          │                                                │
          │        LAN / VPN (稳定内网)                     │        LAN / VPN
          │                                                │
┌─────────┴─────────────────────────────────────┐  ┌──────┴────────────────────────────┐
│              金 桥 库 站 点                     │  │              凯 迪 库 站 点                     │
│  ┌───────────────────────────────────────┐    │  │  ┌───────────────────────────────────────┐    │
│  │         边缘计算盒子 (Edge Box)        │    │  │  │         边缘计算盒子 (Edge Box)        │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │    │  │  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │    │
│  │  │ 车门检测 │ │ 人员检测 │ │ 进出检测 │ │ 车辆检测 │ │    │  │  │  │ 车门检测 │ │ 人员检测 │ │ 进出检测 │ │ 车辆检测 │ │    │
│  │  │ Algorithm│ │ Algorithm│ │ Algorithm│ │ Algorithm│ │    │  │  │  │ Algorithm│ │ Algorithm│ │ Algorithm│ │ Algorithm│ │    │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ │    │  │  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ │    │
│  │       └───────────┴───────────┴───────────┘      │    │  │  │       └───────────┴───────────┴───────────┘      │    │
│  │                    │                   │    │  │  │                    │                   │    │
│  │         ┌──────────▼──────────┐        │    │  │  │         ┌──────────▼──────────┐        │    │
│  │         │   盒子接入网关       │        │    │  │  │         │   盒子接入网关       │        │    │
│  │         │  (HTTP推送/MQTT)    │        │    │  │  │         │  (HTTP推送/MQTT)    │        │    │
│  │         └──────────┬──────────┘        │    │  │  │         └──────────┬──────────┘        │    │
│  └────────────────────┼───────────────────┘    │  │  └────────────────────┼───────────────────┘    │
│                       │                         │  │                       │                         │
│  ┌────────────────────┼───────────────────┐    │  │  ┌────────────────────┼───────────────────┐    │
│  │        NVR (本地视频存储)              │    │  │  │        NVR (本地视频存储)              │    │
│  └───────────────────────────────────────┘    │  │  └───────────────────────────────────────┘    │
└─────────────────────────────────────────────────┘  └─────────────────────────────────────────────────┘
```

---

## 3. 模块划分与交互

### 3.1 模块总览

| 模块 | 职责 | 核心子模块 |
|------|------|-----------|
| **接入网关层** | 接收边缘盒子推送的状态流与报警事件 | 数据接收API、协议适配、数据校验、去重滤波 |
| **规则引擎层** | 基于状态时间序列进行模式匹配与业务判定 | 状态流处理器、模式匹配器、事件生成器 |
| **预警服务** | 实时报警推送、历史报警查询、声光提示 | 实时预警WebSocket推送、报警生命周期管理、图片水印 |
| **报表服务** | PDI作业报表生成、筛选、导出 | 作业时长计算、合格/临界/不合格判定、PDF/Excel导出 |
| **设备服务** | 边缘盒子与摄像头通道管理 | 盒子注册/心跳、远程控制、通道配置同步、实时预览取流 |
| **权限服务** | 用户、角色、权限、审计 | RBAC权限模型、站点数据隔离、登录审计、操作日志 |
| **系统配置服务** | 算法开关、阈值配置、通用参数 | 规则配置、工时阈值、数据留存策略、报警声音配置 |

### 3.2 核心模块交互时序

#### 场景A：PDI作业违规报警生成
```
边缘盒子 ──[每秒推送状态四元组]──▶ 接入网关
                                      │
                                      ▼
                              状态流处理器 (写入时序DB)
                                      │
                                      ▼
                              模式匹配器 (检测 15→16→11 进入序列)
                                      │
                                      ▼
                              事件生成器 (生成"人员进入"事件,
                                          开始计时)
                                      │
                                      ▼
                              [持续接收状态流...]
                                      │
                                      ▼
                              模式匹配器 (检测 11→16→15→13/9 离开序列)
                                      │
                                      ▼
                              事件生成器 (生成"人员离开"事件,
                                          计算实际时长)
                                      │
                                      ▼
                              报表服务 (存储作业记录)
                                      │
                              [若时长 < 阈值]
                                      ▼
                              预警服务 (生成PDI违规报警)
                                      │
                                      ▼
                              Redis Pub/Sub ──▶ WebSocket ──▶ 前端弹窗
```

#### 场景B：抽烟行为实时报警
```
边缘盒子 ──[event_detect_info 报警事件]──▶ 接入网关
                                               │
                                               ▼
                                       预警服务 (持久化报警记录)
                                               │
                                               ▼
                                       Redis Pub/Sub ──▶ WebSocket
                                               │
                                               ▼
                                       前端页面 (顶部消息栏闪烁 + 声音提示)
```

#### 场景C：设备异常告警
```
设备服务 ──[定时心跳检测]──▶ 边缘盒子
      [超时未收到心跳]
            │
            ▼
      设备服务 (生成"盒子离线"告警)
            │
            ▼
      预警服务 (推送设备告警到前端大屏)
```

---

## 4. 技术栈选型与 rationale

### 4.1 后端技术栈

| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| **Java** | 17 LTS | 主开发语言 | 企业级生态成熟，金桥/凯迪厂区IT团队熟悉，易于长期维护 |
| **Spring Boot** | 3.2.x | 主框架 | 开源、社区活跃、快速开发、内置WebSocket/Security/Scheduler支持 |
| **Spring Security** | 6.x | 认证授权 | 与Spring Boot原生集成，支持RBAC、JWT、方法级权限控制 |
| **MyBatis-Plus** | 3.5.x | ORM | 国内广泛使用，代码生成器可快速产出CRUD，降低开发成本 |
| **PostgreSQL** | 15+ | 主数据库 | 开源、功能强大、支持JSONB（灵活规则配置）；当前采用原生 PostgreSQL 表+定时任务处理时序数据，未来若数据量激增可扩展 TimescaleDB |
| **Redis** | 7.x | 缓存/会话/实时推送 | 开源、高性能，Pub/Sub机制天然适合实时报警推送 |
| **RabbitMQ** | 3.12+ | 消息队列（可选） | 如果需要更高可靠性的事件驱动，可接入RabbitMQ做异步解耦；初期可用Redis Streams替代 |
| **MinIO** | 最新稳定版 | 对象存储 | 开源S3兼容，适合存储报警抓拍图、报表导出文件；支持90天生命周期策略 |
| **Drools / 自定义** | - | 规则引擎 | 考虑到状态序列模式匹配的特殊性，采用**自定义Java状态机引擎**更轻量、可控；Drools作为后续扩展备选 |
| **Docker** | 24+ | 容器化 | 统一运行环境，便于部署、升级和回滚 |
| **Nginx** | 1.24+ | 反向代理 | 静态资源托管、SSL终结、WebSocket代理 |

### 4.2 前端技术栈

| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| **Vue 3** | 3.4+ | 前端框架 | 开源、国内生态成熟、性能优异、Composition API代码组织清晰 |
| **TypeScript** | 5.x | 类型系统 | 提升代码可维护性，减少运行时错误 |
| **Element Plus** | 2.5+ | UI组件库 | 开源、企业后台系统首选，组件丰富、文档完善 |
| **ECharts** | 5.x | 图表可视化 | 开源、国产、大屏展示与报表图表首选 |
| **Pinia** | 2.x | 状态管理 | Vue官方推荐，TypeScript友好 |
| **Axios** | 1.6+ | HTTP客户端 | 成熟稳定，支持拦截器、取消请求 |
| **SockJS + STOMP** | - | WebSocket客户端 | 兼容性好，支持自动降级，适合实时报警推送 |

### 4.3 规则引擎设计 rationale

PDI判定的核心是**时间序列上的状态模式匹配**。在四元组（`door_open`、`person_present`、`person_entering_exiting`、`vehicle_present`）下，典型进入/离开模式更新为`15→16→11`和`11→16→15→(13/9)`。这类需求具有以下特点：
- 输入是高频、有序的离散状态事件（每秒1次）
- 规则是有限状态序列模式，而非传统Drools擅长的条件-动作规则
- 需要处理状态抖动、不合理组合（如2/4/6/10/12/14）的滤波
- 新增`vehicle_present`后，作业开始/结束判定可增加"车辆在场"前置条件，避免无车时的误判
- 规则需要可配置（后续工艺可能调整）

**因此选择"自定义状态机引擎"**，核心设计：
1. 为每个通道（channel）维护一个**独立的状态上下文**（State Context）。
2. 上下文内保存最近N条状态记录（滑动窗口）。
3. 模式匹配器扫描窗口，匹配预定义的进入/离开模式；支持将`vehicle_present=false`的状态视为无效并自动跳过。
4. 匹配成功后触发事件，并清空/重置上下文，避免重复触发。
5. 规则配置以JSON形式存储（如`{"require_vehicle": true, "enter_pattern": [15,16,11], "exit_pattern": [11,16,15,[13,9]]}`），支持后台动态加载。

相比Drools，自定义引擎：
- **更轻量**：无额外依赖，启动快
- **更精准**：专为四元组状态流设计
- **更易维护**：Java原生代码，团队无需学习Drools DSL

---

## 5. 数据模型设计

### 5.1 核心实体关系图（ER）

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    Site     │1────N │  EdgeBox    │1────N │   Channel   │
│  (站点)      │       │  (边缘盒子)  │       │  (通道/摄像头)│
└─────────────┘       └─────────────┘       └─────────────┘
       ▲                                            │
       │                                            │
       │N                                    N      │
┌─────────────┐                              ┌─────────────┐
│     User    │                              │   Alarm     │
│   (用户)     │                              │   (报警)     │
└─────────────┘                              └─────────────┘
       │                                            │
       │                                            │
       │N                                    N      │
┌─────────────┐                              ┌─────────────┐
│  Operation  │                              │   Report    │
│   Log (操作日志)│                            │   (PDI报表)  │
└─────────────┘                              └─────────────┘
                                                    │
                                              N     │1
                                              ┌─────────────┐
                                              │  WorkSession│
                                              │ (工位作业会话)│
                                              └─────────────┘
```

### 5.2 核心表结构

#### `site` —— 站点表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 站点ID |
| site_code | VARCHAR(32) | 站点编码（如 `JINQIAO`、`KAIDI`） |
| site_name | VARCHAR(64) | 站点名称 |
| created_at | TIMESTAMP | 创建时间 |

#### `edge_box` —— 边缘计算盒子表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 盒子ID |
| box_id | VARCHAR(64) UNIQUE | 盒子设备编号 |
| box_name | VARCHAR(64) | 盒子名称 |
| site_id | BIGINT FK | 所属站点 |
| ip_address | VARCHAR(32) | IP地址 |
| status | TINYINT | 在线状态：0离线 1在线 |
| last_heartbeat | TIMESTAMP | 最后心跳时间 |
| version | VARCHAR(32) | 固件版本号 |
| cpu_usage | DECIMAL(5,2) | CPU使用率（%） |
| mem_usage | DECIMAL(5,2) | 内存使用率（%） |
| disk_usage | DECIMAL(5,2) | 磁盘使用率（%） |

#### `channel` —— 摄像头通道表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 通道ID |
| channel_id | VARCHAR(64) | 盒子内通道编号 |
| channel_name | VARCHAR(64) | 通道名称 |
| box_id | BIGINT FK | 所属盒子 |
| channel_type | VARCHAR(32) | 类型：`VIDEO_STREAM`、`SNAPSHOT` |
| status | TINYINT | 状态：0离线 1在线 |
| algorithm_type | VARCHAR(32) | 关联算法：`SMOKE`、`PDI_FRONT`、`PDI_REAR`、`PDI_SLIDING`、`BOTH` |
| rtsp_url | VARCHAR(256) | RTSP取流地址 |
| username | VARCHAR(64) | 通道认证用户名 |
| password | VARCHAR(64) | 通道认证密码（加密存储） |

#### `sys_user` —— 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 用户ID |
| username | VARCHAR(64) UNIQUE | 用户名 |
| password_hash | VARCHAR(128) | 密码（BCrypt加密） |
| real_name | VARCHAR(64) | 真实姓名 |
| phone | VARCHAR(16) | 手机号 |
| email | VARCHAR(64) | 邮箱 |
| role_id | BIGINT FK | 角色ID |
| site_id | BIGINT FK NULL | 所属站点（NULL表示全部） |
| status | TINYINT | 状态：0禁用 1启用 |
| last_login_at | TIMESTAMP | 最后登录时间 |
| created_at | TIMESTAMP | 创建时间 |

#### `sys_role` —— 角色表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 角色ID |
| role_code | VARCHAR(32) UNIQUE | 角色编码：`SUPER_ADMIN`、`SITE_ADMIN`、`READONLY` |
| role_name | VARCHAR(64) | 角色名称 |
| permissions | JSONB | 功能权限列表（JSON数组） |
| data_scope | VARCHAR(16) | 数据权限范围：`ALL`、`SITE_SPECIFIC` |

#### `state_stream` —— 基础状态流时序表（重点表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL PK | 自增ID |
| box_id | VARCHAR(64) | 盒子ID |
| channel_id | VARCHAR(64) | 通道ID |
| ts | TIMESTAMP | 状态发生时间（按时间分区） |
| door_open | BOOLEAN | 车门是否开启 |
| person_present | BOOLEAN | 区域内是否有人 |
| person_entering_exiting | BOOLEAN | 是否正在进出 |
| vehicle_present | BOOLEAN | 工位区域是否有车辆 |
| state_combination | TINYINT | 状态组合编号（1-16） |
| snapshot_target | VARCHAR(256) | 目标截图MinIO路径（可选） |
| snapshot_scene | VARCHAR(256) | 场景截图MinIO路径（可选） |

> **设计说明**：`state_stream` 数据量大（每秒每通道1条），采用原生 **PostgreSQL 15** 存储。为简化运维，不使用 TimescaleDB。推荐两种30天清理方案：
> 1. **表分区方案**：`state_stream` 按 `ts` 字段按月分区（`PARTITION BY RANGE (ts)`），通过定时任务（Spring Scheduler 或 pgAgent）删除超期分区。
> 2. **定时删除方案**：若数据量可控（约5000万条/月），可每日凌晨执行 `DELETE FROM state_stream WHERE ts < NOW() - INTERVAL '30 days'`，并配合 `VACUUM` 回收空间。

#### `work_session` —— 工位作业会话表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 会话ID |
| site_id | BIGINT FK | 站点ID |
| channel_id | BIGINT FK | 通道ID |
| vehicle_info | VARCHAR(64) NULL | 车辆信息（车牌/VIN，如有） |
| start_time | TIMESTAMP | 作业开始时间 |
| end_time | TIMESTAMP NULL | 作业结束时间 |
| actual_duration | INT NULL | 实际时长（秒） |
| standard_duration | INT | 标准工时（秒） |
| deviation_pct | DECIMAL(5,2) NULL | 偏差百分比 |
| result | VARCHAR(16) | 判定结果：`QUALIFIED`、`CRITICAL`、`UNQUALIFIED` |
| snapshot_head | VARCHAR(256) NULL | 头帧截图路径 |
| snapshot_tail | VARCHAR(256) NULL | 尾帧截图路径 |
| snapshot_mid | VARCHAR(256) NULL | 关键中间帧路径 |
| status | TINYINT | 状态：0进行中 1已完成 |

#### `alarm` —— 报警记录表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 报警ID |
| alarm_type | VARCHAR(32) | 报警类型：`SMOKE`、`PDI_UNQUALIFIED` |
| site_id | BIGINT FK | 站点ID |
| channel_id | BIGINT FK | 通道ID |
| work_session_id | BIGINT FK NULL | 关联作业会话（PDI报警时） |
| alarm_time | TIMESTAMP | 报警发生时间 |
| process_status | VARCHAR(16) | 处理状态：`UNPROCESSED`、`PROCESSED`、`FALSE_ALARM` |
| processed_by | BIGINT FK NULL | 处理人 |
| processed_at | TIMESTAMP NULL | 处理时间 |
| target_image | VARCHAR(256) NULL | 人员面部/目标截图路径 |
| scene_image | VARCHAR(256) NULL | 场景截图路径 |
| watermark_logo | VARCHAR(128) NULL | 水印Logo配置 |
| description | TEXT NULL | 报警描述 |

#### `operation_log` —— 操作日志表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 日志ID |
| user_id | BIGINT FK | 操作用户ID |
| username | VARCHAR(64) | 用户名（冗余，便于查询） |
| ip_address | VARCHAR(32) | IP地址 |
| operation_type | VARCHAR(32) | 操作类型：`LOGIN`、`LOGOUT`、`CONFIG_CHANGE`、`EXPORT` 等 |
| operation_content | TEXT | 操作内容详情 |
| result | TINYINT | 结果：0失败 1成功 |
| created_at | TIMESTAMP | 操作时间 |

#### `rule_config` —— 业务规则配置表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 配置ID |
| rule_name | VARCHAR(64) | 规则名称 |
| channel_type | VARCHAR(32) | 适用通道类型：`PDI_FRONT`、`PDI_REAR`、`PDI_SLIDING` |
| require_vehicle | BOOLEAN | 是否要求车辆在场（默认 `true`） |
| enter_pattern | JSONB | 进入判定状态序列（如 `[15,16,11]`） |
| exit_pattern | JSONB | 离开判定状态序列（如 `[11,16,15,[13,9]]`） |
| standard_duration | INT | 标准工时（秒） |
| critical_threshold_pct | DECIMAL(5,2) | 临界阈值百分比（默认90%） |
| person_absent_timeout | INT | 人员消失超时阈值（秒） |
| is_enabled | BOOLEAN | 是否启用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

## 6. 接口定义

### 6.1 平台对外接口（RESTful API）

平台内部各模块通过RESTful API对外提供服务，统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1713091200000
}
```

#### 预警模块
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/alarms/realtime` | WebSocket | 订阅实时报警流（STOMP over SockJS） |
| `/api/v1/alarms` | GET | 查询历史报警列表（支持分页、筛选） |
| `/api/v1/alarms/{id}` | GET | 获取报警详情 |
| `/api/v1/alarms/{id}/process` | PUT | 标记报警处理状态 |
| `/api/v1/alarms/{id}/images` | GET | 下载报警图片（带水印） |
| `/api/v1/alarms/export` | POST | 导出历史报警（Excel/CSV） |

#### 报表模块
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/reports/pdi` | GET | PDI作业报表查询 |
| `/api/v1/reports/pdi/{id}` | GET | 报表详情（含截图） |
| `/api/v1/reports/pdi/export` | POST | 报表导出（PDF/Excel，可选图片打包） |

#### 设备模块
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/devices/boxes` | GET/POST/DELETE | 盒子列表、添加、删除 |
| `/api/v1/devices/boxes/{id}/reboot` | POST | 远程重启盒子 |
| `/api/v1/devices/channels` | GET/PUT | 通道列表、编辑通道参数 |
| `/api/v1/devices/channels/{id}/preview` | GET | 获取通道实时预览流地址（从盒子取流） |
| `/api/v1/devices/monitor` | GET | 设备在线/离线统计（大屏数据） |

#### 权限模块
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/auth/login` | POST | 用户名密码登录 |
| `/api/v1/auth/logout` | POST | 登出 |
| `/api/v1/auth/refresh` | POST | 刷新Token |
| `/api/v1/users` | CRUD | 用户管理 |
| `/api/v1/roles` | CRUD | 角色管理 |

#### 系统配置模块
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/config/rules` | GET/PUT | 业务规则配置 |
| `/api/v1/config/thresholds` | GET/PUT | 阈值配置（标准工时、偏差百分比） |
| `/api/v1/config/general` | GET/PUT | 通用配置（声音、留存周期、水印） |

### 6.2 盒子到平台的数据推送接口

边缘盒子通过HTTP POST（或MQTT Publish）将数据推送到平台指定的接入网关地址。

#### A. 实时状态数据推送
**Endpoint：** `POST /gateway/v1/box/state`

**Headers：**
```
Content-Type: application/json
X-Box-Id: BOX_001
X-Box-Secret: <预共享密钥>
```

**Request Body：**
```json
{
  "box_id": "BOX_001",
  "timestamp": 1713091200,
  "channel_id": "CH_01",
  "states": {
    "door_open": true,
    "person_present": true,
    "person_entering_exiting": false,
    "vehicle_present": true
  },
  "snapshot": {
    "target_image": "/9j/4AAQSkZJRg...",
    "scene_image": "/9j/4AAQSkZJRg..."
  }
}
```

**平台处理逻辑：**
1. 校验 `X-Box-Secret`。
2. 将 `states` 转换为组合编号（1-16）。
3. 若有 `snapshot`，异步上传至 MinIO，替换为对象存储路径。
4. 将清洗后的记录写入 `state_stream` 表。
5. 将记录投递到规则引擎对应通道的滑动窗口。

#### B. 报警事件推送
**Endpoint：** `POST /gateway/v1/box/alarm`

**Request Body：**
```json
{
  "box_id": "BOX_001",
  "timestamp": 1713091250,
  "channel_id": "CH_01",
  "event_type": "SMOKE_DETECTED",
  "event_detect_info": {
    "confidence": 0.94,
    "trigger_frames": 5
  },
  "snapshot": {
    "target_image": "/9j/4AAQSkZJRg...",
    "scene_image": "/9j/4AAQSkZJRg..."
  }
}
```

**平台处理逻辑：**
1. 校验盒子身份。
2. 解析 `event_type`，映射为平台报警类型（`SMOKE` 或 `PDI_UNQUALIFIED`）。
3. 保存报警记录到 `alarm` 表。
4. 通过 Redis Pub/Sub 推送实时消息到前端 WebSocket。
5. 触发声音报警（若配置开启）。

#### C. 盒子心跳推送
**Endpoint：** `POST /gateway/v1/box/heartbeat`

**Request Body：**
```json
{
  "box_id": "BOX_001",
  "timestamp": 1713091300,
  "status": "ONLINE",
  "system_info": {
    "cpu_usage": 45.2,
    "mem_usage": 62.1,
    "disk_usage": 78.5,
    "version": "v2.1.3"
  },
  "channels": [
    {
      "channel_id": "CH_01",
      "status": "ONLINE"
    }
  ]
}
```

**平台处理逻辑：**
1. 更新 `box` 表的 `last_heartbeat` 和 `status` 字段。
2. 解析 `system_info` 记录盒子资源使用率。
3. 更新 `channel` 表各通道状态。
4. 若心跳超时（如连续3次未收到，约90秒），设备服务将该盒子及关联通道标记为离线，并生成平台告警。

> **离线数据策略**：边缘盒子在断网期间**不需要本地缓存状态流和报警数据**。网络恢复后，盒子只需恢复心跳和正常推送即可；断网期间的数据允许丢失，以简化盒子端和平台端逻辑。

### 6.3 平台到盒子的控制接口

平台通过调用盒子提供的本地HTTP API实现远程控制（盒子需暴露内网可访问的管理端口）。

| 接口 | 方法 | 说明 |
|------|------|------|
| `http://{box_ip}:8080/api/v1/channels` | GET | 查询盒子通道列表 |
| `http://{box_ip}:8080/api/v1/channels/{id}` | PUT | 修改通道参数（名称、RTSP地址等） |
| `http://{box_ip}:8080/api/v1/system/reboot` | POST | 远程重启盒子 |
| `http://{box_ip}:8080/api/v1/stream/{channel_id}` | GET | 获取实时预览流地址 |

> **注意：** 平台不直接中转视频流，只从盒子获取预览流地址，由前端播放器（如 FLV.js / HLS.js）直连盒子或流媒体服务器取流。

---

## 7. 部署架构

### 7.1 物理部署拓扑

```
                                    ┌─────────────────────┐
                                    │   厂区核心交换机      │
                                    │   (Core Switch)      │
                                    └──────────┬──────────┘
                                               │
              ┌────────────────────────────────┼────────────────────────────────┐
              │                                │                                │
              │ LAN/VPN                        │ LAN/VPN                        │
              ▼                                ▼                                ▼
    ┌───────────────────┐          ┌───────────────────┐          ┌───────────────────┐
    │    金桥库站点      │          │   中心机房/服务器   │          │    凯迪库站点      │
    │  ┌─────────────┐  │          │  ┌─────────────┐  │          │  ┌─────────────┐  │
    │  │  边缘盒子A   │  │          │  │  业务平台    │  │          │  │  边缘盒子B   │  │
    │  │  Edge Box   │──┼──────────┼─▶│  (Docker    │  │◀────────┼──│  Edge Box   │  │
    │  └─────────────┘  │          │  │  Compose)   │  │          │  └─────────────┘  │
    │  ┌─────────────┐  │          │  └─────────────┘  │          │  ┌─────────────┐  │
    │  │    NVR      │  │          │                   │          │  │    NVR      │  │
    │  │ (视频存储)   │  │          │                   │          │  │ (视频存储)   │  │
    │  └─────────────┘  │          │                   │          │  └─────────────┘  │
    └───────────────────┘          └───────────────────┘          └───────────────────┘
```

### 7.2 容器化部署方案（Docker Compose）

推荐采用 **单台物理服务器 + Docker Compose** 部署，适合初期10并发用户的规模，运维简单、成本最低。所有服务运行在同一台 16核CPU / 64GB内存 / 2TB磁盘 的物理机上。

```yaml
# docker-compose.yml (开发/生产直接可用)
version: '3.8'
services:
  nginx:
    image: nginx:1.24-alpine
    container_name: vdc-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./frontend-dist:/usr/share/nginx/html:ro
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 512M
    networks:
      - vdc-network

  app:
    image: vdc-platform:latest
    container_name: vdc-app
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - DB_PORT=5432
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - MINIO_HOST=minio
      - MINIO_PORT=9000
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
      - minio
    deploy:
      resources:
        limits:
          cpus: '4.0'
          memory: 4G
    networks:
      - vdc-network

  postgres:
    image: postgres:15-alpine
    container_name: vdc-postgres
    volumes:
      - pg_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d:ro
    environment:
      - POSTGRES_DB=vdc_platform
      - POSTGRES_USER=vdc
      - POSTGRES_PASSWORD=<强密码>
    ports:
      - "5432:5432"
    deploy:
      resources:
        limits:
          cpus: '4.0'
          memory: 8G
    networks:
      - vdc-network

  redis:
    image: redis:7-alpine
    container_name: vdc-redis
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
    networks:
      - vdc-network

  minio:
    image: minio/minio:latest
    container_name: vdc-minio
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data
    environment:
      - MINIO_ROOT_USER=vdcadmin
      - MINIO_ROOT_PASSWORD=<强密码>
    ports:
      - "9000:9000"
      - "9001:9001"
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 4G
    networks:
      - vdc-network

volumes:
  pg_data:
  redis_data:
  minio_data:

networks:
  vdc-network:
    driver: bridge
```

### 7.3 Dockerfile（Spring Boot 应用）

```dockerfile
# 使用 Eclipse Temurin JDK 17 作为基础镜像
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# 运行时镜像
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/vdc-platform-*.jar app.jar

# JVM 参数优化（4GB 内存限制下）
ENV JAVA_OPTS="-server -Xms2g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 7.4 Nginx 反向代理配置

```nginx
# nginx.conf
worker_processes  auto;

events {
    worker_connections  1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    keepalive_timeout  65;

    # 上游 Spring Boot 应用
    upstream vdc_app {
        server app:8080;
    }

    server {
        listen       80;
        server_name  localhost;

        # 前端静态资源
        location / {
            root   /usr/share/nginx/html;
            index  index.html index.htm;
            try_files $uri $uri/ /index.html;
        }

        # API 和 WebSocket 代理
        location /api/ {
            proxy_pass         http://vdc_app;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
        }

        location /gateway/ {
            proxy_pass         http://vdc_app;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
        }

        # WebSocket 代理（STOMP over SockJS）
        location /ws/ {
            proxy_pass         http://vdc_app;
            proxy_http_version 1.1;
            proxy_set_header   Upgrade $http_upgrade;
            proxy_set_header   Connection "upgrade";
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
        }
    }
}
```

### 7.5 服务器资源配置建议

基于 **16核CPU / 64GB内存 / 2TB磁盘** 的单台物理服务器配置，按 Docker Compose 各服务资源限制分配如下：

| 组件 | CPU限制 | 内存限制 | 磁盘 | 说明 |
|------|---------|----------|------|------|
| **Nginx** | 1核 | 512MB | 10GB | 反向代理 + 静态资源 |
| **业务服务 (app)** | 4核 | 4GB | 50GB | Spring Boot 单体应用 |
| **PostgreSQL** | 4核 | 8GB | 500GB SSD | 主数据库 + 时序数据 |
| **Redis** | 2核 | 2GB | 20GB | 缓存 + 消息推送 |
| **MinIO** | 2核 | 4GB | 2TB | 抓拍图片、报表文件 |
| **系统余量** | 3核 | 45.5GB | - | 留给宿主机 OS、Docker 守护进程、日志和突发负载 |
| **总计** | **16核** | **64GB** | **2TB SSD** | 单台物理服务器 |

> **存储容量估算**：
> - 状态流数据：20通道 × 1条/秒 × 30天 ≈ **5200万条**，约占 **10-20GB**（PostgreSQL 普通表 + 定时清理）。
> - 图片数据：假设每天产生500张抓拍（含报警、头帧、尾帧），每张200KB，90天 ≈ **9GB**。
> - 报表与导出文件：预留 **50GB**。
> - 总计：500GB 数据库盘 + 2TB 对象存储盘，留有充足余量。

### 7.6 高可用与扩展性说明（预留）

虽然初期单台服务器即可满足，但架构设计预留扩展空间：
- **应用层**：Spring Boot 服务无状态化，Session 存入 Redis，未来可通过 Nginx 负载均衡横向扩展为多实例。
- **数据库层**：PostgreSQL 可通过主从复制（Streaming Replication）实现读写分离；若时序数据量激增，可迁移到独立的 TimescaleDB 集群。
- **文件存储**：MinIO 支持分布式模式，未来可扩展为集群部署。
- **消息推送**：Redis Pub/Sub 可替换为 RabbitMQ 集群或 Kafka，支撑更大规模的实时消息分发。

---

## 8. 安全与权限设计

### 8.1 认证与授权机制

#### 认证方式
- **JWT Token**：用户登录成功后，后端签发 JWT（Access Token + Refresh Token），前端存储于内存或 LocalStorage。
- **Token 有效期**：Access Token 2小时，Refresh Token 7天。
- **登录失败锁定**：连续5次密码错误，锁定账号5分钟（记录于 Redis）。

#### 授权模型（RBAC + 数据隔离）
- **功能权限**：基于角色控制菜单、按钮、API访问权限。权限列表存储于 `sys_role.permissions`（JSONB数组）。
- **数据权限**：基于 `sys_user.site_id` 进行站点隔离，SQL 查询统一附加 `WHERE site_id = ?` 条件。
- **超级管理员**：`role_code = SUPER_ADMIN`，`data_scope = ALL`，跳过站点过滤。

**数据权限过滤示例（MyBatis-Plus 拦截器）：**
```java
// 伪代码：在查询拦截器中根据当前用户角色动态追加 site_id 条件
if (!currentUser.isSuperAdmin()) {
    queryWrapper.eq("site_id", currentUser.getSiteId());
}
```

### 8.2 网络安全

| 层面 | 措施 |
|------|------|
| **传输安全** | 平台内部使用 HTTPS（Nginx 配置 SSL 证书）；盒子到平台的推送接口建议使用 HTTPS + 预共享密钥（`X-Box-Secret`）校验。 |
| **网络隔离** | 业务平台部署在中心机房；边缘盒子通过厂区内部 LAN/VPN 访问平台，不直接暴露于公网。 |
| **API 安全** | 接入网关接口（盒子推送）增加 IP 白名单、请求速率限制（Rate Limiting）、重放攻击防护（timestamp 校验，允许 ±5分钟偏差）。 |
| **敏感信息** | 数据库密码、MinIO 密钥、盒子通信密钥统一存入环境变量或配置中心，禁止硬编码。 |

### 8.3 审计与日志

- **操作日志**：所有用户关键操作（登录、配置修改、导出、用户管理）记录到 `operation_log` 表，保留至少180天。
- **系统日志**：Spring Boot 应用日志按 `DEBUG/INFO/WARN/ERROR` 分级，通过 Logback 输出到文件，配合 `logrotate` 自动轮转。
- **盒子通信日志**：记录盒子推送数据的接收时间、数据量、异常信息，便于排查网络或盒子故障。

---

## 9. 性能与扩展性

### 9.1 性能目标达成分析

| 需求指标 | 设计方案 | 预期达成 |
|----------|----------|----------|
| **实时预警延迟 ≤ 3秒** | 盒子HTTP推送 + 平台异步处理 + Redis Pub/Sub + WebSocket 推送 | **≤ 1秒** |
| **并发用户 10人** | 单台 Spring Boot 实例可轻松支撑 100+ 并发 | **无压力** |
| **报表查询 ≤ 5秒** | PostgreSQL 分区表 + 关键索引（`site_id`, `channel_id`, `ts`）+ 报表数据预聚合 | **1-3秒** |
| **数据存储 90天** | PostgreSQL 自动分区清理 + MinIO 生命周期策略 | **自动达成** |

### 9.2 关键性能优化点

1. **时序数据写入优化**
   - 批量写入：状态流数据在接入网关层做**批量缓冲**（如每100ms或每100条批量INSERT一次），减少数据库I/O。
   - 异步处理：快照图片上传 MinIO、规则引擎计算均采用异步线程池，不阻塞数据接收。

2. **查询性能优化**
   - `state_stream` 表建立复合索引：`(channel_id, ts DESC)`。
   - `alarm` 表建立复合索引：`(site_id, alarm_time DESC, alarm_type)`。
   - `work_session` 表建立索引：`(site_id, channel_id, start_time DESC)`。

3. **实时推送性能**
   - WebSocket 连接由 Nginx 代理，支持长连接保持。
   - 报警消息通过 Redis Pub/Sub 广播，应用层再按用户站点权限过滤后推送到对应 WebSocket Session。

4. **报表预聚合（可选）**
   - 对于复杂统计报表，可在每天凌晨运行定时任务，将前一天的 PDI 作业统计数据预计算到 `report_daily_summary` 表中，进一步提升查询速度。

### 9.3 扩展性预留

| 扩展场景 | 预留方案 |
|----------|----------|
| **新增站点** | 在 `site` 表注册新站点，分配边缘盒子即可，无需改动代码。 |
| **新增工位/通道** | 在 `channel` 表注册，绑定对应盒子和算法类型。 |
| **规则调整** | 通过后台修改 `rule_config` 表中的 JSON 规则，规则引擎动态热加载。 |
| **用户规模扩大** | 应用服务无状态化，横向扩展为集群；数据库引入读写分离。 |
| **更多算法类型** | 接入网关和规则引擎采用插件化设计，新增算法类型只需扩展枚举值和匹配逻辑。 |

---

## 10. 附录

### 10.1 状态组合速查表

组合编号采用 4 位二进制映射，各位权重为：
- `vehicle_present`：8
- `door_open`：4
- `person_present`：2
- `person_entering_exiting`：1

| 组合编号 | vehicle_present | door_open | person_present | person_entering_exiting | 典型场景 |
|----------|-----------------|-----------|----------------|-------------------------|----------|
| 1 | false | false | false | false | 空闲（无车） |
| 2 | false | false | false | true | 不合理（无人时无进出） |
| 3 | false | false | true | false | 车门关，有人，无车 |
| 4 | false | false | true | true | 不合理（关门时无进出） |
| 5 | false | true | false | false | 车门开，无人，无车 |
| 6 | false | true | false | true | 不合理（无人时无进出） |
| 7 | false | true | true | false | 车门开，有人，未进出，无车 |
| 8 | false | true | true | true | 车门开，有人，正在进出，无车 |
| 9 | true | false | false | false | 空闲（有车） |
| 10 | true | false | false | true | 不合理（无人时无进出） |
| 11 | true | false | true | false | 车门关，有人，有车（人员在车内） |
| 12 | true | false | true | true | 不合理（关门时无进出） |
| 13 | true | true | false | false | 车门开，无人，有车 |
| 14 | true | true | false | true | 不合理（无人时无进出） |
| 15 | true | true | true | false | 车门开，有人，未进出，有车 |
| 16 | true | true | true | true | 车门开，有人，正在进出，有车 |

> 注：组合 2、4、6、8、10、12、14 在物理上不合理或属于非 PDI 场景（无车），应用层规则引擎可根据 `require_vehicle` 配置做滤波忽略。当 `vehicle_present=false` 时，PDI 作业判定逻辑一般不进入或强制中断。

### 10.2 标准工时参考值（待业务确认后填入）

| 工位 | 标准工时 | 临界阈值（90%） | 备注 |
|------|----------|----------------|------|
| 左前门 | 12 分钟 | 10 分 48 秒 | |
| 左后门 | 3 分钟 | 2 分 42 秒 | |
| 滑移门 | 8 分钟 | 7 分 12 秒 | |
| 合并流程（左前+左后） | 15 分钟 | 13 分 30 秒 | |

### 10.3 参考文档

1. 《金桥基地VDC智能算法项目业务平台需求规格说明书》
2. 《PDI工位作业判定逻辑确认书》

---

**文档结束**

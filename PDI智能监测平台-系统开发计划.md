# PDI智能监测平台 - 系统开发计划

**版本**: v1.0
**日期**: 2026-03-29
**制定**: 资深开发团队

---

## 一、项目概述

**PDI智能监测平台**是基于Java 17 + Spring Boot 3.x + PostgreSQL 16构建的单体架构系统，用于监测车辆预交付检验(PDI)作业过程，通过边缘盒子采集状态流数据，实现作业时长合规性检测与预警。

### 1.1 技术栈
- 开发语言: Java 17 LTS
- 基础框架: Spring Boot 3.2.x
- 数据访问: Spring Data JPA + QueryDSL
- 数据库: PostgreSQL 16
- 缓存: Caffeine
- 安全: Spring Security + JWT
- 实时通信: Spring WebFlux (SSE)
- API文档: SpringDoc OpenAPI

### 1.2 模块清单

| 模块 | 名称 | 负责人 | 描述 |
|------|------|--------|------|
| pdi-common | 公共基础模块 | 后端程序员-I | 实体基类、统一响应、异常处理、工具类 |
| pdi-rule-engine | 规则引擎模块 | 后端程序员-B | 状态机管理、PDI作业计算、报警判定 |
| pdi-auth-management | 权限管理模块 | 后端程序员-E | 用户/角色管理、JWT认证、数据权限 |
| pdi-device-management | 设备管理模块 | 后端程序员-F | 边缘盒子、通道管理、心跳处理 |
| pdi-system-config | 系统配置模块 | 后端程序员-H | 算法开关、业务规则配置 |
| pdi-algorithm-inlet | 算法数据入口 | 后端程序员-A | 接收边缘盒子数据、协议转换 |
| pdi-alarm-center | 预警中心模块 | 后端程序员-C | 报警管理、实时推送(SSE) |
| pdi-behavior-archive | 行为档案模块 | 后端程序员-D | PDI作业档案、时间线、图片下载 |
| pdi-log-management | 日志管理模块 | 后端程序员-G | 操作日志、系统日志、日志清理 |
| pdi-start | 启动入口模块 | 团队共用 | Spring Boot启动类、模块整合 |

---

## 二、模块依赖关系

### 2.1 依赖关系图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              启动层 (Layer 4)                                │
│                              ┌─────────────┐                                │
│                              │  pdi-start  │                                │
│                              └─────────────┘                                │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                            业务层 (Layer 3)                                  │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐      │
│  │ algorithm │ │   alarm   │ │ behavior  │ │   log     │ │   device  │      │
│  │   inlet   │ │  center   │ │  archive  │ │management │ │management │      │
│  └─────┬─────┘ └─────┬─────┘ └─────┬─────┘ └─────┬─────┘ └───────────┘      │
│        │             │             │             │                          │
└────────┼─────────────┼─────────────┼─────────────┼──────────────────────────┘
         │             │             │             │
         └─────────────┴─────────────┴─────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                            核心层 (Layer 2)                                  │
│        ┌─────────────┐    ┌─────────────┐    ┌─────────────┐               │
│        │ rule-engine │    │    auth     │    │   config    │               │
│        │  (规则引擎)  │    │ management  │    │  (系统配置)  │               │
│        └──────┬──────┘    └─────────────┘    └─────────────┘               │
│               │                                                            │
└───────────────┼────────────────────────────────────────────────────────────┘
                │
                ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                            基础层 (Layer 1)                                  │
│                            ┌─────────────┐                                  │
│                            │   common    │                                  │
│                            │  (公共基础)  │                                  │
│                            └─────────────┘                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 依赖矩阵

| 模块 | 依赖的模块 | 被依赖的模块 |
|------|-----------|-------------|
| pdi-common | 无 | 所有其他模块 |
| pdi-rule-engine | pdi-common | pdi-algorithm-inlet, pdi-alarm-center, pdi-behavior-archive, pdi-system-config |
| pdi-auth-management | pdi-common | 所有业务模块(通过Security) |
| pdi-device-management | pdi-common | pdi-algorithm-inlet(心跳) |
| pdi-system-config | pdi-common | pdi-rule-engine(规则配置) |
| pdi-algorithm-inlet | pdi-common, pdi-rule-engine | pdi-start |
| pdi-alarm-center | pdi-common, pdi-rule-engine | pdi-start |
| pdi-behavior-archive | pdi-common, pdi-rule-engine | pdi-start |
| pdi-log-management | pdi-common | 所有业务模块(通过AOP) |
| pdi-start | 所有业务模块 | 无 |

---

## 三、开发阶段规划

### 3.1 阶段划分

| 阶段 | 名称 | 工期 | 模块 |
|------|------|------|------|
| **Phase 1** | 基础设施层 | 5天 | pdi-common |
| **Phase 2** | 核心能力层 | 8天 | pdi-rule-engine, pdi-auth-management, pdi-device-management, pdi-system-config |
| **Phase 3** | 业务功能层 | 10天 | pdi-algorithm-inlet, pdi-alarm-center, pdi-behavior-archive, pdi-log-management |
| **Phase 4** | 集成启动层 | 3天 | pdi-start |
| **Phase 5** | 联调测试 | 5天 | 全系统联调、性能测试 |

**总工期: 31天**

---

## 四、详细开发计划

### Phase 1: 基础设施层 (Day 1-5)

#### Day 1: 项目初始化
**负责人**: 全体后端开发
**任务**:
- [ ] 创建Maven多模块项目结构
- [ ] 配置parent POM (依赖管理、插件配置)
- [ ] 初始化Git仓库，配置分支策略
- [ ] 搭建本地开发环境 (Java 17, PostgreSQL, Maven)
- [ ] 创建数据库 `pdi_monitor`

**交付物**:
- 项目骨架代码
- 数据库初始化脚本

#### Day 2-3: pdi-common 模块开发
**负责人**: 后端程序员-I
**依赖**: 无
**任务**:
- [ ] 实体基类 `BaseEntity` 开发 (含JPA审计)
- [ ] 统一响应结构 `ApiResponse`, `PageResponse` 开发
- [ ] 全局异常处理 `GlobalExceptionHandler` 开发
- [ ] 业务枚举定义 (SiteEnum, AlarmTypeEnum, AlarmStatusEnum, StateCodeEnum, DeviceStatusEnum, ChannelTypeEnum)
- [ ] 工具类开发 (JsonUtils, DateUtils, SecurityUtils, ValidationUtils, IpUtils)
- [ ] 常量定义 `CommonConstant`

**单元测试**:
- [ ] BaseEntityTest
- [ ] ApiResponseTest
- [ ] GlobalExceptionHandlerTest
- [ ] JsonUtilsTest
- [ ] DateUtilsTest

**交付物**:
- pdi-common 模块代码
- 单元测试覆盖率 ≥ 80%

#### Day 4: pdi-common 完善 + 数据库DDL
**负责人**: 后端程序员-I (主导), 其他协助
**任务**:
- [ ] 完善 pdi-common 工具类
- [ ] 编写全量数据库DDL脚本
- [ ] 创建核心表: site, edge_box, channel, state_stream, pdi_task, behavior_archive, alarm_record, archive_timeline, sys_user, sys_role, sys_user_role, sys_operation_log, system_config, algorithm_config, business_rule, idempotency_record
- [ ] 编写初始化数据脚本 (站点、默认角色、系统配置)

**交付物**:
- 完整DDL脚本
- 初始化数据脚本

#### Day 5: Phase 1 验收
**负责人**: 架构师
**任务**:
- [ ] 代码评审 (CR)
- [ ] 单元测试覆盖率检查
- [ ] 数据库设计评审
- [ ] 文档归档

---

### Phase 2: 核心能力层 (Day 6-13)

#### Day 6-7: pdi-rule-engine 规则引擎模块 (Part 1)
**负责人**: 后端程序员-B
**依赖**: pdi-common
**任务**:
- [ ] 状态机核心类开发
  - `State` 枚举定义 (S1, S3, S5, S7, S8)
  - `StateMachine` 状态机实例
  - `StateMachineManager` 状态机管理器
- [ ] 状态序列匹配器 `StateSequenceMatcher` 开发
  - 进入序列匹配: S7→S8→S3
  - 离开序列匹配: S3→S8→S7→S5/S1
  - 滑动窗口算法实现

**单元测试**:
- [ ] StateMachineTest
- [ ] StateSequenceMatcherTest

#### Day 8: pdi-rule-engine 规则引擎模块 (Part 2)
**负责人**: 后端程序员-B
**依赖**: pdi-common
**任务**:
- [ ] PDI作业时长计算器 `PdiTaskCalculator` 开发
  - 单通道时长计算
  - 多车门合并计算 (max(T4,T5,T6) - min(T1,T2,T3))
- [ ] 报警判定器 `AlarmJudge` 开发
  - 合规等级判定 (PASSED/CRITICAL/FAILED)
  - 临界阈值计算 (默认90%)
- [ ] 事件定义与发布
  - StateStreamEvent, TaskStartEvent, TaskEndEvent, AlarmTriggerEvent

**单元测试**:
- [ ] PdiTaskCalculatorTest
- [ ] AlarmJudgeTest
- [ ] RuleEngineIntegrationTest

**交付物**:
- pdi-rule-engine 模块代码
- 单元测试覆盖率 ≥ 85%

#### Day 9-10: pdi-auth-management 权限管理模块
**负责人**: 后端程序员-E
**依赖**: pdi-common
**任务**:
- [ ] 数据库表: sys_user, sys_role, sys_user_role
- [ ] JWT认证组件
  - `JwtTokenProvider` (Token生成与验证)
  - `JwtAuthenticationFilter` (Token过滤器)
  - `UserDetailsServiceImpl` (用户详情加载)
  - `TokenBlacklistService` (Token黑名单)
- [ ] 登录锁定服务 `LoginLockService` (Caffeine实现)
- [ ] 密码策略验证器 `PasswordValidator`

**单元测试**:
- [ ] JwtTokenProviderTest
- [ ] LoginLockServiceTest
- [ ] PasswordValidatorTest

#### Day 11: pdi-auth-management 完善 + pdi-device-management (Part 1)
**负责人**: 后端程序员-E (auth), 后端程序员-F (device)
**依赖**: pdi-common
**任务 (Auth)**:
- [ ] 数据权限AOP切面 `DataScopeAspect`
- [ ] 用户/角色管理服务
- [ ] SecurityConfig 安全配置

**单元测试**:
- [ ] AuthServiceTest
- [ ] DataScopeAspectTest

**任务 (Device - Part 1)**:
- [ ] 数据库表: edge_box, channel
- [ ] 实体类: EdgeBox, Channel
- [ ] Repository层开发

#### Day 12-13: pdi-device-management (Part 2) + pdi-system-config
**负责人**: 后端程序员-F (device), 后端程序员-H (config)
**依赖**: pdi-common

**Device 任务**:
- [ ] 盒子管理服务 `BoxService`
- [ ] 通道管理服务 `ChannelService`
- [ ] 心跳处理器 `HeartbeatProcessor`
- [ ] RTSP地址加密存储 (AES)
- [ ] Controller层API

**单元测试**:
- [ ] BoxServiceTest
- [ ] ChannelServiceTest
- [ ] HeartbeatProcessorTest

**Config 任务**:
- [ ] 数据库表: system_config, algorithm_config, business_rule
- [ ] 配置缓存 `ConfigCache` (Caffeine)
- [ ] 算法配置服务 `AlgorithmConfigService`
- [ ] 业务规则服务 `BusinessRuleService`
- [ ] 系统配置服务 `SystemConfigService`
- [ ] Controller层API

**单元测试**:
- [ ] ConfigCacheTest
- [ ] AlgorithmConfigServiceTest

**Phase 2 交付物**:
- pdi-rule-engine, pdi-auth-management, pdi-device-management, pdi-system-config 模块代码
- 权限接口文档
- 设备管理接口文档
- 配置管理接口文档

---

### Phase 3: 业务功能层 (Day 14-23)

#### Day 14-15: pdi-algorithm-inlet 算法数据入口模块
**负责人**: 后端程序员-A
**依赖**: pdi-common, pdi-rule-engine
**任务**:
- [ ] 数据接收Controller
  - POST /api/v1/inlet/state (状态流)
  - POST /api/v1/inlet/alarm (报警事件)
  - POST /api/v1/inlet/heartbeat (心跳)
- [ ] 盒子认证 `BoxAuthenticator` (HMAC-SHA256)
- [ ] 请求校验器 `RequestValidator`
- [ ] 幂等控制 (本地缓存 + 数据库)
- [ ] 领域事件发布 (Spring Event)
- [ ] 异步事件处理器 `InletEventListener`

**单元测试**:
- [ ] AlgorithmInletControllerTest
- [ ] BoxAuthenticatorTest
- [ ] RequestValidatorTest
- [ ] IdempotencyTest

**交付物**:
- pdi-algorithm-inlet 模块代码
- 边缘盒子接口文档

#### Day 16-17: pdi-alarm-center 预警中心模块
**负责人**: 后端程序员-C
**依赖**: pdi-common, pdi-rule-engine
**任务**:
- [ ] 数据库表: alarm_record
- [ ] 报警服务 `AlarmService`
  - 实时报警列表
  - 历史报警分页查询 (QueryDSL)
  - 报警状态更新 (已处理/误报)
  - 今日统计
- [ ] SSE实时推送服务 `AlarmSSEService`
- [ ] 事件监听器 (监听AlarmTriggerEvent)

**单元测试**:
- [ ] AlarmServiceTest
- [ ] AlarmSSEServiceTest

**交付物**:
- pdi-alarm-center 模块代码
- 预警中心接口文档

#### Day 18-19: pdi-behavior-archive 行为档案模块
**负责人**: 后端程序员-D
**依赖**: pdi-common, pdi-rule-engine
**任务**:
- [ ] 数据库表: behavior_archive, archive_timeline
- [ ] 档案服务 `BehaviorArchiveService`
  - 档案列表查询
  - 档案详情 (含时间线)
  - 档案状态计算
- [ ] 时间线服务 `ArchiveTimelineService`
  - 时间线组装
  - 时间线节点添加
- [ ] 图片包导出服务 `ArchiveExportService` (ZIP流式下载)
- [ ] 事件监听器 (监听TaskStartEvent, TaskEndEvent)

**单元测试**:
- [ ] BehaviorArchiveServiceTest
- [ ] ArchiveTimelineServiceTest

**交付物**:
- pdi-behavior-archive 模块代码
- 行为档案接口文档

#### Day 20-21: pdi-log-management 日志管理模块
**负责人**: 后端程序员-G
**依赖**: pdi-common
**任务**:
- [ ] 数据库表: sys_operation_log, system_log
- [ ] AOP切面 `OperationLogAspect` (记录操作日志)
- [ ] 操作日志服务 `OperationLogService`
- [ ] 系统日志服务 `SystemLogService`
- [ ] 日志清理定时任务 `LogCleanupService`
- [ ] Controller层API

**单元测试**:
- [ ] OperationLogAspectTest
- [ ] LogCleanupServiceTest

**交付物**:
- pdi-log-management 模块代码
- 日志管理接口文档

#### Day 22-23: Phase 3 代码评审与优化
**负责人**: 架构师 + 全体开发
**任务**:
- [ ] 代码评审 (CR)
- [ ] 单元测试覆盖率检查 (≥ 70%)
- [ ] 集成测试用例编写
- [ ] API文档汇总 (Swagger)

---

### Phase 4: 集成启动层 (Day 24-26)

#### Day 24-25: pdi-start 启动模块 + 集成配置
**负责人**: 全体后端开发
**依赖**: 所有业务模块
**任务**:
- [ ] 创建 pdi-start 模块
- [ ] Spring Boot 启动类
- [ ] application.yml 配置文件
  - 数据库连接配置
  - JWT配置
  - 缓存配置
  - 日志配置
- [ ] 模块依赖整合 (pom.xml)
- [ ] 跨模块调用验证
- [ ] 全局异常处理统一
- [ ] CORS配置
- [ ] Swagger/OpenAPI配置

#### Day 26: 冒烟测试
**负责人**: 测试工程师 + 全体开发
**任务**:
- [ ] 服务启动测试
- [ ] 数据库连接测试
- [ ] 登录认证流程测试
- [ ] 基础CRUD操作测试
- [ ] 冒烟测试用例执行

**交付物**:
- pdi-start 模块代码
- 完整配置文件
- 冒烟测试报告

---

### Phase 5: 联调测试 (Day 27-31)

#### Day 27-28: 端到端联调
**负责人**: 全体后端开发 + 前端开发
**任务**:
- [ ] 登录/登出流程联调
- [ ] 设备管理功能联调
- [ ] 算法数据入口联调 (模拟边缘盒子)
- [ ] 状态机流转测试
- [ ] 报警生成与推送测试
- [ ] 行为档案生成测试
- [ ] 系统配置热更新测试

#### Day 29: 性能测试
**负责人**: 测试工程师
**任务**:
- [ ] 算法数据入口压力测试 (目标: ≥ 1000 TPS)
- [ ] 状态流处理延迟测试 (目标: ≤ 3秒)
- [ ] 数据库查询性能测试
- [ ] 内存使用情况监控
- [ ] 并发用户测试

#### Day 30: Bug修复与优化
**负责人**: 全体开发
**任务**:
- [ ] Bug修复
- [ ] 性能优化
- [ ] 代码重构
- [ ] 安全加固

#### Day 31: 验收交付
**负责人**: 架构师 + 项目经理
**任务**:
- [ ] 功能验收
- [ ] 文档归档
- [ ] 部署手册编写
- [ ] 运维手册编写
- [ ] 项目复盘

**交付物**:
- 完整源代码
- 单元测试报告 (覆盖率 ≥ 70%)
- API文档 (Swagger)
- 部署手册
- 运维手册
- 压力测试报告

---

## 五、风险控制

### 5.1 风险识别

| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| 状态机逻辑复杂 | 中 | 高 | 前期充分设计，增加单元测试覆盖率，预留调试时间 |
| SSE推送不稳定 | 中 | 中 | 准备降级方案(WebSocket)，增加心跳检测 |
| 多车门合并计算逻辑错误 | 低 | 高 | 增加边界条件测试，编写详细测试用例 |
| 数据库性能瓶颈 | 中 | 中 | 提前设计索引，准备查询优化方案 |
| 边缘盒子协议变更 | 低 | 中 | 协议层抽象，预留适配空间 |

### 5.2 关键路径

**关键路径**: pdi-common → pdi-rule-engine → pdi-algorithm-inlet → 联调测试

**关键路径工期**: 5天(common) + 3天(rule-engine) + 2天(inlet) + 5天(联调) = **15天**

---

## 六、沟通机制

### 6.1 每日站会
- 时间: 每天上午9:30
- 时长: 15分钟
- 内容: 昨日完成、今日计划、阻塞问题

### 6.2 周会
- 时间: 每周五下午16:00
- 时长: 1小时
- 内容: 进度回顾、风险识别、下周计划

### 6.3 代码评审
- 强制CR: pdi-common, pdi-rule-engine
- 普通CR: 其他模块
- CR人员: 至少1名其他模块负责人

---

## 七、质量标准

### 7.1 代码质量
- 单元测试覆盖率: 核心模块 ≥ 85%，业务模块 ≥ 70%
- SonarQube质量门禁: 无严重缺陷，无安全漏洞
- 代码注释: 公共方法必须有JavaDoc

### 7.2 接口规范
- 统一响应格式
- 统一的错误码
- RESTful API设计规范
- Swagger文档完整

### 7.3 性能指标
- 算法数据入口响应时间: ≤ 50ms (P99)
- 状态流处理延迟: ≤ 3秒
- 报警推送延迟: ≤ 1秒
- 数据库查询: 普通查询 ≤ 100ms，复杂查询 ≤ 500ms

---

**文档结束**

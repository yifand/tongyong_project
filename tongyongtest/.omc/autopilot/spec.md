# Deep Interview Spec: 金桥基地VDC业务平台后端开发

## Metadata
- Interview ID: vdc-platform-2026-04-14
- Rounds: 2
- Final Ambiguity Score: 16.5%
- Type: greenfield
- Generated: 2026-04-14
- Threshold: 20%
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.85 | 0.40 | 0.340 |
| Constraint Clarity | 0.75 | 0.30 | 0.225 |
| Success Criteria | 0.90 | 0.30 | 0.270 |
| **Total Clarity** | | | **0.835** |
| **Ambiguity** | | | **16.5%** |

## Goal
基于《金桥基地VDC业务平台-产品架构设计文档》v1.1，使用 Spring Boot 3 + Java 17 实现完整的后端 API 和规则引擎。交付产物需包含完整的项目结构（Controller/Service/Mapper/Entity）、Swagger API 文档、可运行的 JUnit 单元测试（覆盖关键接口和规则引擎逻辑），以及一套可一键启动后端及依赖中间件（PostgreSQL 15、Redis 7、MinIO）的 docker-compose 配置。

前端 Vue3 部分不在本次开发范围内。

## Constraints
- 技术栈必须遵循文档选型：Spring Boot 3.2.x、Java 17、MyBatis-Plus、PostgreSQL 15、Redis 7、MinIO、Docker
- 后端需实现文档中定义的所有服务端模块：接入网关（盒子数据接入）、规则引擎、预警服务、报表服务、设备服务、权限服务、系统配置服务
- 规则引擎需支持文档中定义的 PDI 状态序列模式匹配（进入序列 15→16→11、离开序列 11→16→15→13/9）以及抽烟检测报警接入
- 数据库 Schema 需与文档中定义的核心表结构保持一致（site、edge_box、channel、sys_user、sys_role、state_stream、work_session、alarm、operation_log、rule_config 等）
- 必须提供 Swagger/OpenAPI 接口文档
- 必须通过 docker-compose 实现一键启动（包含后端服务、PostgreSQL、Redis、MinIO）
- 数据权限模型需支持 RBAC + 站点隔离（SUPER_ADMIN 可跨站点）

## Non-Goals
- 前端 Vue3 管理后台、监控大屏、手机 H5 不在本次范围
- 边缘盒子本身的固件/算法开发不在本次范围
- 生产环境高可用集群部署（多实例负载均衡、TimescaleDB、Kafka 等）不在本次范围
- SSL/HTTPS 证书配置可保留占位但不强求完整配置
- RabbitMQ 可作为可选预留，docker-compose 中不强制要求启动

## Acceptance Criteria
- [ ] 项目能使用 `docker-compose up -d` 一键启动，后端服务健康检查通过
- [ ] Swagger UI 可正常访问并展示所有后端 API 接口定义
- [ ] 数据库初始化脚本（含表结构、基础数据）在容器启动时自动执行
- [ ] 盒子接入网关 API (`/gateway/v1/box/state`, `/gateway/v1/box/alarm`, `/gateway/v1/box/heartbeat`) 能正确接收并处理模拟数据
- [ ] 规则引擎 JUnit 测试覆盖 PDI 进入/离开状态序列匹配逻辑，并通过测试
- [ ] 权限服务 JUnit 测试覆盖 JWT 签发/校验及 RBAC 站点隔离查询逻辑，并通过测试
- [ ] 关键业务接口（报警查询、设备管理、报表查询、规则配置）具备基础的单元测试或集成测试
- [ ] 代码结构清晰，分层规范（Controller / Service / Mapper / Entity / DTO / Config），包名符合 Java  conventions

## Assumptions Exposed & Resolved
| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| 用户要求完整实现前后端全部功能 | 第一轮提问：先从哪个范围切入？ | 明确为"只实现后端 API 和规则引擎"，前端排除 |
| 验收标准可能是"代码结构正确即可"或"核心逻辑跑通" | 第二轮提问：用什么标准判断"做好了"？ | 明确为"完整代码 + Swagger + 可运行测试 + docker-compose 一键启动" |
| 盒子离线数据是否需要补传 | 文档本身已明确 | 文档 6.2 节已声明"边缘盒子在断网期间不需要本地缓存状态流和报警数据"，断网数据允许丢失 |

## Technical Context
项目为 greenfield，当前工作目录中无既有源代码。架构设计文档 v1.1 已提供了详尽的技术选型、模块划分、数据模型、接口定义和 Docker 部署方案，可直接作为开发蓝图。

核心后端模块：
1. **接入网关层**：接收边缘盒子 HTTP 推送的状态流与报警事件
2. **规则引擎层**：自定义 Java 状态机引擎，基于四元组（door_open、person_present、person_entering_exiting、vehicle_present）时序数据进行模式匹配
3. **预警服务**：报警持久化、实时推送（Redis Pub/Sub → WebSocket）
4. **报表服务**：PDI 作业报表、导出
5. **设备服务**：盒子注册、心跳、远程控制、通道管理
6. **权限服务**：JWT 认证、RBAC 角色权限、站点数据隔离
7. **系统配置服务**：规则配置、阈值配置、通用参数

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| 金桥基地VDC业务平台 | core domain | Spring Boot 3 backend, Vue 3 frontend, PostgreSQL 15, Redis 7, MinIO, RabbitMQ, Docker Compose deployment | consumes data from edge box, managed by RBAC auth system, monitors PDI work sessions, generates reports |
| 后端 API | core domain | Spring Boot 3, Controller, Service, Mapper, Entity | serves frontend, invoked by rule engine, protected by RBAC auth, documented by Swagger |
| 规则引擎 | core domain | State Processor, Pattern Matcher, Event Generator, state pattern matching, smoke detection alarms | processes edge box state streams, raises alarms, depends on backend API |
| 边缘盒子 | external system | state streams, alarms, 接入协议 | pushes data to VDC platform, source for rule engine |
| PDI 工位 | supporting | work session monitoring | monitored by VDC platform |
| RBAC 权限系统 | supporting | auth | secures backend API |
| Alert Service | core domain | alert management | part of backend API, consumed by frontend |
| Report Service | core domain | report generation | part of backend API, consumed by frontend |
| Device Service | core domain | device management | part of backend API, consumed by frontend |
| Auth Service | core domain | authentication, authorization | part of backend API, protects other services |
| System Config Service | core domain | configuration management | part of backend API, consumed by frontend |
| Gateway | core domain | box data ingestion | receives data from edge box, feeds rule engine |
| Swagger | supporting | API documentation | documents backend API |
| JUnit | supporting | unit tests | verifies backend API, verifies rule engine |
| docker-compose | supporting | container orchestration | runs backend, runs PostgreSQL, runs Redis, runs MinIO |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 1 | 7 | 7 | - | - | N/A |
| 2 | 15 | 8 | 0 | 7 | 100% |

**Ontology has converged** — all 7 entities from Round 1 were retained in Round 2, and 8 new supporting/domain entities were added to reflect the clarified technical scope. No renames or removals occurred, indicating stable domain understanding.

## Interview Transcript
<details>
<summary>Full Q&A (2 rounds)</summary>

### Round 1
**Q:** 我已经阅读了《金桥基地VDC业务平台-产品架构设计文档》。这份文档覆盖了从后端 Spring Boot、前端 Vue3、数据库、Docker 部署到盒子接入协议的完整架构，内容相当详尽。你的目标是让我基于这份文档开始编码并产出可运行的系统。问题是：你希望我先从哪个范围切入？
**A:** 只实现后端 API 和规则引擎

### Round 2
**Q:** 如果我交付了"后端 API 和规则引擎"，你打算用什么标准来判断它做好了？请选择最贴近你期望的验收方式。
**A:** 提供完整代码 + Swagger + 可运行测试（后端项目结构完整，包含 Controller/Service/Mapper/Entity，Swagger 文档可访问，关键接口和规则引擎有 JUnit 单元测试，能用 docker-compose 一键启动后端+数据库+Redis+MinIO）

</details>

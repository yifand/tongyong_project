# Deep Interview Spec: 金桥基地VDC业务平台开发级架构设计文档

## Metadata
- Interview ID: vdc-arch-dev-ready-interview-002
- Rounds: 3
- Final Ambiguity Score: 8.0%
- Type: greenfield
- Generated: 2026-04-14
- Threshold: 0.2
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.95 | 0.40 | 0.380 |
| Constraint Clarity | 0.92 | 0.30 | 0.276 |
| Success Criteria | 0.88 | 0.30 | 0.264 |
| **Total Clarity** | | | **0.920** |
| **Ambiguity** | | | **8.0%** |

## Goal
基于现有产品架构设计文档，补充开发落地所需的技术细节和部署配置，生成一份可直接用于开发的架构设计文档。该文档需包含完整的技术栈版本、服务拆分、部署拓扑、Docker Compose 配置、Nginx 配置、数据库 DDL 策略以及明确的标准工时参数。

## Constraints
- 部署方式：Docker Compose 在单台 16核CPU / 64GB内存 / 2TB磁盘 物理机上运行
- 服务架构：单体 Spring Boot 3 应用（接入网关、规则引擎、Web API 同进程）
- 数据库：PostgreSQL 15，不使用 TimescaleDB，`state_stream` 通过应用层定时任务或表分区清理
- 缓存/消息：Redis 7，用于会话缓存和实时消息 Pub/Sub
- 对象存储：MinIO（单节点），存储报警截图和报表截图
- 前端：Vue 3 + TypeScript + Vite + Element Plus
- 边缘盒子：已支持 `vehicle_present` 四元组输出；断网期间不缓存数据，恢复后仅恢复心跳
- 网络：金桥库和凯迪库通过稳定 LAN/VPN 连接中心服务器
- 数据隔离：站点级 RBAC + SQL `site_id` 过滤

## Non-Goals
- 边缘盒子算法开发（盒子为外部系统）
- 视频存储/回放（由 NVR 处理）
- SSO 集成（可选，不在当前开发范围内）
- Kubernetes 或多节点高可用部署
- CI/CD 流水线脚本（Jenkins/GitLab CI 等由 DevOps 团队另写）

## Acceptance Criteria
- [ ] 包含整体架构图和服务边界说明（单体应用）
- [ ] 包含技术栈及具体版本号
- [ ] 包含数据模型、数据库表设计及清理策略
- [ ] 包含接口定义（盒子到平台协议、内部 RESTful API）
- [ ] 包含基于 Docker Compose 的部署拓扑，含各服务资源限制
- [ ] 包含 Dockerfile、docker-compose.yml、Nginx 配置示例
- [ ] 包含安全与权限设计
- [ ] 包含性能与可扩展性分析（基于 16核64G 单机）
- [ ] 标准工时附录已填满具体数值

## Assumptions Exposed & Resolved
| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| 架构文档可直接用于开发 | 检查部署配置、服务边界、标准工时 | 不能直接用于开发，需补充 Docker Compose 配置、资源限制、标准工时 |
| 边缘盒子需新增车辆检测算法 | 确认 `vehicle_present` 实现状态 | 盒子上已支持 `vehicle_present` 输出，无需平台端新增开发 |
| 需要 TimescaleDB 处理状态流 | 询问是否需要专门的时序数据库 | 不需要 TimescaleDB，普通 PostgreSQL + 定时清理即可 |
| 服务应拆分为微服务 | 询问服务拆分粒度 | 采用单体 Spring Boot 3，降低部署复杂度 |
| 盒子断网需本地缓存重传 | 询问离线容错策略 | 断网期间允许数据丢失，简化盒子端和平台端逻辑 |
| 服务器硬件规格待定 | 询问目标部署机器配置 | 16核 CPU / 64GB 内存 / 2TB 磁盘 |

## Ontology (Key Entities)
| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| Business Platform | core domain | alerts, reports, users, devices, configs | receives data from Edge Box |
| Edge Computing Box | external system | box_id, site, ip, status, channels | pushes state streams to Business Platform |
| PDI Workstation | core domain | workstation_name, standard_duration, threshold | generates reports and alerts |
| State Stream | core domain | door_open, person_present, person_entering_exiting, vehicle_present, timestamp | input to Business Rule Engine |
| Business Rule Engine | core domain | require_vehicle, enter_rule, exit_rule, duration_calc, compliance_check | processes State Stream |
| Alert/Warning | core domain | type, site, channel, timestamp, images, status | output from Rule Engine / Box events |
| Report | supporting | site, vehicle, workstation, duration, deviation, result | output from PDI processing |
| Site | supporting | site_name, isolation_policy | contains Boxes, Channels, Users |
| Channel/Camera | supporting | channel_id, type, algorithm, status | belongs to Edge Box |
| User/Role | supporting | username, role, site_permissions, functional_permissions | accesses Platform data |
| Docker Compose Stack | infrastructure | Spring Boot, PostgreSQL, Redis, MinIO, Nginx | hosts Business Platform |

## Interview Transcript
<details>
<summary>Full Q&A (3 rounds)</summary>

### Round 1
**Q:** 标准工时现在是否有确定数值？`vehicle_present` 在边缘盒子上是已经实现还是需新增开发？部署方式是什么？是否需要 TimescaleDB？
**A:** 标准工时已确定（左前门12分钟、左后门3分钟、滑移门待定），盒子上已有车辆检测，Docker Compose 部署，不需要 TimescaleDB。
**Ambiguity:** 23.5%

### Round 2
**Q:** 业务平台是单体 Spring Boot 还是微服务拆分？边缘盒子离线后是否需要本地缓存并重传？开发级架构文档中是否需要包含 Dockerfile、docker-compose.yml、Nginx 配置等部署产物？
**A:** 单体应用，盒子允许断网丢数据，文档包含部署配置。
**Ambiguity:** 15.2%

### Round 3
**Q:** 目标服务器的硬件配置大概是怎样的？滑移门和合并流程的标准工时是否有参考值？
**A:** 服务器：16核64G内存2T磁盘单机；滑移门8分钟，合并流程15分钟。
**Ambiguity:** 8.0%

</details>

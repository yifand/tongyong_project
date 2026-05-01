# VDC 业务平台后端实现计划

> 基于《金桥基地VDC业务平台-产品架构设计文档》v1.1 及 deep-interview spec (vdc-platform-2026-04-14) 编制  
> 技术栈：Spring Boot 3.2.x + Java 17 + Maven + MyBatis-Plus + PostgreSQL 15 + Redis 7 + MinIO

---

## 0. 项目结构约定

```
vdc-platform/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── nginx.conf
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/vdc/platform/
│   │   │       ├── VdcPlatformApplication.java
│   │   │       ├── config/               # 配置类
│   │   │       ├── common/               # 公共常量、工具、异常、响应包装
│   │   │       ├── entity/               # MyBatis-Plus 实体
│   │   │       ├── mapper/               # Mapper 接口
│   │   │       ├── service/              # Service 接口及实现
│   │   │       ├── controller/           # REST Controller
│   │   │       ├── dto/                  # 请求/响应 DTO
│   │   │       ├── security/             # JWT、RBAC、数据权限拦截器
│   │   │       ├── gateway/              # 盒子接入网关（独立包，无鉴权）
│   │   │       ├── ruleengine/           # 规则引擎（状态机、模式匹配）
│   │   │       └── websocket/            # WebSocket/STOMP 实时推送
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       └── V2__init_data.sql
│   │       └── mybatis-plus/
│   └── test/
│       └── java/com/vdc/platform/
│           ├── ruleengine/
│           ├── auth/
│           └── service/
```

---

## Phase 1: 项目脚手架与基础配置

**目标**：建立可编译、可运行的 Spring Boot 单体项目骨架，集成 Swagger/OpenAPI、MyBatis-Plus、Redis、MinIO、PostgreSQL 驱动，并配置多环境 YAML。

### Task 1.1 - 创建 Maven 项目与根 POM
- **Task ID**: `P1-T1`
- **描述**: 初始化 Maven 项目，定义 Spring Boot 3.2.x parent、Java 17 编译级别、依赖版本矩阵（MyBatis-Plus 3.5.5、PostgreSQL driver 42.7.x、Jedis/Lettuce、MinIO Java SDK 8.5.x、SpringDoc OpenAPI 2.3.x、JJWT 0.12.x）。
- **输出文件**:
  - `/Users/apple/Desktop/设计文件/通用设计文件/tongyongtest/vdc-platform/pom.xml`
- **依赖**: 无
- **关键实现要点**:
  - `spring-boot-starter-parent` 版本 `3.2.5`（或最新 3.2.x patch）
  - `mybatis-plus-boot-starter` 3.5.5
  - `spring-boot-starter-websocket`, `spring-boot-starter-data-redis`, `spring-boot-starter-security`, `spring-boot-starter-validation`
  - `minio` 8.5.7
  - `springdoc-openapi-starter-webmvc-ui` 2.3.0
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 0.12.5
  - `lombok`（可选但建议用于减少样板代码）

### Task 1.2 - 主启动类与通用响应/异常
- **Task ID**: `P1-T2`
- **描述**: 创建 Spring Boot 启动类、统一响应包装 `ApiResult<T>`、全局异常处理器 `GlobalExceptionHandler`、业务异常 `BizException`、常量类 `ResultCode`。
- **输出文件**:
  - `src/main/java/com/vdc/platform/VdcPlatformApplication.java`
  - `src/main/java/com/vdc/platform/common/ApiResult.java`
  - `src/main/java/com/vdc/platform/common/BizException.java`
  - `src/main/java/com/vdc/platform/common/GlobalExceptionHandler.java`
  - `src/main/java/com/vdc/platform/common/ResultCode.java`
- **依赖**: `P1-T1`
- **关键实现要点**:
  - `ApiResult` 字段：`code`, `message`, `data`, `timestamp`
  - 全局异常处理需覆盖 `MethodArgumentNotValidException`（参数校验）、`BizException`（业务异常）、`Exception`（兜底 500）

### Task 1.3 - 多环境 YAML 配置
- **Task ID**: `P1-T3`
- **描述**: 编写 `application.yml`、`application-dev.yml`、`application-prod.yml`，配置数据源、Redis、MinIO、MyBatis-Plus、JWT、日志级别。
- **输出文件**:
  - `src/main/resources/application.yml`
  - `src/main/resources/application-dev.yml`
  - `src/main/resources/application-prod.yml`
- **依赖**: `P1-T1`
- **关键实现要点**:
  - `spring.datasource.url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/vdc_platform`
  - `spring.data.redis.host`, `port`, `database: 0`
  - `minio.endpoint`, `access-key`, `secret-key`, `bucket-name: vdc-bucket`
  - `vdc.jwt.secret`（Base64 编码的 256-bit 密钥，从环境变量注入）、`access-token-expiration: 7200000`（2小时）、`refresh-token-expiration: 604800000`（7天）
  - `mybatis-plus.configuration.log-impl: org.apache.ibatis.logging.stdout.StdOutImpl`（仅 dev）
  - `mybatis-plus.global-config.db-config.logic-delete-field: deleted`（如需要逻辑删除）

### Task 1.4 - MyBatis-Plus 分页与配置类
- **Task ID**: `P1-T4`
- **描述**: 创建 MyBatis-Plus 配置类，注册分页插件 `MybatisPlusInterceptor`。
- **输出文件**:
  - `src/main/java/com/vdc/platform/config/MyBatisPlusConfig.java`
- **依赖**: `P1-T1`
- **关键实现要点**:
  - `@Configuration` + `@MapperScan("com.vdc.platform.mapper")`
  - `Interceptor` 添加 `PaginationInnerInterceptor(DbType.POSTGRE_SQL)`

### Task 1.5 - Redis 与 MinIO 配置类
- **Task ID**: `P1-T5`
- **描述**: 创建 `RedisTemplate`/`StringRedisTemplate` Bean 和 `MinioClient` Bean。
- **输出文件**:
  - `src/main/java/com/vdc/platform/config/RedisConfig.java`
  - `src/main/java/com/vdc/platform/config/MinioConfig.java`
- **依赖**: `P1-T1`, `P1-T3`
- **关键实现要点**:
  - `RedisConfig` 配置 `StringRedisTemplate` 序列化器为 `StringRedisSerializer`
  - `MinioConfig` 从 `@ConfigurationProperties(prefix = "minio")` 读取配置并构建 `MinioClient`

### Task 1.6 - Swagger/OpenAPI 配置
- **Task ID**: `P1-T6`
- **描述**: 配置 SpringDoc OpenAPI，分组为 "业务 API" 和 "网关 API"，并添加 JWT SecurityScheme。
- **输出文件**:
  - `src/main/java/com/vdc/platform/config/OpenApiConfig.java`
- **依赖**: `P1-T1`
- **关键实现要点**:
  - `OpenAPI` 添加 `SecurityRequirement().addList("bearerAuth")`
  - `SecurityScheme` 类型 `HTTP`，方案 `bearer`，bearerFormat `JWT`
  - 使用 `@GroupedOpenApi` 拆分 `/api/**` 和 `/gateway/**`

### Task 1.7 - 验证项目可编译
- **Task ID**: `P1-T7`
- **描述**: 执行 `mvn clean compile` 确保项目骨架无编译错误。
- **输出文件**: 无（仅验证）
- **依赖**: `P1-T1` ~ `P1-T6`
- **关键实现要点**:
  - 若本地无 Maven，可用 `./mvnw` wrapper；wrapper 文件需一并提交

---

## Phase 2: 数据库层（实体、Mapper、初始化脚本）

**目标**：根据架构文档 5.2 节完成所有核心表的实体、Mapper、Service 接口及初始化 SQL；`state_stream` 表需按月分区。

### Task 2.1 - 数据库初始化脚本（Flyway 或原生 SQL）
- **Task ID**: `P2-T1`
- **描述**: 编写 `V1__init_schema.sql` 和 `V2__init_data.sql`，覆盖所有核心表、索引、外键、分区、初始站点/角色/管理员数据。
- **输出文件**:
  - `src/main/resources/db/migration/V1__init_schema.sql`
  - `src/main/resources/db/migration/V2__init_data.sql`
- **依赖**: `P1-T3`
- **关键实现要点**:
  - 表清单：`site`, `edge_box`, `channel`, `sys_user`, `sys_role`, `state_stream`, `work_session`, `alarm`, `operation_log`, `rule_config`
  - `state_stream` 使用 `PARTITION BY RANGE (ts)`，按月创建分区（至少创建未来 3 个月 + 当前月）
  - `rule_config.enter_pattern` / `exit_pattern` 字段类型 `JSONB`
  - `sys_role.permissions` 类型 `JSONB`
  - 索引：
    - `state_stream`: `(channel_id, ts DESC)`
    - `alarm`: `(site_id, alarm_time DESC, alarm_type)`
    - `work_session`: `(site_id, channel_id, start_time DESC)`
  - 初始数据：
    - `site`: JINQIAO(金桥库), KAIDI(凯迪库)
    - `sys_role`: SUPER_ADMIN(data_scope=ALL), SITE_ADMIN(data_scope=SITE_SPECIFIC), READONLY(data_scope=SITE_SPECIFIC)
    - `sys_user`: admin / BCrypt 密码（如 `admin123` 加密后），role=SUPER_ADMIN, site_id=NULL
    - `rule_config`: 为 PDI_FRONT / PDI_REAR / PDI_SLIDING 插入默认规则（进入 `[15,16,11]`，离开 `[11,16,15,[13,9]]`，require_vehicle=true，standard_duration 按附录 10.2 填入）

### Task 2.2 - 核心实体类（Entity）
- **Task ID**: `P2-T2`
- **描述**: 为所有表创建 MyBatis-Plus 实体类，使用 `@TableName`、`@TableId(type = IdType.AUTO)`、`@TableField`。
- **输出文件**:
  - `src/main/java/com/vdc/platform/entity/Site.java`
  - `src/main/java/com/vdc/platform/entity/EdgeBox.java`
  - `src/main/java/com/vdc/platform/entity/Channel.java`
  - `src/main/java/com/vdc/platform/entity/SysUser.java`
  - `src/main/java/com/vdc/platform/entity/SysRole.java`
  - `src/main/java/com/vdc/platform/entity/StateStream.java`
  - `src/main/java/com/vdc/platform/entity/WorkSession.java`
  - `src/main/java/com/vdc/platform/entity/Alarm.java`
  - `src/main/java/com/vdc/platform/entity/OperationLog.java`
  - `src/main/java/com/vdc/platform/entity/RuleConfig.java`
- **依赖**: `P2-T1`
- **关键实现要点**:
  - `StateStream.state_combination` 用 `Integer`
  - `RuleConfig` 中 `enterPattern` / `exitPattern` 字段使用 `com.baomidou.mybatisplus.extension.handlers.JacksonTypeJsonHandler` 或 `String` + 手动 JSON 转换；推荐用 `List<Object>` 配合 `@TableField(typeHandler = JacksonTypeJsonHandler.class)`
  - `SysRole.permissions` 同样使用 JSON 类型处理器映射为 `List<String>`
  - 所有时间字段使用 `java.time.LocalDateTime`
  - 布尔字段使用 `Boolean` 包装类型

### Task 2.3 - Mapper 接口与 XML（可选纯注解）
- **Task ID**: `P2-T3`
- **描述**: 为每个实体创建 Mapper 接口；复杂查询可预留 XML。
- **输出文件**:
  - `src/main/java/com/vdc/platform/mapper/SiteMapper.java`
  - `src/main/java/com/vdc/platform/mapper/EdgeBoxMapper.java`
  - `src/main/java/com/vdc/platform/mapper/ChannelMapper.java`
  - `src/main/java/com/vdc/platform/mapper/SysUserMapper.java`
  - `src/main/java/com/vdc/platform/mapper/SysRoleMapper.java`
  - `src/main/java/com/vdc/platform/mapper/StateStreamMapper.java`
  - `src/main/java/com/vdc/platform/mapper/WorkSessionMapper.java`
  - `src/main/java/com/vdc/platform/mapper/AlarmMapper.java`
  - `src/main/java/com/vdc/platform/mapper/OperationLogMapper.java`
  - `src/main/java/com/vdc/platform/mapper/RuleConfigMapper.java`
- **依赖**: `P2-T2`
- **关键实现要点**:
  - 所有 Mapper 继承 `BaseMapper<Entity>`
  - `StateStreamMapper` 增加自定义方法 `int batchInsert(List<StateStream> list);`
  - `AlarmMapper` 增加 `List<Alarm> selectAlarmPage(...)` 用于分页查询（可用 `@Select` 注解或 XML）

### Task 2.4 - Service 接口与基础实现（MyBatis-Plus IService）
- **Task ID**: `P2-T4`
- **描述**: 为每个实体创建 Service 接口（继承 `IService`）和基础实现类（继承 `ServiceImpl`）。
- **输出文件**:
  - `src/main/java/com/vdc/platform/service/ISiteService.java` + `impl/SiteServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/IEdgeBoxService.java` + `impl/EdgeBoxServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/IChannelService.java` + `impl/ChannelServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/ISysUserService.java` + `impl/SysUserServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/ISysRoleService.java` + `impl/SysRoleServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/IStateStreamService.java` + `impl/StateStreamServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/IWorkSessionService.java` + `impl/WorkSessionServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/IAlarmService.java` + `impl/AlarmServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/IOperationLogService.java` + `impl/OperationLogServiceImpl.java`
  - `src/main/java/com/vdc/platform/service/IRuleConfigService.java` + `impl/RuleConfigServiceImpl.java`
- **依赖**: `P2-T3`
- **关键实现要点**:
  - 仅做 CRUD 骨架，业务逻辑在后续 Phase 填充
  - `IStateStreamServiceImpl` 实现 `batchInsert` 调用 Mapper

---

## Phase 3: 接入网关（Gateway APIs）

**目标**：实现盒子推送的状态流、报警事件、心跳三个 HTTP 接口，完成身份校验、数据清洗、MinIO 图片上传、数据库写入、规则引擎投递。

### Task 3.1 - 网关安全与校验组件
- **Task ID**: `P3-T1`
- **描述**: 创建网关请求拦截器/过滤器，校验 `X-Box-Id` 与 `X-Box-Secret`、时间戳防重放（±5分钟）、IP 白名单（可选）、速率限制（可选）。
- **输出文件**:
  - `src/main/java/com/vdc/platform/gateway/filter/GatewayAuthFilter.java`
  - `src/main/java/com/vdc/platform/gateway/config/GatewayWebConfig.java`
- **依赖**: `P1-T5`, `P2-T4`
- **关键实现要点**:
  - `GatewayAuthFilter` 实现 `Filter` 或 `HandlerInterceptor`，仅匹配 `/gateway/**`
  - `X-Box-Secret` 与配置文件/数据库中 `edge_box` 表的预共享密钥比对（v1.1 文档未单独定义密钥字段，可在 `edge_box` 增加 `secret_key VARCHAR(128)`）
  - 时间戳校验：读取 body 中 `timestamp`（Unix 秒），与当前服务器时间偏差超过 300 秒则拒绝
  - 使用 `ContentCachingRequestWrapper` 解决 body 只能读一次的问题

### Task 3.2 - 网关 DTO
- **Task ID**: `P3-T2`
- **描述**: 定义状态流、报警、心跳的请求 DTO。
- **输出文件**:
  - `src/main/java/com/vdc/platform/gateway/dto/BoxStateRequest.java`
  - `src/main/java/com/vdc/platform/gateway/dto/BoxAlarmRequest.java`
  - `src/main/java/com/vdc/platform/gateway/dto/BoxHeartbeatRequest.java`
- **依赖**: 无
- **关键实现要点**:
  - `BoxStateRequest` 字段：`boxId`, `timestamp`, `channelId`, `states`（嵌套对象，含 4 个 boolean），`snapshot`（嵌套对象，含 base64 字符串）
  - `BoxAlarmRequest` 字段：`boxId`, `timestamp`, `channelId`, `eventType`, `eventDetectInfo`（Map/Object），`snapshot`
  - `BoxHeartbeatRequest` 字段：`boxId`, `timestamp`, `status`, `systemInfo`, `channels`

### Task 3.3 - MinIO 图片上传工具
- **Task ID**: `P3-T3`
- **描述**: 封装 MinIO 上传服务，支持 base64 图片解码后上传到指定 bucket，返回对象路径。
- **输出文件**:
  - `src/main/java/com/vdc/platform/common/MinioStorageService.java`
- **依赖**: `P1-T5`
- **关键实现要点**:
  - 方法签名：`String uploadBase64Image(String base64Data, String objectNamePrefix)`
  - 生成对象名格式：`{prefix}/{yyyyMMdd}/{UUID}.jpg`
  - 使用 `Base64.getDecoder()` 解码；若 data URI 含 `data:image/jpeg;base64,` 前缀需先剥离
  - 上传后返回完整路径或仅返回 object name（视前端需求，建议返回 object name）

### Task 3.4 - 状态流接收接口 `/gateway/v1/box/state`
- **Task ID**: `P3-T4`
- **描述**: 实现状态流接收 Controller 与 Service，完成四元组转组合编号、图片上传、写入 `state_stream`、投递规则引擎。
- **输出文件**:
  - `src/main/java/com/vdc/platform/gateway/controller/StateGatewayController.java`
  - `src/main/java/com/vdc/platform/gateway/service/StateIngestionService.java`
- **依赖**: `P3-T1`, `P3-T2`, `P3-T3`, `P2-T4`
- **关键实现要点**:
  - 四元组转组合编号算法（文档 10.1 节）：
    ```java
    int combo = 0;
    if (vehiclePresent) combo += 8;
    if (doorOpen) combo += 4;
    if (personPresent) combo += 2;
    if (personEnteringExiting) combo += 1;
    ```
  - 图片上传使用 `@Async` 线程池异步执行，避免阻塞响应；若上传失败，记录日志但不阻断状态流写入
  - 写入 `state_stream` 后，将记录投递到规则引擎的 `StateStreamProcessor`（调用 `ruleEngine.process(stateStream)`）
  - 批量写入优化：可在 Service 层做 100ms/100 条缓冲（使用 `LinkedBlockingQueue` + 定时 flush），但 v1 可先单条写入保证简单性

### Task 3.5 - 报警事件接收接口 `/gateway/v1/box/alarm`
- **Task ID**: `P3-T5`
- **描述**: 实现报警接收接口，解析 `event_type` 为 `SMOKE` 或 `PDI_UNQUALIFIED`，持久化到 `alarm` 表，并触发 Redis Pub/Sub。
- **输出文件**:
  - `src/main/java/com/vdc/platform/gateway/controller/AlarmGatewayController.java`
  - `src/main/java/com/vdc/platform/gateway/service/AlarmIngestionService.java`
- **依赖**: `P3-T1`, `P3-T2`, `P3-T3`, `P2-T4`
- **关键实现要点**:
  - `event_type` 映射：盒子推送 `SMOKE_DETECTED` -> 平台 `SMOKE`；其余 PDI 相关可映射为 `PDI_UNQUALIFIED`
  - 异步上传 snapshot 到 MinIO，替换为路径后写入 `alarm`
  - 写入完成后发布 Redis 消息：`redisTemplate.convertAndSend("vdc:alarm:realtime", alarmId)`
  - 返回 200 OK，body 可为空或简单确认 JSON

### Task 3.6 - 心跳接收接口 `/gateway/v1/box/heartbeat`
- **Task ID**: `P3-T6`
- **描述**: 实现心跳接口，更新 `edge_box` 和 `channel` 状态。
- **输出文件**:
  - `src/main/java/com/vdc/platform/gateway/controller/HeartbeatGatewayController.java`
  - `src/main/java/com/vdc/platform/gateway/service/HeartbeatService.java`
- **依赖**: `P3-T1`, `P3-T2`, `P2-T4`
- **关键实现要点**:
  - 根据 `boxId` 更新 `edge_box.last_heartbeat = now()`, `status = 1`, `cpu_usage`, `mem_usage`, `disk_usage`, `version`
  - 遍历 `channels` 列表，更新每个 `channel.status`（ONLINE->1, OFFLINE->0）
  - 使用乐观锁或简单 `UPDATE ... WHERE box_id = ?` 即可

---

## Phase 4: 权限服务（Auth Service）

**目标**：实现 JWT 认证、RBAC 权限控制、MyBatis-Plus 数据范围拦截器（站点隔离），并提供登录/登出/刷新/用户管理/角色管理接口。

### Task 4.1 - JWT 工具类与 Token 服务
- **Task ID**: `P4-T1`
- **描述**: 封装 JWT 生成、解析、校验逻辑，以及 Refresh Token 存储（Redis）。
- **输出文件**:
  - `src/main/java/com/vdc/platform/security/jwt/JwtUtil.java`
  - `src/main/java/com/vdc/platform/security/jwt/TokenService.java`
- **依赖**: `P1-T3`, `P1-T5`
- **关键实现要点**:
  - `JwtUtil` 方法：`generateAccessToken(UserDetails)`, `generateRefreshToken(...)`, `validateToken(String)`, `getUsernameFromToken(String)`, `getExpirationDate(String)`
  - 使用 `io.jsonwebtoken` 0.12.x API（`Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)`）
  - `TokenService` 将 refresh token 以 `refresh:{username}:{tokenJti}` 为 key 存入 Redis，TTL 7 天
  - Access Token  claims 中携带：`username`, `roleCode`, `siteId`, `permissions`

### Task 4.2 - Spring Security 配置（业务 API）
- **Task ID**: `P4-T2`
- **描述**: 配置 `SecurityFilterChain`，放行 Swagger、登录接口、网关接口；其余接口需 JWT 认证。
- **输出文件**:
  - `src/main/java/com/vdc/platform/security/config/SecurityConfig.java`
  - `src/main/java/com/vdc/platform/security/filter/JwtAuthenticationFilter.java`
- **依赖**: `P4-T1`
- **关键实现要点**:
  - `SecurityConfig` 中 `csrf.disable()`，配置 `sessionManagement.sessionCreationPolicy(STATELESS)`
  - 放行路径：`/swagger-ui/**`, `/v3/api-docs/**`, `/api/v1/auth/login`, `/gateway/**`, `/ws/**`
  - `JwtAuthenticationFilter` 继承 `OncePerRequestFilter`，从 `Authorization: Bearer <token>` 中提取 token，校验后构建 `UsernamePasswordAuthenticationToken` 放入 `SecurityContextHolder`
  - 自定义 `AuthenticationEntryPoint` 返回 401 JSON（使用 `ApiResult` 格式）

### Task 4.3 - UserDetailsService 与密码编码器
- **Task ID**: `P4-T3`
- **描述**: 实现 `UserDetailsService.loadUserByUsername`，从 `sys_user` + `sys_role` 表加载用户及权限。
- **输出文件**:
  - `src/main/java/com/vdc/platform/security/service/CustomUserDetailsService.java`
  - `src/main/java/com/vdc/platform/security/model/SecurityUser.java`（实现 `UserDetails`）
- **依赖**: `P2-T4`, `P4-T2`
- **关键实现要点**:
  - `SecurityUser` 除标准 `UserDetails` 字段外，额外携带 `userId`, `siteId`, `roleCode`, `dataScope`, `permissions`
  - 密码编码器使用 `BCryptPasswordEncoder`
  - 登录失败锁定：在 `CustomUserDetailsService` 或登录 Controller 中集成 Redis 计数器，key 为 `login_fail:{username}`，连续 5 次失败则抛出 `LockedException`

### Task 4.4 - MyBatis-Plus 数据权限拦截器
- **Task ID**: `P4-T4`
- **描述**: 实现 MyBatis-Plus `InnerInterceptor`，自动为涉及站点隔离的查询追加 `site_id = ?` 条件。
- **输出文件**:
  - `src/main/java/com/vdc/platform/security/interceptor/DataScopeInterceptor.java`
- **依赖**: `P4-T3`, `P1-T4`
- **关键实现要点**:
  - 拦截器在 `beforeQuery` 阶段执行
  - 从 `SecurityContextHolder` 获取当前 `SecurityUser`
  - 若 `roleCode == SUPER_ADMIN` 或 `dataScope == ALL`，直接放行
  - 否则，解析当前 SQL 的 `Table` 对象，判断表名是否在站点隔离名单（`edge_box`, `channel`, `work_session`, `alarm` 等），若是则追加 `WHERE site_id = #{siteId}`
  - 使用 `JsqlParser` 或 MyBatis-Plus 的 `TenantLineInnerInterceptor` 改造思路；推荐基于 `TenantLineInnerInterceptor` 自定义 `DataScopeInnerInterceptor`

### Task 4.5 - Auth Controller（登录/登出/刷新）
- **Task ID**: `P4-T5`
- **描述**: 实现 `/api/v1/auth/login`, `/logout`, `/refresh` 接口。
- **输出文件**:
  - `src/main/java/com/vdc/platform/controller/AuthController.java`
  - `src/main/java/com/vdc/platform/dto/LoginRequest.java`
  - `src/main/java/com/vdc/platform/dto/LoginResponse.java`
  - `src/main/java/com/vdc/platform/dto/RefreshTokenRequest.java`
- **依赖**: `P4-T1`, `P4-T3`
- **关键实现要点**:
  - `login`：校验用户名密码 -> 生成 access + refresh token -> 记录 `operation_log`（LOGIN, 成功）-> 返回 token 及用户信息
  - `logout`：将当前 access token 加入 Redis 黑名单（可选，TTL 设为 token 剩余有效期），并删除 refresh token
  - `refresh`：校验 refresh token -> 从 Redis 检查有效性 -> 重新签发 access token

### Task 4.6 - 用户管理 CRUD Controller
- **Task ID**: `P4-T6`
- **描述**: 实现 `/api/v1/users` 的增删改查及分页查询。
- **输出文件**:
  - `src/main/java/com/vdc/platform/controller/SysUserController.java`
  - `src/main/java/com/vdc/platform/dto/SysUserRequest.java`
  - `src/main/java/com/vdc/platform/dto/SysUserPageQuery.java`
- **依赖**: `P4-T4`, `P2-T4`
- **关键实现要点**:
  - 新增用户时密码使用 `BCryptPasswordEncoder` 加密
  - 只有 SUPER_ADMIN 可创建跨站点用户（siteId=null）；SITE_ADMIN 只能创建本站点用户
  - 修改密码需单独接口或字段处理
  - 删除用户记录 `operation_log`

### Task 4.7 - 角色管理 CRUD Controller
- **Task ID**: `P4-T7`
- **描述**: 实现 `/api/v1/roles` 的增删改查。
- **输出文件**:
  - `src/main/java/com/vdc/platform/controller/SysRoleController.java`
  - `src/main/java/com/vdc/platform/dto/SysRoleRequest.java`
- **依赖**: `P4-T4`, `P2-T4`
- **关键实现要点**:
  - `permissions` 接收 JSON 数组字符串，存储到 `sys_role.permissions`
  - 内置角色（SUPER_ADMIN, SITE_ADMIN, READONLY）不允许删除

---

## Phase 5: 规则引擎（Rule Engine）

**目标**：实现自定义 Java 状态机引擎，支持 PDI 进入序列 `15→16→11` 和离开序列 `11→16→15→(13/9)` 的模式匹配，以及抽烟检测报警接入。

### Task 5.1 - 规则引擎核心模型与状态上下文
- **Task ID**: `P5-T1`
- **描述**: 定义规则引擎内部使用的领域模型：状态上下文 `ChannelStateContext`、模式定义 `PatternDefinition`、事件枚举 `RuleEventType`。
- **输出文件**:
  - `src/main/java/com/vdc/platform/ruleengine/model/ChannelStateContext.java`
  - `src/main/java/com/vdc/platform/ruleengine/model/PatternDefinition.java`
  - `src/main/java/com/vdc/platform/ruleengine/model/RuleEvent.java`
  - `src/main/java/com/vdc/platform/ruleengine/enums/RuleEventType.java`
- **依赖**: `P2-T4`
- **关键实现要点**:
  - `ChannelStateContext` 字段：`channelId`, `window`（`Deque<StateStream>`，最大容量 64 或 128），`currentSession`（`WorkSession` 引用或 ID），`state`（枚举：IDLE, ENTERING, WORKING, EXITING）
  - `PatternDefinition` 字段：`enterPattern`（`List<Integer>`），`exitPattern`（`List<Object>`，支持单值或列表），`requireVehicle`
  - `RuleEvent` 字段：`eventType`（ENTER, EXIT, VIOLATION, SMOKE），`channelId`, `timestamp`, `workSessionId`, `description`

### Task 5.2 - 状态组合转换器
- **Task ID**: `P5-T2`
- **描述**: 实现四元组到组合编号的转换工具，以及无效组合过滤逻辑。
- **输出文件**:
  - `src/main/java/com/vdc/platform/ruleengine/util/StateCombinationUtil.java`
- **依赖**: 无
- **关键实现要点**:
  - `int compute(boolean vehicle, boolean door, boolean person, boolean entering)`
  - `boolean isInvalid(int combo)`：返回 true 当 combo 属于 {2,4,6,8,10,12,14}
  - `boolean shouldSkip(boolean vehiclePresent, RuleConfig config)`：若 `requireVehicle=true` 且 `vehiclePresent=false`，返回 true

### Task 5.3 - 模式匹配器（Pattern Matcher）
- **Task ID**: `P5-T3`
- **描述**: 实现模式匹配核心算法，扫描滑动窗口检测进入/离开序列。
- **输出文件**:
  - `src/main/java/com/vdc/platform/ruleengine/core/PatternMatcher.java`
- **依赖**: `P5-T1`, `P5-T2`
- **关键实现要点**:
  - 方法签名：`RuleEvent match(ChannelStateContext ctx, StateStream newState, RuleConfig config)`
  - 将新状态加入窗口尾部；若窗口超限，移除头部旧状态
  - 若 `requireVehicle=true` 且新状态 `vehicle_present=false`，可清空窗口或标记中断（根据业务需求，文档建议“强制中断”或“不进入”）
  - 进入匹配：在窗口中查找子序列等于 `[15, 16, 11]`；匹配成功后生成 `ENTER` 事件，清空窗口，切换上下文状态为 `WORKING`
  - 离开匹配：仅在 `WORKING` 状态下执行；查找子序列等于 `[11, 16, 15, 13]` 或 `[11, 16, 15, 9]`；匹配成功后生成 `EXIT` 事件，清空窗口，切换上下文状态为 `IDLE`
  - 为避免重复匹配，匹配成功后应重置窗口或移除已匹配部分

### Task 5.4 - 事件生成器与业务动作执行器
- **Task ID**: `P5-T4`
- **描述**: 根据匹配结果生成事件，并调用业务 Service 创建 `work_session` 或 `alarm`。
- **输出文件**:
  - `src/main/java/com/vdc/platform/ruleengine/core/EventGenerator.java`
  - `src/main/java/com/vdc/platform/ruleengine/core/RuleActionExecutor.java`
- **依赖**: `P5-T3`, `P2-T4`
- **关键实现要点**:
  - `EventGenerator`：将 `PatternMatcher` 的匹配结果转换为 `RuleEvent` 对象
  - `RuleActionExecutor`：
    - `onEnter(RuleEvent event)`：创建 `WorkSession`（status=0, start_time=event.timestamp, standard_duration 从 `rule_config` 读取）
    - `onExit(RuleEvent event)`：更新 `WorkSession`（end_time, actual_duration, deviation_pct, result=QUALIFIED/CRITICAL/UNQUALIFIED）。若 `actual_duration < standard_duration * critical_threshold_pct`，生成 `PDI_UNQUALIFIED` 报警并写入 `alarm` 表
    - `onSmoke(RuleEvent event)`：直接写入 `alarm` 表（alarm_type=SMOKE）
  - 所有业务写操作使用 `@Transactional`

### Task 5.5 - 状态流处理器与规则引擎入口
- **Task ID**: `P5-T5`
- **描述**: 实现 `StateStreamProcessor`，作为规则引擎的统一入口，维护每个 channel 的上下文缓存。
- **输出文件**:
  - `src/main/java/com/vdc/platform/ruleengine/core/StateStreamProcessor.java`
  - `src/main/java/com/vdc/platform/ruleengine/core/RuleEngine.java`
- **依赖**: `P5-T4`, `P3-T4`
- **关键实现要点**:
  - `StateStreamProcessor` 使用 `ConcurrentHashMap<String, ChannelStateContext>` 缓存各 channel 状态上下文
  - 接收 `StateStream` 记录后：
    1. 获取或创建对应 `ChannelStateContext`
    2. 查询 `rule_config` 获取该 channel 的算法类型对应的规则（通过 `channel.algorithm_type` 关联 `rule_config.channel_type`）
    3. 调用 `PatternMatcher.match(...)`
    4. 若有事件生成，调用 `RuleActionExecutor` 执行
  - 提供 `clearContext(String channelId)` 方法用于测试或 channel 下线清理
  - `RuleEngine` 作为 facade，暴露 `process(StateStream)` 方法供 Gateway 调用

### Task 5.6 - 规则配置热加载（可选但推荐）
- **Task ID**: `P5-T6`
- **描述**: 在 `RuleEngine` 中缓存 `RuleConfig`，并提供定时刷新或手动刷新接口。
- **输出文件**:
  - `src/main/java/com/vdc/platform/ruleengine/config/RuleConfigCache.java`
- **依赖**: `P5-T5`
- **关键实现要点**:
  - 使用 `ConcurrentHashMap<String, RuleConfig>` 缓存，key 为 `channelType`
  - `@Scheduled(fixedRate = 60000)` 每分钟从数据库刷新一次
  - 若规则未找到，使用默认规则（硬编码 fallback）

---

## Phase 6: 业务服务实现

**目标**：实现预警、报表、设备、系统配置四个业务模块的 Controller + Service + DTO。

### Task 6.1 - 预警服务（Alarm Service）
- **Task ID**: `P6-T1`
- **描述**: 实现报警查询、处理、导出、WebSocket 实时推送。
- **输出文件**:
  - `src/main/java/com/vdc/platform/controller/AlarmController.java`
  - `src/main/java/com/vdc/platform/dto/AlarmPageQuery.java`
  - `src/main/java/com/vdc/platform/dto/AlarmProcessRequest.java`
  - `src/main/java/com/vdc/platform/websocket/AlarmWebSocketController.java`
  - `src/main/java/com/vdc/platform/websocket/service/AlarmPushService.java`
- **依赖**: `P4-T4`, `P2-T4`, `P3-T5`
- **关键实现要点**:
  - `GET /api/v1/alarms`：分页查询，支持筛选 `alarmType`, `siteId`, `channelId`, `processStatus`, `startTime`, `endTime`
  - `GET /api/v1/alarms/{id}`：返回详情，含图片预签名 URL（MinIO `getPresignedObjectUrl`）
  - `PUT /api/v1/alarms/{id}/process`：更新 `process_status`, `processed_by`, `processed_at`
  - `POST /api/v1/alarms/export`：使用 EasyPOI 或 Apache POI 导出 Excel（可选，v1 可先 CSV）
  - WebSocket：使用 Spring STOMP，订阅路径 `/topic/alarms`。`AlarmPushService` 监听 Redis Pub/Sub 频道 `vdc:alarm:realtime`，收到消息后按用户 `siteId` 权限过滤，通过 `SimpMessagingTemplate.convertAndSendToUser` 或广播到 `/topic/alarms`
  - 图片下载 `GET /api/v1/alarms/{id}/images`：返回 MinIO 预签名 URL（target + scene 两张）

### Task 6.2 - 报表服务（Report Service）
- **Task ID**: `P6-T2`
- **描述**: 实现 PDI 作业报表查询、详情、导出。
- **输出文件**:
  - `src/main/java/com/vdc/platform/controller/ReportController.java`
  - `src/main/java/com/vdc/platform/dto/ReportPageQuery.java`
  - `src/main/java/com/vdc/platform/dto/ReportExportRequest.java`
- **依赖**: `P4-T4`, `P2-T4`
- **关键实现要点**:
  - `GET /api/v1/reports/pdi`：分页查询 `work_session`，支持 `siteId`, `channelId`, `result`, `startTime`, `endTime`
  - `GET /api/v1/reports/pdi/{id}`：返回 `WorkSession` 详情及 MinIO 图片预签名 URL
  - `POST /api/v1/reports/pdi/export`：导出 Excel/PDF。v1 优先 Excel（使用 EasyExcel 或 POI），包含字段：站点、通道、车辆信息、开始时间、结束时间、实际时长、标准工时、偏差、结果

### Task 6.3 - 设备服务（Device Service）
- **Task ID**: `P6-T3`
- **描述**: 实现盒子、通道的 CRUD、远程控制、在线统计。
- **输出文件**:
  - `src/main/java/com/vdc/platform/controller/DeviceController.java`
  - `src/main/java/com/vdc/platform/dto/EdgeBoxRequest.java`
  - `src/main/java/com/vdc/platform/dto/ChannelRequest.java`
  - `src/main/java/com/vdc/platform/service/impl/DeviceRemoteService.java`
- **依赖**: `P4-T4`, `P2-T4`, `P3-T6`
- **关键实现要点**:
  - `GET /api/v1/devices/boxes`：分页查询盒子列表
  - `POST /api/v1/devices/boxes`：新增盒子（需校验 `box_id` 唯一性，生成默认 `secret_key`）
  - `DELETE /api/v1/devices/boxes/{id}`：删除盒子及关联通道
  - `POST /api/v1/devices/boxes/{id}/reboot`：通过 `RestTemplate`/`WebClient` 调用盒子本地 API `http://{box_ip}:8080/api/v1/system/reboot`
  - `GET /api/v1/devices/channels`：分页查询通道
  - `PUT /api/v1/devices/channels/{id}`：更新通道名称、RTSP 地址等
  - `GET /api/v1/devices/channels/{id}/preview`：调用盒子本地 API `http://{box_ip}:8080/api/v1/stream/{channel_id}` 获取预览流地址并返回
  - `GET /api/v1/devices/monitor`：统计各站点盒子在线/离线数量、通道在线/离线数量（使用 MyBatis-Plus `selectMaps` 或自定义 SQL）
  - 心跳超时检测：Spring Scheduler 定时任务（每 30 秒）检查 `last_heartbeat < NOW() - INTERVAL '90 seconds'`，将超时盒子及通道标记为离线，并生成 `alarm` 记录（alarm_type 可定义为 `DEVICE_OFFLINE` 或复用现有类型；文档未明确类型，建议新增 `DEVICE_OFFLINE` 或暂不生成报警，仅标记离线）

### Task 6.4 - 系统配置服务（Config Service）
- **Task ID**: `P6-T4`
- **描述**: 实现规则配置、阈值配置、通用配置的查询与修改接口。
- **输出文件**:
  - `src/main/java/com/vdc/platform/controller/ConfigController.java`
  - `src/main/java/com/vdc/platform/dto/RuleConfigRequest.java`
  - `src/main/java/com/vdc/platform/dto/ThresholdConfigRequest.java`
  - `src/main/java/com/vdc/platform/dto/GeneralConfigRequest.java`
- **依赖**: `P4-T4`, `P2-T4`
- **关键实现要点**:
  - `GET/PUT /api/v1/config/rules`：查询/更新 `rule_config` 表；更新后触发 `RuleConfigCache` 刷新
  - `GET/PUT /api/v1/config/thresholds`：查询/更新各 `rule_config` 的 `standard_duration` 和 `critical_threshold_pct`
  - `GET/PUT /api/v1/config/general`：通用配置（如报警声音开关、数据留存天数、水印 Logo）。文档未定义通用配置表，需新增 `system_config` 表（`config_key`, `config_value`）或直接用 Redis 存储。推荐新增表 `system_config`：
    - `id`, `config_key VARCHAR(64)`, `config_value TEXT`, `description VARCHAR(256)`, `updated_at`
  - 修改配置记录 `operation_log`

---

## Phase 7: 测试、Swagger 验证、Docker 部署

**目标**：完成 JUnit 单元测试/集成测试、Swagger UI 验证、docker-compose 一键启动。

### Task 7.1 - 规则引擎 JUnit 测试
- **Task ID**: `P7-T1`
- **描述**: 编写规则引擎核心逻辑的单元测试，覆盖进入/离开序列匹配、无效组合过滤、车辆在场校验。
- **输出文件**:
  - `src/test/java/com/vdc/platform/ruleengine/PatternMatcherTest.java`
  - `src/test/java/com/vdc/platform/ruleengine/StateCombinationUtilTest.java`
  - `src/test/java/com/vdc/platform/ruleengine/RuleEngineIntegrationTest.java`
- **依赖**: `P5-T6`, `P2-T4`
- **关键实现要点**:
  - `PatternMatcherTest`：
    - 测试进入序列 `[15, 16, 11]` 匹配成功
    - 测试离开序列 `[11, 16, 15, 13]` 和 `[11, 16, 15, 9]` 匹配成功
    - 测试包含无效组合（如 2, 4, 6）的序列被正确过滤/跳过
    - 测试 `requireVehicle=true` 时无车状态被跳过
  - `RuleEngineIntegrationTest`：使用 `@SpringBootTest` + `@Transactional` + H2/Testcontainers（推荐 Testcontainers PostgreSQL），模拟完整状态流输入，验证 `work_session` 和 `alarm` 是否正确生成

### Task 7.2 - 权限服务 JUnit 测试
- **Task ID**: `P7-T2`
- **描述**: 编写 JWT 签发/校验、RBAC 站点隔离查询的单元测试和集成测试。
- **输出文件**:
  - `src/test/java/com/vdc/platform/auth/JwtUtilTest.java`
  - `src/test/java/com/vdc/platform/auth/DataScopeInterceptorTest.java`
  - `src/test/java/com/vdc/platform/auth/AuthControllerIntegrationTest.java`
- **依赖**: `P4-T7`, `P2-T4`
- **关键实现要点**:
  - `JwtUtilTest`：测试 token 生成、解析、过期校验、非法 token 拒绝
  - `DataScopeInterceptorTest`：模拟 SUPER_ADMIN 和普通 SITE_ADMIN 查询 `alarm` 表，验证 SQL 是否被正确改写
  - `AuthControllerIntegrationTest`：
    - 测试正确登录返回 token
    - 测试错误密码 5 次后账号锁定
    - 测试 refresh token 刷新 access token
    - 测试无 token 访问受保护接口返回 401

### Task 7.3 - 关键业务接口测试
- **Task ID**: `P7-T3`
- **描述**: 为报警查询、设备管理、报表查询编写集成测试。
- **输出文件**:
  - `src/test/java/com/vdc/platform/controller/AlarmControllerTest.java`
  - `src/test/java/com/vdc/platform/controller/DeviceControllerTest.java`
  - `src/test/java/com/vdc/platform/controller/ReportControllerTest.java`
- **依赖**: `P6-T4`, `P7-T2`
- **关键实现要点**:
  - 使用 `@SpringBootTest` + `MockMvc` + `@WithMockUser` 或自定义 JWT 注入
  - 每个测试类包含至少 3 个场景：列表查询成功、详情查询成功、无权限访问失败

### Task 7.4 - docker-compose 与 Dockerfile
- **Task ID**: `P7-T4`
- **描述**: 编写并验证 `docker-compose.yml`、`Dockerfile`、`nginx.conf`，确保 `docker-compose up -d` 可一键启动。
- **输出文件**:
  - `/Users/apple/Desktop/设计文件/通用设计文件/tongyongtest/vdc-platform/docker-compose.yml`
  - `/Users/apple/Desktop/设计文件/通用设计文件/tongyongtest/vdc-platform/Dockerfile`
  - `/Users/apple/Desktop/设计文件/通用设计文件/tongyongtest/vdc-platform/nginx.conf`
- **依赖**: `P1-T1` ~ `P7-T3`
- **关键实现要点**:
  - `docker-compose.yml` 包含：nginx、app（Spring Boot）、postgres、redis、minio
  - `postgres` 挂载 `src/main/resources/db/migration/` 到 `/docker-entrypoint-initdb.d`
  - `app` 镜像通过 Dockerfile 构建（multi-stage build：maven package -> jre runtime）
  - `app` 环境变量：`SPRING_PROFILES_ACTIVE=prod`, `DB_HOST=postgres`, `REDIS_HOST=redis`, `MINIO_HOST=minio`
  - `nginx.conf` 代理 `/api/`、`/gateway/`、`/ws/` 到 `app:8080`
  - 健康检查：`app` 容器可配置 `HEALTHCHECK --interval=30s --timeout=3s --start-period=60s CMD wget -qO- http://localhost:8080/actuator/health || exit 1`（需引入 `spring-boot-starter-actuator`）

### Task 7.5 - Swagger UI 验证与健康检查
- **Task ID**: `P7-T5`
- **描述**: 启动应用后访问 `http://localhost:8080/swagger-ui.html`，验证所有 Controller 接口已正确注册并展示。
- **输出文件**: 无（仅验证）
- **依赖**: `P7-T4`
- **关键实现要点**:
  - 确认 `@Tag` 和 `@Operation` 注解已添加到所有 Controller 类和方法
  - 确认 JWT 授权按钮可用（点击后输入 Bearer token，后续请求自动携带）
  - 确认 Gateway 接口分组可见

### Task 7.6 - 网关端到端验证
- **Task ID**: `P7-T6`
- **描述**: 使用 curl/Postman 模拟盒子推送，验证 `/gateway/v1/box/state`、`/alarm`、`/heartbeat` 能正确处理数据。
- **输出文件**: 无（仅验证）
- **依赖**: `P7-T4`
- **关键实现要点**:
  - 准备 3 组 curl 脚本：
    1. 心跳：更新盒子状态为在线
    2. 状态流：推送 15->16->11->... 序列，观察 `work_session` 生成
    3. 报警：推送 `SMOKE_DETECTED`，观察 `alarm` 表记录及 Redis 消息

---

## 依赖关系图（简化）

```
P1 (脚手架)
  │
  ├──▶ P2 (数据库层)
  │      │
  │      ├──▶ P3 (网关) ──▶ P5 (规则引擎)
  │      │
  │      ├──▶ P4 (权限)
  │      │
  │      └──▶ P6 (业务服务)
  │
  └──▶ P7 (测试/部署) ◀── 依赖 P3/P4/P5/P6 全部完成
```

---

## 执行建议（给 Executor Agents）

1. **并行工作流**：
   - 在 `P1` 和 `P2` 完成后，可并行启动 `P3`、`P4`、`P5`。
   - `P6` 各任务（报警、报表、设备、配置）彼此独立，可 4 路并行。
   - `P7` 必须在所有代码完成后串行执行。

2. **代码生成策略**：
   - 实体、Mapper、基础 Service 可使用 MyBatis-Plus 代码生成器快速产出，再手工调整 JSON 类型处理器和字段注释。
   - DTO 建议手写，配合 `jakarta.validation` 注解做参数校验。

3. **测试策略**：
   - 规则引擎优先写纯单元测试（不依赖 Spring 容器），确保模式匹配算法正确。
   - 权限拦截器和 Controller 使用 `@SpringBootTest` + Testcontainers PostgreSQL 做集成测试。

4. **风险点**：
   - `DataScopeInterceptor` 的 SQL 改写容易影响复杂联表查询，建议先在简单单表查询上验证，再推广到全项目。
   - `state_stream` 分区表在 Testcontainers 中需确保 PostgreSQL 15 镜像支持；若遇到语法问题，可降级为普通表 + 索引（部署时保留分区）。
   - MinIO 图片上传在本地测试时若不想启动真实 MinIO，可用 `@MockBean` 模拟 `MinioClient`。


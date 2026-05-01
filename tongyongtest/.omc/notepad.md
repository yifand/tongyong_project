# Notepad
<!-- Auto-managed by OMC. Manual edits preserved in MANUAL section. -->

## Priority Context
<!-- ALWAYS loaded. Keep under 500 chars. Critical discoveries only. -->
Phase B E2E backend orchestration: fix Flyway scope, create application-e2e.yml, docker-compose.test.yml, globalSetup.ts, globalTeardown.ts, DeviceRemoteService E2E stub, fix DeviceController.monitor() contract, add Flyway history verification.

## Working Memory
<!-- Session notes. Auto-pruned after 7 days. -->
### 2026-04-14 11:46
Writing autopilot implementation plan for VDC Spring Boot backend to .omc/plans/autopilot-impl.md
### 2026-04-14 12:16
Writing JUnit tests for auth service (P7-T2) of VDC platform backend. Need to create: JwtUtilTest, DataScopeInterceptorTest, AuthControllerIntegrationTest.
### 2026-04-14 12:16
JwtUtilTest: pure unit test, no Spring context. Need to instantiate JwtUtil and set secretKey via reflection or call init(). DataScopeInterceptorTest: mock MappedStatement, BoundSql, Executor. AuthControllerIntegrationTest: @SpringBootTest + MockMvc, need to mock AuthenticationManager, CustomUserDetailsService, JwtUtil, TokenService, IOperationLogService. Need to handle Redis mocking for login failure lock tests.
### 2026-04-15 11:33
Phase B plan: 1) pom.xml remove test scope from flyway-core. 2) application-e2e.yml with DB 5433, Redis 6380, MinIO 9001, fast JWT, Flyway enabled. 3) docker-compose.test.yml (pg 5433, redis 6380, minio 9001/9002). 4) globalSetup.ts start compose, wait health, build+launch backend, wait actuator/health, seed MinIO. 5) globalTeardown.ts stop backend+compose. 6) E2E DeviceRemoteService mock @Profile("e2e"). 7) Fix DeviceController.monitor() to return onlineBoxes, offlineBoxes, todayAlarms, todaySessions, alarmTrend fields. 8) Add Flyway history verification in globalSetup.
### 2026-04-15 11:36
Phase B files created/modified: pom.xml flyway scope fixed; application-e2e.yml created; docker-compose.test.yml created; globalSetup.ts and globalTeardown.ts created; DeviceRemoteServiceE2EStub.java created; DeviceController.java updated with monitor() contract fields and added alarmService/workSessionService dependencies. No bash/lsp diagnostics available in this environment, but code reviewed for correctness.


## 2026-04-14 11:46
Writing autopilot implementation plan for VDC Spring Boot backend to .omc/plans/autopilot-impl.md
### 2026-04-14 12:16
Writing JUnit tests for auth service (P7-T2) of VDC platform backend. Need to create: JwtUtilTest, DataScopeInterceptorTest, AuthControllerIntegrationTest.
### 2026-04-14 12:16
JwtUtilTest: pure unit test, no Spring context. Need to instantiate JwtUtil and set secretKey via reflection or call init(). DataScopeInterceptorTest: mock MappedStatement, BoundSql, Executor. AuthControllerIntegrationTest: @SpringBootTest + MockMvc, need to mock AuthenticationManager, CustomUserDetailsService, JwtUtil, TokenService, IOperationLogService. Need to handle Redis mocking for login failure lock tests.
### 2026-04-15 11:33
Phase B plan: 1) pom.xml remove test scope from flyway-core. 2) application-e2e.yml with DB 5433, Redis 6380, MinIO 9001, fast JWT, Flyway enabled. 3) docker-compose.test.yml (pg 5433, redis 6380, minio 9001/9002). 4) globalSetup.ts start compose, wait health, build+launch backend, wait actuator/health, seed MinIO. 5) globalTeardown.ts stop backend+compose. 6) E2E DeviceRemoteService mock @Profile("e2e"). 7) Fix DeviceController.monitor() to return onlineBoxes, offlineBoxes, todayAlarms, todaySessions, alarmTrend fields. 8) Add Flyway history verification in globalSetup.


## 2026-04-14 11:46
Writing autopilot implementation plan for VDC Spring Boot backend to .omc/plans/autopilot-impl.md
### 2026-04-14 12:16
Writing JUnit tests for auth service (P7-T2) of VDC platform backend. Need to create: JwtUtilTest, DataScopeInterceptorTest, AuthControllerIntegrationTest.
### 2026-04-14 12:16
JwtUtilTest: pure unit test, no Spring context. Need to instantiate JwtUtil and set secretKey via reflection or call init(). DataScopeInterceptorTest: mock MappedStatement, BoundSql, Executor. AuthControllerIntegrationTest: @SpringBootTest + MockMvc, need to mock AuthenticationManager, CustomUserDetailsService, JwtUtil, TokenService, IOperationLogService. Need to handle Redis mocking for login failure lock tests.


## 2026-04-14 11:46
Writing autopilot implementation plan for VDC Spring Boot backend to .omc/plans/autopilot-impl.md
### 2026-04-14 12:16
Writing JUnit tests for auth service (P7-T2) of VDC platform backend. Need to create: JwtUtilTest, DataScopeInterceptorTest, AuthControllerIntegrationTest.


## 2026-04-14 11:46
Writing autopilot implementation plan for VDC Spring Boot backend to .omc/plans/autopilot-impl.md


## MANUAL
<!-- User content. Never auto-pruned. -->


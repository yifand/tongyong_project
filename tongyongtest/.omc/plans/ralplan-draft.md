# RALPLAN Draft: VDC Frontend Playwright E2E Testing

## 1. Overview

Implement comprehensive Playwright E2E tests for `vdc-frontend` (Vue 3 + Vite + Element Plus), covering all 13 routes and major CRUD operations. The suite must auto-spin-up `vdc-platform` (Spring Boot 3) with PostgreSQL, Redis, and MinIO so `npx playwright test` passes from a clean checkout.

---

## 2. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Flyway not on runtime classpath** | High — backend starts with empty DB, all tests fail | Move `flyway-core` from `<scope>test</scope>` to default (compile) scope in `vdc-platform/pom.xml` before any test execution |
| **Dashboard API contract mismatch** | High — `DeviceController.monitor()` shape does not match `DashboardView.vue` expectations | Fix dashboard API to return `onlineBoxes`, `offlineBoxes`, `todayAlarms`, `todaySessions`, and `alarmTrend`; or downgrade assertions to "page loads and charts render" |
| **External device calls fail (502s)** | High — `rebootBox` and `getChannelPreview` endpoints hit real edge-box IPs | Stub `DeviceRemoteService` with an `@Profile("e2e")` mock bean that returns deterministic fake responses |
| **MinIO buckets empty on clean start** | Medium — alarm/PDI detail presigned image URLs 404 | Seed MinIO bucket and placeholder objects in `globalSetup` after backend health passes, or skip image-load assertions |
| **Realtime alarm WebSocket untestable** | Medium — no physical devices generate alarms | Limit `/alarm/realtime` tests to smoke-test scope (page load, empty state, toggle presence); defer deep message injection |
| **Element Plus selector brittleness** | Medium — dialogs/tables targeted by visible text/placeholder break easily | Incrementally add `data-testid` attributes to login form, sidebar menu, and primary CRUD dialogs |
| **Method security assumption unverified** | Low — `@PreAuthorize` is dead code today, but could be enabled later | Document assumption; verify admin user can access all endpoints during auth setup |

---

## 3. Concrete Implementation Steps

### Phase A: Infrastructure & Tooling (1–2 days)

| Step | Action | File(s) |
|------|--------|---------|
| A1 | Install Playwright and TypeScript support in `vdc-frontend` | `vdc-frontend/package.json` |
| A2 | Create `playwright.config.ts` with project defaults, baseURL `http://localhost:5173`, and `globalSetup`/`globalTeardown` hooks | `vdc-frontend/playwright.config.ts` |
| A3 | Add `.gitignore` entries for `test-results/`, `playwright-report/`, `playwright/.cache/` | `vdc-frontend/.gitignore` |
| A4 | Add a `test:e2e` npm script | `vdc-frontend/package.json` |
| A5 | Verify `npx playwright install chromium` is documented in README/setup notes | docs |
| A6 | **Add `data-testid` attributes to critical UI components** — login inputs, sidebar menu items, and primary CRUD dialogs (user add, box add, role add) | relevant `.vue` files |

**Details:**
- Use `@playwright/test`.
- Configure `fullyParallel: false` for this brownfield app (shared backend state) or use serial mode per file.
- Set `viewport: { width: 1280, height: 720 }`.

---

### Phase B: Backend Auto-Spin-Up Orchestration (3 days)

**Goal:** Running `npx playwright test` must start the backend and dependencies automatically.

**Pre-execution backend fixes (must complete before Phase B test integration):**
1. **Fix Flyway scope** in `vdc-platform/pom.xml` — remove `<scope>test</scope>` from `flyway-core`.
2. **Fix dashboard API** — align `DeviceController.monitor()` response fields with `DashboardView.vue` expectations.
3. **Stub `DeviceRemoteService`** — create an E2E-profile mock bean for `rebootBox` and `getChannelPreview`.

| Step | Action | File(s) |
|------|--------|---------|
| B1 | Create a Docker Compose file for test-time infrastructure: PostgreSQL 15, Redis, MinIO | `vdc-frontend/e2e/docker-compose.test.yml` |
| B2 | Create a Node.js `globalSetup.ts` that: (1) starts Docker Compose services, (2) waits for health, (3) starts the Spring Boot backend JAR or `mvn spring-boot:run`, (4) waits for `/actuator/health`, (5) **seeds MinIO bucket/objects** | `vdc-frontend/e2e/setup/globalSetup.ts` |
| B3 | Create matching `globalTeardown.ts` to stop backend and Docker Compose | `vdc-frontend/e2e/setup/globalTeardown.ts` |
| B4 | Provide a seed SQL/script or reuse Flyway migrations + `V2__init_data.sql` for baseline data | `vdc-frontend/e2e/fixtures/seed.sql` (optional wrapper) |
| B5 | **Verify backend schema initialization** — assert Flyway history table exists after startup | `globalSetup.ts` health-check sequence |

**Backend startup strategy:**
- Preferred: pre-build `vdc-platform` with `mvn clean package -DskipTests`, then run `java -jar target/vdc-platform-*.jar --spring.profiles.active=e2e`.
- Create `vdc-platform/src/main/resources/application-e2e.yml`:
  - Point DB/Redis/MinIO to Docker Compose mapped ports.
  - Enable Flyway.
  - Use a fast JWT expiration (e.g., 15 min access / 30 min refresh) to keep tests snappy.

**Docker Compose ports (suggested):**
- PostgreSQL: `5433:5432`
- Redis: `6380:6379`
- MinIO: `9001:9000` and `9002:9001` (console)

**Health checks:**
- Postgres: `pg_isready`
- Redis: `redis-cli ping`
- Backend: `curl -f http://localhost:8080/actuator/health`
- Schema: `SELECT 1 FROM flyway_schema_history LIMIT 1` against Postgres

---

### Phase C: Shared Test Fixtures & Auth Setup (1 day)

| Step | Action | File(s) |
|------|--------|---------|
| C1 | Create `auth.setup.ts` that logs in as `admin` / `admin123`, saves storage state to `playwright/.auth/admin.json` | `vdc-frontend/e2e/auth.setup.ts` |
| C2 | Create `fixtures/page-objects/LoginPage.ts` PO | `vdc-frontend/e2e/page-objects/LoginPage.ts` |
| C3 | Create base `fixtures/test.ts` extending `@playwright/test` with a `loggedInPage` fixture that reuses storage state | `vdc-frontend/e2e/fixtures/test.ts` |
| C4 | Add common selectors/utilities for Element Plus (e.g., `el-dialog`, `el-table`, `el-message`) | `vdc-frontend/e2e/utils/element-plus.ts` |

**Auth flow details (from `vdc-frontend/src/stores/auth.ts` + `LoginView.vue`):**
- POST `/api/v1/auth/login` returns `accessToken`, `refreshToken`.
- Token is stored in `localStorage` keys `vdc_token` and `vdc_refresh_token`.
- Playwright can either UI-login once and reuse storage state, or seed storage + cookies directly.

---

### Phase D: Page Object Models (1–2 days)

Create POs under `vdc-frontend/e2e/page-objects/` for stable selectors:

| PO | Covers |
|----|--------|
| `LoginPage.ts` | Username, password inputs, login button, title validation |
| `LayoutPage.ts` | Sidebar menu navigation, logout button, header user name |
| `DashboardPage.ts` | Stat cards, ECharts canvases |
| `RealtimeAlarmPage.ts` | Alarm grid, detail dialog, WebSocket indicator |
| `HistoryAlarmPage.ts` | Filters, table, export, detail dialog |
| `PdiReportPage.ts` | Filters, table, export, detail dialog |
| `BoxListPage.ts` | Filters, table, add dialog, delete confirmation |
| `ChannelListPage.ts` | Filters, table, edit dialog, preview dialog |
| `UserManagePage.ts` | Filters, table, add/edit dialog, delete confirmation |
| `RoleManagePage.ts` | Table, add/edit dialog, delete confirmation |
| `RuleConfigPage.ts` | Table, edit dialog, JSON inputs |
| `ThresholdConfigPage.ts` | Config form, save button |
| `GeneralConfigPage.ts` | Config form, save button |
| `OperationLogPage.ts` | Filters, table, pagination |

**Selector conventions:**
- Prefer `data-testid` where possible; fallback to visible text/placeholder for Element Plus components.
- Use `page.getByPlaceholder('请输入用户名')` for login.
- Use `page.getByRole('button', { name: '登录' })` for buttons.

---

### Phase E: Test Cases by Route (5 days)

All specs live under `vdc-frontend/e2e/specs/`. Use `test.describe.serial` per file to avoid cross-test backend state collisions.

#### E1. Login (`login.spec.ts`)
- `should display login page and title`
- `should login with valid credentials and redirect to dashboard`
- `should show error message with invalid credentials`

#### E2. Dashboard (`dashboard.spec.ts`)
- `should load dashboard with stat cards and charts`
- `should display device monitor data from API`

#### E3. Realtime Alarm (`alarm-realtime.spec.ts`) — Smoke-test scope only
- `should load realtime alarm page`
- `should display empty state when no alarms`
- `should show toggle switch for alarm filtering`
- *Deferred: deep WebSocket message injection*

#### E4. History Alarm (`alarm-history.spec.ts`)
- `should search alarms with filters`
- `should open alarm detail`
- `should export alarms (happy path)`

#### E5. PDI Report (`report-pdi.spec.ts`)
- `should search PDI reports with filters`
- `should open report detail dialog`
- `should export reports (happy path)`

#### E6. Device Boxes (`device-boxes.spec.ts`) — CRUD
- `should search boxes with site/status filters`
- `should create a new box`
- `should reboot a box`
- `should delete a box`

#### E7. Device Channels (`device-channels.spec.ts`) — CRUD
- `should search channels with filters`
- `should edit a channel`
- `should open preview dialog`

#### E8. System Users (`system-users.spec.ts`) — CRUD
- `should search users by username`
- `should create a new user`
- `should edit an existing user`
- `should delete a user`

#### E9. System Roles (`system-roles.spec.ts`) — CRUD
- `should list roles`
- `should create a new role`
- `should edit a role`
- `should delete a role`

#### E10. System Rules (`system-rules.spec.ts`) — CRUD
- `should list rule configs`
- `should edit a rule config and save`

#### E11. System Thresholds (`system-thresholds.spec.ts`) — CRUD
- `should load threshold configs`
- `should update a threshold value and save`

#### E12. System General (`system-general.spec.ts`) — CRUD
- `should load general configs`
- `should update a general config value and save`

#### E13. System Logs (`system-logs.spec.ts`)
- `should load operation logs`
- `should filter by operation type and date range`

#### E14. Navigation / Layout (`layout.spec.ts`)
- `should navigate through all sidebar menu items`
- `should logout and redirect to login`

---

### Phase F: Test Data Seeding Strategy (1 day)

| Approach | Details |
|----------|---------|
| Baseline | Flyway migrations `V1__init_schema.sql`, `V2__init_data.sql`, `V3__system_config.sql`, `V4__system_config_seed.sql` already seed sites, roles, admin user, and rule configs. |
| Per-test cleanup | For destructive tests (create/delete users, roles, boxes), either: (a) delete created entities in `test.afterEach`, or (b) use uniquely prefixed test data (e.g., `E2E_Box_${Date.now()}`) and delete afterward. |
| API seeding helper | Add `vdc-frontend/e2e/utils/api-seed.ts` exposing helper functions that hit backend REST directly with admin JWT to create prerequisite data (e.g., a box before creating a channel). |
| MinIO seeding | In `globalSetup`, after backend health passes, create the MinIO `vdc` bucket and upload a few small placeholder images so alarm/PDI detail dialogs can verify presigned URL loading. |

---

### Phase G: CI-Local Parity & Documentation (0.5 day)

- Document prerequisites: Docker Desktop (or Docker Engine), Java 17, Maven 3.9+, Node 20+.
- Document one-command run: `cd vdc-frontend && npm install && npx playwright test`.
- Add `vdc-frontend/e2e/README.md` explaining architecture, seeding, and troubleshooting.

---

## 4. RALPLAN-DR Summary

### Principles
1. **Clean-state execution:** Every run starts from zero; no reliance on a pre-running backend.
2. **Single-command UX:** `npx playwright test` must be sufficient (after host prerequisites are installed).
3. **Minimal backend changes, with acknowledged exceptions for E2E infrastructure:** A new `application-e2e.yml` profile, plus mandatory pre-execution fixes (Flyway scope, dashboard API contract, `DeviceRemoteService` E2E stub). No business-logic changes.
4. **Route coverage first, then CRUD depth:** Every route gets at least one passing test; major CRUD modules get full create/read/update/delete coverage.
5. **Reuse existing auth & data:** Use the built-in admin user and Flyway seeds to avoid redundant seed scripts, contingent on verifying admin authority compatibility.

### Decision Drivers
- The backend already uses Testcontainers for integration tests, but Playwright cannot easily orchestrate Testcontainers from Node. We need an out-of-process backend.
- The frontend has no existing tests, so we are establishing conventions (PO pattern, fixtures, serial execution).
- Element Plus components are best targeted via `data-testid` where possible, with fallback to placeholder/label/role selectors.

### Viable Options

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A. Docker Compose for everything** (frontend dev server + backend + deps) | Run Vite dev server, Spring Boot JAR, Postgres, Redis, MinIO all in Compose. | Fully isolated; identical to CI. | Slower feedback loop; harder to debug frontend during test authorship. |
| **B. Node globalSetup + Docker Compose for deps + local Spring Boot JAR** (recommended) | Playwright `globalSetup` starts Compose (DB/Redis/MinIO), then launches the backend JAR via `spawn`, then starts Vite dev server via `vite` programmatically or `npm run dev`. | Fastest local authorship; leverages existing Vite proxy; clean-state. | Requires Java/Maven on host machine; needs backend fixes before it works. |
| **C. Point tests at a persistent staging backend** | Skip backend spin-up; assume an environment is always running. | Zero infra code. | Violates the spec constraint of clean-state single-command execution. |
| **D. Testcontainers from Node via `testcontainers-node`** | Use Node Testcontainers library to start Postgres/Redis/MinIO, then launch backend. | Programmatic control. | More complex networking (backend JAR needs to reach Testcontainers); less mature than Java Testcontainers. |

### Recommendation
**Adopt Option B:** Node `globalSetup` + Docker Compose for data stores + local Spring Boot JAR + Vite dev server.

- It satisfies the single-command constraint.
- It minimizes changes to the backend (just a new `application-e2e.yml`, plus acknowledged infrastructure fixes).
- It keeps the frontend dev experience intact (Vite proxy already handles `/api` and `/ws`).
- It is the easiest to debug because developers can run the same stack manually.

---

## 5. Testable Acceptance Criteria

- [ ] `cd vdc-frontend && npm install && npx playwright test` completes with zero failures on a clean checkout (assuming Docker, Java 17, Maven are installed).
- [ ] Playwright report shows at least one passing test for every route:
  - `/login`, `/dashboard`, `/alarm/realtime`, `/alarm/history`, `/report/pdi`, `/device/boxes`, `/device/channels`, `/system/users`, `/system/roles`, `/system/rules`, `/system/thresholds`, `/system/general`, `/system/logs`
- [ ] Major CRUD operations are covered:
  - **Create:** users, roles, boxes
  - **Read:** all list/table pages load and display data
  - **Update:** users, roles, channels, rules, thresholds, general config
  - **Delete:** users, roles, boxes
  - **Search/Filter:** history alarms, PDI reports, operation logs, boxes, channels
- [ ] Auth flow is covered:
  - Login with valid/invalid credentials
  - Reusable authenticated state across specs
  - Logout redirects to login
- [ ] Backend auto-start is implemented:
  - Docker Compose services start before tests
  - Spring Boot JAR starts and passes health check
  - Flyway schema initialization is verified
  - MinIO bucket is seeded
  - Services tear down after tests
- [ ] Test data seeding is documented and reproducible (Flyway baseline + MinIO seeding + optional API seed helpers).

---

## 6. Verification Steps

1. **Clean checkout simulation:**
   ```bash
   cd /Users/apple/Desktop/设计文件/通用设计文件/tongyongtest/vdc-frontend
   rm -rf node_modules package-lock.json
   npm install
   npx playwright install chromium
   npx playwright test
   ```
   Expected: all tests pass, report generated.

2. **Route coverage audit:**
   ```bash
   npx playwright test --reporter=list
   ```
   Expected: 13+ spec files, each touching a distinct route.

3. **Backend health during run:**
   While tests are running, `curl http://localhost:8080/actuator/health` should return `{"status":"UP"}`.

4. **Schema initialization verification:**
   After backend health passes, query Postgres:
   ```bash
   psql -h localhost -p 5433 -U vdc -d vdc -c "SELECT version FROM flyway_schema_history LIMIT 1;"
   ```
   Expected: at least one row returned.

5. **MinIO seeding verification:**
   While tests are running, list the `vdc` bucket:
   ```bash
   mc alias set local http://localhost:9001 minioadmin minioadmin
   mc ls local/vdc
   ```
   Expected: placeholder objects present.

6. **Teardown verification:**
   After tests finish, `docker ps` should not show `vdc-e2e-postgres`, `vdc-e2e-redis`, or `vdc-e2e-minio` containers still running.

7. **CRUD trace review:**
   Open `playwright-report/index.html` and spot-check that create/update/delete dialogs were captured in traces for at least `system-users`, `system-roles`, and `device-boxes`.

---

## 7. Key File References

| File | Role |
|------|------|
| `vdc-frontend/src/router/index.ts` | Route definitions for coverage mapping |
| `vdc-frontend/src/stores/auth.ts` | Auth state (localStorage keys, token flow) |
| `vdc-frontend/src/api/request.ts` | Base API wrapper (Axios interceptors, 401 handling) |
| `vdc-frontend/src/views/LoginView.vue` | Login UI selectors |
| `vdc-frontend/src/views/LayoutView.vue` | Sidebar navigation selectors |
| `vdc-frontend/vite.config.ts` | Dev server proxy (`/api`, `/ws`) |
| `vdc-platform/src/test/java/com/vdc/platform/AbstractIntegrationTest.java` | Existing Testcontainers pattern (reference only) |
| `vdc-platform/src/main/resources/db/migration/V2__init_data.sql` | Baseline seed data (admin user, roles, sites, rules) |
| `vdc-platform/src/main/resources/application-dev.yml` | Dev config template for `application-e2e.yml` |
| `vdc-platform/pom.xml` | Build config for producing the runnable JAR; must fix Flyway scope |

---

## 8. Estimated Timeline

- Phase A (Tooling + `data-testid`): 1–2 days
- Phase B (Orchestration + backend fixes): 3 days
- Phase C (Fixtures/Auth): 1 day
- Phase D (Page Objects): 1–2 days
- Phase E (Test Cases): 5 days
- Phase F (Seeding/Helpers): 1 day
- Phase G (Docs/Verification): 0.5 day

**Total: ~12 days** (can be compressed with parallel work on POs and specs once backend fixes land).

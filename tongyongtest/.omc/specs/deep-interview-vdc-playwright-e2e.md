# Deep Interview Spec: VDC Frontend Playwright E2E Testing

## Metadata
- Interview ID: 56B6437A-6230-4F80-8805-01D5AEB69943
- Rounds: 3
- Final Ambiguity Score: 18.5%
- Type: brownfield
- Generated: 2026-04-15
- Threshold: 0.2
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.85 | 0.35 | 0.2975 |
| Constraint Clarity | 0.75 | 0.25 | 0.1875 |
| Success Criteria Clarity | 0.90 | 0.25 | 0.2250 |
| Context Clarity | 0.70 | 0.15 | 0.1050 |
| **Total Clarity** | | | **0.8150** |
| **Ambiguity** | | | **0.1850** |

## Goal
Implement comprehensive Playwright end-to-end tests for the `vdc-frontend` Vue 3 application, covering all 11+ functional routes and their major CRUD operations. The test suite must be executable locally with `npx playwright test` from a clean state, automatically spinning up the `vdc-platform` Spring Boot backend and its dependencies (PostgreSQL, Redis, MinIO) as needed.

## Constraints
- Tests must run from a clean state with a single command (`npx playwright test`)
- The `vdc-platform` backend and its dependencies must auto-start for the test suite
- Every frontend route must have at least one passing E2E test
- Every major CRUD operation must be covered
- The frontend codebase uses Vue 3 + Vite + Element Plus + Pinia + Axios + SockJS/STOMP
- The backend codebase uses Spring Boot 3 + Java 17 + MyBatis-Plus + PostgreSQL + Redis + MinIO + JWT auth

## Non-Goals
- Performance testing or load testing
- Mobile viewport testing (H5)
- Cross-browser matrix beyond Chromium (unless Playwright default)
- Exhaustive form validation edge-case testing beyond major happy-path + basic error flows
- Modification of backend business logic or existing backend integration tests

## Acceptance Criteria
- [ ] Playwright is installed and configured in `vdc-frontend`
- [ ] Every route (`/login`, `/dashboard`, `/alarm/realtime`, `/alarm/history`, `/report/pdi`, `/device/boxes`, `/device/channels`, `/system/users`, `/system/roles`, `/system/rules`, `/system/thresholds`, `/system/general`, `/system/logs`) has at least one passing E2E test
- [ ] Major CRUD operations (create, read, update, delete/search) are covered across relevant modules
- [ ] The test suite can spin up the backend and its dependencies automatically (e.g., via Docker Compose or Testcontainers orchestration)
- [ ] `npx playwright test` passes locally from a clean checkout
- [ ] Test data seeding strategy is documented and reproducible
- [ ] Login/auth flow is covered and reusable across tests

## Assumptions Exposed & Resolved
| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| "Comprehensive" means exhaustive UI testing | Asked for scope clarification (Option A/B/C) | Chose Option B (exhaustive page/form/table/dialog coverage) but paired with Option A acceptance criteria (route-level + CRUD + local run) |
| Success criteria are self-evident | Asked for concrete definition of done | Defined as: every route has a passing test, major CRUD covered, `npx playwright test` passes locally |
| Backend handling is assumed | Asked how tests should deal with backend dependency | Chose Option B: backend auto-spins up for clean-state test runs |

## Technical Context
### vdc-frontend
- Framework: Vue 3 + TypeScript + Vite
- UI Library: Element Plus
- State: Pinia
- HTTP: Axios (`/api/v1/*`)
- Real-time: SockJS + STOMP (`/ws`)
- Routes: 11+ functional pages under `src/views/`
- No existing tests

### vdc-platform
- Framework: Spring Boot 3.2.5 + Java 17
- ORM: MyBatis-Plus
- Database: PostgreSQL 15 (Flyway migrations)
- Cache: Redis
- Storage: MinIO
- Auth: JWT (access + refresh tokens)
- Existing tests: Integration tests with Testcontainers (`AbstractIntegrationTest`)

### Key Files
- Frontend routes: `vdc-frontend/src/router/index.ts`
- Frontend auth store: `vdc-frontend/src/stores/auth.ts`
- Frontend API wrapper: `vdc-frontend/src/api/request.ts`
- Backend migrations: `vdc-platform/src/main/resources/db/migration/`
- Backend integration test base: `vdc-platform/src/test/java/com/vdc/platform/AbstractIntegrationTest.java`

## Ontology (Key Entities)
| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| vdc-frontend | core domain | Vue 3, TypeScript, Vite, Element Plus, Pinia, Axios, ECharts, SockJS/STOMP, 11+ routes | tested by Playwright E2E tests; depends on vdc-platform |
| vdc-platform | core domain | Spring Boot 3, Java 17, MyBatis-Plus, PostgreSQL, Redis, MinIO, JWT auth, WebSocket | serves backend for vdc-frontend; has integration tests with Testcontainers |
| Playwright E2E tests | supporting | comprehensive full-process, npx playwright test, route coverage, CRUD coverage | tests vdc-frontend; runs in Clean-state test environment |
| Route module | core domain | login, dashboard, alarm/realtime, alarm/history, report/pdi, device/boxes, device/channels, system/users, system/roles, system/rules, system/thresholds, system/general, system/logs | belongs to vdc-frontend; covered by Playwright E2E tests |
| Clean-state test environment | supporting | auto_spin_up_requirement, PostgreSQL, Redis, MinIO | enables Playwright E2E tests; orchestrates vdc-platform |
| Acceptance criteria set | artifact | every route tested, CRUD covered, local run passes | defines success for Playwright E2E tests |
| Testcontainers | supporting | integration tests | used by vdc-platform integration tests |
| 金桥基地VDC业务平台产品架构设计文档 | external system | architecture design document | referenced as blueprint for testing scope |

## Ontology Convergence
| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|-----------------|
| 1 | 5 | 5 | - | - | N/A |
| 2 | 7 | 2 | 0 | 5 | 71.4% |
| 3 | 8 | 1 | 0 | 7 | 87.5% |

## Interview Transcript
<details>
<summary>Full Q&A (3 rounds)</summary>

### Round 1
**Q:** The frontend has 11+ functional routes... When you say "comprehensive full-process testing," do you mean:
**A:** B (Exhaustive coverage of every page, every form validation, every table operation, and every dialog across all 11+ modules)
**Ambiguity:** 46% (Goal: 0.75, Constraints: 0.4, Criteria: 0.35, Context: 0.6)

### Round 2
**Q:** To turn "exhaustive coverage" into a verifiable definition of done, which of the following acceptance criteria sets matches your intent?
**A:** A (Every frontend route has at least one passing E2E test, every major CRUD operation is covered, and the full suite runs successfully with `npx playwright test` locally)
**Ambiguity:** 25% (Goal: 0.85, Constraints: 0.55, Criteria: 0.8, Context: 0.75)

### Round 3
**Q:** The vdc-platform backend is required for most frontend pages... For the Playwright test environment, which constraint do you want to enforce?
**A:** B (The test suite should automatically spin up the backend and its dependencies so `npx playwright test` works from a clean state)
**Ambiguity:** 18.5% (Goal: 0.85, Constraints: 0.75, Criteria: 0.9, Context: 0.7)

</details>

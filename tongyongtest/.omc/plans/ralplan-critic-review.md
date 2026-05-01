# RALPLAN Critic Review: VDC Frontend Playwright E2E Testing

**Reviewer:** Critic Agent (Consensus Mode)  
**Date:** 2026-04-15  
**Plan:** `/Users/apple/Desktop/设计文件/通用设计文件/tongyongtest/.omc/plans/ralplan-draft.md`  
**Architect Review:** `/Users/apple/Desktop/设计文件/通用设计文件/tongyongtest/.omc/plans/ralplan-architect-review.md`  
**Spec:** `/Users/apple/Desktop/设计文件/通用设计文件/tongyongtest/.omc/specs/deep-interview-vdc-playwright-e2e.md`

---

## 1. Evaluation Summary

| Criterion | Assessment | Notes |
|-----------|------------|-------|
| **1. Principles / Option Consistency** | **STRONG** | Option B is consistent with all principles. Principle 3 is now honestly framed as "minimal backend changes, with acknowledged exceptions for E2E infrastructure." |
| **2. Risks & Mitigations** | **STRONG** | Section 2 contains a comprehensive risk table covering Flyway scope, dashboard API mismatch, device-service stubbing, MinIO seeding, WebSocket scope, selector brittleness, and method security assumptions. |
| **3. Acceptance Criteria Testability** | **STRONG** | Section 5 acceptance criteria are concrete, binary, and map directly to the spec. Each criterion has a clear pass/fail state. |
| **4. Verification Steps Sufficiency** | **STRONG** | Section 6 covers clean checkout, route audit, backend health, schema initialization, MinIO seeding, teardown, and CRUD trace review. All architect-identified blockers now have corresponding verification steps. |
| **5. Architect Blockers & Tradeoffs** | **FULLY ADDRESSED** | Every hard blocker from the architect review is explicitly acknowledged and front-loaded in the plan as a pre-execution task or mitigation. |

---

## 2. Detailed Findings

### Finding A: Risks & Mitigations — Resolved
The revised plan adds a full "Risks & Mitigations" table in Section 2. It correctly surfaces:
- Flyway runtime failure (empty DB) and the `pom.xml` scope fix.
- Dashboard API contract mismatch and the dual mitigation paths.
- External device call failures and the `@Profile("e2e")` mock bean strategy.
- MinIO empty-bucket 404s and the `globalSetup` seeding approach.
- WebSocket untestability and the smoke-test scope limitation.
- Element Plus selector brittleness and the `data-testid` mitigation.
- Method security assumption and the verification/documentation approach.

**Verdict:** Adequate.

### Finding B: Backend Fix Scope — Resolved
Principle 3 has been revised to "minimal backend changes, with acknowledged exceptions for E2E infrastructure." Phase B now explicitly lists the three mandatory pre-execution backend fixes:
1. Fix Flyway scope in `vdc-platform/pom.xml`.
2. Fix dashboard API to align `DeviceController.monitor()` with `DashboardView.vue` expectations.
3. Stub `DeviceRemoteService` with an `@Profile("e2e")` mock bean.

These are framed as infrastructure fixes, not business-logic changes, which is consistent with the spec's non-goals.

**Verdict:** Adequate.

### Finding C: Timeline — Resolved
The timeline has been expanded from ~9.5 days to ~12 days:
- Phase B increased from 2 days to 3 days (orchestration + backend fixes).
- Phase E increased from 3–4 days to 5 days (test cases, accounting for selector brittleness and API fixes).

This is a realistic estimate for a brownfield codebase with zero existing test infrastructure.

**Verdict:** Adequate.

### Finding D: WebSocket Strategy — Resolved
Phase E3 now explicitly limits `/alarm/realtime` tests to smoke-test scope:
- Page load
- Empty state display
- Toggle switch presence

Deep WebSocket message injection is deferred to a follow-up task.

**Verdict:** Adequate.

### Finding E: MinIO Seeding — Resolved
Step B2 in Phase B now states that `globalSetup.ts` must "(5) seeds MinIO bucket/objects" after backend health passes. Phase F also documents MinIO seeding as part of the test data strategy.

**Verdict:** Adequate.

### Finding F: Selector Strategy — Resolved
Step A6 explicitly adds `data-testid` attributes to:
- Login inputs
- Sidebar menu items
- Primary CRUD dialogs (user add, box add, role add)

This directly implements the architect's recommendation for incremental but strict `data-testid` tagging.

**Verdict:** Adequate.

### Finding G: Schema Initialization Verification — Resolved
Step B5 adds verification that the Flyway history table exists after startup. Section 6 (Verification Steps) includes a SQL health check:
```bash
psql -h localhost -p 5433 -U vdc -d vdc -c "SELECT version FROM flyway_schema_history LIMIT 1;"
```

**Verdict:** Adequate.

### Finding H: Method Security Assumption — Resolved
The Risks table acknowledges method security as a low-risk assumption and states that "admin user authority compatibility will be verified during auth setup."

**Verdict:** Adequate.

---

## 3. Residual Concerns (Non-Blockers)

1. **Host toolchain burden.** The plan still requires Java 17, Maven 3.9+, and Docker on the host. This is acceptable because the spec explicitly demands clean-state auto-spin-up, and no viable alternative eliminates all host dependencies.

2. **Maven build invocation vs. pre-built JAR.** The plan lists "pre-build `vdc-platform` with `mvn clean package -DskipTests`" as the preferred path but does not explicitly state whether `globalSetup` should run Maven or assume a pre-built JAR. The architect recommends the latter for faster feedback loops. This is an implementation detail, not a plan-level flaw.

3. **CRUD depth is still aggressive.** Phase E covers full CRUD across 7 modules. While the 5-day estimate is more realistic, the executor should still prioritize route-level smoke tests before deepening any single module.

---

## 4. Synthesis

The revised plan has evolved from an optimistically scoped draft into a realistic, executable roadmap. All previously identified blockers are now explicitly acknowledged and front-loaded. The Risks & Mitigations section is comprehensive. The timeline accounts for brownfield complexity. The acceptance criteria and verification steps are concrete and testable. The architect's STRONG verdict confirms that the plan is technically sound.

---

## 5. Verdict

**APPROVE**

The plan is ready for execution. The executor should proceed with the following guidance:
- Do not let `globalSetup` invoke Maven; pre-build the JAR once and launch it directly.
- Prioritize route-level smoke tests before investing in deep CRUD for any module.
- Keep the `data-testid` tagging minimal but strict: login form, sidebar menu items, and the primary "添加"/"确定"/"取消" buttons in the three most critical dialogs.

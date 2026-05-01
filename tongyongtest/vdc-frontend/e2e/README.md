# VDC Frontend E2E Tests

This directory contains the end-to-end (E2E) test suite for the VDC Frontend application.

## Architecture

The E2E testing stack is built on the following components:

- **Playwright**: The core test automation framework. It handles browser interaction, assertions, and test execution.
- **Global Setup (`setup/globalSetup.ts`)**: A Node.js script executed once before all tests. It orchestrates the entire test environment by:
  1. Starting Docker Compose services (PostgreSQL, Redis, MinIO) via `docker-compose.test.yml`.
  2. Waiting for PostgreSQL and Redis to be ready.
  3. Building the Spring Boot backend JAR from the `vdc-platform` project.
  4. Launching the backend process with the `e2e` Spring profile.
  5. Waiting for the backend `/actuator/health` endpoint to return OK.
  6. Verifying Flyway database migrations have been applied.
  7. Seeding MinIO with a placeholder object.
- **Global Teardown (`setup/globalTeardown.ts`)**: A Node.js script executed once after all tests. It stops the backend Java process and tears down the Docker Compose services.
- **Docker Compose (`docker-compose.test.yml`)**: Defines the test-scoped infrastructure services:
  - `postgres` (port `5433`)
  - `redis` (port `6380`)
  - `minio` (ports `9001` / `9002`)
- **Spring Boot Backend**: The `vdc-platform` backend is compiled and started directly from its JAR during global setup, connecting to the Dockerized services.
- **Page Objects (`page-objects/`)**: A collection of TypeScript classes encapsulating selectors and interactions for each application page, promoting maintainability and reducing selector brittleness.
- **Auth Setup (`auth.setup.ts`)**: A dedicated Playwright setup project test that logs in as `admin` and persists the browser storage state for reuse across tests.
- **Custom Fixtures (`fixtures/test.ts`)**: Extends Playwright's `test` fixture to provide a `loggedInPage`, which automatically uses the saved admin storage state.

## Prerequisites

Before running the E2E tests, ensure the following tools are installed and available in your `PATH`:

- **Docker** (with Docker Compose v2)
- **Java 17+**
- **Maven 3.9+** (or the `./mvnw` wrapper in `vdc-platform`)
- **Node.js 20+**

## How to Run Tests

All commands should be run from the `vdc-frontend` directory.

### One-command execution

The simplest way to run the full E2E suite is:

```bash
npx playwright test
```

This single command triggers the global setup, runs all tests, and then triggers the global teardown.

### Run with UI mode (for debugging)

```bash
npx playwright test --ui
```

### Run a specific test file

```bash
npx playwright test e2e/specs/login.spec.ts
```

### View the HTML report

```bash
npx playwright show-report
```

## Project Structure

```
vdc-frontend/
├── playwright.config.ts          # Playwright configuration
├── e2e/
│   ├── README.md                 # This file
│   ├── auth.setup.ts             # Authentication setup project
│   ├── docker-compose.test.yml   # Test infrastructure services
│   ├── setup/
│   │   ├── globalSetup.ts        # Environment bootstrap
│   │   └── globalTeardown.ts     # Environment cleanup
│   ├── fixtures/
│   │   └── test.ts               # Custom Playwright fixtures
│   ├── page-objects/             # Page Object Models (13+)
│   │   ├── LoginPage.ts
│   │   ├── LayoutPage.ts
│   │   ├── DashboardPage.ts
│   │   ├── RealtimeAlarmPage.ts
│   │   ├── HistoryAlarmPage.ts
│   │   ├── PdiReportPage.ts
│   │   ├── BoxListPage.ts
│   │   ├── ChannelListPage.ts
│   │   ├── UserManagePage.ts
│   │   ├── RoleManagePage.ts
│   │   ├── RuleConfigPage.ts
│   │   ├── ThresholdConfigPage.ts
│   │   ├── GeneralConfigPage.ts
│   │   └── OperationLogPage.ts
│   ├── specs/                    # Test specifications
│   └── utils/
│       └── element-plus.ts       # Helpers for Element Plus UI components
```

## Troubleshooting

### Port conflicts

The global setup and Docker Compose use the following local ports:

- `5173`: Vite dev server (frontend)
- `8080`: Spring Boot backend
- `5433`: PostgreSQL test container
- `6380`: Redis test container
- `9001` / `9002`: MinIO test container

If any of these ports are already in use, the setup will fail. Free the ports or modify the configuration in `playwright.config.ts` and `docker-compose.test.yml` accordingly.

### Backend startup issues

- Check `e2e/logs/backend.log` for Spring Boot startup errors.
- Ensure Java 17 is active (`java -version`).
- Ensure Maven can build `vdc-platform` successfully by running `./mvnw clean package -DskipTests` manually in the `vdc-platform` directory.

### Selector brittleness

If a test fails due to a missing element:

1. Run the test in UI mode (`npx playwright test --ui`) to inspect the DOM at the failure point.
2. Update the corresponding Page Object in `e2e/page-objects/` with a more stable selector (prefer `data-testid`, `role`, or text-based locators over CSS class names).
3. For Element Plus components (tables, dialogs, dropdowns), use the helpers in `e2e/utils/element-plus.ts`.

### Docker Compose failures

- Ensure Docker is running.
- If containers fail to start, try cleaning up manually:
  ```bash
  docker compose -f e2e/docker-compose.test.yml down -v
  ```
- On macOS, ensure Rosetta 2 is installed if you are running on Apple Silicon and using x86-based images.

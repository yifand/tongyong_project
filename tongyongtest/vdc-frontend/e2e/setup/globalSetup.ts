import { execSync, spawn, execFileSync } from 'child_process'
import { createWriteStream, writeFileSync, readdirSync, mkdirSync } from 'fs'
import { request } from '@playwright/test'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const COMPOSE_FILE = path.resolve(__dirname, '../docker-compose.test.yml')
const BACKEND_DIR = path.resolve(__dirname, '../../../vdc-platform')
const BACKEND_JAR_GLOB = path.join(BACKEND_DIR, 'target/vdc-platform-*.jar')
const LOG_DIR = path.resolve(__dirname, '../logs')
const FRONTEND_DIR = path.resolve(__dirname, '../..')

let backendProcess: ReturnType<typeof spawn> | null = null
let frontendProcess: ReturnType<typeof spawn> | null = null

function log(msg: string) {
  // eslint-disable-next-line no-console
  console.log(`[globalSetup] ${msg}`)
}

async function waitForHealth(url: string, options: { timeout?: number; interval?: number } = {}) {
  const { timeout = 120000, interval = 2000 } = options
  const start = Date.now()
  const ctx = await request.newContext()
  while (Date.now() - start < timeout) {
    try {
      const res = await ctx.get(url, { timeout: interval })
      if (res.ok()) {
        await ctx.dispose()
        return
      }
    } catch {
      // ignore
    }
    await new Promise((r) => setTimeout(r, interval))
  }
  await ctx.dispose()
  throw new Error(`Health check failed for ${url}`)
}

async function waitForPostgres() {
  const start = Date.now()
  const timeout = 60000
  while (Date.now() - start < timeout) {
    try {
      execFileSync('docker', ['exec', 'vdc-postgres-test', 'pg_isready', '-U', 'vdc', '-d', 'vdc_platform'], { stdio: 'pipe' })
      return
    } catch {
      await new Promise((r) => setTimeout(r, 2000))
    }
  }
  throw new Error('PostgreSQL did not become ready in time')
}

async function waitForRedis() {
  const start = Date.now()
  const timeout = 60000
  while (Date.now() - start < timeout) {
    try {
      execFileSync('docker', ['exec', 'vdc-redis-test', 'redis-cli', 'ping'], { stdio: 'pipe' })
      return
    } catch {
      await new Promise((r) => setTimeout(r, 2000))
    }
  }
  throw new Error('Redis did not become ready in time')
}

async function seedMinio() {
  log('Seeding MinIO bucket...')
  const bucket = 'vdc-bucket'
  const accessKey = process.env.MINIO_ROOT_USER || 'vdcadmin'
  const secretKey = process.env.MINIO_ROOT_PASSWORD || 'vdcadmin123'

  try {
    execFileSync('docker', ['exec', 'vdc-minio-test', 'mc', 'alias', 'set', 'local', 'http://localhost:9000', accessKey, secretKey], { stdio: 'pipe' })
    execFileSync('docker', ['exec', 'vdc-minio-test', 'mc', 'mb', `local/${bucket}`, '--ignore-existing'], { stdio: 'pipe' })
    execFileSync('docker', ['exec', 'vdc-minio-test', 'mc', 'anonymous', 'set', 'download', `local/${bucket}`], { stdio: 'pipe' })
  } catch {
    // ignore
  }

  const placeholder = Buffer.from(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==',
    'base64'
  )
  const tmpFile = path.resolve(__dirname, '../.minio-placeholder.png')
  writeFileSync(tmpFile, placeholder)
  try {
    execFileSync('docker', ['cp', tmpFile, 'vdc-minio-test:/tmp/placeholder.png'], { stdio: 'pipe' })
    execFileSync('docker', ['exec', 'vdc-minio-test', 'mc', 'cp', '/tmp/placeholder.png', `local/${bucket}/placeholder.png`], { stdio: 'pipe' })
  } catch {
    // ignore
  }
}

async function verifyFlyway() {
  log('Verifying Flyway schema history...')
  const result = execFileSync(
    'docker',
    ['exec', 'vdc-postgres-test', 'psql', '-U', 'vdc', '-d', 'vdc_platform', '-t', '-c', 'SELECT COUNT(*) FROM flyway_schema_history;'],
    { encoding: 'utf-8', stdio: 'pipe' }
  )
  const count = parseInt(result.trim(), 10)
  if (Number.isNaN(count) || count === 0) {
    throw new Error('Flyway schema history table is empty; migrations may have failed')
  }
  log(`Flyway schema history has ${count} row(s)`)
}

async function buildBackend() {
  log('Building Spring Boot backend...')
  try {
    execFileSync('./mvnw', ['clean', 'package', '-DskipTests', '-P', 'e2e'], { cwd: BACKEND_DIR, stdio: 'inherit' })
  } catch {
    execFileSync('mvn', ['clean', 'package', '-DskipTests'], { cwd: BACKEND_DIR, stdio: 'inherit' })
  }
}

function findBackendJar(): string {
  const files = readdirSync(path.join(BACKEND_DIR, 'target'))
  const jar = files.find((f) => f.startsWith('vdc-platform-') && f.endsWith('.jar'))
  if (!jar) {
    throw new Error('Backend JAR not found')
  }
  return path.join(BACKEND_DIR, 'target', jar)
}

function launchBackend() {
  log('Launching Spring Boot backend...')
  const jar = findBackendJar()
  log(`Using JAR: ${jar}`)

  const out = createWriteStream(path.join(LOG_DIR, 'backend.log'))
  backendProcess = spawn('java', ['-jar', '-Dspring.profiles.active=e2e', jar], {
    cwd: BACKEND_DIR,
    env: { ...process.env, SPRING_PROFILES_ACTIVE: 'e2e' },
  })
  backendProcess.stdout?.pipe(out)
  backendProcess.stderr?.pipe(out)

  if (backendProcess.pid) {
    writeFileSync(path.join(LOG_DIR, 'backend.pid'), String(backendProcess.pid))
  }
}

function launchFrontend() {
  log('Launching Vite dev server...')
  const out = createWriteStream(path.join(LOG_DIR, 'frontend.log'))
  frontendProcess = spawn('npm', ['run', 'dev'], {
    cwd: FRONTEND_DIR,
    env: { ...process.env, BROWSER: 'none' },
  })
  frontendProcess.stdout?.pipe(out)
  frontendProcess.stderr?.pipe(out)

  if (frontendProcess.pid) {
    writeFileSync(path.join(LOG_DIR, 'frontend.pid'), String(frontendProcess.pid))
  }
}

export default async function globalSetup() {
  mkdirSync(LOG_DIR, { recursive: true })
  log('Starting Docker Compose services...')
  try {
    execFileSync('docker', ['compose', '-f', COMPOSE_FILE, 'down', '-v'], { stdio: 'inherit' })
  } catch {
    // ignore
  }
  execFileSync('docker', ['compose', '-f', COMPOSE_FILE, 'up', '-d'], { stdio: 'inherit' })

  log('Waiting for PostgreSQL...')
  await waitForPostgres()
  log('Waiting for Redis...')
  await waitForRedis()

  log('Building backend...')
  await buildBackend()

  log('Launching backend...')
  launchBackend()

  log('Launching frontend...')
  launchFrontend()

  log('Waiting for backend /actuator/health...')
  await waitForHealth('http://localhost:8080/actuator/health')

  log('Waiting for frontend...')
  await waitForHealth('http://localhost:5173/')

  log('Verifying Flyway migrations...')
  await verifyFlyway()

  log('Seeding MinIO...')
  await seedMinio()

  log('Global setup complete')
}

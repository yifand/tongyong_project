import { readFileSync } from 'fs'
import { execFileSync } from 'child_process'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const COMPOSE_FILE = path.resolve(__dirname, '../docker-compose.test.yml')
const LOG_DIR = path.resolve(__dirname, '../logs')

function log(msg: string) {
  // eslint-disable-next-line no-console
  console.log(`[globalTeardown] ${msg}`)
}

function killProcess(pidFile: string, label: string) {
  try {
    const pid = parseInt(readFileSync(pidFile, 'utf-8'), 10)
    if (pid) {
      log(`Stopping ${label} (PID ${pid})...`)
      process.kill(pid, 'SIGTERM')
    }
  } catch {
    // ignore
  }
}

export default async function globalTeardown() {
  killProcess(path.join(LOG_DIR, 'backend.pid'), 'backend')
  killProcess(path.join(LOG_DIR, 'frontend.pid'), 'frontend')

  log('Stopping Docker Compose services...')
  try {
    execFileSync('docker', ['compose', '-f', COMPOSE_FILE, 'down', '-v'], { stdio: 'inherit' })
  } catch {
    // ignore
  }

  log('Global teardown complete')
}

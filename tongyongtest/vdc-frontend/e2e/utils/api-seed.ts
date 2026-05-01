/**
 * API seeding helpers for E2E tests.
 *
 * These functions hit the backend REST API directly using an admin JWT
 * to create / delete prerequisite test data. They are intended to be used
 * inside authenticated specs or global setup when the UI
 * flow for data preparation is too slow or unavailable.
 *
 * Cleanup strategy
 * ----------------
 * 1. Prefer `test.afterEach` / `test.afterAll` hooks that call the
 *    corresponding `delete*` helper for every resource created.
 * 2. For tests that create many resources, collect ids in an array and
 *    delete in reverse order (children before parents).
 * 3. As a fallback, a nightly job can truncate the non-reference tables
 *    (channels, boxes, users, roles) in the test database.
 * 4. Built-in roles (SUPER_ADMIN, SITE_ADMIN, READONLY) cannot be deleted
 *    by the backend; do not attempt to create them with the same codes.
 */

export type Channel = {
  id: number
  channelId: string
  channelName?: string
  boxId: number
  channelType?: string
  algorithmType?: string
  rtspUrl?: string
}

const API_BASE = process.env.API_BASE_URL || 'http://localhost:8080/api/v1'

async function apiFetch<T>(
  token: string,
  method: string,
  path: string,
  body?: unknown
): Promise<T> {
  const url = `${API_BASE}${path}`
  const headers: Record<string, string> = {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  }

  const res = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })

  const data = (await res.json().catch(() => ({}))) as { code?: number; message?: string; data?: T }

  if (!res.ok || data.code !== 200) {
    throw new Error(
      `API ${method} ${path} failed: ${data.message || res.statusText} (HTTP ${res.status})`
    )
  }

  return data.data as T
}

/* ------------------------------------------------------------------ */
/* Box helpers                                                        */
/* ------------------------------------------------------------------ */

export interface CreateBoxData {
  boxId: string
  boxName?: string
  siteId: number
  ipAddress?: string
  version?: string
}

export function createBox(token: string, data: CreateBoxData): Promise<void> {
  return apiFetch(token, 'POST', '/devices/boxes', data)
}

export function deleteBox(token: string, id: number): Promise<void> {
  return apiFetch(token, 'DELETE', `/devices/boxes/${id}`)
}

/* ------------------------------------------------------------------ */
/* Channel helpers                                                    */
/* ------------------------------------------------------------------ */

export interface CreateChannelData {
  channelId: string
  channelName?: string
  boxId: number
  channelType?: string
  algorithmType?: string
  rtspUrl?: string
  username?: string
  password?: string
}

/**
 * @note The backend currently exposes `PUT /api/v1/devices/channels/{id}`
 * but does **not** expose a dedicated channel creation endpoint. This
 * helper calls `POST /devices/channels` which is the expected REST
 * convention; if the endpoint is missing, seed channels via the gateway
 * or add the endpoint in the backend first.
 */
export function createChannel(token: string, data: CreateChannelData): Promise<Channel> {
  return apiFetch(token, 'POST', '/devices/channels', data)
}

/**
 * @note The backend currently does **not** expose `DELETE /devices/channels/{id}`.
 * If channel cleanup is required, truncate the table in the test DB or
 * add the endpoint in the backend first.
 */
export function deleteChannel(token: string, id: number): Promise<void> {
  return apiFetch(token, 'DELETE', `/devices/channels/${id}`)
}

/* ------------------------------------------------------------------ */
/* User helpers                                                       */
/* ------------------------------------------------------------------ */

export interface CreateUserData {
  username: string
  password?: string
  realName?: string
  phone?: string
  email?: string
  roleId: number
  siteId?: number
  status: number
}

export function createUser(token: string, data: CreateUserData): Promise<void> {
  return apiFetch(token, 'POST', '/users', data)
}

export function deleteUser(token: string, id: number): Promise<void> {
  return apiFetch(token, 'DELETE', `/users/${id}`)
}

/* ------------------------------------------------------------------ */
/* Role helpers                                                       */
/* ------------------------------------------------------------------ */

export interface CreateRoleData {
  roleCode: string
  roleName: string
  permissions: string[]
  dataScope: string
}

export function createRole(token: string, data: CreateRoleData): Promise<void> {
  return apiFetch(token, 'POST', '/roles', data)
}

export function deleteRole(token: string, id: number): Promise<void> {
  return apiFetch(token, 'DELETE', `/roles/${id}`)
}

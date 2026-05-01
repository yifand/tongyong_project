import axios from 'axios'
import type { LoginRequest, LoginResponse } from '@/types'

const rawAxios = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

export function login(data: LoginRequest): Promise<LoginResponse> {
  return rawAxios.post('/api/v1/auth/login', data).then((res) => res.data.data)
}

export function logout(accessToken: string): Promise<void> {
  return rawAxios.post('/api/v1/auth/logout', null, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  })
}

export function refreshToken(token: string): Promise<LoginResponse> {
  return rawAxios.post('/api/v1/auth/refresh', { refreshToken: token }).then((res) => res.data.data)
}

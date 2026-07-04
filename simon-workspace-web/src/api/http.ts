import axios, { AxiosHeaders } from 'axios'

import { buildAuthHeader, readStoredSession } from '../stores/authSession'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 30_000,
})

http.interceptors.request.use((config) => {
  const authHeader = buildAuthHeader(readStoredSession())
  if (authHeader.Authorization) {
    config.headers = AxiosHeaders.from(config.headers)
    config.headers.set('Authorization', authHeader.Authorization)
  }
  return config
})

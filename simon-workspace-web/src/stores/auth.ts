import { defineStore } from 'pinia'

import { unwrapApiResponse, type ApiResponse } from '../api/errors'
import { http } from '../api/http'
import {
  clearStoredSession,
  isSessionExpired,
  readStoredSession,
  type AuthSession,
  writeStoredSession,
} from './authSession'

export interface CurrentUser {
  id: string
  username: string
  nickname: string
  avatarUrl?: string
  email?: string
  roles: string[]
  permissions: string[]
}

interface LoginData {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: CurrentUser
}

interface AuthState {
  session: AuthSession | null
  user: CurrentUser | null
  restored: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    session: readStoredSession(),
    user: null,
    restored: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.session && !isSessionExpired(state.session)),
    displayName: (state) => state.user?.nickname || state.user?.username || 'Simon',
    hasRole: (state) => (role: string) => state.user?.roles?.includes(role) ?? false,
    hasPermission: (state) => (permission: string) => {
      if (state.user?.roles?.includes('OWNER')) {
        return true
      }
      return state.user?.permissions?.includes(permission) ?? false
    },
  },
  actions: {
    async login(username: string, password: string) {
      const response = await http.post<ApiResponse<LoginData>>('/auth/login', { username, password })
      const loginData = unwrapApiResponse(response.data)

      const session: AuthSession = {
        accessToken: loginData.accessToken,
        tokenType: loginData.tokenType || 'Bearer',
        expiresIn: loginData.expiresIn,
        loginTime: Date.now(),
      }

      this.session = session
      this.user = loginData.user
      this.restored = true
      writeStoredSession(session)
    },

    async restore() {
      if (this.restored) {
        return this.isAuthenticated
      }

      if (!this.session || isSessionExpired(this.session)) {
        this.clear()
        return false
      }

      try {
        const response = await http.get<ApiResponse<CurrentUser>>('/auth/me')
        this.user = unwrapApiResponse(response.data)
        this.restored = true
        return true
      } catch {
        this.clear()
        return false
      }
    },

    async logout() {
      try {
        if (this.session) {
          await http.post('/auth/logout')
        }
      } finally {
        this.clear()
      }
    },

    clear() {
      this.session = null
      this.user = null
      this.restored = true
      clearStoredSession()
    },
  },
})

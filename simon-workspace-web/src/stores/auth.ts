import { defineStore } from 'pinia'

import { http } from '../api/http'
import { translate } from '../i18n'
import {
  clearStoredSession,
  isSessionExpired,
  readStoredSession,
  type AuthSession,
  writeStoredSession,
} from './authSession'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

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
      if (response.data.code !== 0) {
        throw new Error(response.data.message || translate('login.failed'))
      }

      const session: AuthSession = {
        accessToken: response.data.data.accessToken,
        tokenType: response.data.data.tokenType || 'Bearer',
        expiresIn: response.data.data.expiresIn,
        loginTime: Date.now(),
      }

      this.session = session
      this.user = response.data.data.user
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
        if (response.data.code !== 0) {
          this.clear()
          return false
        }

        this.user = response.data.data
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

import { defineStore } from 'pinia'

import { AUTH_TOKEN_STORAGE_KEY, http } from '../api/http'

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
  token: string
  user: CurrentUser
}

interface AuthState {
  token: string
  user: CurrentUser | null
  restored: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem(AUTH_TOKEN_STORAGE_KEY) ?? '',
    user: null,
    restored: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
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
        throw new Error(response.data.message || '登录失败')
      }

      this.token = response.data.data.token
      this.user = response.data.data.user
      this.restored = true
      localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, this.token)
    },

    async restore() {
      if (this.restored) {
        return this.isAuthenticated
      }

      if (!this.token) {
        this.restored = true
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
        if (this.token) {
          await http.post('/auth/logout')
        }
      } finally {
        this.clear()
      }
    },

    clear() {
      this.token = ''
      this.user = null
      this.restored = true
      localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
    },
  },
})

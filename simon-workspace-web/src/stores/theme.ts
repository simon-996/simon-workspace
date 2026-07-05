import { defineStore } from 'pinia'

export type AppThemeMode = 'light' | 'dark'

const THEME_STORAGE_KEY = 'simon-workspace-theme'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: loadThemeMode(),
  }),
  getters: {
    isDark: (state) => state.mode === 'dark',
  },
  actions: {
    setTheme(mode: AppThemeMode) {
      this.mode = mode
      applyThemeMode(mode)
    },
    toggleTheme() {
      this.setTheme(this.mode === 'dark' ? 'light' : 'dark')
    },
  },
})

function loadThemeMode(): AppThemeMode {
  const stored = typeof localStorage === 'undefined'
    ? null
    : localStorage.getItem(THEME_STORAGE_KEY)
  const mode: AppThemeMode = stored === 'dark' ? 'dark' : 'light'
  applyThemeMode(mode)
  return mode
}

function applyThemeMode(mode: AppThemeMode) {
  if (typeof document !== 'undefined') {
    document.documentElement.dataset.theme = mode
  }
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem(THEME_STORAGE_KEY, mode)
  }
}

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

describe('theme store', () => {
  const storage = new Map<string, string>()

  beforeEach(() => {
    vi.stubGlobal('localStorage', {
      clear: () => storage.clear(),
      getItem: (key: string) => storage.get(key) ?? null,
      setItem: (key: string, value: string) => storage.set(key, value),
    })
    vi.stubGlobal('document', {
      documentElement: {
        dataset: {} as Record<string, string>,
        removeAttribute: (name: string) => {
          if (name === 'data-theme') {
            delete document.documentElement.dataset.theme
          }
        },
      },
    })
    localStorage.clear()
    document.documentElement.removeAttribute('data-theme')
    setActivePinia(createPinia())
    vi.resetModules()
  })

  it('defaults to light theme and applies it to the document', async () => {
    const { useThemeStore } = await import('./theme')
    const theme = useThemeStore()

    expect(theme.mode).toBe('light')
    expect(theme.isDark).toBe(false)
    expect(document.documentElement.dataset.theme).toBe('light')
  })

  it('persists dark theme when toggled', async () => {
    const { useThemeStore } = await import('./theme')
    const theme = useThemeStore()

    theme.toggleTheme()

    expect(theme.mode).toBe('dark')
    expect(theme.isDark).toBe(true)
    expect(localStorage.getItem('simon-workspace-theme')).toBe('dark')
    expect(document.documentElement.dataset.theme).toBe('dark')
  })
})

import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

import appSource from './App.vue?raw'
import accountSource from './components/AccountCenterModal.vue?raw'
import headerSource from './components/AppHeader.vue?raw'
import languageSource from './components/LanguageSwitcher.vue?raw'
import homeSource from './views/HomeView.vue?raw'
import loginSource from './views/LoginView.vue?raw'
import workspaceSource from './views/WorkspaceView.vue?raw'

const styles = readFileSync(fileURLToPath(new URL('./style.css', import.meta.url)), 'utf-8')
describe('global dark theme styles', () => {
  it('defines a deep global dark palette and page background', () => {
    expect(styles).toContain(':root[data-theme="dark"]')
    expect(styles).toContain('--sw-bg: #070b10')
    expect(styles).toContain('--sw-page-bg:')
    expect(styles).toContain('background: var(--sw-page-bg)')
    expect(styles).toContain('--sw-panel-bg:')
    expect(styles).toContain('--sw-table-row-hover:')
  })

  it('keeps login page colors on shared theme variables', () => {
    expect(loginSource).toContain('background: var(--sw-page-bg)')
    expect(loginSource).toContain('color: var(--sw-text)')
    expect(loginSource).toContain('background: var(--sw-panel-bg)')
    expect(loginSource).not.toContain('linear-gradient(180deg, #fbfcfc')
    expect(loginSource).not.toContain('color: #17212b')
  })

  it('keeps primary shells and shared controls on theme variables', () => {
    expect(appSource).toContain('computed<GlobalThemeOverrides>')
    expect(appSource).toContain("theme.isDark ? '#f2f7fa' : '#17212b'")
    expect(headerSource).toContain('var(--sw-panel-bg-hover)')
    expect(languageSource).toContain('background: var(--sw-panel-bg)')
    expect(accountSource).toContain('background: var(--sw-surface-muted)')
    expect(homeSource).toContain('background: var(--sw-page-bg)')
    expect(workspaceSource).toContain('background: var(--sw-page-bg)')
  })
})

import { describe, expect, it } from 'vitest'

import homeSource from '../views/HomeView.vue?raw'
import loginSource from '../views/LoginView.vue?raw'
import workspaceSource from '../views/WorkspaceView.vue?raw'

describe('AppHeader page usage', () => {
  it('is shared by the public, login, and workspace shells', () => {
    expect(homeSource).toContain('AppHeader')
    expect(loginSource).toContain('AppHeader')
    expect(workspaceSource).toContain('AppHeader')
  })

  it('keeps language switching inside the shared header only', () => {
    expect(homeSource).not.toContain('LanguageSwitcher')
    expect(loginSource).not.toContain('LanguageSwitcher')
    expect(workspaceSource).not.toContain('LanguageSwitcher')
  })

  it('keeps account exit controls in the shared header instead of the workspace content header', () => {
    expect(workspaceSource).not.toContain('logout-button')
    expect(workspaceSource).not.toContain('async function logout')
  })
})

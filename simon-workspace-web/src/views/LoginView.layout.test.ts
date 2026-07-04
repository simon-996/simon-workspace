import { describe, expect, it } from 'vitest'

import source from './LoginView.vue?raw'

describe('LoginView layout', () => {
  it('centers a single login panel without the left copy column', () => {
    expect(source).toContain('<AppHeader />')
    expect(source).toContain('login-shell')
    expect(source).toContain('login-panel')
    expect(source).not.toContain('login-copy')
    expect(source).not.toContain('grid-template-columns: minmax(0, 0.95fr) minmax(340px, 420px)')
  })

  it('removes the account status header from the form panel', () => {
    expect(source).not.toContain('panel-header')
    expect(source).not.toContain("t('login.panelTitle')")
    expect(source).not.toContain("t('login.session')")
  })
})

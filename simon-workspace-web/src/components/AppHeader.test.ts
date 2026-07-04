import { describe, expect, it } from 'vitest'

import source from './AppHeader.vue?raw'

describe('AppHeader', () => {
  it('builds global navigation from shared route and site state', () => {
    expect(source).toContain('buildHeaderNavItems')
    expect(source).toContain('usePublicSiteConfig')
    expect(source).toContain('LanguageSwitcher')
  })

  it('renders the current navigation item as disabled text', () => {
    expect(source).toContain('v-if="item.disabled"')
    expect(source).toContain('aria-current="page"')
    expect(source).toContain('RouterLink')
  })

  it('keeps one responsive menu button for small screens', () => {
    expect(source).toContain('navOpen')
    expect(source).toContain('menu-button')
    expect(source).toContain('watch(')
  })

  it('restores auth state so the shared header can show the current account', () => {
    expect(source).toContain('useRouter')
    expect(source).toContain('void auth.restore()')
  })

  it('renders an authenticated account dropdown with workspace and logout actions', () => {
    expect(source).toContain('NDropdown')
    expect(source).toContain('v-if="auth.isAuthenticated"')
    expect(source).toContain('accountLabel')
    expect(source).toContain('accountOptions')
    expect(source).toContain("key: 'workspace'")
    expect(source).toContain("key: 'logout'")
    expect(source).toContain('selectAccountAction')
    expect(source).toContain('await auth.logout()')
  })
})

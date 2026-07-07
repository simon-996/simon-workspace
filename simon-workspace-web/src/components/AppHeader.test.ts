import { describe, expect, it } from 'vitest'

import indexHtml from '../../index.html?raw'
import source from './AppHeader.vue?raw'

describe('AppHeader', () => {
  it('builds global navigation from shared route and site state', () => {
    expect(source).toContain('buildHeaderNavItems')
    expect(source).toContain('usePublicSiteConfig')
    expect(source).toContain('LanguageSwitcher')
  })

  it('renders a global theme switch with moon and sun icons', () => {
    expect(source).toContain('useThemeStore')
    expect(source).toContain('theme-toggle')
    expect(source).toContain('theme-toggle-track')
    expect(source).toContain('theme-toggle-indicator')
    expect(source).toContain('active')
    expect(source).toContain('Moon')
    expect(source).toContain('Sun')
    expect(source).toContain('theme.toggleTheme')
  })

  it('renders the current navigation item as disabled text', () => {
    expect(source).toContain('v-if="item.disabled"')
    expect(source).toContain('aria-current="page"')
    expect(source).toContain('RouterLink')
  })

  it('keeps the brand link text-only without the title image avatar', () => {
    expect(source).not.toContain('class="brand-mark"')
    expect(source).not.toContain('src="/site-title-logo.png"')
    expect(source).not.toContain('image-rendering: pixelated')
  })

  it('uses the same title image as the browser tab icon', () => {
    expect(indexHtml).toContain('rel="icon"')
    expect(indexHtml).toContain('href="/site-title-logo.png"')
  })

  it('keeps one responsive menu button for small screens', () => {
    expect(source).toContain('navOpen')
    expect(source).toContain('menu-button')
    expect(source).toContain('watch(')
  })

  it('prevents accidental text selection in the navigation bar', () => {
    expect(source).toContain('user-select: none')
  })

  it('restores auth state so the shared header can show the current account', () => {
    expect(source).toContain('useRouter')
    expect(source).toContain('void auth.restore()')
  })

  it('renders an authenticated account dropdown with profile and logout actions', () => {
    expect(source).toContain('NDropdown')
    expect(source).toContain('v-if="auth.isAuthenticated"')
    expect(source).toContain('accountLabel')
    expect(source).toContain('accountAvatarUrl')
    expect(source).toContain('data-avatar-src')
    expect(source).toContain('resolveAvatarUrl')
    expect(source).toContain('account-avatar')
    expect(source).toContain('v-if="accountAvatarUrl"')
    expect(source).toContain('v-else')
    expect(source).toContain('accountOptions')
    expect(source).toContain("key: 'profile'")
    expect(source).not.toContain("key: 'workspace'")
    expect(source).toContain("key: 'logout'")
    expect(source).toContain('selectAccountAction')
    expect(source).toContain('accountCenterOpen.value = true')
    expect(source).toContain('await auth.logout()')
  })

  it('renders the account center modal from the shared header', () => {
    expect(source).toContain('AccountCenterModal')
    expect(source).toContain('v-model:show="accountCenterOpen"')
  })
})

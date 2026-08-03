import { describe, expect, it } from 'vitest'

import source from '../WorkspaceView.vue?raw'

describe('WorkspaceView navigation shell', () => {
  it('renders desktop and mobile navigation from the shared permission-filtered model', () => {
    expect(source).toContain('buildWorkspaceNavigation')
    expect(source).toContain('isWorkspaceNavigationActive(route.path, to)')
    expect(source).toContain('navigation.groups')
    expect(source).toContain('navigation.mobileItems')
    expect(source).toContain('navigation.moreGroups')
    expect(source).not.toContain('const navItems')
  })

  it('uses a More drawer instead of horizontally scrolling mobile tabs', () => {
    expect(source).toContain('<n-drawer')
    expect(source).toContain("t('workspace.nav.more')")
    expect(source).toContain('moreOpen')
    expect(source).toContain('() => route.fullPath')
    expect(source).not.toContain('overflow-x: auto')
  })

  it('names the desktop landmark and mobile More dialog for assistive technology', () => {
    const moreButton = source.match(/<button[\s\S]*?<\/button>/)?.[0] ?? ''
    const drawerTag = source.match(/<n-drawer[\s\S]*?>/)?.[0] ?? ''

    expect(source).toContain(`<nav class="side-nav" :aria-label="t('workspace.aria')">`)
    expect(moreButton).toContain(':aria-expanded="moreOpen"')
    expect(moreButton).toContain('aria-haspopup="dialog"')
    expect(moreButton).toContain('aria-controls="workspace-more-navigation"')
    expect(drawerTag).toContain('id="workspace-more-navigation"')
    expect(drawerTag).toContain(`:aria-label="t('workspace.nav.more')"`)
  })
})

import { describe, expect, it } from 'vitest'

import source from '../WorkspaceView.vue?raw'

describe('WorkspaceView navigation shell', () => {
  it('renders desktop and mobile navigation from the shared permission-filtered model', () => {
    expect(source).toContain('buildWorkspaceNavigation')
    expect(source).toContain('navigation.groups')
    expect(source).toContain('navigation.mobileItems')
    expect(source).toContain('navigation.moreGroups')
    expect(source).not.toContain('const navItems')
  })

  it('uses a More drawer instead of horizontally scrolling mobile tabs', () => {
    expect(source).toContain('<n-drawer')
    expect(source).toContain("t('workspace.nav.more')")
    expect(source).toContain('moreOpen')
    expect(source).not.toContain('overflow-x: auto')
  })
})

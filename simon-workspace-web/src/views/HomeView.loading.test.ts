import { describe, expect, it } from 'vitest'

import source from './HomeView.vue?raw'

describe('HomeView loading state', () => {
  it('does not render a hard-coded owner before public site config loads', () => {
    expect(source).toContain('usePublicSiteConfig')
    expect(source).not.toContain("ownerName: 'Chen Ximeng'")
    expect(source).not.toContain("ownerName: 'Simon'")
  })

  it('renders a skeleton branch while site config is loading', () => {
    expect(source).toContain('v-if="site"')
    expect(source).toContain('v-else')
    expect(source).toContain('intro-skeleton')
    expect(source).toContain('AppHeader')
  })
})

import { describe, expect, it } from 'vitest'

import detailSource from './BlogDetailView.vue?raw'

describe('BlogDetailView', () => {
  it('matches the blog editor top spacing on desktop and mobile', () => {
    expect(detailSource).toContain('padding: 10px 0 72px;')
    expect(detailSource).toContain('padding: 0 0 48px;')
    expect(detailSource).not.toContain('padding: 92px 0 72px;')
  })
})

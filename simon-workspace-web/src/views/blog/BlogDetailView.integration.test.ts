import { describe, expect, it } from 'vitest'

import detailSource from './BlogDetailView.vue?raw'

describe('BlogDetailView', () => {
  it('matches the blog editor top spacing on desktop and mobile', () => {
    expect(detailSource).toContain('padding: 10px 0 72px;')
    expect(detailSource).toContain('padding: 0 0 48px;')
    expect(detailSource).not.toContain('padding: 92px 0 72px;')
  })

  it('surfaces compact reading meta and a calmer comment composer', () => {
    expect(detailSource).toContain('class="article-meta"')
    expect(detailSource).toContain('class="comment-submit-row"')
    expect(detailSource).toContain('class="comment-count"')
    expect(detailSource).toContain('class="comment-empty"')
  })
})

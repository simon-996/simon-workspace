import { describe, expect, it } from 'vitest'

import listSource from './BlogListView.vue?raw'

describe('BlogListView UI', () => {
  it('uses a compact browser layout with featured and regular reading lanes', () => {
    expect(listSource).toContain('featuredPost')
    expect(listSource).toContain('regularPosts')
    expect(listSource).toContain('class="post-card featured"')
    expect(listSource).not.toContain('class="blog-metrics"')
    expect(listSource).not.toContain("t('blog.list.metrics")
  })

  it('keeps only a 10px top offset below the header', () => {
    expect(listSource).toContain('padding: 10px 0 18px;')
    expect(listSource).toContain('padding-top: 10px;')
    expect(listSource).not.toContain('padding: 96px 0 18px;')
    expect(listSource).not.toContain('padding-top: 92px;')
  })

  it('keeps filters operable from keyboard and form submission', () => {
    expect(listSource).toContain('@submit.prevent="load"')
    expect(listSource).toContain('@keydown.enter.prevent="openPost(post)"')
    expect(listSource).toContain('@keydown.space.prevent="openPost(post)"')
    expect(listSource).toContain('class="category-count"')
  })
})

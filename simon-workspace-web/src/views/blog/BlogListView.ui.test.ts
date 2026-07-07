import { describe, expect, it } from 'vitest'

import listSource from './BlogListView.vue?raw'

describe('BlogListView UI', () => {
  it('uses a compact browser layout with featured and regular reading lanes', () => {
    expect(listSource).toContain('featuredPost')
    expect(listSource).toContain('regularPosts')
    expect(listSource).toContain('class="post-card featured"')
    expect(listSource).toContain('class="blog-metrics"')
  })

  it('keeps filters operable from keyboard and form submission', () => {
    expect(listSource).toContain('@submit.prevent="load"')
    expect(listSource).toContain('@keydown.enter.prevent="openPost(post)"')
    expect(listSource).toContain('@keydown.space.prevent="openPost(post)"')
    expect(listSource).toContain('class="category-count"')
  })
})

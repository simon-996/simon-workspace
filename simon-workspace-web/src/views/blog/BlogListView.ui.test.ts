import { describe, expect, it } from 'vitest'

import listSource from './BlogListView.vue?raw'

describe('BlogListView UI', () => {
  it('uses the same card structure for every post in the list', () => {
    expect(listSource).toContain('v-for="post in posts"')
    expect(listSource).not.toContain('featuredPost')
    expect(listSource).not.toContain('regularPosts')
    expect(listSource).not.toContain('class="post-card featured"')
    expect(listSource).not.toContain('.post-card.featured')
    expect(listSource).not.toContain('class="blog-metrics"')
    expect(listSource).not.toContain("t('blog.list.metrics")
  })

  it('places category before tags and the date after the comment count', () => {
    const categoryIndex = listSource.indexOf('post.category?.name')
    const tagsIndex = listSource.indexOf('class="tag-row"')
    const commentsIndex = listSource.indexOf('post.commentCount')
    const dateIndex = listSource.indexOf('formatDate(post.publishedTime)')

    expect(categoryIndex).toBeGreaterThan(-1)
    expect(tagsIndex).toBeGreaterThan(categoryIndex)
    expect(commentsIndex).toBeGreaterThan(tagsIndex)
    expect(dateIndex).toBeGreaterThan(commentsIndex)
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

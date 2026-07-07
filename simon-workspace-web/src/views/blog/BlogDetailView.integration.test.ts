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

  it('shows an edit action only when the current user owns the post', () => {
    expect(detailSource).toContain('canEditPost')
    expect(detailSource).toContain('post.value?.authorUserId')
    expect(detailSource).toContain('auth.user?.id')
    expect(detailSource).toContain("auth.hasPermission('blog:post:update')")
    expect(detailSource).toContain('v-if="canEditPost"')
    expect(detailSource).toContain("t('blog.detail.edit')")
    expect(detailSource).toContain('router.push(`/blog/${post.value.id}/edit`)')
  })

  it('renders a floating table of contents with scroll-aware active headings', () => {
    expect(detailSource).toContain('extractArticleHeadings')
    expect(detailSource).toContain('activeHeadingId')
    expect(detailSource).toContain('IntersectionObserver')
    expect(detailSource).toContain('scrollToHeading')
    expect(detailSource).toContain('class="article-toc"')
    expect(detailSource).toContain(':class="{ active: activeHeadingId === item.id }"')
    expect(detailSource).toContain('position: sticky;')
    expect(detailSource).toContain('scroll-margin-top: 104px;')
  })

  it('adds breathing room between the article frame and rendered markdown', () => {
    expect(detailSource).toContain('grid-template-columns: minmax(0, 1fr) 230px;')
    expect(detailSource).toContain('padding: 16px;')
    expect(detailSource).toContain('padding: 44px 48px;')
    expect(detailSource).toContain('max-width: 76ch;')
  })
})

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

  it('keeps rendered markdown left-aligned and shrinkable on narrow screens', () => {
    expect(detailSource).toContain('grid-template-columns: minmax(0, 1fr) 230px;')
    expect(detailSource).toContain('padding: 16px;')
    expect(detailSource).toContain('padding: 44px 48px;')
    expect(detailSource).toContain('max-width: 76ch;')
    expect(detailSource).not.toContain('max-width: 14ch;')
    expect(detailSource).toContain('max-width: 100%;')
    expect(detailSource).toContain('text-align: left;')
    expect(detailSource).toContain('min-width: 0;')
    expect(detailSource).toContain('box-sizing: border-box;')
    expect(detailSource).toContain('overflow-wrap: anywhere;')
    expect(detailSource).toContain('padding: 24px 18px;')
  })

  it('enhances rendered markdown for technical reading', () => {
    expect(detailSource).toContain('handleMarkdownRendered')
    expect(detailSource).toContain('enhanceRenderedMarkdown')
    expect(detailSource).toContain('article-code-toolbar')
    expect(detailSource).toContain('article-code-copy')
    expect(detailSource).toContain('navigator.clipboard.writeText')
    expect(detailSource).toContain("anchor.rel = 'noopener noreferrer'")
    expect(detailSource).toContain('display: block;')
    expect(detailSource).toContain('overflow-x: auto;')
    expect(detailSource).toContain('max-width: 100%;')
  })
})

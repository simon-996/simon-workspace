import { describe, expect, it } from 'vitest'

import detailSource from './BlogDetailView.vue?raw'
import listSource from './BlogListView.vue?raw'
import editorSource from './BlogEditorView.vue?raw'
import en from '../../i18n/locales/en'
import zhCN from '../../i18n/locales/zh-CN'
import thTH from '../../i18n/locales/th-TH'

describe('blog views i18n', () => {
  it('defines blog operation strings for all supported languages', () => {
    for (const locale of [en, zhCN, thTH]) {
      expect(locale.blog.list.title).toBeTruthy()
      expect(locale.blog.detail.comments).toBeTruthy()
      expect(locale.blog.editor.publish).toBeTruthy()
      expect(locale.blog.editor.backToBlog).toBeTruthy()
      expect(locale.blog.editor.leaveConfirm).toBeTruthy()
      expect(locale.blog.messages.loadFailed).toBeTruthy()
    }
  })

  it('uses blog wording for Chinese write actions', () => {
    expect(zhCN.blog.list.write).toBe('写博客')
    expect(zhCN.workspace.blogPosts.actions.write).toBe('写博客')
  })

  it('uses blog wording throughout the Chinese blog interface', () => {
    const blogCopy = {
      publicBlog: zhCN.blog,
      homeBlogLabel: zhCN.home.notesAndEssays,
      workspacePage: zhCN.workspace.pages.blogPosts,
      workspaceBlogCategories: zhCN.workspace.blog,
      workspaceBlogPosts: zhCN.workspace.blogPosts,
    }

    expect(JSON.stringify(blogCopy)).not.toContain('文章')
  })

  it('does not hard-code common operation text in blog views', () => {
    const combined = `${listSource}\n${detailSource}\n${editorSource}`

    for (const text of [
      'No posts yet.',
      'Failed to load blog',
      'Write a comment',
      'Sign in to comment.',
      'New category',
      'Only .md files are supported',
    ]) {
      expect(combined).not.toContain(text)
    }
  })

  it('keeps the blog index visually minimal', () => {
    expect(listSource).not.toContain("t('blog.list.title')")
    expect(listSource).not.toContain("t('blog.list.subtitle')")
    expect(listSource).not.toContain("t('blog.list.emptyText')")
  })
})

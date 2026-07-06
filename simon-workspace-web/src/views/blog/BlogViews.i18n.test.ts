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
      expect(locale.blog.messages.loadFailed).toBeTruthy()
    }
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
})

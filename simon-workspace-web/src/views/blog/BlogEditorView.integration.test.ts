import { describe, expect, it } from 'vitest'

import editorSource from './BlogEditorView.vue?raw'

describe('BlogEditorView', () => {
  it('uses md-editor-v3 and uploads blog editor images as public blog assets', () => {
    expect(editorSource).toContain('md-editor-v3')
    expect(editorSource).toContain('BLOG_EDITOR')
    expect(editorSource).toContain('onUploadImg')
  })

  it('uses i18n keys for editor operation text', () => {
    expect(editorSource).toContain("t('blog.editor.title')")
    expect(editorSource).toContain("t('blog.editor.publish')")
    expect(editorSource).not.toContain('Write in Markdown.')
    expect(editorSource).not.toContain('Title and content are required')
  })
})

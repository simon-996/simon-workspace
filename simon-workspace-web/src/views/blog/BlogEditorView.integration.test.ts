import { describe, expect, it } from 'vitest'

import editorSource from './BlogEditorView.vue?raw'

describe('BlogEditorView', () => {
  it('uses md-editor-v3 and uploads blog editor images as public blog assets', () => {
    expect(editorSource).toContain('md-editor-v3')
    expect(editorSource).toContain('BLOG_EDITOR')
    expect(editorSource).toContain('onUploadImg')
  })

  it('uses i18n keys for editor operation text', () => {
    expect(editorSource).toContain("t('blog.editor.publish')")
    expect(editorSource).not.toContain('Write in Markdown.')
    expect(editorSource).not.toContain('Title and content are required')
  })

  it('shows an error when editor image upload fails', () => {
    expect(editorSource).toContain("t('blog.messages.imageUploadFailed')")
    expect(editorSource).toContain('catch (error)')
  })

  it('stacks mobile editor actions without cramming three buttons into one row', () => {
    expect(editorSource).toContain('.editor-actions {\n    grid-template-columns: repeat(2, minmax(0, 1fr));')
    expect(editorSource).toContain('.import-button {\n    grid-column: 1 / -1;')
  })

  it('shows a soft waiting state while images upload', () => {
    expect(editorSource).toContain('uploadingImages')
    expect(editorSource).toContain('uploading-overlay')
    expect(editorSource).toContain("t('blog.editor.uploadingImage')")
  })

  it('hides the md editor crop modal immediately while image uploads', () => {
    expect(editorSource).toContain('blogImageUploadingClass')
    expect(editorSource).toContain('body.blog-image-uploading .md-editor-modal-clip')
    expect(editorSource).toContain('display: none !important')
    expect(editorSource).toContain('document.body.classList.toggle')
    expect(editorSource).not.toContain('uploadingImagePreview')
    expect(editorSource).not.toContain('blog-image-crop-upload-card')
  })
})

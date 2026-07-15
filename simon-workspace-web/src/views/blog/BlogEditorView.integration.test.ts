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
    expect(editorSource).toContain("t('blog.editor.backToBlog')")
    expect(editorSource).not.toContain('Write in Markdown.')
    expect(editorSource).not.toContain('Title and content are required')
  })

  it('keeps writing inside the blog flow and protects unsaved edits', () => {
    expect(editorSource).toContain('onBeforeRouteLeave')
    expect(editorSource).toContain('hasUnsavedChanges')
    expect(editorSource).toContain('backToBlog')
    expect(editorSource).toContain("t('blog.editor.leaveConfirm')")
  })

  it('loads an existing post and updates it when used from the edit route', () => {
    expect(editorSource).toContain('useRoute')
    expect(editorSource).toContain('fetchManageBlogPostDetail')
    expect(editorSource).toContain('updateBlogPost')
    expect(editorSource).toContain('editingPostId')
    expect(editorSource).toContain('loadPostForEdit')
    expect(editorSource).toContain('updateBlogPost(editingPostId.value')
    expect(editorSource).toContain("t('blog.editor.editKicker')")
  })

  it('keeps saved drafts in the editable route instead of the public detail route', () => {
    expect(editorSource).toContain("status === 'PUBLISHED' ? `/blog/${post.id}` : `/blog/${post.id}/edit`")
  })

  it('requires a category before publishing a blog post', () => {
    expect(editorSource).toContain("status === 'PUBLISHED' && !categoryId.value")
    expect(editorSource).toContain("t('blog.messages.categoryRequired')")
    expect(editorSource).toContain('categorySelectStatus')
    expect(editorSource).toContain(':status="categorySelectStatus"')
  })

  it('searches reusable tags and lets authors create normalized tags inline', () => {
    expect(editorSource).toContain('fetchBlogTags')
    expect(editorSource).toContain('remoteTags')
    expect(editorSource).toContain('tagOptions')
    expect(editorSource).not.toContain(':remote="true"')
    expect(editorSource).toContain(':on-create="createTagOption"')
    expect(editorSource).toContain(':max-tag-count="\'responsive\'"')
    expect(editorSource).toContain('@search="queueTagSearch"')
    expect(editorSource).toContain(':value="tags"')
    expect(editorSource).toContain('@update:value="updateTags"')
    expect(editorSource).toContain("t('blog.messages.tagLimit')")
    expect(editorSource).toContain('tagSearchTimer')
    expect(editorSource).toContain('tagSearchSequence')
    expect(editorSource).toContain('normalizeSelectedTags(tags.value)')
  })

  it('shows an error when editor image upload fails', () => {
    expect(editorSource).toContain("t('blog.messages.imageUploadFailed')")
    expect(editorSource).toContain('catch (error)')
  })

  it('keeps the editor compact and places mobile actions in one row', () => {
    expect(editorSource).toContain('padding: 10px 0 42px;')
    expect(editorSource).toContain('padding: 0 0 28px;')
    expect(editorSource).toContain('.editor-actions {\n    grid-template-columns: repeat(3, minmax(0, 1fr));')
    expect(editorSource).not.toContain('grid-column: 1 / -1;')
  })

  it('shows a soft waiting state while images upload', () => {
    expect(editorSource).toContain('uploadingImages')
    expect(editorSource).toContain('uploading-overlay')
    expect(editorSource).toContain("t('blog.editor.uploadingImage')")
    expect(editorSource).toContain('uploadProgress')
    expect(editorSource).toContain('upload-progress-bar')
    expect(editorSource).toContain("t('blog.editor.processingImage')")
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

import { describe, expect, it } from 'vitest'

import source from './FileUploadDialog.vue?raw'

describe('FileUploadDialog', () => {
  it('owns upload state through an explicitly injected API dependency', () => {
    expect(source).toContain('useFileUploadDialog')
    expect(source).toContain('upload: uploadFileResource')
    expect(source).toContain('uploadProgress')
    expect(source).toContain('<n-progress')
    expect(source).toContain('uploadError')
    expect(source).toContain('role="alert"')
  })

  it('emits the uploaded resource and closes only after success', () => {
    expect(source).toContain("'uploaded': [resource: FileResource]")
    expect(source).toContain("emit('uploaded', resource)")
    expect(source).toContain("emit('update:show', false)")
    expect(source).toContain('if (!resource) return')
  })

  it('guards every close path and editable field while uploading', () => {
    expect(source).toContain('@update:show="handleShowUpdate"')
    expect(source).toContain(':mask-closable="!uploading"')
    expect(source).toContain(':close-on-esc="!uploading"')
    expect(source).toContain(':closable="!uploading"')
    expect(source).toContain('if (uploading.value) return')
    expect(source).toContain('type="file"')
    expect(source).toContain(':disabled="uploading"')
    expect(source).toContain('<n-radio-group')
    expect(source).toContain('setSelectedFile')
    expect(source).toContain('resetDialog')
  })

  it('uses responsive token-based styling without gradients', () => {
    expect(source).toContain("width: 'min(520px, calc(100vw - 32px))'")
    expect(source).toContain("borderRadius: '8px'")
    expect(source).toContain('var(--sw-')
    expect(source).not.toMatch(/gradient/i)
  })
})

import { describe, expect, it, vi } from 'vitest'

import type { FileResource } from '../api/workspace'
import { useFileUploadDialog } from './useFileUploadDialog'

function createResource(filename = 'lesson-plan.pdf'): FileResource {
  return {
    id: 'file-1',
    ownerUserId: 'user-1',
    sourceType: 'UPLOAD',
    originalFilename: filename,
    storageProvider: 'LOCAL',
    visibility: 'PUBLIC',
    fileSize: 12,
    status: 'ACTIVE',
  }
}

function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })

  return { promise, resolve, reject }
}

describe('useFileUploadDialog', () => {
  it('requires a selected file before submitting', async () => {
    const upload = vi.fn()
    const state = useFileUploadDialog({
      upload,
      fileRequired: () => 'Choose a file',
      failed: () => 'Upload failed',
    })

    await expect(state.submit()).resolves.toBeNull()
    expect(state.uploadError.value).toBe('Choose a file')
    expect(upload).not.toHaveBeenCalled()
  })

  it('tracks a pending public upload and returns its resource', async () => {
    const request = deferred<FileResource>()
    const upload = vi.fn((_file: File, _visibility: 'PRIVATE' | 'PUBLIC' = 'PRIVATE', onProgress?: (progress: number) => void) => {
      onProgress?.(64)
      return request.promise
    })
    const state = useFileUploadDialog({
      upload,
      fileRequired: () => 'Choose a file',
      failed: () => 'Upload failed',
    })
    const file = new File(['lesson'], 'lesson-plan.pdf')

    state.setSelectedFile(file)
    state.visibility.value = 'PUBLIC'
    const pending = state.submit()

    expect(state.uploading.value).toBe(true)
    expect(state.uploadProgress.value).toBe(64)
    expect(upload).toHaveBeenCalledWith(file, 'PUBLIC', expect.any(Function))

    const resource = createResource()
    request.resolve(resource)

    await expect(pending).resolves.toBe(resource)
    expect(state.uploading.value).toBe(false)
    expect(state.selectedFile.value).toBe(file)
    expect(state.visibility.value).toBe('PUBLIC')
  })

  it('preserves failed input and clears stale feedback before a successful retry', async () => {
    const resource = createResource()
    let onProgress: ((progress: number) => void) | undefined
    const upload = vi.fn((_file: File, _visibility: 'PRIVATE' | 'PUBLIC' = 'PRIVATE', progress?: (value: number) => void): Promise<FileResource> => {
      onProgress = progress
      return Promise.reject(new Error('Storage unavailable'))
    })
    const state = useFileUploadDialog({
      upload,
      fileRequired: () => 'Choose a file',
      failed: () => 'Upload failed',
    })
    const file = new File(['lesson'], 'lesson-plan.pdf')
    state.setSelectedFile(file)
    state.visibility.value = 'PUBLIC'

    const firstAttempt = state.submit()
    onProgress?.(37)
    await expect(firstAttempt).resolves.toBeNull()

    expect(state.selectedFile.value).toBe(file)
    expect(state.visibility.value).toBe('PUBLIC')
    expect(state.uploadProgress.value).toBe(37)
    expect(state.uploadError.value).toBe('Storage unavailable')
    expect(state.uploading.value).toBe(false)

    upload.mockImplementationOnce(() => {
      expect(state.uploadProgress.value).toBe(0)
      expect(state.uploadError.value).toBe('')
      return Promise.resolve(resource)
    })

    await expect(state.submit()).resolves.toBe(resource)
    expect(upload).toHaveBeenCalledTimes(2)
    expect(state.uploading.value).toBe(false)
  })

  it('clears stale failure progress when selecting a new file', async () => {
    let onProgress: ((progress: number) => void) | undefined
    const state = useFileUploadDialog({
      upload: vi.fn((_file, _visibility, progress) => {
        onProgress = progress
        return Promise.reject(new Error('Storage unavailable'))
      }),
      fileRequired: () => 'Choose a file',
      failed: () => 'Upload failed',
    })
    state.setSelectedFile(new File(['old'], 'old.pdf'))

    const firstAttempt = state.submit()
    onProgress?.(37)
    await firstAttempt

    const replacement = new File(['new'], 'new.pdf')
    state.setSelectedFile(replacement)

    expect(state.selectedFile.value).toBe(replacement)
    expect(state.uploadProgress.value).toBe(0)
    expect(state.uploadError.value).toBe('')
  })

  it('uses the translated fallback for non-Error failures', async () => {
    const state = useFileUploadDialog({
      upload: vi.fn().mockRejectedValue('offline'),
      fileRequired: () => 'Choose a file',
      failed: () => 'Localized failure',
    })
    state.setSelectedFile(new File(['lesson'], 'lesson-plan.pdf'))

    await expect(state.submit()).resolves.toBeNull()
    expect(state.uploadError.value).toBe('Localized failure')
    expect(state.uploading.value).toBe(false)
  })

  it.each([new Error(''), new Error('   ')])(
    'uses the translated fallback for Error failures without a useful message',
    async (error) => {
      const state = useFileUploadDialog({
        upload: vi.fn().mockRejectedValue(error),
        fileRequired: () => 'Choose a file',
        failed: () => 'Localized failure',
      })
      state.setSelectedFile(new File(['lesson'], 'lesson-plan.pdf'))

      await expect(state.submit()).resolves.toBeNull()
      expect(state.uploadError.value).toBe('Localized failure')
    },
  )

  it('resets every field to its initial state', () => {
    const state = useFileUploadDialog({
      upload: vi.fn(),
      fileRequired: () => 'Choose a file',
      failed: () => 'Upload failed',
    })

    state.setSelectedFile(new File(['lesson'], 'lesson-plan.pdf'))
    state.visibility.value = 'PUBLIC'
    state.uploadProgress.value = 82
    state.uploadError.value = 'Failed'
    state.reset()

    expect(state.selectedFile.value).toBeNull()
    expect(state.visibility.value).toBe('PRIVATE')
    expect(state.uploading.value).toBe(false)
    expect(state.uploadProgress.value).toBe(0)
    expect(state.uploadError.value).toBe('')
  })

  it('ignores duplicate submissions while an upload is active', async () => {
    const request = deferred<FileResource>()
    const upload = vi.fn(() => request.promise)
    const state = useFileUploadDialog({
      upload,
      fileRequired: () => 'Choose a file',
      failed: () => 'Upload failed',
    })
    state.setSelectedFile(new File(['lesson'], 'lesson-plan.pdf'))

    const firstAttempt = state.submit()
    await expect(state.submit()).resolves.toBeNull()
    expect(upload).toHaveBeenCalledTimes(1)

    request.resolve(createResource())
    await firstAttempt
  })

  it('ignores reset while uploading and permits reset only after the request settles', async () => {
    const request = deferred<FileResource>()
    const upload = vi.fn((_file, _visibility, onProgress) => {
      onProgress?.(37)
      return request.promise
    })
    const state = useFileUploadDialog({
      upload,
      fileRequired: () => 'Choose a file',
      failed: () => 'Upload failed',
    })
    const file = new File(['lesson'], 'lesson-plan.pdf')
    state.setSelectedFile(file)

    const pending = state.submit()
    state.reset()

    expect(state.selectedFile.value).toBe(file)
    expect(state.uploadProgress.value).toBe(37)
    expect(state.uploading.value).toBe(true)
    await expect(state.submit()).resolves.toBeNull()
    expect(upload).toHaveBeenCalledTimes(1)

    request.resolve(createResource())
    await pending
    state.reset()

    expect(state.selectedFile.value).toBeNull()
    expect(state.uploadProgress.value).toBe(0)
    expect(state.uploading.value).toBe(false)
  })
})

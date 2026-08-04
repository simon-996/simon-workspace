import { beforeEach, describe, expect, it, vi } from 'vitest'

import { uploadBlogEditorImage, uploadFileResource } from './workspace'
import { http } from './http'

vi.mock('./http', () => ({
  http: {
    post: vi.fn(),
  },
}))

describe('workspace uploads', () => {
  beforeEach(() => {
    vi.mocked(http.post).mockReset()
  })

  it('reports upload progress for blog editor images', async () => {
    vi.mocked(http.post).mockImplementation(async (_url, _data, config) => {
      config?.onUploadProgress?.({ loaded: 24, total: 60, bytes: 24, lengthComputable: true })
      return {
        data: {
          code: 0,
          data: {
            id: 'file-1',
            publicUrl: 'https://cdn.example.com/file.webp',
          },
        },
      }
    })

    const onProgress = vi.fn()
    const result = await uploadBlogEditorImage(new File(['image'], 'image.webp'), 'BLOG_EDITOR', onProgress)

    expect(result.publicUrl).toBe('https://cdn.example.com/file.webp')
    expect(onProgress).toHaveBeenCalledWith(40)
  })

  it('reports upload progress for file resources', async () => {
    vi.mocked(http.post).mockImplementation(async (_url, _data, config) => {
      config?.onUploadProgress?.({ loaded: 3, total: 4, bytes: 3, lengthComputable: true })
      return {
        data: {
          code: 0,
          data: {
            originalFilename: 'notes.pdf',
          },
        },
      }
    })

    const onProgress = vi.fn()
    const result = await uploadFileResource(new File(['notes'], 'notes.pdf'), 'PRIVATE', onProgress)

    expect(result.originalFilename).toBe('notes.pdf')
    expect(onProgress).toHaveBeenCalledWith(75)
    expect(http.post).toHaveBeenCalledWith(
      '/files',
      expect.any(FormData),
      expect.objectContaining({
        params: {
          sourceType: 'UPLOAD',
          visibility: 'PRIVATE',
        },
      }),
    )
  })

  it.each([0, undefined])('does not report progress without a positive total (%s)', async (total) => {
    vi.mocked(http.post).mockImplementation(async (_url, _data, config) => {
      config?.onUploadProgress?.({ loaded: 3, total, bytes: 3, lengthComputable: false })
      return {
        data: {
          code: 0,
          data: {
            originalFilename: 'notes.pdf',
          },
        },
      }
    })

    const onProgress = vi.fn()
    await uploadFileResource(new File(['notes'], 'notes.pdf'), 'PRIVATE', onProgress)

    expect(onProgress).not.toHaveBeenCalled()
  })

  it('clamps file resource upload progress to 100', async () => {
    vi.mocked(http.post).mockImplementation(async (_url, _data, config) => {
      config?.onUploadProgress?.({ loaded: 5, total: 4, bytes: 5, lengthComputable: true })
      return {
        data: {
          code: 0,
          data: {
            originalFilename: 'notes.pdf',
          },
        },
      }
    })

    const onProgress = vi.fn()
    await uploadFileResource(new File(['notes'], 'notes.pdf'), 'PUBLIC', onProgress)

    expect(onProgress).toHaveBeenCalledWith(100)
  })
})

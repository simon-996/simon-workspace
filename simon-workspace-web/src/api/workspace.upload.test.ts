import { beforeEach, describe, expect, it, vi } from 'vitest'

import { uploadBlogEditorImage } from './workspace'
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
})

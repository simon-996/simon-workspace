import { afterEach, describe, expect, it, vi } from 'vitest'

import { resolveAvatarUrl } from './avatarUrl'

describe('resolveAvatarUrl', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('keeps absolute r2 avatar urls unchanged', () => {
    const url = 'https://pub-9a2ecd5e709b4a6b80d23d97010e0ae2.r2.dev/files/2026/07/avatar.webp'

    expect(resolveAvatarUrl(url)).toBe(url)
  })

  it('prints avatar url diagnostics when debug mode is enabled', () => {
    const info = vi.spyOn(console, 'info').mockImplementation(() => undefined)
    vi.stubGlobal('localStorage', {
      getItem: (key: string) => key === 'simon-workspace-avatar-debug' ? '1' : null,
    })

    const url = 'https://pub-9a2ecd5e709b4a6b80d23d97010e0ae2.r2.dev/files/2026/07/avatar.webp'

    expect(resolveAvatarUrl(url)).toBe(url)
    expect(info).toHaveBeenCalledWith('[avatar-url]', {
      input: url,
      output: url,
      reason: 'absolute',
    })
  })
})

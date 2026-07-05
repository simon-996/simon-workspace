import { describe, expect, it, vi } from 'vitest'

vi.stubGlobal('window', {
  location: {
    origin: 'https://site.example',
  },
})

describe('avatar url resolver', () => {
  it('keeps absolute and api avatar urls renderable', async () => {
    const { resolveAvatarUrl } = await import('./avatarUrl')

    expect(resolveAvatarUrl('https://cdn.example/avatar.webp')).toBe('https://cdn.example/avatar.webp')
    expect(resolveAvatarUrl('/api/files/1/download')).toBe('/api/files/1/download')
  })

  it('resolves file object keys through the configured api base url', async () => {
    const { resolveAvatarUrl } = await import('./avatarUrl')

    expect(resolveAvatarUrl('files/2026/07/avatar.webp', 'https://api.example/api')).toBe(
      'https://api.example/api/files/2026/07/avatar.webp',
    )
    expect(resolveAvatarUrl('/files/2026/07/avatar.webp', '/api')).toBe('/api/files/2026/07/avatar.webp')
  })

  it('leaves blank avatar values blank', async () => {
    const { resolveAvatarUrl } = await import('./avatarUrl')

    expect(resolveAvatarUrl('')).toBe('')
    expect(resolveAvatarUrl('   ')).toBe('')
    expect(resolveAvatarUrl(null)).toBe('')
  })
})

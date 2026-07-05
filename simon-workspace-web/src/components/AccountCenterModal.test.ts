import { describe, expect, it } from 'vitest'

import source from './AccountCenterModal.vue?raw'

describe('AccountCenterModal', () => {
  it('renders image avatars separately from initial fallback avatars', () => {
    expect(source).toContain('avatarPreview')
    expect(source).toContain('v-if="avatarPreview"')
    expect(source).toContain('v-else')
    expect(source).toContain('data-avatar-src')
  })
})

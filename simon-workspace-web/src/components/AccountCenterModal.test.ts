import { describe, expect, it } from 'vitest'

import source from './AccountCenterModal.vue?raw'

describe('AccountCenterModal', () => {
  it('renders image avatars separately from initial fallback avatars', () => {
    expect(source).toContain('avatarPreview')
    expect(source).toContain('v-if="avatarPreview"')
    expect(source).toContain('v-else')
    expect(source).toContain('data-avatar-src')
  })

  it('keeps avatar crop upload interaction explicit and recoverable', () => {
    expect(source).toContain('avatar-compare-grid')
    expect(source).toContain('avatar-crop-shell')
    expect(source).toContain('avatar-upload-overlay')
    expect(source).toContain('resetAvatarCrop')
    expect(source).toContain('account.avatar.reselect')
    expect(source).toContain('account.avatar.resetCrop')
    expect(source).toContain('account.avatar.uploading')
  })
})

import { describe, expect, it } from 'vitest'

import source from './AccountCenterModal.vue?raw'

describe('AccountCenterModal', () => {
  it('uses a compact modal with profile, avatar, and password tabs', () => {
    expect(source).toContain('NModal')
    expect(source).toContain('NTabs')
    expect(source).toContain('name="profile"')
    expect(source).toContain('name="avatar"')
    expect(source).toContain('name="password"')
  })

  it('saves profile fields through the auth store', () => {
    expect(source).toContain('profileForm')
    expect(source).toContain('auth.updateProfile')
    expect(source).toContain('nickname')
    expect(source).toContain('email')
    expect(source).toContain('avatarUrl')
  })

  it('marks mismatched confirmation passwords and disables password save', () => {
    expect(source).toContain('passwordMismatch')
    expect(source).toContain(':status="passwordMismatch ? \'error\' : undefined"')
    expect(source).toContain(':disabled="passwordMismatch')
    expect(source).toContain('auth.updatePassword')
  })

  it('keeps avatar upload ready for the crop flow', () => {
    expect(source).toContain('avatarFileInput')
    expect(source).toContain('accept="image/*"')
    expect(source).toContain('selectAvatarFile')
  })
})

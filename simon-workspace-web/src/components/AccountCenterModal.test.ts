import { describe, expect, it } from 'vitest'

import packageJson from '../../package.json?raw'
import source from './AccountCenterModal.vue?raw'

describe('AccountCenterModal', () => {
  it('uses a compact modal with profile, avatar, and password tabs', () => {
    expect(source).toContain('NModal')
    expect(source).toContain('NTabs')
    expect(source).toContain('modalStyle')
    expect(source).toContain('modalContentStyle')
    expect(source).toContain('account-layout')
    expect(source).toContain('account-summary')
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
    expect(source).toContain('resolveAvatarUrl')
    expect(source).not.toContain('account.profile.avatarUrl')
  })

  it('marks mismatched confirmation passwords and disables password save', () => {
    expect(source).toContain('passwordMismatch')
    expect(source).toContain(':status="passwordMismatch ? \'error\' : undefined"')
    expect(source).toContain(':disabled="passwordMismatch')
    expect(source).toContain('auth.updatePassword')
  })

  it('keeps avatar upload ready for the crop flow', () => {
    const pkg = JSON.parse(packageJson)

    expect(pkg.dependencies).toHaveProperty('vue-advanced-cropper')
    expect(source).toContain('avatarFileInput')
    expect(source).toContain('accept="image/*"')
    expect(source).toContain('selectAvatarFile')
    expect(source).toContain('vue-advanced-cropper')
    expect(source).toContain('<Cropper')
    expect(source).toContain('<Preview')
    expect(source).toContain('avatarCropCanvas')
    expect(source).not.toContain('NSlider')
    expect(source).not.toContain('cropImageFileToAvatar')
    expect(source).toContain('uploadCroppedAvatar')
  })
})

import { describe, expect, it } from 'vitest'

import workspaceApiSource from '../api/workspace.ts?raw'
import source from './auth.ts?raw'

describe('auth profile actions', () => {
  it('exposes self-service profile and password actions', () => {
    expect(source).toContain('interface ProfileUpdatePayload')
    expect(source).toContain('interface PasswordUpdatePayload')
    expect(source).toContain('async updateProfile(payload: ProfileUpdatePayload)')
    expect(source).toContain('async updatePassword(payload: PasswordUpdatePayload)')
    expect(source).toContain("http.put<ApiResponse<CurrentUser>>('/auth/me/profile', payload)")
    expect(source).toContain("http.put<ApiResponse<null>>('/auth/me/password', payload)")
  })

  it('updates the current user after profile save', () => {
    expect(source).toContain('this.user = updatedUser')
    expect(source).toContain('return updatedUser')
  })

  it('uploads avatars as public avatar file resources', () => {
    expect(workspaceApiSource).toContain('export async function uploadAvatarResource(file: File)')
    expect(workspaceApiSource).toContain("sourceType: 'AVATAR'")
    expect(workspaceApiSource).toContain("visibility: 'PUBLIC'")
    expect(source).toContain('async uploadAvatar(file: File)')
    expect(source).toContain('uploadAvatarResource(file)')
  })
})

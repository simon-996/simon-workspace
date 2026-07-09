import { describe, expect, it } from 'vitest'

import routerSource from '../router/index.ts?raw'
import authStoreSource from '../stores/auth.ts?raw'
import workspaceApiSource from '../api/workspace.ts?raw'
import registerViewSource from './RegisterView.vue?raw'
import securityViewSource from './workspace/SecurityManagementView.vue?raw'

describe('registration and account review UI', () => {
  it('adds a public register route and auth api', () => {
    expect(routerSource).toContain("path: '/register'")
    expect(authStoreSource).toContain('/auth/register')
    expect(registerViewSource).toContain('submitRegister')
    expect(registerViewSource).toContain('pendingTitle')
  })

  it('adds workspace review actions for pending accounts', () => {
    expect(workspaceApiSource).toContain('/security/users/${id}/approve')
    expect(workspaceApiSource).toContain('/security/users/${id}/reject')
    expect(workspaceApiSource).toContain('/security/users/${id}/disable')
    expect(securityViewSource).toContain('openApproveModal')
    expect(securityViewSource).toContain('rejectUser')
    expect(securityViewSource).toContain('disableUser')
  })
})

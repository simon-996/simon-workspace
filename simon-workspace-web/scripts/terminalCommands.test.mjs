import { describe, expect, it } from 'vitest'

import { evaluateTerminalCommand, terminalCommands } from '../src/components/terminalCommands.ts'

describe('terminal commands', () => {
  const anonymous = {
    isAuthenticated: false,
    hasPermission: () => false,
  }

  it('allows public commands', () => {
    expect(evaluateTerminalCommand('about', anonymous).status).toBe('run')
    expect(evaluateTerminalCommand('about', anonymous).to).toBe('#about')
  })

  it('requires login before protected commands', () => {
    const loginRequired = evaluateTerminalCommand('site', anonymous)

    expect(loginRequired.status).toBe('login-required')
    expect(loginRequired.permission).toBe('site:config')
  })

  it('blocks protected commands without permission', () => {
    const forbidden = evaluateTerminalCommand('site', {
      isAuthenticated: true,
      hasPermission: () => false,
    })

    expect(forbidden.status).toBe('forbidden')
    expect(forbidden.permission).toBe('site:config')
  })

  it('runs protected commands with permission', () => {
    const allowed = evaluateTerminalCommand('site', {
      isAuthenticated: true,
      hasPermission: (permission) => permission === 'site:config',
    })

    expect(allowed.status).toBe('run')
    expect(allowed.to).toBe('/workspace/site')
  })

  it('keeps course management behind course permission', () => {
    expect(terminalCommands).toContainEqual(expect.objectContaining({
      command: 'courses',
      to: '/courses',
    }))
    expect(terminalCommands).toContainEqual(expect.objectContaining({
      command: 'courses-admin',
      permission: 'course:manage',
    }))
  })
})

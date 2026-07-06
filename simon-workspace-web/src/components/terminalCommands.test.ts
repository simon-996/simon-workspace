import { describe, expect, it } from 'vitest'

import { evaluateTerminalCommand } from './terminalCommands'

const guestContext = {
  isAuthenticated: false,
  username: '',
  hasPermission: () => false,
}

const ownerContext = {
  isAuthenticated: true,
  username: 'simon',
  hasPermission: () => true,
}

describe('evaluateTerminalCommand', () => {
  it('returns structured help documentation instead of a bare command list', () => {
    const result = evaluateTerminalCommand('help', guestContext)

    expect(result.status).toBe('help')
    expect(result.message).toContain('help nav')
    expect(result.message).toContain('open <target>')
    expect(result.message).toContain('theme <light|dark|toggle>')
    expect(result.message).toContain('lang <en|zh-CN|th-TH>')
  })

  it('documents workspace commands separately with protected destinations', () => {
    const result = evaluateTerminalCommand('help workspace', ownerContext)

    expect(result.status).toBe('help')
    expect(result.message).toContain('open workspace')
    expect(result.message).toContain('open courses-admin')
    expect(result.message).toContain('open security')
  })

  it('opens public and protected routes through the same terminal grammar', () => {
    expect(evaluateTerminalCommand('open blog', guestContext)).toMatchObject({
      status: 'run',
      command: 'open blog',
      to: '/blog',
    })

    expect(evaluateTerminalCommand('courses', guestContext)).toMatchObject({
      status: 'run',
      command: 'courses',
      to: '/courses',
    })

    expect(evaluateTerminalCommand('open blog-admin', guestContext)).toMatchObject({
      status: 'login-required',
      command: 'open blog-admin',
      permission: 'blog:category:manage',
    })

    expect(evaluateTerminalCommand('open blog-admin', ownerContext)).toMatchObject({
      status: 'run',
      command: 'open blog-admin',
      to: '/workspace/blog',
    })
  })

  it('returns explicit terminal actions for theme, language, and clear commands', () => {
    expect(evaluateTerminalCommand('theme dark', guestContext)).toMatchObject({
      status: 'theme',
      command: 'theme dark',
      themeMode: 'dark',
    })

    expect(evaluateTerminalCommand('lang zh', guestContext)).toMatchObject({
      status: 'language',
      command: 'lang zh',
      language: 'zh-CN',
    })

    expect(evaluateTerminalCommand('clear', guestContext)).toMatchObject({
      status: 'clear',
      command: 'clear',
    })
  })

  it('rejects unsupported command arguments with usage hints', () => {
    expect(evaluateTerminalCommand('theme sepia', guestContext)).toMatchObject({
      status: 'invalid',
      command: 'theme sepia',
    })
    expect(evaluateTerminalCommand('theme sepia', guestContext).message).toContain('theme <light|dark|toggle>')

    expect(evaluateTerminalCommand('lang de', guestContext)).toMatchObject({
      status: 'invalid',
      command: 'lang de',
    })
    expect(evaluateTerminalCommand('lang de', guestContext).message).toContain('lang <en|zh-CN|th-TH>')
  })
})

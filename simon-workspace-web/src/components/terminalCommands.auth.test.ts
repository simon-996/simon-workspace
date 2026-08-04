import { describe, expect, it } from 'vitest'

import {
  evaluateTerminalCommand,
  parseTerminalInput,
} from './terminalCommands'
import { messages } from '../i18n/messages'

const context = {
  isAuthenticated: false,
  username: '',
  hasPermission: () => false,
}

describe('terminal auth commands', () => {
  it('parses quoted command arguments', () => {
    expect(parseTerminalInput('open "blog admin"')).toEqual({
      command: 'open',
      args: ['blog admin'],
    })
  })

  it.each(['login', 'login simon', 'login simon secret'])(
    'navigates %s to the login page without returning credentials',
    (input) => {
      const result = evaluateTerminalCommand(input, context)

      expect(result).toMatchObject({ status: 'run', command: 'login', to: '/login' })
      expect(result).not.toHaveProperty('args')
      expect(JSON.stringify(result)).not.toContain('simon')
      expect(JSON.stringify(result)).not.toContain('secret')
    },
  )

  it('keeps session help credential-free while retaining account commands', () => {
    const result = evaluateTerminalCommand('help session', context)

    expect(result.message.split('\n')).toEqual(['login', 'logout', 'whoami'])
  })

  it('detects logout commands', () => {
    expect(evaluateTerminalCommand('logout', context).status).toBe('logout')
  })

  it('prints the current account for whoami', () => {
    const result = evaluateTerminalCommand('whoami', {
      isAuthenticated: true,
      username: 'simon',
      hasPermission: () => true,
    })

    expect(result.status).toBe('info')
    expect(result.message).toContain('simon')
  })

  it('does not ship obsolete terminal credential-login messages', () => {
    for (const message of Object.values(messages)) {
      expect(message.terminal).not.toHaveProperty('loginUsage')
      expect(message.terminal).not.toHaveProperty('loginSuccess')
    }
  })
})

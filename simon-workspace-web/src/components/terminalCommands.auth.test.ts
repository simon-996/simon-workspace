import { describe, expect, it } from 'vitest'

import {
  evaluateTerminalCommand,
  parseTerminalInput,
} from './terminalCommands'

const context = {
  isAuthenticated: false,
  username: '',
  hasPermission: () => false,
}

describe('terminal auth commands', () => {
  it('parses quoted command arguments', () => {
    expect(parseTerminalInput('login "chen ximeng" "secret pass"')).toEqual({
      command: 'login',
      args: ['chen ximeng', 'secret pass'],
    })
  })

  it('detects parameterized login and logout commands', () => {
    expect(evaluateTerminalCommand('login simon secret', context).status).toBe('login')
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
})

import assert from 'node:assert/strict'

import { evaluateTerminalCommand, terminalCommands } from '../src/components/terminalCommands.ts'

const anonymous = {
  isAuthenticated: false,
  hasPermission: () => false,
}

assert.equal(evaluateTerminalCommand('about', anonymous).status, 'run')
assert.equal(evaluateTerminalCommand('about', anonymous).to, '#about')

const loginRequired = evaluateTerminalCommand('site', anonymous)
assert.equal(loginRequired.status, 'login-required')
assert.equal(loginRequired.permission, 'site:config')

const forbidden = evaluateTerminalCommand('site', {
  isAuthenticated: true,
  hasPermission: () => false,
})
assert.equal(forbidden.status, 'forbidden')
assert.equal(forbidden.permission, 'site:config')

const allowed = evaluateTerminalCommand('site', {
  isAuthenticated: true,
  hasPermission: (permission) => permission === 'site:config',
})
assert.equal(allowed.status, 'run')
assert.equal(allowed.to, '/workspace/site')

assert.ok(terminalCommands.some((command) => command.command === 'courses' && command.permission === 'course:manage'))

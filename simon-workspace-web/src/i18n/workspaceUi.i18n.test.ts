import { describe, expect, it } from 'vitest'

import { messages } from './messages'

const workspaceNavigationPaths = [
  ['workspace', 'navGroups', 'teaching'],
  ['workspace', 'navGroups', 'content'],
  ['workspace', 'navGroups', 'records'],
  ['workspace', 'navGroups', 'system'],
  ['workspace', 'nav', 'more'],
] as const

function getMessageValue(message: unknown, path: ReadonlyArray<string>): unknown {
  return path.reduce<unknown>((current, key) => {
    if (typeof current !== 'object' || current === null || !(key in current)) {
      return undefined
    }

    return (current as Record<string, unknown>)[key]
  }, message)
}

describe('workspace navigation messages', () => {
  it('provides every workspace navigation label in each locale', () => {
    for (const [locale, message] of Object.entries(messages)) {
      for (const path of workspaceNavigationPaths) {
        const value = getMessageValue(message, path)
        const key = path.join('.')

        expect(value, `${locale}:${key}`).toEqual(expect.any(String))
        expect((value as string).trim(), `${locale}:${key}`).not.toBe('')
      }
    }
  })
})

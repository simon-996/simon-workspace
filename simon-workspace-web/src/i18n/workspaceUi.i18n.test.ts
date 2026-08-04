import { describe, expect, it } from 'vitest'

import { messages } from './messages'

const workspaceNavigationPaths = [
  ['workspace', 'navGroups', 'teaching'],
  ['workspace', 'navGroups', 'content'],
  ['workspace', 'navGroups', 'records'],
  ['workspace', 'navGroups', 'system'],
  ['workspace', 'nav', 'more'],
] as const

const workspaceHomePaths = [
  ['workspace', 'home', 'actions', 'courses'],
  ['workspace', 'home', 'actions', 'coursesHelp'],
  ['workspace', 'home', 'actions', 'upload'],
  ['workspace', 'home', 'actions', 'uploadHelp'],
  ['workspace', 'home', 'actions', 'write'],
  ['workspace', 'home', 'actions', 'writeHelp'],
  ['workspace', 'home', 'recent', 'title'],
  ['workspace', 'home', 'recent', 'courses'],
  ['workspace', 'home', 'recent', 'files'],
  ['workspace', 'home', 'recent', 'drafts'],
  ['workspace', 'home', 'recent', 'emptyCourses'],
  ['workspace', 'home', 'recent', 'emptyFiles'],
  ['workspace', 'home', 'recent', 'emptyDrafts'],
  ['workspace', 'home', 'recent', 'retry'],
  ['workspace', 'home', 'recent', 'loadFailed'],
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

  it('provides every workspace home label in each locale', () => {
    for (const [locale, message] of Object.entries(messages)) {
      for (const path of workspaceHomePaths) {
        const value = getMessageValue(message, path)
        const key = path.join('.')

        expect(value, `${locale}:${key}`).toEqual(expect.any(String))
        expect((value as string).trim(), `${locale}:${key}`).not.toBe('')
      }
    }
  })
})

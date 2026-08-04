import { describe, expect, it } from 'vitest'
import type { LocationQuery } from 'vue-router'

import { consumeFileUploadAction, shouldOpenFileUpload } from './fileUploadRoute'

describe('file upload route query', () => {
  const routeCases: Array<[LocationQuery, boolean]> = [
    [{ action: 'upload' }, true],
    [{ action: ['preview', 'upload'] }, true],
    [{ action: 'download' }, false],
    [{ action: ['preview', 'download'] }, false],
    [{ action: null }, false],
    [{}, false],
  ]

  it.each(routeCases)('opens only for an upload action in %o', (query, expected) => {
    expect(shouldOpenFileUpload(query)).toBe(expected)
  })

  it('removes only the action and preserves every other query value', () => {
    const query = {
      action: ['preview', 'upload'],
      keyword: 'lesson',
      page: '2',
      tag: ['pdf', 'public'],
    }

    expect(consumeFileUploadAction(query)).toEqual({
      keyword: 'lesson',
      page: '2',
      tag: ['pdf', 'public'],
    })
    expect(query.action).toEqual(['preview', 'upload'])
  })
})

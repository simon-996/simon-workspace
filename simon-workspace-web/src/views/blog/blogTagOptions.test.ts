import { describe, expect, it } from 'vitest'

import {
  buildTagOptions,
  createTagOption,
  normalizeSelectedTags,
  normalizeTagName,
} from './blogTagOptions'

describe('blog tag options', () => {
  it('normalizes whitespace and removes duplicates case-insensitively', () => {
    expect(normalizeTagName('  Spring   Boot  ')).toBe('Spring Boot')
    expect(normalizeSelectedTags([' Vue ', 'vue', 'Spring   Boot'])).toEqual([
      'Vue',
      'Spring Boot',
    ])
  })

  it('caps selected tags at the backend limit', () => {
    const values = Array.from({ length: 10 }, (_, index) => `Tag ${index + 1}`)

    expect(normalizeSelectedTags(values)).toHaveLength(8)
  })

  it('creates a normalized option from typed input', () => {
    expect(createTagOption('  New   Tag ')).toEqual({
      label: 'New Tag',
      value: 'New Tag',
    })
  })

  it('keeps selected spelling while merging reusable database tags', () => {
    expect(buildTagOptions(
      [{ name: 'Vue' }],
      ['vue', 'TypeScript'],
    )).toEqual([
      { label: 'vue', value: 'vue' },
      { label: 'TypeScript', value: 'TypeScript' },
    ])
  })
})

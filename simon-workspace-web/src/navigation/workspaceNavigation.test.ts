import { describe, expect, it } from 'vitest'

import { buildWorkspaceNavigation } from './workspaceNavigation'

describe('buildWorkspaceNavigation', () => {
  it('builds ordered desktop groups with unique item keys', () => {
    const navigation = buildWorkspaceNavigation(() => true)
    const itemKeys = navigation.groups.flatMap((group) => group.items.map((item) => item.key))

    expect(navigation.groups.map((group) => group.key)).toEqual(['teaching', 'content', 'records', 'system'])
    expect(new Set(itemKeys).size).toBe(itemKeys.length)
    expect(itemKeys).toContain('overview')
    expect(itemKeys).toContain('site')
  })

  it('uses fixed mobile destinations without duplicating them in more groups', () => {
    const navigation = buildWorkspaceNavigation(() => true)
    const mobileItemKeys = navigation.mobileItems.map((item) => item.key)
    const moreItemKeys = navigation.moreGroups.flatMap((group) => group.items.map((item) => item.key))

    expect(mobileItemKeys).toEqual(['overview', 'courses', 'files', 'blogPosts'])
    expect(moreItemKeys.filter((key) => mobileItemKeys.includes(key))).toEqual([])
  })

  it('filters forbidden items and empty groups from every navigation surface', () => {
    const permissions = new Set(['workspace:view', 'file:manage'])
    const navigation = buildWorkspaceNavigation((permission) => permissions.has(permission))

    expect(navigation.mobileItems.map((item) => item.key)).toEqual(['overview', 'files'])
    expect(navigation.groups.flatMap((group) => group.items.map((item) => item.key))).toEqual([
      'overview',
      'files',
      'storage',
    ])
    expect(navigation.groups.every((group) => group.items.length > 0)).toBe(true)
    expect(navigation.moreGroups.every((group) => group.items.length > 0)).toBe(true)
  })
})

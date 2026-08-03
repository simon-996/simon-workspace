import { describe, expect, it } from 'vitest'
import {
  Book,
  Calendar,
  CircleCheck,
  Cloud,
  FileText,
  Files,
  History,
  Notebook,
  Settings,
  Template,
  Users,
} from '@vicons/tabler'

import { buildWorkspaceNavigation, isWorkspaceNavigationActive } from './workspaceNavigation'

const expectedGroups = [
  {
    key: 'teaching',
    labelKey: 'workspace.navGroups.teaching',
    items: [
      {
        key: 'overview',
        to: '/workspace',
        labelKey: 'workspace.nav.overview',
        permission: 'workspace:view',
        icon: CircleCheck,
        mobile: true,
      },
      {
        key: 'courses',
        to: '/workspace/courses',
        labelKey: 'workspace.nav.courses',
        permission: 'course:manage',
        icon: Book,
        mobile: true,
      },
      {
        key: 'classes',
        to: '/workspace/classes',
        labelKey: 'workspace.nav.classes',
        permission: 'class:manage',
        icon: FileText,
        mobile: false,
      },
      {
        key: 'semesters',
        to: '/workspace/semesters',
        labelKey: 'workspace.nav.semesters',
        permission: 'semester:manage',
        icon: Calendar,
        mobile: false,
      },
    ],
  },
  {
    key: 'content',
    labelKey: 'workspace.navGroups.content',
    items: [
      {
        key: 'templates',
        to: '/workspace/templates',
        labelKey: 'workspace.nav.templates',
        permission: 'template:manage',
        icon: Template,
        mobile: false,
      },
      {
        key: 'files',
        to: '/workspace/files',
        labelKey: 'workspace.nav.files',
        permission: 'file:manage',
        icon: Files,
        mobile: true,
      },
      {
        key: 'storage',
        to: '/workspace/storage',
        labelKey: 'workspace.nav.storage',
        permission: 'file:manage',
        icon: Cloud,
        mobile: false,
      },
      {
        key: 'blogPosts',
        to: '/workspace/posts',
        labelKey: 'workspace.nav.blogPosts',
        permission: 'blog:post:create',
        icon: Notebook,
        mobile: true,
      },
      {
        key: 'blog',
        to: '/workspace/blog',
        labelKey: 'workspace.nav.blog',
        permission: 'blog:category:manage',
        icon: Notebook,
        mobile: false,
      },
    ],
  },
  {
    key: 'records',
    labelKey: 'workspace.navGroups.records',
    items: [
      {
        key: 'history',
        to: '/workspace/history',
        labelKey: 'workspace.nav.history',
        permission: 'generation:history',
        icon: History,
        mobile: false,
      },
    ],
  },
  {
    key: 'system',
    labelKey: 'workspace.navGroups.system',
    items: [
      {
        key: 'security',
        to: '/workspace/security',
        labelKey: 'workspace.nav.security',
        permission: 'user:manage',
        icon: Users,
        mobile: false,
      },
      {
        key: 'site',
        to: '/workspace/site',
        labelKey: 'workspace.nav.site',
        permission: 'site:config',
        icon: Settings,
        mobile: false,
      },
    ],
  },
] as const

describe('buildWorkspaceNavigation', () => {
  it('matches the complete ordered desktop navigation contract', () => {
    const navigation = buildWorkspaceNavigation(() => true)
    const itemKeys = navigation.groups.flatMap((group) => group.items.map((item) => item.key))

    expect(navigation.groups).toHaveLength(expectedGroups.length)
    navigation.groups.forEach((group, groupIndex) => {
      const expectedGroup = expectedGroups[groupIndex]

      expect({ key: group.key, labelKey: group.labelKey }).toEqual({
        key: expectedGroup.key,
        labelKey: expectedGroup.labelKey,
      })
      expect(group.items).toHaveLength(expectedGroup.items.length)
      group.items.forEach((item, itemIndex) => {
        const expectedItem = expectedGroup.items[itemIndex]
        const { icon: actualIcon, ...actualFields } = item
        const { icon: expectedIcon, ...expectedFields } = expectedItem

        expect(actualFields).toEqual(expectedFields)
        expect(actualIcon).toBe(expectedIcon)
      })
    })
    expect(new Set(itemKeys).size).toBe(itemKeys.length)
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
    const groupItemKeys = navigation.groups.flatMap((group) => group.items.map((item) => item.key))
    const mobileItemKeys = navigation.mobileItems.map((item) => item.key)
    const moreItemKeys = navigation.moreGroups.flatMap((group) => group.items.map((item) => item.key))

    expect(navigation.groups.map((group) => ({ key: group.key, items: group.items.map((item) => item.key) }))).toEqual([
      { key: 'teaching', items: ['overview'] },
      { key: 'content', items: ['files', 'storage'] },
    ])
    expect(mobileItemKeys).toEqual(['overview', 'files'])
    expect(navigation.moreGroups.map((group) => ({ key: group.key, items: group.items.map((item) => item.key) }))).toEqual([
      { key: 'content', items: ['storage'] },
    ])
    expect(groupItemKeys).toEqual(['overview', 'files', 'storage'])
    expect(moreItemKeys.filter((key) => mobileItemKeys.includes(key))).toEqual([])
    expect([...mobileItemKeys, ...moreItemKeys].sort()).toEqual([...groupItemKeys].sort())
  })

  it('isolates item objects between surfaces and subsequent builds', () => {
    const firstNavigation = buildWorkspaceNavigation(() => true)
    const groupOverview = firstNavigation.groups[0].items[0] as unknown as { to: string }
    const groupClasses = firstNavigation.groups[0].items[2] as unknown as { to: string }

    groupOverview.to = '/polluted'
    groupClasses.to = '/polluted'
    const observedRoutes = [
      firstNavigation.mobileItems[0].to,
      firstNavigation.moreGroups[0].items[0].to,
      buildWorkspaceNavigation(() => true).groups[0].items[0].to,
      buildWorkspaceNavigation(() => true).groups[0].items[2].to,
    ]
    groupOverview.to = '/workspace'
    groupClasses.to = '/workspace/classes'

    expect(observedRoutes).toEqual(['/workspace', '/workspace/classes', '/workspace', '/workspace/classes'])
  })
})

describe('isWorkspaceNavigationActive', () => {
  it('normalizes trailing slashes without treating sibling prefixes as descendants', () => {
    const targets = expectedGroups.flatMap((group) => group.items.map((item) => item.to))

    expect(targets.filter((to) => isWorkspaceNavigationActive('/workspace', to))).toEqual(['/workspace'])
    expect(targets.filter((to) => isWorkspaceNavigationActive('/workspace/', to))).toEqual(['/workspace'])
    expect(isWorkspaceNavigationActive('/workspace/courses', '/workspace/courses')).toBe(true)
    expect(isWorkspaceNavigationActive('/workspace/courses/42', '/workspace/courses')).toBe(true)
    expect(isWorkspaceNavigationActive('/workspace/courses-other', '/workspace/courses')).toBe(false)
    expect(isWorkspaceNavigationActive('/workspace/courses', '/workspace')).toBe(false)
  })
})

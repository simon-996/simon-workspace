import { describe, expect, it } from 'vitest'

import { buildHeaderNavItems } from './appHeaderNav'

describe('buildHeaderNavItems', () => {
  it('marks the current top-level route as active and disabled', () => {
    const items = buildHeaderNavItems({
      path: '/login',
      name: 'login',
      hash: '',
      authenticated: false,
      site: {
        blogVisible: true,
        projectsVisible: true,
        workspaceEntryVisible: true,
      },
    })

    expect(items.find((item) => item.key === 'login')).toMatchObject({
      active: true,
      disabled: true,
      to: '/login',
    })
    expect(items.find((item) => item.key === 'home')).toMatchObject({
      active: false,
      disabled: false,
    })
  })

  it('treats every workspace child page as the workspace navigation item', () => {
    const items = buildHeaderNavItems({
      path: '/workspace/courses',
      name: 'workspace-courses',
      hash: '',
      authenticated: true,
      site: {
        blogVisible: false,
        projectsVisible: false,
        workspaceEntryVisible: false,
      },
    })

    expect(items.find((item) => item.key === 'workspace')).toMatchObject({
      active: true,
      disabled: true,
      to: '/workspace',
    })
    expect(items.some((item) => item.key === 'blog')).toBe(false)
    expect(items.some((item) => item.key === 'projects')).toBe(false)
  })

  it('uses public site visibility to decide optional navigation entries', () => {
    const items = buildHeaderNavItems({
      path: '/',
      name: 'home',
      hash: '',
      authenticated: false,
      site: {
        blogVisible: true,
        projectsVisible: false,
        workspaceEntryVisible: true,
      },
    })

    expect(items.map((item) => item.key)).toEqual(['home', 'blog', 'login', 'workspace'])
    expect(items.find((item) => item.key === 'home')).toMatchObject({
      active: true,
      disabled: true,
    })
  })

  it('treats blog as a real top-level page instead of a home anchor', () => {
    const items = buildHeaderNavItems({
      path: '/blog',
      name: 'blog',
      hash: '',
      authenticated: true,
      site: {
        blogVisible: true,
        projectsVisible: false,
        workspaceEntryVisible: false,
      },
    })

    expect(items.find((item) => item.key === 'blog')).toMatchObject({
      to: '/blog',
      active: true,
      disabled: true,
    })
  })

  it('shows courses as a public top-level page when enabled', () => {
    const items = buildHeaderNavItems({
      path: '/courses/3',
      name: 'course-detail',
      hash: '',
      authenticated: false,
      site: {
        courseVisible: true,
        blogVisible: false,
        projectsVisible: false,
        workspaceEntryVisible: false,
      },
    })

    expect(items.find((item) => item.key === 'courses')).toMatchObject({
      to: '/courses',
      active: true,
      disabled: true,
    })
  })
})

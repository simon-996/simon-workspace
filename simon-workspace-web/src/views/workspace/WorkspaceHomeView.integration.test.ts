import { describe, expect, it } from 'vitest'

import source from './WorkspaceHomeView.vue?raw'

describe('WorkspaceHomeView', () => {
  it('offers only the three approved permission-filtered primary actions', () => {
    expect(source).toContain("{ to: '/workspace/courses', permission: 'course:manage' }")
    expect(source).toContain("{ to: '/workspace/files?action=upload', permission: 'file:manage' }")
    expect(source).toContain("{ to: '/blog/new', permission: 'blog:post:create' }")
    expect(source).toContain('auth.hasPermission')

    expect(source).not.toContain('workspace.home.apiReady')
    expect(source).not.toContain('workspace.home.modules')
    expect(source).not.toContain('const modules')
  })

  it('loads each permission-aware recent-work section', () => {
    expect(source).toContain('useWorkspaceOverview')
    expect(source).toContain('overview.loadAll()')
    expect(source).toContain('overview.courses.load')
    expect(source).toContain('overview.files.load')
    expect(source).toContain('overview.drafts.load')
    expect(source).toContain('overview.courses.enabled')
    expect(source).toContain('overview.files.enabled')
    expect(source).toContain('overview.drafts.enabled')
  })

  it('renders loading, error, retry, empty, and populated states without leaking inaccessible work', () => {
    expect(source.match(/<n-skeleton/g)).toHaveLength(3)
    expect(source.match(/role="alert"/g)).toHaveLength(3)
    expect(source).toContain(':disabled="coursesLoading"')
    expect(source).toContain(':disabled="filesLoading"')
    expect(source).toContain(':disabled="draftsLoading"')
    expect(source).toContain("t('workspace.home.recent.emptyCourses')")
    expect(source).toContain("t('workspace.home.recent.emptyFiles')")
    expect(source).toContain("t('workspace.home.recent.emptyDrafts')")
    expect(source).toContain('to="/workspace/courses"')
    expect(source).toContain('to="/workspace/files"')
    expect(source).toContain('`/blog/${draft.id}/edit`')
  })

  it('keeps the workspace compact and avoids summary metrics or gradients', () => {
    expect(source).toContain('@media (max-width: 860px)')
    expect(source).not.toMatch(/metric|summary-counter/i)
    expect(source).not.toMatch(/gradient/i)
  })
})

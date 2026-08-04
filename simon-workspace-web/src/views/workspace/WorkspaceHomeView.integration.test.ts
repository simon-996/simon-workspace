import { describe, expect, it } from 'vitest'

import source from './WorkspaceHomeView.vue?raw'

function expectRetryContract(
  sectionSource: string,
  names: { loading: string, retrying: string, visibleError: string, retry: string },
) {
  const errorGuard = `v-if="${names.visibleError}"`
  const loadingGuard = `v-if="${names.loading}"`

  expect(sectionSource).toContain(errorGuard)
  expect(sectionSource).toContain(`{{ ${names.visibleError} }}`)
  expect(sectionSource).toContain(`:disabled="${names.loading} || ${names.retrying}"`)
  expect(sectionSource).toContain(`@click="${names.retry}"`)
  expect(sectionSource).toContain(loadingGuard)
  expect(sectionSource).toContain('<div v-else class="recent-content">')
  expect(sectionSource.indexOf(errorGuard)).toBeLessThan(sectionSource.indexOf(loadingGuard))
}

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
    expect(source).toContain('useWorkspaceSectionRetry')
    expect(source.match(/<n-skeleton/g)).toHaveLength(3)
    expect(source.match(/role="alert"/g)).toHaveLength(3)
    const coursesSection = source.slice(
      source.indexOf('<section v-if="overview.courses.enabled"'),
      source.indexOf('<section v-if="overview.files.enabled"'),
    )
    const filesSection = source.slice(
      source.indexOf('<section v-if="overview.files.enabled"'),
      source.indexOf('<section v-if="overview.drafts.enabled"'),
    )
    const draftsSection = source.slice(source.indexOf('<section v-if="overview.drafts.enabled"'))

    expectRetryContract(coursesSection, {
      loading: 'coursesLoading',
      retrying: 'coursesRetrying',
      visibleError: 'coursesVisibleError',
      retry: 'retryCourses',
    })
    expectRetryContract(filesSection, {
      loading: 'filesLoading',
      retrying: 'filesRetrying',
      visibleError: 'filesVisibleError',
      retry: 'retryFiles',
    })
    expectRetryContract(draftsSection, {
      loading: 'draftsLoading',
      retrying: 'draftsRetrying',
      visibleError: 'draftsVisibleError',
      retry: 'retryDrafts',
    })
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

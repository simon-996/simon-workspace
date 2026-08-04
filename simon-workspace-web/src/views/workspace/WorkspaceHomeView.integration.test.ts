import { describe, expect, it } from 'vitest'

import source from './WorkspaceHomeView.vue?raw'

function expectRetryContract(
  sectionSource: string,
  names: { loading: string, pending: string, retrying: string, visibleError: string, retry: string },
) {
  const errorGuard = `v-if="${names.visibleError}"`
  const pendingGuard = `v-if="${names.pending}"`

  expect(sectionSource).toContain(errorGuard)
  expect(sectionSource).toContain(`{{ ${names.visibleError} }}`)
  expect(sectionSource).toContain(`:disabled="${names.loading} || ${names.retrying}"`)
  expect(sectionSource).toContain(`@click="${names.retry}"`)
  expect(sectionSource).toContain(pendingGuard)
  expect(sectionSource).toContain('<div v-else class="recent-content">')
  expect(sectionSource.indexOf(errorGuard)).toBeLessThan(sectionSource.indexOf(pendingGuard))
}

describe('WorkspaceHomeView', () => {
  it('renders primary actions from the shared permission-filtered builder', () => {
    expect(source).toContain('buildWorkspaceHomeActions')
    expect(source).toContain('buildWorkspaceHomeActions((permission) => auth.hasPermission(permission))')
    expect(source).toContain('<RouterLink v-for="action in actions"')
    expect(source).not.toContain('actionMetadata')
    expect(source).not.toContain('actionPresentation')
    expect(source).not.toContain('workspace.home.apiReady')
    expect(source).not.toContain('workspace.home.modules')
    expect(source).not.toContain('const modules')
    expect(source).not.toContain('/workspace/posts')
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
      source.indexOf('v-if="overview.courses.enabled"'),
      source.indexOf('v-if="overview.files.enabled"'),
    )
    const filesSection = source.slice(
      source.indexOf('v-if="overview.files.enabled"'),
      source.indexOf('v-if="overview.drafts.enabled"'),
    )
    const draftsSection = source.slice(source.indexOf('v-if="overview.drafts.enabled"'))

    expectRetryContract(coursesSection, {
      loading: 'coursesLoading',
      pending: 'coursesPending',
      retrying: 'coursesRetrying',
      visibleError: 'coursesVisibleError',
      retry: 'retryCourses',
    })
    expectRetryContract(filesSection, {
      loading: 'filesLoading',
      pending: 'filesPending',
      retrying: 'filesRetrying',
      visibleError: 'filesVisibleError',
      retry: 'retryFiles',
    })
    expectRetryContract(draftsSection, {
      loading: 'draftsLoading',
      pending: 'draftsPending',
      retrying: 'draftsRetrying',
      visibleError: 'draftsVisibleError',
      retry: 'retryDrafts',
    })
    expect(source).toContain("t('workspace.home.recent.emptyCourses')")
    expect(source).toContain("t('workspace.home.recent.emptyFiles')")
    expect(source).toContain("t('workspace.home.recent.emptyDrafts')")
    expect(source).toContain(':aria-busy="coursesPending"')
    expect(source).toContain(':aria-busy="filesPending"')
    expect(source).toContain(':aria-busy="draftsPending"')
    expect(source).toContain('to="/workspace/courses"')
    expect(source).toContain('to="/workspace/files"')
    expect(source).toContain('`/blog/${draft.id}/edit`')
  })

  it('uses shell-safe headings and hides unavailable recent work', () => {
    expect(source).not.toContain('<h1')
    expect(source).toContain("<h2>{{ t('workspace.home.title') }}</h2>")
    expect(source).toContain('<section v-if="hasRecentSections" class="recent-work">')
    expect(source).toContain('hasAvailableWork')
    expect(source).toContain("t('workspace.home.noAvailableTasks')")
    expect(source).toContain('role="status"')
  })

  it('keeps the workspace compact and avoids summary metrics or gradients', () => {
    expect(source).toContain('@media (max-width: 860px)')
    expect(source).toContain('repeat(auto-fit, minmax(min(100%, 250px), 1fr))')
    expect(source).toContain('@media (prefers-reduced-motion: reduce)')
    const reducedMotionStyles = source.slice(source.indexOf('@media (prefers-reduced-motion: reduce)'))
    expect(reducedMotionStyles).toContain(':deep(.n-skeleton)')
    expect(reducedMotionStyles).toContain('animation: none')
    const actionHelpStyles = source.match(/\.action-card small \{[\s\S]*?\}/)?.[0] ?? ''
    expect(actionHelpStyles).not.toContain('white-space: nowrap')
    expect(actionHelpStyles).not.toContain('text-overflow: ellipsis')
    expect(source).not.toMatch(/metric|summary-counter/i)
    expect(source).not.toMatch(/gradient/i)
  })
})

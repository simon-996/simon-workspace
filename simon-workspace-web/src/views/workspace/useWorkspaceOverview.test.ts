import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  fetchCourses,
  fetchFiles,
  fetchManageBlogPosts,
  type BlogPostSummary,
  type Course,
  type FileResource,
} from '../../api/workspace'
import { useWorkspaceOverview } from './useWorkspaceOverview'

vi.mock('../../api/workspace', () => ({
  fetchCourses: vi.fn(),
  fetchFiles: vi.fn(),
  fetchManageBlogPosts: vi.fn(),
}))

const mockedFetchCourses = vi.mocked(fetchCourses)
const mockedFetchFiles = vi.mocked(fetchFiles)
const mockedFetchManageBlogPosts = vi.mocked(fetchManageBlogPosts)

function course(id: string): Course {
  return {
    id,
    courseName: `Course ${id}`,
    totalHours: 32,
    status: 'ACTIVE',
  }
}

function file(id: string): FileResource {
  return {
    id,
    ownerUserId: 'owner-1',
    sourceType: 'UPLOAD',
    originalFilename: `file-${id}.pdf`,
    storageProvider: 'LOCAL',
    visibility: 'PRIVATE',
    fileSize: 1024,
    status: 'ACTIVE',
  }
}

function draft(id: string): BlogPostSummary {
  return {
    id,
    title: `Draft ${id}`,
    slug: `draft-${id}`,
    status: 'DRAFT',
    authorName: 'Author',
    authorUserId: 'author-1',
    tags: [],
    viewCount: 0,
    commentCount: 0,
  }
}

function createOverview(overrides: Partial<Parameters<typeof useWorkspaceOverview>[0]> = {}) {
  return useWorkspaceOverview({
    courses: true,
    files: true,
    drafts: true,
    fallbackError: () => 'fallback error',
    ...overrides,
  })
}

describe('useWorkspaceOverview', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('tracks initialization without flashing empty enabled sections before their first load', async () => {
    mockedFetchCourses.mockResolvedValue([])
    const overview = createOverview({ courses: true, files: false, drafts: false })

    expect(overview.courses.initialized.value).toBe(false)
    expect(overview.files.initialized.value).toBe(true)
    expect(overview.drafts.initialized.value).toBe(true)

    await overview.courses.load()

    expect(overview.courses.initialized.value).toBe(true)
    expect(overview.courses.loading.value).toBe(false)
  })

  it('loads only the first three items for every enabled section', async () => {
    const courses = Array.from({ length: 5 }, (_, index) => course(String(index + 1)))
    const files = Array.from({ length: 4 }, (_, index) => file(String(index + 1)))
    const drafts = Array.from({ length: 6 }, (_, index) => draft(String(index + 1)))
    mockedFetchCourses.mockResolvedValue(courses)
    mockedFetchFiles.mockResolvedValue(files)
    mockedFetchManageBlogPosts.mockResolvedValue(drafts)

    const overview = createOverview()
    await overview.loadAll()

    expect(overview.courses.items.value).toEqual(courses.slice(0, 3))
    expect(overview.files.items.value).toEqual(files.slice(0, 3))
    expect(overview.drafts.items.value).toEqual(drafts.slice(0, 3))
    expect(mockedFetchCourses).toHaveBeenCalledTimes(1)
    expect(mockedFetchFiles).toHaveBeenCalledTimes(1)
    expect(mockedFetchManageBlogPosts).toHaveBeenCalledExactlyOnceWith({ status: 'DRAFT' })
  })

  it('does not request disabled sections', async () => {
    mockedFetchFiles.mockResolvedValue([file('1')])

    const overview = createOverview({ courses: false, files: true, drafts: false })
    await overview.loadAll()

    expect(mockedFetchCourses).not.toHaveBeenCalled()
    expect(mockedFetchFiles).toHaveBeenCalledTimes(1)
    expect(mockedFetchManageBlogPosts).not.toHaveBeenCalled()
  })

  it('keeps successful section data when another section fails', async () => {
    const files = [file('1')]
    mockedFetchCourses.mockRejectedValue(new Error('course error'))
    mockedFetchFiles.mockResolvedValue(files)
    mockedFetchManageBlogPosts.mockResolvedValue([])

    const overview = createOverview()
    await overview.loadAll()

    expect(overview.courses.error.value).toBe('course error')
    expect(overview.files.items.value).toEqual(files)
    expect(overview.files.error.value).toBe('')
    expect(overview.drafts.items.value).toEqual([])
    expect(overview.drafts.error.value).toBe('')
  })

  it('clears an error and replaces items when a section retry succeeds', async () => {
    mockedFetchCourses.mockRejectedValueOnce(new Error('temporary error'))
    const overview = createOverview()

    await overview.courses.load()
    expect(overview.courses.error.value).toBe('temporary error')

    const courses = [course('1'), course('2')]
    mockedFetchCourses.mockResolvedValueOnce(courses)
    await overview.courses.load()

    expect(overview.courses.error.value).toBe('')
    expect(overview.courses.items.value).toEqual(courses)
  })

  it('uses the fallback message for non-Error rejections', async () => {
    const fallbackError = vi.fn(() => 'unknown error')
    mockedFetchFiles.mockRejectedValueOnce('failure')
    const overview = createOverview({ fallbackError })

    await overview.files.load()

    expect(overview.files.error.value).toBe('unknown error')
    expect(fallbackError).toHaveBeenCalledTimes(1)
  })
})

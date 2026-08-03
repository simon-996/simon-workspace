import { ref, shallowRef, type Ref, type ShallowRef } from 'vue'

import {
  fetchCourses,
  fetchFiles,
  fetchManageBlogPosts,
  type BlogPostSummary,
  type Course,
  type FileResource,
} from '../../api/workspace'

interface OverviewOptions {
  courses: boolean
  files: boolean
  drafts: boolean
  fallbackError: () => string
}

interface OverviewSection<T> {
  enabled: boolean
  items: ShallowRef<T[]>
  loading: Ref<boolean>
  error: Ref<string>
  load: () => Promise<void>
}

function createSection<T>(
  enabled: boolean,
  request: () => Promise<T[]>,
  fallbackError: () => string,
): OverviewSection<T> {
  const items = shallowRef<T[]>([])
  const loading = ref(false)
  const error = ref('')

  async function load(): Promise<void> {
    if (!enabled) {
      return
    }

    loading.value = true
    error.value = ''
    try {
      items.value = (await request()).slice(0, 3)
    } catch (caught) {
      error.value = caught instanceof Error ? caught.message : fallbackError()
    } finally {
      loading.value = false
    }
  }

  return { enabled, items, loading, error, load }
}

export function useWorkspaceOverview(options: OverviewOptions) {
  const courses = createSection<Course>(options.courses, () => fetchCourses(), options.fallbackError)
  const files = createSection<FileResource>(options.files, () => fetchFiles(), options.fallbackError)
  const drafts = createSection<BlogPostSummary>(
    options.drafts,
    () => fetchManageBlogPosts({ status: 'DRAFT' }),
    options.fallbackError,
  )

  async function loadAll(): Promise<void> {
    await Promise.all([courses.load(), files.load(), drafts.load()])
  }

  return { courses, files, drafts, loadAll }
}

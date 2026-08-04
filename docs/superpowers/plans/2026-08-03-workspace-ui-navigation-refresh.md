# Workspace UI and Navigation Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the module-first workspace home and scrolling mobile navigation with an action-first, permission-aware interface; add the missing file-upload UI; and remove credential entry and forced focus from the homepage terminal.

**Architecture:** A pure navigation module is the single source of truth for desktop groups, mobile destinations, permissions, routes, icons, and labels. A small overview composable owns independent recent-course, recent-file, and draft-post request states. File upload lives in a focused dialog component backed by the existing API, while terminal command evaluation stays pure and treats login as navigation.

**Tech Stack:** Vue 3, TypeScript, Vue Router, Pinia, Naive UI, vue-i18n, Axios, Vitest, Vite.

---

## File Map

### Create

- `simon-workspace-web/src/navigation/workspaceNavigation.ts`: canonical workspace groups, mobile destinations, and permission filtering.
- `simon-workspace-web/src/navigation/workspaceNavigation.test.ts`: pure navigation and permission tests.
- `simon-workspace-web/src/views/workspace/useWorkspaceOverview.ts`: independent loading state for recent courses, files, and drafts.
- `simon-workspace-web/src/views/workspace/useWorkspaceOverview.test.ts`: overview orchestration tests with mocked APIs.
- `simon-workspace-web/src/components/FileUploadDialog.vue`: upload form, validation, progress, and retry state.
- `simon-workspace-web/src/components/FileUploadDialog.test.ts`: source-contract test for dialog responsibilities.
- `simon-workspace-web/src/views/workspace/fileUploadRoute.ts`: pure helpers for consuming `?action=upload`.
- `simon-workspace-web/src/views/workspace/fileUploadRoute.test.ts`: query helper tests.
- `simon-workspace-web/src/views/workspace/WorkspaceView.navigation.test.ts`: shell integration contract.
- `simon-workspace-web/src/views/workspace/WorkspaceHomeView.integration.test.ts`: action and overview integration contract.
- `simon-workspace-web/src/views/workspace/FileCenterView.upload.test.ts`: file-center upload integration contract.
- `simon-workspace-web/src/i18n/workspaceUi.i18n.test.ts`: translation completeness tests.

### Modify

- `simon-workspace-web/src/views/WorkspaceView.vue`: grouped desktop navigation and fixed mobile navigation with a More drawer.
- `simon-workspace-web/src/views/workspace/WorkspaceHomeView.vue`: permission-aware quick actions and recent work.
- `simon-workspace-web/src/views/workspace/FileCenterView.vue`: upload button, query deep-link, and dialog integration.
- `simon-workspace-web/src/api/workspace.ts`: progress callback for `uploadFileResource`.
- `simon-workspace-web/src/api/workspace.upload.test.ts`: file-upload progress coverage.
- `simon-workspace-web/src/components/terminalCommands.ts`: login navigation and safe help text.
- `simon-workspace-web/src/components/terminalCommands.auth.test.ts`: credential-discarding regression tests.
- `simon-workspace-web/src/components/TerminalPanel.vue`: remove login side effect and autofocus prop.
- `simon-workspace-web/src/components/TerminalPanel.focus.test.ts`: pointer-focus-only contract.
- `simon-workspace-web/src/views/HomeView.vue`: stop passing terminal autofocus intent.
- `simon-workspace-web/src/views/HomeView.layout.test.ts`: no-autofocus regression assertion.
- `simon-workspace-web/src/i18n/locales/zh-CN.ts`: Chinese navigation, home, upload, and terminal copy.
- `simon-workspace-web/src/i18n/locales/en.ts`: English navigation, home, upload, and terminal copy.
- `simon-workspace-web/src/i18n/locales/th-TH.ts`: Thai navigation, home, upload, and terminal copy.
- `docs/design-interaction.md`: distinguish public mobile navigation from workspace mobile navigation.

## Task 1: Build the Canonical Workspace Navigation Model

**Files:**
- Create: `simon-workspace-web/src/navigation/workspaceNavigation.test.ts`
- Create: `simon-workspace-web/src/navigation/workspaceNavigation.ts`

- [ ] **Step 1: Write the failing navigation tests**

```ts
import { describe, expect, it } from 'vitest'

import { buildWorkspaceNavigation } from './workspaceNavigation'

describe('workspace navigation', () => {
  it('groups every permitted workspace route once', () => {
    const navigation = buildWorkspaceNavigation(() => true)
    const desktopKeys = navigation.groups.flatMap((group) => group.items.map((item) => item.key))

    expect(navigation.groups.map((group) => group.key)).toEqual([
      'teaching',
      'content',
      'records',
      'system',
    ])
    expect(new Set(desktopKeys).size).toBe(desktopKeys.length)
    expect(desktopKeys).toContain('overview')
    expect(desktopKeys).toContain('site')
  })

  it('keeps the approved mobile destinations stable', () => {
    const navigation = buildWorkspaceNavigation(() => true)

    expect(navigation.mobileItems.map((item) => item.key)).toEqual([
      'overview',
      'courses',
      'files',
      'blogPosts',
    ])
    expect(navigation.moreGroups.flatMap((group) => group.items.map((item) => item.key))).not.toEqual(
      expect.arrayContaining(['overview', 'courses', 'files', 'blogPosts']),
    )
  })

  it('removes forbidden items and empty groups without substitutions', () => {
    const permissions = new Set(['workspace:view', 'file:manage'])
    const navigation = buildWorkspaceNavigation((permission) => permissions.has(permission))

    expect(navigation.mobileItems.map((item) => item.key)).toEqual(['overview', 'files'])
    expect(navigation.groups.flatMap((group) => group.items.map((item) => item.key))).toEqual([
      'overview',
      'files',
      'storage',
    ])
    expect(navigation.groups.every((group) => group.items.length > 0)).toBe(true)
  })
})
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
npm --prefix simon-workspace-web test -- src/navigation/workspaceNavigation.test.ts
```

Expected: FAIL because `workspaceNavigation.ts` does not exist.

- [ ] **Step 3: Implement the navigation module**

```ts
import type { Component } from 'vue'
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

export type WorkspaceNavGroupKey = 'teaching' | 'content' | 'records' | 'system'
export type WorkspaceNavItemKey =
  | 'overview'
  | 'courses'
  | 'classes'
  | 'semesters'
  | 'templates'
  | 'files'
  | 'storage'
  | 'history'
  | 'blogPosts'
  | 'blog'
  | 'security'
  | 'site'

export interface WorkspaceNavItem {
  key: WorkspaceNavItemKey
  to: string
  labelKey: string
  permission: string
  icon: Component
  mobile: boolean
}

export interface WorkspaceNavGroup {
  key: WorkspaceNavGroupKey
  labelKey: string
  items: WorkspaceNavItem[]
}

const groups: WorkspaceNavGroup[] = [
  {
    key: 'teaching',
    labelKey: 'workspace.navGroups.teaching',
    items: [
      { key: 'overview', to: '/workspace', labelKey: 'workspace.nav.overview', permission: 'workspace:view', icon: CircleCheck, mobile: true },
      { key: 'courses', to: '/workspace/courses', labelKey: 'workspace.nav.courses', permission: 'course:manage', icon: Book, mobile: true },
      { key: 'classes', to: '/workspace/classes', labelKey: 'workspace.nav.classes', permission: 'class:manage', icon: FileText, mobile: false },
      { key: 'semesters', to: '/workspace/semesters', labelKey: 'workspace.nav.semesters', permission: 'semester:manage', icon: Calendar, mobile: false },
    ],
  },
  {
    key: 'content',
    labelKey: 'workspace.navGroups.content',
    items: [
      { key: 'templates', to: '/workspace/templates', labelKey: 'workspace.nav.templates', permission: 'template:manage', icon: Template, mobile: false },
      { key: 'files', to: '/workspace/files', labelKey: 'workspace.nav.files', permission: 'file:manage', icon: Files, mobile: true },
      { key: 'storage', to: '/workspace/storage', labelKey: 'workspace.nav.storage', permission: 'file:manage', icon: Cloud, mobile: false },
      { key: 'blogPosts', to: '/workspace/posts', labelKey: 'workspace.nav.blogPosts', permission: 'blog:post:create', icon: Notebook, mobile: true },
      { key: 'blog', to: '/workspace/blog', labelKey: 'workspace.nav.blog', permission: 'blog:category:manage', icon: Notebook, mobile: false },
    ],
  },
  {
    key: 'records',
    labelKey: 'workspace.navGroups.records',
    items: [
      { key: 'history', to: '/workspace/history', labelKey: 'workspace.nav.history', permission: 'generation:history', icon: History, mobile: false },
    ],
  },
  {
    key: 'system',
    labelKey: 'workspace.navGroups.system',
    items: [
      { key: 'security', to: '/workspace/security', labelKey: 'workspace.nav.security', permission: 'user:manage', icon: Users, mobile: false },
      { key: 'site', to: '/workspace/site', labelKey: 'workspace.nav.site', permission: 'site:config', icon: Settings, mobile: false },
    ],
  },
]

export function buildWorkspaceNavigation(hasPermission: (permission: string) => boolean) {
  const permittedGroups = groups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => hasPermission(item.permission)),
    }))
    .filter((group) => group.items.length > 0)
  const mobileItems = permittedGroups.flatMap((group) => group.items).filter((item) => item.mobile)
  const mobileKeys = new Set(mobileItems.map((item) => item.key))
  const moreGroups = permittedGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => !mobileKeys.has(item.key)),
    }))
    .filter((group) => group.items.length > 0)

  return { groups: permittedGroups, mobileItems, moreGroups }
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run the command from Step 2.

Expected: 3 tests pass.

- [ ] **Step 5: Commit the navigation model**

```powershell
git add simon-workspace-web/src/navigation/workspaceNavigation.ts simon-workspace-web/src/navigation/workspaceNavigation.test.ts
git commit -m "refactor(web): centralize workspace navigation"
```

## Task 2: Render Grouped Desktop and Fixed Mobile Navigation

**Files:**
- Create: `simon-workspace-web/src/views/workspace/WorkspaceView.navigation.test.ts`
- Modify: `simon-workspace-web/src/views/WorkspaceView.vue`

- [ ] **Step 1: Write the failing shell contract test**

```ts
import { describe, expect, it } from 'vitest'

import source from '../WorkspaceView.vue?raw'

describe('WorkspaceView navigation shell', () => {
  it('renders desktop and mobile navigation from the shared model', () => {
    expect(source).toContain('buildWorkspaceNavigation')
    expect(source).toContain('navigation.groups')
    expect(source).toContain('navigation.mobileItems')
    expect(source).toContain('navigation.moreGroups')
  })

  it('uses an accessible More drawer instead of a scrolling tab list', () => {
    expect(source).toContain('<n-drawer')
    expect(source).toContain("t('workspace.nav.more')")
    expect(source).toContain('moreOpen')
    expect(source).not.toContain('overflow-x: auto')
  })
})
```

- [ ] **Step 2: Run the focused test and verify RED**

```powershell
npm --prefix simon-workspace-web test -- src/views/workspace/WorkspaceView.navigation.test.ts
```

Expected: FAIL because the shell still contains its local flat `navItems` array and scrolling mobile tabs.

- [ ] **Step 3: Replace duplicate navigation state with the shared model**

Use this script structure in `WorkspaceView.vue`:

```ts
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { Menu2 } from '@vicons/tabler'
import { NDrawer, NDrawerContent, NIcon } from 'naive-ui'

import AppHeader from '../components/AppHeader.vue'
import { buildWorkspaceNavigation } from '../navigation/workspaceNavigation'
import { useAuthStore } from '../stores/auth'

const { t } = useI18n()
const route = useRoute()
const auth = useAuthStore()
const moreOpen = ref(false)
const navigation = computed(() => buildWorkspaceNavigation((permission) => auth.hasPermission(permission)))
const pageTitle = computed(() => {
  const titleKey = typeof route.meta.titleKey === 'string' ? route.meta.titleKey : 'workspace.title'
  return t(titleKey)
})

watch(() => route.fullPath, () => {
  moreOpen.value = false
})
```

Render desktop groups with a translated group label and the existing exact-active link behavior. Render mobile direct links followed by a real button for More:

```vue
<aside class="workspace-sider" :aria-label="t('workspace.aria')">
  <nav class="side-nav">
    <section v-for="group in navigation.groups" :key="group.key" class="nav-group">
      <span class="nav-group-label">{{ t(group.labelKey) }}</span>
      <RouterLink v-for="item in group.items" :key="item.key" :to="item.to" class="nav-link">
        <n-icon :component="item.icon" />
        <span>{{ t(item.labelKey) }}</span>
      </RouterLink>
    </section>
  </nav>
</aside>

<nav class="mobile-tabs" :aria-label="t('workspace.mobileAria')">
  <RouterLink v-for="item in navigation.mobileItems" :key="item.key" :to="item.to">
    <n-icon :component="item.icon" />
    <span>{{ t(item.labelKey) }}</span>
  </RouterLink>
  <button v-if="navigation.moreGroups.length" type="button" :aria-expanded="moreOpen" @click="moreOpen = true">
    <n-icon :component="Menu2" />
    <span>{{ t('workspace.nav.more') }}</span>
  </button>
</nav>

<n-drawer v-model:show="moreOpen" placement="bottom" height="min(72dvh, 560px)">
  <n-drawer-content :title="t('workspace.nav.more')" closable>
    <nav class="more-nav" :aria-label="t('workspace.nav.more')">
      <section v-for="group in navigation.moreGroups" :key="group.key" class="more-group">
        <span class="nav-group-label">{{ t(group.labelKey) }}</span>
        <RouterLink v-for="item in group.items" :key="item.key" :to="item.to" class="more-link">
          <n-icon :component="item.icon" />
          <span>{{ t(item.labelKey) }}</span>
        </RouterLink>
      </section>
    </nav>
  </n-drawer-content>
</n-drawer>
```

Change the mobile styles so the bar uses non-scrolling equal-width items:

```css
.mobile-tabs {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 20;
  display: flex;
  min-height: 64px;
  border-top: 1px solid var(--sw-border);
  background: var(--sw-panel-bg-strong);
  backdrop-filter: blur(16px);
}

.mobile-tabs a,
.mobile-tabs button {
  display: grid;
  flex: 1 1 0;
  min-width: 0;
  place-items: center;
  align-content: center;
  gap: 3px;
  border: 0;
  background: transparent;
  color: var(--sw-muted);
  font-size: 11px;
  font-weight: 700;
}
```

Use existing CSS tokens for group borders, labels, active states, and the drawer. Do not add literal light-only colors.

- [ ] **Step 4: Run navigation tests and verify GREEN**

```powershell
npm --prefix simon-workspace-web test -- src/navigation/workspaceNavigation.test.ts src/views/workspace/WorkspaceView.navigation.test.ts
```

Expected: 5 tests pass.

- [ ] **Step 5: Commit the workspace shell**

```powershell
git add simon-workspace-web/src/views/WorkspaceView.vue simon-workspace-web/src/views/workspace/WorkspaceView.navigation.test.ts
git commit -m "feat(web): group workspace navigation"
```

## Task 3: Add Independent Recent-Work State

**Files:**
- Create: `simon-workspace-web/src/views/workspace/useWorkspaceOverview.test.ts`
- Create: `simon-workspace-web/src/views/workspace/useWorkspaceOverview.ts`

- [ ] **Step 1: Write failing composable tests**

```ts
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { fetchCourses, fetchFiles, fetchManageBlogPosts } from '../../api/workspace'
import { useWorkspaceOverview } from './useWorkspaceOverview'

vi.mock('../../api/workspace', () => ({
  fetchCourses: vi.fn(),
  fetchFiles: vi.fn(),
  fetchManageBlogPosts: vi.fn(),
}))

describe('useWorkspaceOverview', () => {
  beforeEach(() => vi.resetAllMocks())

  it('loads enabled sections independently and keeps only three recent rows', async () => {
    vi.mocked(fetchCourses).mockResolvedValue(Array.from({ length: 5 }, (_, index) => ({ id: String(index), courseName: `Course ${index}` })) as never)
    vi.mocked(fetchFiles).mockResolvedValue(Array.from({ length: 4 }, (_, index) => ({ id: String(index), originalFilename: `File ${index}` })) as never)
    vi.mocked(fetchManageBlogPosts).mockResolvedValue(Array.from({ length: 6 }, (_, index) => ({ id: String(index), title: `Draft ${index}` })) as never)
    const overview = useWorkspaceOverview({ courses: true, files: true, drafts: true, fallbackError: () => 'failed' })

    await overview.loadAll()

    expect(overview.courses.items.value).toHaveLength(3)
    expect(overview.files.items.value).toHaveLength(3)
    expect(overview.drafts.items.value).toHaveLength(3)
    expect(fetchManageBlogPosts).toHaveBeenCalledWith({ status: 'DRAFT' })
  })

  it('does not request forbidden sections', async () => {
    const overview = useWorkspaceOverview({ courses: false, files: true, drafts: false, fallbackError: () => 'failed' })
    vi.mocked(fetchFiles).mockResolvedValue([])

    await overview.loadAll()

    expect(fetchCourses).not.toHaveBeenCalled()
    expect(fetchManageBlogPosts).not.toHaveBeenCalled()
    expect(fetchFiles).toHaveBeenCalledOnce()
  })

  it('keeps one section failure from clearing successful sections', async () => {
    vi.mocked(fetchCourses).mockRejectedValue(new Error('course error'))
    vi.mocked(fetchFiles).mockResolvedValue([{ id: 'file-1', originalFilename: 'notes.pdf' }] as never)
    vi.mocked(fetchManageBlogPosts).mockResolvedValue([])
    const overview = useWorkspaceOverview({ courses: true, files: true, drafts: true, fallbackError: () => 'failed' })

    await overview.loadAll()

    expect(overview.courses.error.value).toBe('course error')
    expect(overview.files.items.value).toHaveLength(1)
    expect(overview.drafts.error.value).toBe('')
  })
})
```

- [ ] **Step 2: Run the focused test and verify RED**

```powershell
npm --prefix simon-workspace-web test -- src/views/workspace/useWorkspaceOverview.test.ts
```

Expected: FAIL because the composable does not exist.

- [ ] **Step 3: Implement isolated section loaders**

```ts
import { ref, shallowRef } from 'vue'

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

function createSection<T>(enabled: boolean, request: () => Promise<T[]>, fallbackError: () => string) {
  const items = shallowRef<T[]>([])
  const loading = ref(false)
  const error = ref('')

  async function load() {
    if (!enabled) return
    loading.value = true
    error.value = ''
    try {
      items.value = (await request()).slice(0, 3)
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : fallbackError()
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

  async function loadAll() {
    await Promise.all([courses.load(), files.load(), drafts.load()])
  }

  return { courses, files, drafts, loadAll }
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run the command from Step 2.

Expected: 3 tests pass.

- [ ] **Step 5: Commit the overview state**

```powershell
git add simon-workspace-web/src/views/workspace/useWorkspaceOverview.ts simon-workspace-web/src/views/workspace/useWorkspaceOverview.test.ts
git commit -m "refactor(web): isolate workspace overview loading"
```

## Task 4: Replace the Workspace Home with Task-First UI

**Files:**
- Create: `simon-workspace-web/src/views/workspace/WorkspaceHomeView.integration.test.ts`
- Modify: `simon-workspace-web/src/views/workspace/WorkspaceHomeView.vue`
- Modify: `simon-workspace-web/src/views/workspace/BlogPostManagementView.integration.test.ts`

- [ ] **Step 1: Write the failing home integration contract**

```ts
import { describe, expect, it } from 'vitest'

import source from './WorkspaceHomeView.vue?raw'

describe('WorkspaceHomeView', () => {
  it('leads with the three approved permission-aware tasks', () => {
    expect(source).toContain("to: '/workspace/courses'")
    expect(source).toContain("to: '/workspace/files?action=upload'")
    expect(source).toContain("to: '/blog/new'")
    expect(source).toContain("permission: 'course:manage'")
    expect(source).toContain("permission: 'file:manage'")
    expect(source).toContain("permission: 'blog:post:create'")
    expect(source).not.toContain('workspace.home.apiReady')
  })

  it('uses independent recent-work states and retry actions', () => {
    expect(source).toContain('useWorkspaceOverview')
    expect(source).toContain('overview.courses.load')
    expect(source).toContain('overview.files.load')
    expect(source).toContain('overview.drafts.load')
  })
})
```

Update `BlogPostManagementView.integration.test.ts` so it checks that the home links to `/blog/new` and that mobile navigation still exposes `/workspace/posts`; remove the old assertion that the home itself links to `/workspace/posts`.

- [ ] **Step 2: Run the focused tests and verify RED**

```powershell
npm --prefix simon-workspace-web test -- src/views/workspace/WorkspaceHomeView.integration.test.ts src/views/workspace/BlogPostManagementView.integration.test.ts
```

Expected: FAIL because the current home renders API-ready module cards.

- [ ] **Step 3: Implement the task-first page**

Use permission-filtered action metadata and the overview composable:

```ts
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Book, FileUpload, Notes } from '@vicons/tabler'

import { useAuthStore } from '../../stores/auth'
import { useWorkspaceOverview } from './useWorkspaceOverview'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const actions = computed(() => [
  { key: 'courses', titleKey: 'workspace.home.actions.courses', descriptionKey: 'workspace.home.actions.coursesHelp', to: '/workspace/courses', permission: 'course:manage', icon: Book },
  { key: 'upload', titleKey: 'workspace.home.actions.upload', descriptionKey: 'workspace.home.actions.uploadHelp', to: '/workspace/files?action=upload', permission: 'file:manage', icon: FileUpload },
  { key: 'write', titleKey: 'workspace.home.actions.write', descriptionKey: 'workspace.home.actions.writeHelp', to: '/blog/new', permission: 'blog:post:create', icon: Notes },
].filter((action) => auth.hasPermission(action.permission)))
const overview = useWorkspaceOverview({
  courses: auth.hasPermission('course:manage'),
  files: auth.hasPermission('file:manage'),
  drafts: auth.hasPermission('blog:post:create'),
  fallbackError: () => t('workspace.home.recent.loadFailed'),
})

onMounted(() => void overview.loadAll())
```

The template must contain:

- A compact page intro, not a marketing hero.
- A quick-action grid with one to three cards, each a real button or router link.
- Three explicit recent sections guarded by `section.enabled`.
- Skeleton rows when a section is loading.
- Inline error text and a retry button calling that section's `load` function.
- An empty message when a successful section has no rows.
- Course and file rows linking to their management page.
- Draft rows linking to `/blog/{id}/edit`.

Use a two-column recent-work grid on wide screens and one column below 860 px. Keep borders, 8 px radius, and existing theme variables. Avoid metric-card counters and decorative gradients.

- [ ] **Step 4: Run home and overview tests and verify GREEN**

```powershell
npm --prefix simon-workspace-web test -- src/views/workspace/useWorkspaceOverview.test.ts src/views/workspace/WorkspaceHomeView.integration.test.ts src/views/workspace/BlogPostManagementView.integration.test.ts
```

Expected: all focused tests pass.

- [ ] **Step 5: Commit the task-first home**

```powershell
git add simon-workspace-web/src/views/workspace/WorkspaceHomeView.vue simon-workspace-web/src/views/workspace/WorkspaceHomeView.integration.test.ts simon-workspace-web/src/views/workspace/BlogPostManagementView.integration.test.ts
git commit -m "feat(web): make workspace home task first"
```

## Task 5: Add Upload Progress to the Existing File API and Build the Dialog

**Files:**
- Modify: `simon-workspace-web/src/api/workspace.upload.test.ts`
- Modify: `simon-workspace-web/src/api/workspace.ts`
- Create: `simon-workspace-web/src/components/FileUploadDialog.test.ts`
- Create: `simon-workspace-web/src/components/FileUploadDialog.vue`

- [ ] **Step 1: Write failing API and dialog tests**

Append this test to `workspace.upload.test.ts`:

```ts
it('reports upload progress for file-center uploads', async () => {
  vi.mocked(http.post).mockImplementation(async (_url, _data, config) => {
    config?.onUploadProgress?.({ loaded: 3, total: 4, bytes: 3, lengthComputable: true })
    return { data: { code: 0, data: { id: 'file-2', originalFilename: 'notes.pdf' } } }
  })
  const onProgress = vi.fn()

  const result = await uploadFileResource(new File(['notes'], 'notes.pdf'), 'PRIVATE', onProgress)

  expect(result.originalFilename).toBe('notes.pdf')
  expect(onProgress).toHaveBeenCalledWith(75)
})
```

Create the source-contract test:

```ts
import { describe, expect, it } from 'vitest'

import source from './FileUploadDialog.vue?raw'

describe('FileUploadDialog', () => {
  it('owns selection, visibility, progress, validation, and retry state', () => {
    expect(source).toContain('uploadFileResource')
    expect(source).toContain("ref<'PRIVATE' | 'PUBLIC'>('PRIVATE')")
    expect(source).toContain('uploadProgress')
    expect(source).toContain('<n-progress')
    expect(source).toContain('uploadError')
    expect(source).toContain("emit('uploaded'")
  })
})
```

- [ ] **Step 2: Run focused tests and verify RED**

```powershell
npm --prefix simon-workspace-web test -- src/api/workspace.upload.test.ts src/components/FileUploadDialog.test.ts
```

Expected: FAIL because `uploadFileResource` has no progress callback and the dialog does not exist.

- [ ] **Step 3: Extend the API helper and implement the dialog**

Change the API signature and Axios configuration:

```ts
export async function uploadFileResource(
  file: File,
  visibility: 'PRIVATE' | 'PUBLIC' = 'PRIVATE',
  onProgress?: (progress: number) => void,
) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<FileResource>>('/files', formData, {
    params: { sourceType: 'UPLOAD', visibility },
    onUploadProgress: (event) => {
      if (!event.total) return
      onProgress?.(Math.min(100, Math.round((event.loaded / event.total) * 100)))
    },
  })
  return unwrapApiResponse(response.data)
}
```

Implement `FileUploadDialog.vue` with this state contract:

```ts
const props = defineProps<{ show: boolean }>()
const emit = defineEmits<{
  'update:show': [value: boolean]
  uploaded: [file: FileResource]
}>()
const selectedFile = ref<File | null>(null)
const visibility = ref<'PRIVATE' | 'PUBLIC'>('PRIVATE')
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadError = ref('')

function reset() {
  selectedFile.value = null
  visibility.value = 'PRIVATE'
  uploadProgress.value = 0
  uploadError.value = ''
}

function selectFile(event: Event) {
  selectedFile.value = (event.target as HTMLInputElement).files?.[0] ?? null
  uploadError.value = ''
}

async function submit() {
  if (!selectedFile.value) {
    uploadError.value = t('workspace.files.upload.fileRequired')
    return
  }
  uploading.value = true
  uploadError.value = ''
  try {
    const resource = await uploadFileResource(selectedFile.value, visibility.value, (value) => {
      uploadProgress.value = value
    })
    emit('uploaded', resource)
    emit('update:show', false)
    reset()
  } catch (cause) {
    uploadError.value = cause instanceof Error ? cause.message : t('workspace.files.upload.failed')
  } finally {
    uploading.value = false
  }
}
```

Render an `n-modal` card with a native labelled file input, filename/size summary, `n-radio-group` for private/public visibility, `n-progress` during upload, inline `role="alert"` error text, Cancel, and Upload buttons. Disable close and file changes only while the request is actively uploading; keep state intact after a failed request.

- [ ] **Step 4: Run focused tests and verify GREEN**

Run the command from Step 2.

Expected: upload API and dialog tests pass.

- [ ] **Step 5: Commit the upload component**

```powershell
git add simon-workspace-web/src/api/workspace.ts simon-workspace-web/src/api/workspace.upload.test.ts simon-workspace-web/src/components/FileUploadDialog.vue simon-workspace-web/src/components/FileUploadDialog.test.ts
git commit -m "feat(web): add file upload dialog"
```

## Task 6: Integrate Upload Deep-Linking into the File Center

**Files:**
- Create: `simon-workspace-web/src/views/workspace/fileUploadRoute.test.ts`
- Create: `simon-workspace-web/src/views/workspace/fileUploadRoute.ts`
- Create: `simon-workspace-web/src/views/workspace/FileCenterView.upload.test.ts`
- Modify: `simon-workspace-web/src/views/workspace/FileCenterView.vue`

- [ ] **Step 1: Write failing query and integration tests**

```ts
import { describe, expect, it } from 'vitest'

import { consumeFileUploadAction, shouldOpenFileUpload } from './fileUploadRoute'

describe('file upload route helpers', () => {
  it('recognizes only the upload action', () => {
    expect(shouldOpenFileUpload({ action: 'upload' })).toBe(true)
    expect(shouldOpenFileUpload({ action: 'download' })).toBe(false)
    expect(shouldOpenFileUpload({})).toBe(false)
  })

  it('removes only the consumed action parameter', () => {
    expect(consumeFileUploadAction({ action: 'upload', keyword: 'lesson' })).toEqual({ keyword: 'lesson' })
  })
})
```

```ts
import { describe, expect, it } from 'vitest'

import source from './FileCenterView.vue?raw'

describe('FileCenterView upload integration', () => {
  it('opens uploads from the toolbar and route action, then reloads the list', () => {
    expect(source).toContain('FileUploadDialog')
    expect(source).toContain('shouldOpenFileUpload(route.query)')
    expect(source).toContain('consumeFileUploadAction(route.query)')
    expect(source).toContain('@uploaded="handleUploaded"')
    expect(source).toContain('uploadOpen.value = true')
  })
})
```

- [ ] **Step 2: Run focused tests and verify RED**

```powershell
npm --prefix simon-workspace-web test -- src/views/workspace/fileUploadRoute.test.ts src/views/workspace/FileCenterView.upload.test.ts
```

Expected: FAIL because the helpers and integration do not exist.

- [ ] **Step 3: Implement query consumption and file-center integration**

```ts
import type { LocationQuery, LocationQueryRaw } from 'vue-router'

export function shouldOpenFileUpload(query: LocationQuery) {
  return query.action === 'upload' || (Array.isArray(query.action) && query.action.includes('upload'))
}

export function consumeFileUploadAction(query: LocationQuery): LocationQueryRaw {
  const next = { ...query }
  delete next.action
  return next
}
```

In `FileCenterView.vue`, import `useRoute`, `useRouter`, `FileUpload`, `FileUploadDialog`, and the helpers. Add `const uploadOpen = ref(false)`. On mount, load files and consume a route upload action:

```ts
onMounted(() => {
  void loadFiles()
  if (shouldOpenFileUpload(route.query)) {
    uploadOpen.value = true
    void router.replace({ query: consumeFileUploadAction(route.query) })
  }
})

async function handleUploaded(file: FileResource) {
  message.success(t('workspace.files.upload.succeeded', { filename: file.originalFilename }))
  await loadFiles()
}
```

Add a primary Upload button to the toolbar and an Upload action to the empty state. Render:

```vue
<FileUploadDialog v-model:show="uploadOpen" @uploaded="handleUploaded" />
```

Keep search and refresh in the same compact toolbar. On mobile, let toolbar actions wrap into a full-width two-button row without horizontal overflow.

- [ ] **Step 4: Run upload and file-center tests and verify GREEN**

```powershell
npm --prefix simon-workspace-web test -- src/api/workspace.upload.test.ts src/components/FileUploadDialog.test.ts src/views/workspace/fileUploadRoute.test.ts src/views/workspace/FileCenterView.upload.test.ts
```

Expected: all focused tests pass.

- [ ] **Step 5: Commit file-center integration**

```powershell
git add simon-workspace-web/src/views/workspace/FileCenterView.vue simon-workspace-web/src/views/workspace/FileCenterView.upload.test.ts simon-workspace-web/src/views/workspace/fileUploadRoute.ts simon-workspace-web/src/views/workspace/fileUploadRoute.test.ts
git commit -m "feat(web): open file center directly into upload"
```

## Task 7: Make Terminal Login Safe and Remove Forced Focus

**Files:**
- Modify: `simon-workspace-web/src/components/terminalCommands.auth.test.ts`
- Modify: `simon-workspace-web/src/components/terminalCommands.ts`
- Modify: `simon-workspace-web/src/components/TerminalPanel.vue`
- Modify: `simon-workspace-web/src/components/TerminalPanel.focus.test.ts`
- Modify: `simon-workspace-web/src/views/HomeView.vue`
- Modify: `simon-workspace-web/src/views/HomeView.layout.test.ts`

- [ ] **Step 1: Replace unsafe expectations with failing safety tests**

```ts
it.each(['login', 'login simon', 'login simon secret'])('navigates %s to the login page without returning credentials', (input) => {
  const result = evaluateTerminalCommand(input, context)

  expect(result).toMatchObject({ status: 'run', command: 'login', to: '/login' })
  expect(result).not.toHaveProperty('args')
  expect(JSON.stringify(result)).not.toContain('secret')
})
```

Replace the first focus test with:

```ts
it('does not expose or execute an autofocus prop', () => {
  expect(source).not.toContain('autoFocus?: boolean')
  expect(source).not.toContain('if (props.autoFocus)')
})
```

Change the HomeView focus assertion to:

```ts
it('does not pass initial focus intent into the terminal panel', () => {
  expect(homeSource).toContain('<TerminalPanel v-if="site" />')
  expect(homeSource).not.toContain('auto-focus')
})
```

- [ ] **Step 2: Run terminal and home tests and verify RED**

```powershell
npm --prefix simon-workspace-web test -- src/components/terminalCommands.auth.test.ts src/components/TerminalPanel.focus.test.ts src/views/HomeView.layout.test.ts
```

Expected: FAIL because login still requires credentials and HomeView still requests autofocus.

- [ ] **Step 3: Remove credential side effects and autofocus**

In `terminalCommands.ts`:

- Remove `'login'` from `TerminalCommandStatus`.
- Remove `args` from `TerminalCommandResult` if no remaining command uses it.
- Remove the special `if (command.command === 'login')` branch.
- Change session help to `login`, `logout`, and `whoami`.
- Remove `terminal.loginUsage` from fallback messages and all locale objects.

The existing login command metadata already has `to: '/login'`, so it will fall through to `runTerminalCommand(command, context, t, commandName)` and return only the canonical command name.

In `TerminalPanel.vue`, remove the prop declaration/default and the `result.status === 'login'` branch that calls `auth.login`. Keep logout, identity display, pointer focus, navigation, theme, language, and clear behavior.

In `HomeView.vue`, change:

```vue
<TerminalPanel v-if="site" auto-focus />
```

to:

```vue
<TerminalPanel v-if="site" />
```

- [ ] **Step 4: Run terminal and home tests and verify GREEN**

Run the command from Step 2, then run the broader terminal suite:

```powershell
npm --prefix simon-workspace-web test -- src/components/terminalCommands.test.ts src/components/terminalCommands.auth.test.ts src/components/TerminalPanel.focus.test.ts src/views/HomeView.layout.test.ts
```

Expected: all focused tests pass and no test expects credential login.

- [ ] **Step 5: Commit terminal safety changes**

```powershell
git add simon-workspace-web/src/components/terminalCommands.ts simon-workspace-web/src/components/terminalCommands.auth.test.ts simon-workspace-web/src/components/TerminalPanel.vue simon-workspace-web/src/components/TerminalPanel.focus.test.ts simon-workspace-web/src/views/HomeView.vue simon-workspace-web/src/views/HomeView.layout.test.ts
git commit -m "fix(web): route terminal login safely"
```

## Task 8: Complete Three-Language Copy and Update Interaction Documentation

**Files:**
- Create: `simon-workspace-web/src/i18n/workspaceUi.i18n.test.ts`
- Modify: `simon-workspace-web/src/i18n/locales/zh-CN.ts`
- Modify: `simon-workspace-web/src/i18n/locales/en.ts`
- Modify: `simon-workspace-web/src/i18n/locales/th-TH.ts`
- Modify: `docs/design-interaction.md`

- [ ] **Step 1: Write the failing translation completeness test**

```ts
import { describe, expect, it } from 'vitest'

import { messages } from './messages'

describe('workspace UI translations', () => {
  it('defines navigation, task, recent-work, and upload copy in every locale', () => {
    for (const locale of Object.values(messages)) {
      expect(locale.workspace.navGroups.teaching.trim()).not.toBe('')
      expect(locale.workspace.navGroups.content.trim()).not.toBe('')
      expect(locale.workspace.navGroups.records.trim()).not.toBe('')
      expect(locale.workspace.navGroups.system.trim()).not.toBe('')
      expect(locale.workspace.nav.more.trim()).not.toBe('')
      expect(locale.workspace.home.actions.courses.trim()).not.toBe('')
      expect(locale.workspace.home.actions.upload.trim()).not.toBe('')
      expect(locale.workspace.home.actions.write.trim()).not.toBe('')
      expect(locale.workspace.home.recent.loadFailed.trim()).not.toBe('')
      expect(locale.workspace.files.upload.fileRequired.trim()).not.toBe('')
      expect(locale.workspace.files.upload.succeeded.trim()).not.toBe('')
    }
  })
})
```

- [ ] **Step 2: Run the focused test and verify RED**

```powershell
npm --prefix simon-workspace-web test -- src/i18n/workspaceUi.i18n.test.ts
```

Expected: FAIL because the new keys do not exist.

- [ ] **Step 3: Add semantic locale objects and update the design guideline**

Add matching object shapes to each locale:

```ts
navGroups: {
  teaching: '教学',
  content: '内容',
  records: '记录',
  system: '系统',
},
```

```ts
navGroups: {
  teaching: 'Teaching',
  content: 'Content',
  records: 'Records',
  system: 'System',
},
```

```ts
navGroups: {
  teaching: 'การสอน',
  content: 'เนื้อหา',
  records: 'บันทึก',
  system: 'ระบบ',
},
```

Add complete sibling keys for `workspace.nav.more`, `workspace.home.actions`, `workspace.home.recent`, and `workspace.files.upload`. Keep the same nested shape in all locales. Use concise copy that can wrap at 390 px. The upload success string accepts `{filename}`.

In `docs/design-interaction.md`, replace the single ambiguous mobile navigation rule with two explicit subsections:

```markdown
### Public Mobile Navigation

- Use the shared top collapsed menu.
- Keep language and theme controls accessible.
- Do not add a public-site bottom tab bar.

### Workspace Mobile Navigation

- Use a fixed, non-scrolling bottom bar for Overview, Courses, Files, Articles, and More when permitted.
- Put remaining permitted workspace routes in the grouped More drawer.
- Keep content clear of the fixed bar and verify the layout at 390 px.
```

- [ ] **Step 4: Run i18n and affected tests and verify GREEN**

```powershell
npm --prefix simon-workspace-web test -- src/i18n/workspaceUi.i18n.test.ts src/i18n/language.test.ts src/views/workspace/WorkspaceHomeView.integration.test.ts src/views/workspace/FileCenterView.upload.test.ts
```

Expected: all focused tests pass.

- [ ] **Step 5: Commit copy and documentation**

```powershell
git add simon-workspace-web/src/i18n/workspaceUi.i18n.test.ts simon-workspace-web/src/i18n/locales/zh-CN.ts simon-workspace-web/src/i18n/locales/en.ts simon-workspace-web/src/i18n/locales/th-TH.ts docs/design-interaction.md
git commit -m "docs(web): align workspace navigation copy"
```

## Task 9: Full Verification and Maintainability Review

**Files:**
- Review: all files changed in Tasks 1-8

- [ ] **Step 1: Run the complete frontend suite**

```powershell
npm --prefix simon-workspace-web test
```

Expected: all tests pass with zero failures. If the pre-existing Windows line-ending assertion in `BlogEditorView.integration.test.ts` still fails, record it separately and do not modify unrelated editor behavior in this branch.

- [ ] **Step 2: Run the production build**

```powershell
npm --prefix simon-workspace-web run build
```

Expected: TypeScript and Vite finish with exit code 0.

- [ ] **Step 3: Check diff hygiene and scope**

```powershell
git diff --check
git status --short
git diff --stat 5b2e4e0..HEAD
```

Expected: no whitespace errors; only frontend UI, tests, i18n, and interaction documentation from this plan are changed. `simon-workspace-api/src/main/resources/application-dev.yml` remains outside all commits.

- [ ] **Step 4: Verify the changed UI in the browser**

Run the frontend against the configured local API and inspect:

- Desktop workspace at 1440 px: grouped side navigation, task actions, recent states, active route.
- Mobile workspace at 390 px: non-scrolling bottom navigation, More drawer, no covered content.
- File center deep link: `/workspace/files?action=upload` opens once and consumes the query.
- Upload dialog: private default, progress, retry after a simulated failure, success list refresh.
- Homepage terminal: initial focus remains outside the terminal; clicking focuses it; `login simon secret` navigates to `/login` without echoing `simon` or `secret`.
- Light and dark themes plus Chinese, English, and Thai at both viewports.

- [ ] **Step 5: Request code review and address only in-scope findings**

Use the `requesting-code-review` skill. Review for duplicated route metadata, permission leaks, fixed-mobile-bar overlap, missing inline states, credential retention, untranslated strings, and light-only colors. Apply fixes through a new failing test where behavior changes.

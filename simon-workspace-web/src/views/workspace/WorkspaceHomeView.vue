<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink } from 'vue-router'
import { NIcon, NSkeleton } from 'naive-ui'
import { Book, FileUpload, Notes } from '@vicons/tabler'

import { useAuthStore } from '../../stores/auth'
import { useWorkspaceOverview } from './useWorkspaceOverview'
import { useWorkspaceSectionRetry } from './useWorkspaceSectionRetry'

const { locale, t } = useI18n()
const auth = useAuthStore()

const actionMetadata = [
  { to: '/workspace/courses', permission: 'course:manage' },
  { to: '/workspace/files?action=upload', permission: 'file:manage' },
  { to: '/blog/new', permission: 'blog:post:create' },
] as const

const actionPresentation = [
  {
    icon: Book,
    titleKey: 'workspace.home.actions.courses',
    descriptionKey: 'workspace.home.actions.coursesHelp',
  },
  {
    icon: FileUpload,
    titleKey: 'workspace.home.actions.upload',
    descriptionKey: 'workspace.home.actions.uploadHelp',
  },
  {
    icon: Notes,
    titleKey: 'workspace.home.actions.write',
    descriptionKey: 'workspace.home.actions.writeHelp',
  },
] as const

const actions = computed(() => actionMetadata
  .map((metadata, index) => ({ ...metadata, ...actionPresentation[index] }))
  .filter((action) => auth.hasPermission(action.permission)))

const overview = useWorkspaceOverview({
  courses: auth.hasPermission('course:manage'),
  files: auth.hasPermission('file:manage'),
  drafts: auth.hasPermission('blog:post:create'),
  fallbackError: () => t('workspace.home.recent.loadFailed'),
})

const courseItems = overview.courses.items
const coursesLoading = overview.courses.loading
const fileItems = overview.files.items
const filesLoading = overview.files.loading
const draftItems = overview.drafts.items
const draftsLoading = overview.drafts.loading
const {
  retry: retryCourses,
  running: coursesRetrying,
  visibleError: coursesVisibleError,
} = useWorkspaceSectionRetry(overview.courses.error, overview.courses.load)
const {
  retry: retryFiles,
  running: filesRetrying,
  visibleError: filesVisibleError,
} = useWorkspaceSectionRetry(overview.files.error, overview.files.load)
const {
  retry: retryDrafts,
  running: draftsRetrying,
  visibleError: draftsVisibleError,
} = useWorkspaceSectionRetry(overview.drafts.error, overview.drafts.load)

onMounted(() => void overview.loadAll())

function courseStatus(status: string) {
  return status === 'ARCHIVED' ? t('common.states.archived') : t('common.states.active')
}

function formatFileSize(size: number) {
  const formatter = new Intl.NumberFormat(locale.value, { maximumFractionDigits: 1 })
  if (size >= 1024 * 1024) return `${formatter.format(size / 1024 / 1024)} MB`
  if (size >= 1024) return `${formatter.format(size / 1024)} KB`
  return `${formatter.format(size)} B`
}

function formatDate(value?: string | null) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value.slice(0, 10)
  return new Intl.DateTimeFormat(locale.value, { dateStyle: 'medium' }).format(date)
}
</script>

<template>
  <section class="workspace-home">
    <header class="home-intro">
      <h1>{{ t('workspace.home.title') }}</h1>
      <p>{{ t('workspace.home.description') }}</p>
    </header>

    <nav v-if="actions.length" class="action-grid" :aria-label="t('workspace.home.title')">
      <RouterLink v-for="action in actions" :key="action.to" :to="action.to" class="action-card">
        <n-icon :component="action.icon" aria-hidden="true" />
        <span>
          <strong>{{ t(action.titleKey) }}</strong>
          <small>{{ t(action.descriptionKey) }}</small>
        </span>
      </RouterLink>
    </nav>

    <section class="recent-work">
      <h2>{{ t('workspace.home.recent.title') }}</h2>

      <div class="recent-grid">
        <section v-if="overview.courses.enabled" class="recent-panel">
          <header>
            <h3>{{ t('workspace.home.recent.courses') }}</h3>
          </header>

          <div v-if="coursesVisibleError" class="recent-error" role="alert">
            <span>{{ coursesVisibleError }}</span>
            <button type="button" :disabled="coursesLoading || coursesRetrying" @click="retryCourses">
              {{ t('workspace.home.recent.retry') }}
            </button>
          </div>
          <div v-if="coursesLoading" class="recent-skeletons">
            <n-skeleton v-for="index in 3" :key="index" height="52px" :sharp="false" />
          </div>
          <div v-else class="recent-content">
            <p v-if="!coursesVisibleError && courseItems.length === 0" class="recent-empty">
              {{ t('workspace.home.recent.emptyCourses') }}
            </p>
            <div v-else-if="courseItems.length" class="recent-rows">
              <RouterLink v-for="course in courseItems" :key="course.id" to="/workspace/courses" class="recent-row">
                <strong>{{ course.courseName }}</strong>
                <span>{{ course.courseCode || '—' }} · {{ courseStatus(course.status) }}</span>
              </RouterLink>
            </div>
          </div>
        </section>

        <section v-if="overview.files.enabled" class="recent-panel">
          <header>
            <h3>{{ t('workspace.home.recent.files') }}</h3>
          </header>

          <div v-if="filesVisibleError" class="recent-error" role="alert">
            <span>{{ filesVisibleError }}</span>
            <button type="button" :disabled="filesLoading || filesRetrying" @click="retryFiles">
              {{ t('workspace.home.recent.retry') }}
            </button>
          </div>
          <div v-if="filesLoading" class="recent-skeletons">
            <n-skeleton v-for="index in 3" :key="index" height="52px" :sharp="false" />
          </div>
          <div v-else class="recent-content">
            <p v-if="!filesVisibleError && fileItems.length === 0" class="recent-empty">
              {{ t('workspace.home.recent.emptyFiles') }}
            </p>
            <div v-else-if="fileItems.length" class="recent-rows">
              <RouterLink v-for="file in fileItems" :key="file.id" to="/workspace/files" class="recent-row">
                <strong>{{ file.originalFilename }}</strong>
                <span>{{ formatFileSize(file.fileSize) }} · {{ formatDate(file.updatedTime || file.createdTime) }}</span>
              </RouterLink>
            </div>
          </div>
        </section>

        <section v-if="overview.drafts.enabled" class="recent-panel">
          <header>
            <h3>{{ t('workspace.home.recent.drafts') }}</h3>
          </header>

          <div v-if="draftsVisibleError" class="recent-error" role="alert">
            <span>{{ draftsVisibleError }}</span>
            <button type="button" :disabled="draftsLoading || draftsRetrying" @click="retryDrafts">
              {{ t('workspace.home.recent.retry') }}
            </button>
          </div>
          <div v-if="draftsLoading" class="recent-skeletons">
            <n-skeleton v-for="index in 3" :key="index" height="52px" :sharp="false" />
          </div>
          <div v-else class="recent-content">
            <p v-if="!draftsVisibleError && draftItems.length === 0" class="recent-empty">
              {{ t('workspace.home.recent.emptyDrafts') }}
            </p>
            <div v-else-if="draftItems.length" class="recent-rows">
              <RouterLink
                v-for="draft in draftItems"
                :key="draft.id"
                :to="`/blog/${draft.id}/edit`"
                class="recent-row"
              >
                <strong>{{ draft.title }}</strong>
                <span>{{ formatDate(draft.updatedTime) }}</span>
              </RouterLink>
            </div>
          </div>
        </section>
      </div>
    </section>
  </section>
</template>

<style scoped>
.workspace-home {
  display: grid;
  gap: 24px;
}

.home-intro {
  display: grid;
  gap: 5px;
}

.home-intro h1,
.recent-work h2,
.recent-panel h3 {
  margin: 0;
  color: var(--sw-text);
}

.home-intro h1 {
  font-size: 24px;
  font-weight: 750;
  line-height: 1.2;
}

.home-intro p {
  margin: 0;
  color: var(--sw-muted);
  font-size: 14px;
  line-height: 1.55;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.action-card {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  min-height: 86px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg-strong);
  padding: 14px;
  transition:
    border-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 220ms cubic-bezier(0.16, 1, 0.3, 1);
}

.action-card:hover {
  border-color: color-mix(in srgb, var(--sw-accent) 42%, var(--sw-border));
  background: var(--sw-panel-bg-hover);
  transform: translate3d(0, -1px, 0);
}

.action-card:active {
  transform: translate3d(0, 1px, 0);
}

.action-card .n-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  font-size: 20px;
}

.action-card > span {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.action-card strong {
  color: var(--sw-text);
  font-size: 15px;
  font-weight: 700;
}

.action-card small {
  overflow: hidden;
  color: var(--sw-muted);
  font-size: 12px;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-work {
  display: grid;
  gap: 12px;
}

.recent-work > h2 {
  font-size: 18px;
  font-weight: 700;
}

.recent-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.recent-panel {
  overflow: hidden;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg-strong);
}

.recent-panel > header {
  border-bottom: 1px solid var(--sw-border-soft);
  padding: 13px 14px;
}

.recent-panel h3 {
  font-size: 14px;
  font-weight: 700;
}

.recent-skeletons,
.recent-content {
  display: grid;
  gap: 1px;
  padding: 8px;
}

.recent-skeletons {
  gap: 7px;
}

.recent-error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  color: var(--sw-muted);
  margin: 8px 8px 0;
  padding: 9px 10px;
  font-size: 12px;
}

.recent-error span {
  min-width: 0;
  overflow-wrap: anywhere;
}

.recent-error button {
  flex: 0 0 auto;
  border: 0;
  border-radius: 6px;
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  padding: 5px 9px;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.recent-error button:disabled {
  cursor: wait;
  opacity: 0.58;
}

.recent-empty {
  margin: 0;
  color: var(--sw-muted);
  padding: 22px 10px;
  font-size: 13px;
  text-align: center;
}

.recent-rows {
  display: grid;
}

.recent-row {
  display: grid;
  gap: 3px;
  border-bottom: 1px solid var(--sw-border-soft);
  border-radius: 6px;
  padding: 10px;
  transition: background-color 180ms cubic-bezier(0.16, 1, 0.3, 1);
}

.recent-row:last-child {
  border-bottom: 0;
}

.recent-row:hover {
  background: var(--sw-panel-bg-hover);
}

.recent-row strong {
  overflow: hidden;
  color: var(--sw-text);
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-row span {
  overflow: hidden;
  color: var(--sw-muted);
  font-size: 11px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 860px) {
  .action-grid,
  .recent-grid {
    grid-template-columns: 1fr;
  }
}
</style>

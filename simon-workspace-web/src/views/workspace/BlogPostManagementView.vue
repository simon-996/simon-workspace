<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { NButton, NEmpty, NIcon, NInput, NPopconfirm, NSelect, NSpin, NTag, useMessage } from 'naive-ui'
import { Edit, Eye, FileText, Plus, Refresh, Trash } from '@vicons/tabler'

import {
  deleteBlogPost,
  fetchManageBlogPosts,
  type BlogPostSummary,
} from '../../api/workspace'

const { t } = useI18n()
const router = useRouter()
const message = useMessage()

const posts = ref<BlogPostSummary[]>([])
const keyword = ref('')
const status = ref<string | null>(null)
const loading = ref(false)
const deletingId = ref('')
const error = ref('')

const draftCount = computed(() => posts.value.filter((post) => post.status === 'DRAFT').length)
const publishedCount = computed(() => posts.value.filter((post) => post.status === 'PUBLISHED').length)
const statusOptions = computed(() => [
  { label: t('workspace.blogPosts.status.all'), value: null },
  { label: t('workspace.blogPosts.status.draft'), value: 'DRAFT' },
  { label: t('workspace.blogPosts.status.published'), value: 'PUBLISHED' },
])

onMounted(() => {
  void loadPosts()
})

async function loadPosts() {
  loading.value = true
  error.value = ''
  try {
    posts.value = await fetchManageBlogPosts({
      keyword: keyword.value.trim(),
      status: status.value,
    })
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('workspace.blogPosts.messages.loadFailed')
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

function formatDate(value?: string | null) {
  return value ? value.slice(0, 16).replace('T', ' ') : '-'
}

function statusType(value: string) {
  return value === 'PUBLISHED' ? 'success' : 'warning'
}

function statusText(value: string) {
  return value === 'PUBLISHED'
    ? t('workspace.blogPosts.status.published')
    : t('workspace.blogPosts.status.draft')
}

function editPost(post: BlogPostSummary) {
  void router.push(`/blog/${post.id}/edit`)
}

function openPost(post: BlogPostSummary) {
  if (post.status === 'PUBLISHED') {
    void router.push(`/blog/${post.id}`)
    return
  }
  editPost(post)
}

async function removePost(post: BlogPostSummary) {
  deletingId.value = post.id
  try {
    await deleteBlogPost(post.id)
    message.success(t('workspace.blogPosts.messages.deleted'))
    await loadPosts()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.blogPosts.messages.deleteFailed'))
  } finally {
    deletingId.value = ''
  }
}
</script>

<template>
  <section class="post-management-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="FileText" />
        <span>{{ t('workspace.blogPosts.summary.total') }}</span>
        <strong>{{ posts.length }}</strong>
      </article>
      <article>
        <n-icon :component="Edit" />
        <span>{{ t('workspace.blogPosts.summary.drafts') }}</span>
        <strong>{{ draftCount }}</strong>
      </article>
      <article>
        <n-icon :component="Eye" />
        <span>{{ t('workspace.blogPosts.summary.published') }}</span>
        <strong>{{ publishedCount }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        :placeholder="t('workspace.blogPosts.searchPlaceholder')"
        @keyup.enter="loadPosts"
      />
      <n-select v-model:value="status" :options="statusOptions" />
      <n-button secondary class="icon-button" @click="loadPosts">
        <template #icon>
          <n-icon :component="Refresh" />
        </template>
        {{ t('common.actions.refresh') }}
      </n-button>
      <n-button type="primary" class="icon-button" @click="router.push('/blog/new')">
        <template #icon>
          <n-icon :component="Plus" />
        </template>
        {{ t('workspace.blogPosts.actions.write') }}
      </n-button>
    </section>

    <section class="post-panel">
      <div v-if="error" class="empty-state">
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadPosts">{{ t('common.actions.retry') }}</n-button>
      </div>

      <div v-else-if="loading && posts.length === 0" class="post-skeleton" aria-hidden="true">
        <span v-for="index in 6" :key="index" />
      </div>

      <n-spin v-else :show="loading">
        <n-empty v-if="posts.length === 0" class="empty-state" :description="t('workspace.blogPosts.empty')" />
        <div v-else class="post-list">
          <article v-for="post in posts" :key="post.id" class="post-row">
            <div class="post-main">
              <strong>{{ post.title }}</strong>
              <p>{{ post.summary || t('blog.list.noSummary') }}</p>
              <div>
                <n-tag size="small" round :type="statusType(post.status)">
                  {{ statusText(post.status) }}
                </n-tag>
                <span>{{ post.category?.name || t('blog.list.uncategorized') }}</span>
                <span>{{ formatDate(post.updatedTime) }}</span>
              </div>
            </div>

            <div class="post-actions">
              <n-button size="small" secondary @click="openPost(post)">
                <template #icon>
                  <n-icon :component="post.status === 'PUBLISHED' ? Eye : Edit" />
                </template>
                {{ post.status === 'PUBLISHED' ? t('workspace.blogPosts.actions.view') : t('common.actions.edit') }}
              </n-button>
              <n-button size="small" tertiary @click="editPost(post)">
                {{ t('common.actions.edit') }}
              </n-button>
              <n-popconfirm @positive-click="removePost(post)">
                <template #trigger>
                  <n-button size="small" tertiary type="error" :loading="deletingId === post.id">
                    <template #icon>
                      <n-icon :component="Trash" />
                    </template>
                    {{ t('common.actions.delete') }}
                  </n-button>
                </template>
                {{ t('workspace.blogPosts.messages.deleteConfirm') }}
              </n-popconfirm>
            </div>
          </article>
        </div>
      </n-spin>
    </section>
  </section>
</template>

<style scoped>
.post-management-page {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.summary-grid article,
.toolbar,
.post-panel {
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  box-shadow: var(--sw-shadow-soft);
}

.summary-grid article {
  display: grid;
  grid-template-columns: 42px 1fr;
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: 12px;
  min-height: 86px;
  padding: 16px;
}

.summary-grid .n-icon {
  grid-row: 1 / 3;
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  font-size: 22px;
}

.summary-grid span,
.post-main div span {
  color: var(--sw-muted);
  font-size: 12px;
  font-weight: 800;
}

.summary-grid strong {
  color: var(--sw-text);
  font-size: 24px;
  font-weight: 800;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px auto auto;
  gap: 10px;
  padding: 12px;
}

.post-panel {
  min-height: 420px;
  padding: 12px;
}

.post-skeleton {
  display: grid;
  gap: 10px;
}

.post-skeleton span {
  min-height: 78px;
  border-radius: 8px;
  background: linear-gradient(90deg, #e9eef1 0%, #f8fafb 45%, #e9eef1 100%);
  background-size: 200% 100%;
  animation: sw-shimmer 1.45s ease-in-out infinite;
}

.post-list {
  display: grid;
  gap: 1px;
  overflow: hidden;
  border: 1px solid var(--sw-border-soft);
  border-radius: 8px;
  background: var(--sw-border-soft);
}

.post-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  background: var(--sw-panel-bg-strong);
  padding: 14px;
}

.post-main {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.post-main strong {
  overflow: hidden;
  color: var(--sw-text);
  font-size: 16px;
  font-weight: 850;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-main p {
  overflow: hidden;
  margin: 0;
  color: var(--sw-muted);
  font-size: 13px;
  line-height: 1.55;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-main div,
.post-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.empty-state {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 360px;
  color: var(--sw-muted);
}

@media (max-width: 900px) {
  .summary-grid,
  .toolbar,
  .post-row {
    grid-template-columns: 1fr;
  }

  .post-actions .n-button {
    flex: 1;
  }
}
</style>

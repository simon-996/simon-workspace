<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NButton,
  NEmpty,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpin,
  NTag,
  useMessage,
} from 'naive-ui'
import { AlertTriangle, CircleCheck, Notebook, Plus, Refresh } from '@vicons/tabler'

import {
  createBlogCategory,
  deleteBlogCategory,
  fetchBlogManageCategories,
  updateBlogCategory,
  type BlogCategory,
} from '../../api/workspace'

const { t } = useI18n()
const message = useMessage()

const categories = ref<BlogCategory[]>([])
const loading = ref(false)
const saving = ref(false)
const deletingId = ref<string | null>(null)
const error = ref('')
const modalVisible = ref(false)
const editingCategory = ref<BlogCategory | null>(null)

const form = reactive({
  name: '',
  slug: '',
  description: '',
  sortOrder: 100,
  status: 'ACTIVE',
})

const activeCount = computed(() => categories.value.filter((category) => category.status === 'ACTIVE').length)
const disabledCount = computed(() => categories.value.filter((category) => category.status === 'DISABLED').length)
const usedCount = computed(() => categories.value.filter((category) => Number(category.postCount || 0) > 0).length)

const statusOptions = computed(() => [
  { label: t('workspace.blog.status.active'), value: 'ACTIVE' },
  { label: t('workspace.blog.status.disabled'), value: 'DISABLED' },
])

onMounted(() => {
  void loadCategories()
})

async function loadCategories() {
  loading.value = true
  error.value = ''
  try {
    categories.value = await fetchBlogManageCategories()
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('workspace.blog.messages.loadFailed')
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  editingCategory.value = null
  syncForm()
  modalVisible.value = true
}

function openEditModal(category: BlogCategory) {
  editingCategory.value = category
  syncForm(category)
  modalVisible.value = true
}

async function saveCategory() {
  if (!form.name.trim()) {
    message.warning(t('workspace.blog.messages.nameRequired'))
    return
  }

  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      slug: textOrNull(form.slug),
      description: textOrNull(form.description),
      sortOrder: form.sortOrder,
      status: form.status,
    }
    if (editingCategory.value) {
      await updateBlogCategory(editingCategory.value.id, payload)
      message.success(t('workspace.blog.messages.updated'))
    } else {
      await createBlogCategory(payload)
      message.success(t('workspace.blog.messages.created'))
    }
    modalVisible.value = false
    await loadCategories()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.blog.messages.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function removeCategory(category: BlogCategory) {
  if (!canDeleteCategory(category)) {
    message.warning(t('workspace.blog.messages.deleteBlocked'))
    return
  }

  deletingId.value = category.id
  try {
    await deleteBlogCategory(category.id)
    message.success(t('workspace.blog.messages.deleted'))
    await loadCategories()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.blog.messages.deleteFailed'))
  } finally {
    deletingId.value = null
  }
}

function syncForm(category?: BlogCategory) {
  form.name = category?.name ?? ''
  form.slug = category?.slug ?? ''
  form.description = category?.description ?? ''
  form.sortOrder = category?.sortOrder ?? 100
  form.status = category?.status ?? 'ACTIVE'
}

function canDeleteCategory(category: BlogCategory) {
  return Number(category.postCount || 0) === 0
}

function statusType(status: string) {
  return status === 'ACTIVE' ? 'success' : 'default'
}

function statusText(status: string) {
  return status === 'ACTIVE' ? t('workspace.blog.status.active') : t('workspace.blog.status.disabled')
}

function textOrNull(value?: string | null) {
  const trimmed = value?.trim() ?? ''
  return trimmed ? trimmed : null
}
</script>

<template>
  <section class="blog-management-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Notebook" />
        <span>{{ t('workspace.blog.summary.total') }}</span>
        <strong>{{ categories.length }}</strong>
      </article>
      <article>
        <n-icon :component="CircleCheck" />
        <span>{{ t('workspace.blog.summary.active') }}</span>
        <strong>{{ activeCount }}</strong>
      </article>
      <article>
        <n-icon :component="AlertTriangle" />
        <span>{{ t('workspace.blog.summary.disabled') }}</span>
        <strong>{{ disabledCount }}</strong>
      </article>
      <article>
        <n-icon :component="Notebook" />
        <span>{{ t('workspace.blog.summary.used') }}</span>
        <strong>{{ usedCount }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <span>{{ t('workspace.blog.toolbarText') }}</span>
      <div class="toolbar-actions">
        <n-button secondary class="icon-button" @click="loadCategories">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          {{ t('common.actions.refresh') }}
        </n-button>
        <n-button type="primary" class="icon-button" @click="openCreateModal">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          {{ t('workspace.blog.actions.create') }}
        </n-button>
      </div>
    </section>

    <section class="category-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadCategories">{{ t('common.actions.retry') }}</n-button>
      </div>

      <div v-else-if="loading && categories.length === 0" class="category-skeleton" aria-hidden="true">
        <span v-for="index in 6" :key="index" />
      </div>

      <n-spin v-else :show="loading">
        <n-empty v-if="categories.length === 0" class="empty-state" :description="t('workspace.blog.empty')" />
        <div v-else class="category-list">
          <article v-for="category in categories" :key="category.id" class="category-row">
            <div class="category-main">
              <strong>{{ category.name }}</strong>
              <span>{{ category.slug }}</span>
              <p v-if="category.description">{{ category.description }}</p>
            </div>

            <div class="category-meta">
              <n-tag size="small" round :type="statusType(category.status)">
                {{ statusText(category.status) }}
              </n-tag>
              <span>{{ t('workspace.blog.table.posts', { count: category.postCount || 0 }) }}</span>
              <span>{{ t('workspace.blog.table.sortOrder', { order: category.sortOrder }) }}</span>
            </div>

            <div class="category-actions">
              <n-button size="small" secondary @click="openEditModal(category)">
                {{ t('common.actions.edit') }}
              </n-button>
              <n-popconfirm
                :disabled="!canDeleteCategory(category)"
                @positive-click="removeCategory(category)"
              >
                <template #trigger>
                  <n-button
                    size="small"
                    tertiary
                    type="error"
                    :disabled="!canDeleteCategory(category)"
                    :loading="deletingId === category.id"
                  >
                    {{ t('common.actions.delete') }}
                  </n-button>
                </template>
                {{ t('workspace.blog.messages.deleteConfirm') }}
              </n-popconfirm>
              <small v-if="!canDeleteCategory(category)">
                {{ t('workspace.blog.messages.deleteBlocked') }}
              </small>
            </div>
          </article>
        </div>
      </n-spin>
    </section>

    <n-modal v-model:show="modalVisible" preset="card" :title="editingCategory ? t('workspace.blog.modal.edit') : t('workspace.blog.modal.create')" class="category-modal">
      <form class="category-form" @submit.prevent="saveCategory">
        <label class="field">
          <span>{{ t('workspace.blog.fields.name') }}</span>
          <n-input v-model:value="form.name" :placeholder="t('workspace.blog.fields.namePlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.blog.fields.slug') }}</span>
          <n-input v-model:value="form.slug" :placeholder="t('workspace.blog.fields.slugPlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.blog.fields.sortOrder') }}</span>
          <n-input-number v-model:value="form.sortOrder" :min="0" :max="9999" />
        </label>

        <label class="field">
          <span>{{ t('workspace.blog.fields.status') }}</span>
          <n-select v-model:value="form.status" :options="statusOptions" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.blog.fields.description') }}</span>
          <n-input
            v-model:value="form.description"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }"
            :placeholder="t('workspace.blog.fields.descriptionPlaceholder')"
          />
        </label>

        <div class="form-actions span-2">
          <n-button @click="modalVisible = false">{{ t('common.actions.cancel') }}</n-button>
          <n-button type="primary" attr-type="submit" :loading="saving">{{ t('common.actions.save') }}</n-button>
        </div>
      </form>
    </n-modal>
  </section>
</template>

<style scoped>
.blog-management-page {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.summary-grid article {
  display: grid;
  grid-template-columns: 42px 1fr;
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: 12px;
  min-height: 86px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
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
.toolbar span,
.field span {
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  padding: 12px;
}

.toolbar-actions,
.form-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.icon-button {
  --n-border-radius: 8px !important;
  font-weight: 700;
}

.category-panel {
  min-height: 420px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  overflow: hidden;
}

.category-list {
  display: grid;
  gap: 10px;
  padding: 12px;
}

.category-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px 230px;
  align-items: center;
  gap: 14px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  padding: 14px;
  transition:
    border-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 220ms cubic-bezier(0.16, 1, 0.3, 1);
}

.category-row:hover {
  border-color: color-mix(in srgb, var(--sw-accent) 42%, var(--sw-border));
  transform: translate3d(0, -1px, 0);
}

.category-main {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.category-main strong {
  color: var(--sw-text);
  font-size: 16px;
  font-weight: 800;
}

.category-main span,
.category-main p,
.category-actions small,
.category-meta span {
  color: var(--sw-muted);
  font-size: 12px;
  font-weight: 700;
}

.category-main p {
  margin: 2px 0 0;
  line-height: 1.6;
}

.category-meta {
  display: grid;
  gap: 7px;
  justify-items: start;
}

.category-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.category-actions small {
  flex-basis: 100%;
  text-align: right;
}

.category-skeleton {
  display: grid;
  gap: 10px;
  padding: 12px;
}

.category-skeleton span {
  min-height: 76px;
  border-radius: 8px;
  background: linear-gradient(90deg, #e9eef1 0%, #f8fafb 45%, #e9eef1 100%);
  background-size: 200% 100%;
  animation: sw-shimmer 1.45s ease-in-out infinite;
}

.empty-state,
.error-state {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 380px;
  color: var(--sw-muted);
  text-align: center;
}

.error-state .n-icon {
  color: #b76b2b;
  font-size: 30px;
}

.category-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: grid;
  gap: 7px;
}

.span-2 {
  grid-column: 1 / -1;
}

.form-actions {
  padding-top: 8px;
}

@keyframes sw-shimmer {
  0% {
    background-position: 120% 0;
  }
  100% {
    background-position: -120% 0;
  }
}

@media (max-width: 1040px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .category-row {
    grid-template-columns: 1fr;
    align-items: stretch;
  }

  .category-actions {
    justify-content: flex-start;
  }

  .category-actions small {
    text-align: left;
  }
}

@media (max-width: 640px) {
  .summary-grid,
  .category-form {
    grid-template-columns: 1fr;
  }

  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-actions,
  .category-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .toolbar-actions .n-button,
  .category-actions .n-button {
    width: 100%;
  }
}
</style>

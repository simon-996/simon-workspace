<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import {
  NButton,
  NIcon,
  NInput,
  NModal,
  NSelect,
  NSpin,
  useMessage,
} from 'naive-ui'
import { ArrowLeft, DeviceFloppy, Plus, Upload } from '@vicons/tabler'

import AppHeader from '../../components/AppHeader.vue'
import {
  createBlogCategory,
  createBlogPost,
  fetchBlogCategories,
  fetchManageBlogPostDetail,
  fetchBlogTags,
  type BlogCategory,
  type BlogTag,
  updateBlogPost,
  uploadBlogEditorImage,
} from '../../api/workspace'
import { useAuthStore } from '../../stores/auth'
import { useThemeStore } from '../../stores/theme'
import {
  buildTagOptions,
  createTagOption as buildCreatedTagOption,
  normalizeSelectedTags,
} from './blogTagOptions'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { locale, t } = useI18n()
const auth = useAuthStore()
const theme = useThemeStore()
const categories = ref<BlogCategory[]>([])
const title = ref('')
const summary = ref('')
const categoryId = ref<string | null>(null)
const tags = ref<string[]>([])
const remoteTags = ref<BlogTag[]>([])
const content = ref('')
const saving = ref(false)
const tagLoading = ref(false)
const uploadingImages = ref(false)
const uploadProgress = ref(0)
const publishAttempted = ref(false)
const categoryModal = ref(false)
const categoryName = ref('')
const blogEditorSourceType = 'BLOG_EDITOR'
const blogImageUploadingClass = 'blog-image-uploading'
let bypassUnsavedGuard = false
let tagSearchTimer: number | undefined
let tagSearchSequence = 0

const canManageCategory = computed(() => auth.hasPermission('blog:category:manage'))
const categoryOptions = computed(() => categories.value.map((item) => ({ label: item.name, value: item.id })))
const tagOptions = computed(() => buildTagOptions(remoteTags.value, tags.value))
const editorLanguage = computed(() => locale.value === 'zh-CN' ? 'zh-CN' : 'en-US')
const editingPostId = computed(() => {
  const id = route.params.id
  return Array.isArray(id) ? id[0] : typeof id === 'string' ? id : ''
})
const isEditing = computed(() => Boolean(editingPostId.value))
const requiredPostPermission = computed(() => isEditing.value ? 'blog:post:update' : 'blog:post:create')
const editorRedirectPath = computed(() => isEditing.value ? `/blog/${editingPostId.value}/edit` : '/blog/new')
const categorySelectStatus = computed(() => publishAttempted.value && !categoryId.value ? 'error' : undefined)
const hasUnsavedChanges = computed(() => {
  return Boolean(
    title.value.trim()
    || summary.value.trim()
    || categoryId.value
    || tags.value.length
    || content.value.trim()
    || categoryName.value.trim(),
  )
})

onMounted(async () => {
  const ok = await auth.restore()
  if (!ok || !auth.hasPermission(requiredPostPermission.value)) {
    await router.push({
      name: 'login',
      query: {
        redirect: editorRedirectPath.value,
      },
    })
    return
  }
  await Promise.all([
    loadCategories(),
    loadTags(''),
  ])
  if (isEditing.value) {
    await loadPostForEdit()
  }
})

watch(uploadingImages, (uploading) => {
  document.body.classList.toggle(blogImageUploadingClass, uploading)
})

onBeforeUnmount(() => {
  document.body.classList.remove(blogImageUploadingClass)
  if (tagSearchTimer !== undefined) window.clearTimeout(tagSearchTimer)
  tagSearchSequence += 1
})

onBeforeRouteLeave((_to, _from, next) => {
  if (bypassUnsavedGuard || !hasUnsavedChanges.value) {
    next()
    return
  }

  if (window.confirm(t('blog.editor.leaveConfirm'))) {
    next()
    return
  }

  next(false)
})

async function loadCategories() {
  categories.value = await fetchBlogCategories()
}

async function loadTags(keyword: string) {
  const sequence = ++tagSearchSequence
  tagLoading.value = true
  try {
    const results = await fetchBlogTags(keyword.trim())
    if (sequence === tagSearchSequence) {
      remoteTags.value = results
    }
  } catch (error) {
    if (sequence === tagSearchSequence) {
      message.error(error instanceof Error ? error.message : t('blog.messages.loadFailed'))
    }
  } finally {
    if (sequence === tagSearchSequence) {
      tagLoading.value = false
    }
  }
}

function queueTagSearch(keyword: string) {
  if (tagSearchTimer !== undefined) window.clearTimeout(tagSearchTimer)
  tagSearchTimer = window.setTimeout(() => {
    tagSearchTimer = undefined
    void loadTags(keyword)
  }, 220)
}

function createTagOption(value: string) {
  return buildCreatedTagOption(
    value,
    tagOptions.value.map((option) => ({ name: option.value })),
  )
}

function updateTags(values: Array<string | number>) {
  const stringValues = values.map(String)
  if (normalizeSelectedTags(stringValues, 9).length > 8) {
    message.warning(t('blog.messages.tagLimit'))
  }
  tags.value = normalizeSelectedTags(stringValues)
}

async function loadPostForEdit() {
  if (!editingPostId.value) return
  const existing = await fetchManageBlogPostDetail(editingPostId.value)
  title.value = existing.title
  summary.value = existing.summary || ''
  categoryId.value = existing.category?.id || null
  tags.value = existing.tags.map((tag) => tag.name)
  content.value = existing.contentMd
}

async function backToBlog() {
  await router.push('/blog')
}

async function save(status: 'DRAFT' | 'PUBLISHED') {
  if (!title.value.trim() || !content.value.trim()) {
    message.warning(t('blog.messages.titleContentRequired'))
    return
  }
  if (status === 'PUBLISHED' && !categoryId.value) {
    publishAttempted.value = true
    message.warning(t('blog.messages.categoryRequired'))
    return
  }
  saving.value = true
  try {
    tags.value = normalizeSelectedTags(tags.value)
    const payload = {
      title: title.value.trim(),
      summary: summary.value.trim() || null,
      categoryId: categoryId.value,
      tags: normalizeSelectedTags(tags.value),
      contentMd: content.value,
      status,
    }
    const post = editingPostId.value
      ? await updateBlogPost(editingPostId.value, payload)
      : await createBlogPost(payload)
    message.success(status === 'PUBLISHED' ? t('blog.messages.published') : t('blog.messages.draftSaved'))
    bypassUnsavedGuard = true
    await router.push(status === 'PUBLISHED' ? `/blog/${post.id}` : `/blog/${post.id}/edit`)
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('blog.messages.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function addCategory() {
  if (!categoryName.value.trim()) return
  try {
    const category = await createBlogCategory({
      name: categoryName.value.trim(),
      status: 'ACTIVE',
    })
    categoryName.value = ''
    categoryModal.value = false
    await loadCategories()
    categoryId.value = category.id
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('blog.messages.categoryCreateFailed'))
  }
}

function importMarkdown(file: File) {
  if (!file.name.toLowerCase().endsWith('.md')) {
    message.warning(t('blog.messages.markdownOnly'))
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    const text = String(reader.result || '')
    content.value = text
    const heading = text.match(/^#\s+(.+)$/m)
    if (!title.value && heading?.[1]) title.value = heading[1].trim()
  }
  reader.readAsText(file)
}

async function onUploadImg(files: File[], callback: (urls: string[]) => void) {
  uploadingImages.value = true
  uploadProgress.value = 0
  try {
    const urls: string[] = []
    for (const [index, file] of files.entries()) {
      const resource = await uploadBlogEditorImage(file, blogEditorSourceType, (progress) => {
        uploadProgress.value = Math.round(((index + (progress / 100)) / files.length) * 100)
      })
      urls.push(resource.publicUrl || `/api/files/${resource.id}/download`)
    }
    uploadProgress.value = 100
    callback(urls)
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('blog.messages.imageUploadFailed'))
  } finally {
    uploadingImages.value = false
    uploadProgress.value = 0
  }
}
</script>

<template>
  <main class="blog-shell">
    <AppHeader />
    <section class="editor-layout">
      <header>
        <div class="editor-context">
          <n-button quaternary class="blog-back-button" @click="backToBlog">
            <template #icon>
              <n-icon :component="ArrowLeft" />
            </template>
            {{ t('blog.editor.backToBlog') }}
          </n-button>
          <span aria-hidden="true">/</span>
          <strong>{{ isEditing ? t('blog.editor.editKicker') : t('blog.editor.kicker') }}</strong>
        </div>
        <div class="editor-actions">
          <input
            id="md-import"
            type="file"
            accept=".md,text/markdown,text/plain"
            hidden
            @change="event => {
              const input = event.target as HTMLInputElement
              if (input.files?.[0]) importMarkdown(input.files[0])
              input.value = ''
            }"
          >
          <label class="import-button" for="md-import">
            <n-icon :component="Upload" />
            {{ t('blog.editor.importMd') }}
          </label>
          <n-button secondary :loading="saving" @click="save('DRAFT')">
            <template #icon>
              <n-icon :component="DeviceFloppy" />
            </template>
            {{ t('blog.editor.draft') }}
          </n-button>
          <n-button type="primary" :loading="saving" @click="save('PUBLISHED')">
            {{ t('blog.editor.publish') }}
          </n-button>
        </div>
      </header>

      <section class="meta-panel">
        <n-input v-model:value="title" :placeholder="t('blog.editor.titlePlaceholder')" />
        <n-input v-model:value="summary" :placeholder="t('blog.editor.summaryPlaceholder')" />
        <div class="category-row">
          <n-select
            v-model:value="categoryId"
            clearable
            :status="categorySelectStatus"
            :options="categoryOptions"
            :placeholder="t('blog.editor.categoryPlaceholder')"
          />
          <n-button v-if="canManageCategory" secondary @click="categoryModal = true">
            <template #icon>
              <n-icon :component="Plus" />
            </template>
          </n-button>
        </div>
        <n-select
          :value="tags"
          multiple
          tag
          filterable
          clearable
          :loading="tagLoading"
          :placeholder="t('blog.editor.tagsPlaceholder')"
          :options="tagOptions"
          :on-create="createTagOption"
          :max-tag-count="'responsive'"
          @update:value="updateTags"
          @search="queueTagSearch"
        />
      </section>

      <div class="editor-stage">
        <MdEditor
          v-model="content"
          class="markdown-editor"
          :theme="theme.isDark ? 'dark' : 'light'"
          :language="editorLanguage"
          preview-theme="github"
          :on-upload-img="onUploadImg"
        />
        <Transition name="uploading-fade">
          <div v-if="uploadingImages" class="uploading-overlay" role="status" aria-live="polite">
            <div class="uploading-copy">
              <n-spin size="small" />
              <span>{{ uploadProgress >= 100 ? t('blog.editor.processingImage') : t('blog.editor.uploadingImage') }}</span>
            </div>
            <div class="upload-progress-track" aria-hidden="true">
              <span class="upload-progress-bar" :style="{ width: `${uploadProgress}%` }" />
            </div>
          </div>
        </Transition>
      </div>
    </section>

    <n-modal
      v-model:show="categoryModal"
      preset="card"
      :title="t('blog.editor.newCategory')"
      class="category-modal"
    >
      <div class="modal-form">
        <n-input
          v-model:value="categoryName"
          :placeholder="t('blog.editor.categoryNamePlaceholder')"
          @keyup.enter="addCategory"
        />
        <n-button type="primary" @click="addCategory">{{ t('common.actions.create') }}</n-button>
      </div>
    </n-modal>
  </main>
</template>

<style scoped>
.blog-shell {
  min-height: 100vh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.editor-layout {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
  padding: 10px 0 42px;
}

.editor-layout header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.editor-context {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.editor-context span,
.editor-context strong {
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 800;
  text-transform: uppercase;
}

.editor-context strong {
  color: var(--sw-text);
}

.blog-back-button {
  --n-border-radius: 6px !important;
  min-width: 0;
}

.editor-actions,
.category-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.import-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  color: var(--sw-text);
  cursor: pointer;
  padding: 0 14px;
  font-weight: 800;
}

.meta-panel {
  display: grid;
  grid-template-columns: 1fr 1fr 260px 260px;
  gap: 10px;
  margin-bottom: 12px;
}

.editor-stage {
  position: relative;
}

.markdown-editor {
  min-height: 620px;
  border-radius: 8px;
  overflow: hidden;
}

.uploading-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 5;
  display: grid;
  gap: 8px;
  width: 190px;
  min-height: 52px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--sw-surface-solid) 88%, transparent);
  box-shadow: var(--sw-shadow-soft);
  color: var(--sw-text);
  padding: 10px 12px;
  font-size: 12px;
  font-weight: 800;
  backdrop-filter: blur(14px);
}

.uploading-copy {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.upload-progress-track {
  overflow: hidden;
  height: 4px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--sw-border) 58%, transparent);
}

.upload-progress-bar {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--sw-accent);
  transition: width 180ms cubic-bezier(0.16, 1, 0.3, 1);
}

:global(body.blog-image-uploading .md-editor-modal-clip) {
  display: none !important;
}

.uploading-fade-enter-active,
.uploading-fade-leave-active {
  transition:
    opacity 180ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 180ms cubic-bezier(0.16, 1, 0.3, 1);
}

.uploading-fade-enter-from,
.uploading-fade-leave-to {
  opacity: 0;
  transform: translate3d(0, -4px, 0);
}

.modal-form {
  display: grid;
  gap: 12px;
}

.modal-form .n-button {
  justify-self: end;
}

@media (max-width: 920px) {
  .editor-layout {
    padding: 0 0 28px;
  }

  .editor-layout header,
  .editor-actions {
    display: grid;
    align-items: stretch;
  }

  .editor-context {
    justify-content: start;
  }

  .editor-actions {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .editor-actions .n-button,
  .import-button {
    width: 100%;
    justify-content: center;
  }

  .meta-panel {
    grid-template-columns: 1fr;
  }

}
</style>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
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
import { DeviceFloppy, Plus, Upload } from '@vicons/tabler'

import AppHeader from '../../components/AppHeader.vue'
import {
  createBlogCategory,
  createBlogPost,
  fetchBlogCategories,
  type BlogCategory,
  uploadBlogEditorImage,
} from '../../api/workspace'
import { useAuthStore } from '../../stores/auth'
import { useThemeStore } from '../../stores/theme'

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
const content = ref('')
const saving = ref(false)
const uploadingImages = ref(false)
const uploadingImagePreview = ref('')
const categoryModal = ref(false)
const categoryName = ref('')
const blogEditorSourceType = 'BLOG_EDITOR'
const blogImageUploadingClass = 'blog-image-uploading'

const canManageCategory = computed(() => auth.hasPermission('blog:category:manage'))
const categoryOptions = computed(() => categories.value.map((item) => ({ label: item.name, value: item.id })))
const editorLanguage = computed(() => locale.value === 'zh-CN' ? 'zh-CN' : 'en-US')

onMounted(async () => {
  document.addEventListener('click', captureBlogImageUploadPreview, true)
  const ok = await auth.restore()
  if (!ok || !auth.hasPermission('blog:post:create')) {
    await router.push('/login?redirect=/blog/new')
    return
  }
  await loadCategories()
})

watch(uploadingImages, (uploading) => {
  document.body.classList.toggle(blogImageUploadingClass, uploading)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', captureBlogImageUploadPreview, true)
  document.body.classList.remove(blogImageUploadingClass)
})

async function loadCategories() {
  categories.value = await fetchBlogCategories()
}

async function save(status: 'DRAFT' | 'PUBLISHED') {
  if (!title.value.trim() || !content.value.trim()) {
    message.warning(t('blog.messages.titleContentRequired'))
    return
  }
  saving.value = true
  try {
    const post = await createBlogPost({
      title: title.value.trim(),
      summary: summary.value.trim() || null,
      categoryId: categoryId.value,
      tags: tags.value,
      contentMd: content.value,
      status,
    })
    message.success(status === 'PUBLISHED' ? t('blog.messages.published') : t('blog.messages.draftSaved'))
    await router.push(`/blog/${post.id}`)
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

function captureBlogImageUploadPreview(event: MouseEvent) {
  const target = event.target instanceof Element ? event.target : null
  const uploadButton = target?.closest('.md-editor-modal-clip .md-editor-btn')
  if (!uploadButton) {
    return
  }

  const modal = uploadButton.closest('.md-editor-modal-clip')
  const image = modal?.querySelector<HTMLImageElement>('.md-editor-clip-cropper img[src]')
  uploadingImagePreview.value = image?.src || ''
}

async function onUploadImg(files: File[], callback: (urls: string[]) => void) {
  uploadingImages.value = true
  try {
    const urls: string[] = []
    for (const file of files) {
      const resource = await uploadBlogEditorImage(file, blogEditorSourceType)
      urls.push(resource.publicUrl || `/api/files/${resource.id}/download`)
    }
    callback(urls)
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('blog.messages.imageUploadFailed'))
  } finally {
    uploadingImages.value = false
    uploadingImagePreview.value = ''
  }
}
</script>

<template>
  <main class="blog-shell">
    <AppHeader />
    <section class="editor-layout">
      <header>
        <div>
          <span>{{ t('blog.editor.kicker') }}</span>
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
          v-model:value="tags"
          multiple
          tag
          filterable
          :placeholder="t('blog.editor.tagsPlaceholder')"
          :options="tags.map(tag => ({ label: tag, value: tag }))"
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
            <n-spin size="small" />
            <span>{{ t('blog.editor.uploadingImage') }}</span>
          </div>
        </Transition>
      </div>
    </section>

    <Teleport to="body">
      <Transition name="uploading-fade">
        <div v-if="uploadingImages" class="blog-image-crop-upload-overlay" role="status" aria-live="polite">
          <div class="blog-image-crop-upload-card">
            <img v-if="uploadingImagePreview" :src="uploadingImagePreview" alt="">
            <div v-else class="blog-image-crop-upload-placeholder" />
            <div>
              <n-spin size="small" />
              <span>{{ t('blog.editor.uploadingImage') }}</span>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

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
  padding: 92px 0 42px;
}

.editor-layout header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.editor-layout header span {
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 800;
  text-transform: uppercase;
}

.editor-layout h1 {
  margin: 6px 0 0;
  font-size: clamp(26px, 4vw, 40px);
  line-height: 1.08;
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
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 36px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--sw-surface-solid) 88%, transparent);
  box-shadow: var(--sw-shadow-soft);
  color: var(--sw-text);
  padding: 0 12px;
  font-size: 12px;
  font-weight: 800;
  backdrop-filter: blur(14px);
}

.blog-image-crop-upload-overlay {
  position: fixed;
  inset: 0;
  z-index: 10020;
  display: grid;
  place-items: center;
  pointer-events: none;
}

.blog-image-crop-upload-card {
  display: grid;
  justify-items: center;
  gap: 12px;
  width: min(260px, calc(100vw - 56px));
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--sw-surface-solid) 90%, transparent);
  box-shadow: var(--sw-shadow-soft);
  color: var(--sw-text);
  padding: 14px;
  backdrop-filter: blur(14px);
}

.blog-image-crop-upload-card img,
.blog-image-crop-upload-placeholder {
  width: 100%;
  aspect-ratio: 16 / 10;
  border: 1px solid var(--sw-border);
  border-radius: 6px;
  background: var(--sw-surface-muted);
  object-fit: contain;
}

.blog-image-crop-upload-placeholder {
  background:
    linear-gradient(90deg, transparent, color-mix(in srgb, var(--sw-surface-solid) 42%, transparent), transparent),
    var(--sw-surface-muted);
  background-size: 180% 100%;
  animation: blog-crop-uploading 1.2s ease-in-out infinite;
}

.blog-image-crop-upload-card div:last-child {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 800;
}

:global(body.blog-image-uploading .md-editor-modal-clip .md-editor-modal-body) {
  position: relative;
}

:global(body.blog-image-uploading .md-editor-modal-clip .md-editor-clip) {
  pointer-events: none;
}

:global(body.blog-image-uploading .md-editor-modal-clip .md-editor-modal-func) {
  pointer-events: none;
  opacity: 0.44;
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

@keyframes blog-crop-uploading {
  from {
    background-position: 120% 0;
  }

  to {
    background-position: -120% 0;
  }
}

.modal-form {
  display: grid;
  gap: 12px;
}

.modal-form .n-button {
  justify-self: end;
}

@media (max-width: 920px) {
  .editor-layout header,
  .editor-actions {
    display: grid;
    align-items: stretch;
  }

  .editor-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .editor-actions .n-button,
  .import-button {
    width: 100%;
    justify-content: center;
  }

  .import-button {
    grid-column: 1 / -1;
  }

  .meta-panel {
    grid-template-columns: 1fr;
  }

  .blog-image-crop-upload-overlay {
    align-items: start;
    padding-top: min(42vh, 260px);
  }
}
</style>

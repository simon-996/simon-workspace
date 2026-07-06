<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import {
  NButton,
  NIcon,
  NInput,
  NModal,
  NSelect,
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
const auth = useAuthStore()
const theme = useThemeStore()
const categories = ref<BlogCategory[]>([])
const title = ref('')
const summary = ref('')
const categoryId = ref<string | null>(null)
const tags = ref<string[]>([])
const content = ref('')
const saving = ref(false)
const categoryModal = ref(false)
const categoryName = ref('')
const blogEditorSourceType = 'BLOG_EDITOR'

const canManageCategory = computed(() => auth.hasPermission('blog:category:manage'))
const categoryOptions = computed(() => categories.value.map((item) => ({ label: item.name, value: item.id })))

onMounted(async () => {
  const ok = await auth.restore()
  if (!ok || !auth.hasPermission('blog:post:create')) {
    await router.push('/login?redirect=/blog/new')
    return
  }
  await loadCategories()
})

async function loadCategories() {
  categories.value = await fetchBlogCategories()
}

async function save(status: 'DRAFT' | 'PUBLISHED') {
  if (!title.value.trim() || !content.value.trim()) {
    message.warning('Title and content are required')
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
    message.success(status === 'PUBLISHED' ? 'Published' : 'Draft saved')
    await router.push(`/blog/${post.id}`)
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'Failed to save post')
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
    message.error(error instanceof Error ? error.message : 'Failed to create category')
  }
}

function importMarkdown(file: File) {
  if (!file.name.toLowerCase().endsWith('.md')) {
    message.warning('Only .md files are supported')
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
  const urls: string[] = []
  for (const file of files) {
    const resource = await uploadBlogEditorImage(file, blogEditorSourceType)
    urls.push(resource.publicUrl || `/api/files/${resource.id}/download`)
  }
  callback(urls)
}
</script>

<template>
  <main class="blog-shell">
    <AppHeader />
    <section class="editor-layout">
      <header>
        <div>
          <span>New Post</span>
          <h1>Write in Markdown.</h1>
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
            Import
          </label>
          <n-button secondary :loading="saving" @click="save('DRAFT')">
            <template #icon>
              <n-icon :component="DeviceFloppy" />
            </template>
            Draft
          </n-button>
          <n-button type="primary" :loading="saving" @click="save('PUBLISHED')">Publish</n-button>
        </div>
      </header>

      <section class="meta-panel">
        <n-input v-model:value="title" placeholder="Title" />
        <n-input v-model:value="summary" placeholder="Summary" />
        <div class="category-row">
          <n-select v-model:value="categoryId" clearable :options="categoryOptions" placeholder="Category" />
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
          placeholder="Tags"
          :options="tags.map(tag => ({ label: tag, value: tag }))"
        />
      </section>

      <MdEditor
        v-model="content"
        class="markdown-editor"
        :theme="theme.isDark ? 'dark' : 'light'"
        language="en-US"
        preview-theme="github"
        :on-upload-img="onUploadImg"
      />
    </section>

    <n-modal v-model:show="categoryModal" preset="card" title="New category" class="category-modal">
      <div class="modal-form">
        <n-input v-model:value="categoryName" placeholder="Category name" @keyup.enter="addCategory" />
        <n-button type="primary" @click="addCategory">Create</n-button>
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
  padding: 96px 0 42px;
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
  font-size: clamp(30px, 5vw, 54px);
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

.markdown-editor {
  min-height: 620px;
  border-radius: 8px;
  overflow: hidden;
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

  .meta-panel {
    grid-template-columns: 1fr;
  }
}
</style>

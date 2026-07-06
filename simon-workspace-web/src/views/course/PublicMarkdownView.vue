<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { NButton, NIcon, NSpin, useMessage } from 'naive-ui'
import { ArrowLeft } from '@vicons/tabler'

import AppHeader from '../../components/AppHeader.vue'
import { fetchPublicCourseDetail, type CourseMaterial } from '../../api/workspace'
import { useThemeStore } from '../../stores/theme'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { t } = useI18n()
const theme = useThemeStore()
const loading = ref(false)
const material = ref<CourseMaterial | null>(null)
const content = ref('')

onMounted(() => {
  void load()
})

async function load() {
  loading.value = true
  try {
    const detail = await fetchPublicCourseDetail(String(route.params.id))
    material.value = [...detail.documents, ...detail.courseware, ...detail.resources]
      .find((item) => item.id === String(route.params.materialId)) || null
    if (!material.value?.fileId) return
    const response = await fetch(`/api/files/public/${material.value.fileId}/view`)
    content.value = await response.text()
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('courses.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="markdown-shell">
    <AppHeader />
    <n-spin :show="loading">
      <article class="markdown-page">
        <n-button quaternary @click="router.push(`/courses/${route.params.id}`)">
          <template #icon>
            <n-icon :component="ArrowLeft" />
          </template>
          {{ t('courses.back') }}
        </n-button>
        <header>
          <span>{{ material?.originalFilename || material?.title }}</span>
          <h1>{{ material?.title }}</h1>
        </header>
        <MdPreview :model-value="content" :theme="theme.isDark ? 'dark' : 'light'" preview-theme="github" />
      </article>
    </n-spin>
  </main>
</template>

<style scoped>
.markdown-shell {
  min-height: 100vh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.markdown-page {
  width: min(820px, calc(100% - 32px));
  margin: 0 auto;
  padding: 10px 0 72px;
}

.markdown-page header {
  border-bottom: 1px solid var(--sw-border);
  margin-bottom: 18px;
  padding: 18px 0 22px;
}

.markdown-page header span {
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 800;
}

.markdown-page h1 {
  margin: 10px 0 0;
  font-size: clamp(26px, 4vw, 40px);
}
</style>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NIcon, NSpin, useMessage } from 'naive-ui'
import { ArrowLeft, Download, ExternalLink, FileText } from '@vicons/tabler'

import AppHeader from '../../components/AppHeader.vue'
import { fetchPublicCourseDetail, type CourseMaterial, type PublicCourseDetail } from '../../api/workspace'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { t } = useI18n()
const loading = ref(false)
const detail = ref<PublicCourseDetail | null>(null)

onMounted(() => {
  void load()
})

async function load() {
  loading.value = true
  try {
    detail.value = await fetchPublicCourseDetail(String(route.params.id))
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('courses.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

function publicFileUrl(material: CourseMaterial, action: 'view' | 'download') {
  return `/api/files/public/${material.fileId}/${action}`
}

function openMaterial(material: CourseMaterial) {
  if (material.materialType === 'LINK' && material.externalUrl) {
    window.open(material.externalUrl, '_blank', 'noopener,noreferrer')
    return
  }
  if (!material.fileId) return
  if (material.fileExtension?.toLowerCase() === 'md') {
    void router.push(`/courses/${route.params.id}/materials/${material.id}/read`)
    return
  }
  window.open(publicFileUrl(material, 'view'), '_blank', 'noopener,noreferrer')
}

function downloadMaterial(material: CourseMaterial) {
  if (!material.fileId) return
  window.open(publicFileUrl(material, 'download'), '_blank', 'noopener,noreferrer')
}
</script>

<template>
  <main class="course-shell">
    <AppHeader />
    <n-spin :show="loading">
      <section v-if="detail" class="course-detail">
        <n-button quaternary @click="router.push('/courses')">
          <template #icon>
            <n-icon :component="ArrowLeft" />
          </template>
          {{ t('courses.back') }}
        </n-button>

        <header>
          <span>{{ detail.course.courseCode || t('courses.kicker') }}</span>
          <h1>{{ detail.course.courseName }}</h1>
          <p>{{ detail.course.description || t('courses.noDescription') }}</p>
        </header>

        <section class="material-grid">
          <section class="material-section">
            <h2>{{ t('courses.sections.documents') }}</h2>
            <article v-for="item in detail.documents" :key="item.id" class="material-item">
              <n-icon :component="FileText" />
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.description || item.originalFilename || item.externalUrl }}</p>
              </div>
              <n-button size="small" secondary @click="openMaterial(item)">
                <n-icon :component="ExternalLink" />
              </n-button>
              <n-button v-if="item.fileId" size="small" quaternary @click="downloadMaterial(item)">
                <n-icon :component="Download" />
              </n-button>
            </article>
          </section>

          <section class="material-section">
            <h2>{{ t('courses.sections.courseware') }}</h2>
            <article v-for="item in detail.courseware" :key="item.id" class="material-item">
              <n-icon :component="FileText" />
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.description || item.originalFilename || item.externalUrl }}</p>
              </div>
              <n-button size="small" secondary @click="openMaterial(item)">
                <n-icon :component="ExternalLink" />
              </n-button>
              <n-button v-if="item.fileId" size="small" quaternary @click="downloadMaterial(item)">
                <n-icon :component="Download" />
              </n-button>
            </article>
          </section>

          <section class="material-section">
            <h2>{{ t('courses.sections.resources') }}</h2>
            <article v-for="item in detail.resources" :key="item.id" class="material-item">
              <n-icon :component="FileText" />
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.description || item.originalFilename || item.externalUrl }}</p>
              </div>
              <n-button size="small" secondary @click="openMaterial(item)">
                <n-icon :component="ExternalLink" />
              </n-button>
              <n-button v-if="item.fileId" size="small" quaternary @click="downloadMaterial(item)">
                <n-icon :component="Download" />
              </n-button>
            </article>
          </section>
        </section>
      </section>
    </n-spin>
  </main>
</template>

<style scoped>
.course-shell {
  min-height: 100vh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.course-detail {
  width: min(980px, calc(100% - 32px));
  margin: 0 auto;
  padding: 10px 0 72px;
}

.course-detail header {
  border-bottom: 1px solid var(--sw-border);
  margin-bottom: 18px;
  padding: 18px 0 22px;
}

.course-detail header span {
  color: #16708f;
  font-size: 13px;
  font-weight: 800;
}

.course-detail h1 {
  margin: 10px 0;
  font-size: clamp(28px, 5vw, 44px);
}

.course-detail header p,
.material-item p {
  color: var(--sw-muted);
  font-size: 14px;
  line-height: 1.7;
}

.material-grid {
  display: grid;
  gap: 18px;
}

.material-section h2 {
  margin: 0 0 10px;
  font-size: 18px;
}

.material-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 10px;
  border-top: 1px solid var(--sw-border);
  padding: 12px 0;
}

.material-item strong,
.material-item p {
  display: block;
  margin: 0;
}

@media (max-width: 760px) {
  .course-detail {
    padding-top: 0;
  }

  .material-item {
    grid-template-columns: 24px minmax(0, 1fr) auto;
  }
}
</style>

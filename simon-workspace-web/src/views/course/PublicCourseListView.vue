<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { NIcon, NSpin, useMessage } from 'naive-ui'
import { Book2 } from '@vicons/tabler'

import AppHeader from '../../components/AppHeader.vue'
import { fetchPublicCourses, type Course } from '../../api/workspace'

const router = useRouter()
const message = useMessage()
const { t } = useI18n()
const loading = ref(false)
const courses = ref<Course[]>([])

onMounted(() => {
  void load()
})

async function load() {
  loading.value = true
  try {
    courses.value = await fetchPublicCourses()
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('courses.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

function openCourse(course: Course) {
  void router.push(`/courses/${course.id}`)
}
</script>

<template>
  <main class="course-shell">
    <AppHeader />
    <section class="course-layout">
      <header class="course-head">
        <span>{{ t('courses.kicker') }}</span>
      </header>

      <n-spin :show="loading">
        <section v-if="courses.length" class="course-list">
          <article v-for="course in courses" :key="course.id" class="course-card" @click="openCourse(course)">
            <n-icon :component="Book2" />
            <div>
              <h2>{{ course.courseName }}</h2>
              <p>{{ course.description || course.courseCode || t('courses.noDescription') }}</p>
            </div>
            <span>{{ course.totalHours || 0 }}h</span>
          </article>
        </section>
        <section v-else-if="!loading" class="empty-state">
          {{ t('courses.empty') }}
        </section>
      </n-spin>
    </section>
  </main>
</template>

<style scoped>
.course-shell {
  min-height: 100vh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.course-layout {
  width: min(1000px, calc(100% - 32px));
  margin: 0 auto;
  padding: 10px 0 72px;
}

.course-head {
  border-bottom: 1px solid var(--sw-border);
  padding: 18px 0;
}

.course-head span {
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 800;
  text-transform: uppercase;
}

.course-list {
  display: grid;
  gap: 12px;
  padding-top: 18px;
}

.course-card {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  cursor: pointer;
  padding: 18px;
  transition: border-color 180ms ease, transform 180ms ease;
}

.course-card:hover {
  border-color: #85afbc;
  transform: translateY(-2px);
}

.course-card h2 {
  margin: 0 0 6px;
  font-size: 20px;
}

.course-card p,
.course-card span {
  margin: 0;
  color: var(--sw-muted);
  font-size: 14px;
}

.empty-state {
  display: grid;
  place-items: center;
  min-height: 320px;
  color: var(--sw-muted);
}

@media (max-width: 760px) {
  .course-layout {
    padding-top: 0;
  }
}
</style>

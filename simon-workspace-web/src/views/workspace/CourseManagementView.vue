<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpin,
  useMessage,
} from 'naive-ui'
import { AlertTriangle, Edit, Plus, Refresh, Search, Trash } from '@vicons/tabler'

import {
  createCourse,
  deleteCourse,
  fetchCourses,
  updateCourse,
  type Course,
  type CoursePayload,
} from '../../api/workspace'

const message = useMessage()

const courses = ref<Course[]>([])
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const modalVisible = ref(false)
const editingId = ref<string | null>(null)

const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '归档', value: 'ARCHIVED' },
]

const form = reactive({
  courseName: '',
  courseCode: '',
  major: '',
  grade: '',
  totalHours: 0,
  theoryHours: null as number | null,
  experimentHours: null as number | null,
  weeklyHours: null as number | null,
  credit: null as number | null,
  textbook: '',
  courseGoal: '',
  keyPoint: '',
  difficultPoint: '',
  assessmentMethod: '',
  syllabus: '',
  description: '',
  status: 'ACTIVE',
})

const activeCount = computed(() => courses.value.filter((course) => course.status === 'ACTIVE').length)
const archivedCount = computed(() => courses.value.filter((course) => course.status === 'ARCHIVED').length)
const totalHours = computed(() => courses.value.reduce((sum, course) => sum + (course.totalHours ?? 0), 0))
const modalTitle = computed(() => (editingId.value ? '编辑课程' : '新增课程'))

onMounted(() => {
  void loadCourses()
})

async function loadCourses() {
  loading.value = true
  error.value = ''
  try {
    courses.value = await fetchCourses(keyword.value.trim())
  } catch (err) {
    error.value = err instanceof Error ? err.message : '课程列表加载失败'
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  modalVisible.value = true
}

function openEdit(course: Course) {
  editingId.value = course.id
  form.courseName = course.courseName ?? ''
  form.courseCode = course.courseCode ?? ''
  form.major = course.major ?? ''
  form.grade = course.grade ?? ''
  form.totalHours = course.totalHours ?? 0
  form.theoryHours = course.theoryHours ?? null
  form.experimentHours = course.experimentHours ?? null
  form.weeklyHours = course.weeklyHours ?? null
  form.credit = course.credit ?? null
  form.textbook = course.textbook ?? ''
  form.courseGoal = course.courseGoal ?? ''
  form.keyPoint = course.keyPoint ?? ''
  form.difficultPoint = course.difficultPoint ?? ''
  form.assessmentMethod = course.assessmentMethod ?? ''
  form.syllabus = course.syllabus ?? ''
  form.description = course.description ?? ''
  form.status = course.status || 'ACTIVE'
  modalVisible.value = true
}

async function submitCourse() {
  if (!form.courseName.trim()) {
    message.warning('请输入课程名称')
    return
  }

  if (form.totalHours < 0) {
    message.warning('总学时不能小于 0')
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updateCourse(editingId.value, payload)
      message.success('课程已更新')
    } else {
      await createCourse(payload)
      message.success('课程已新增')
    }
    modalVisible.value = false
    await loadCourses()
  } catch (err) {
    message.error(err instanceof Error ? err.message : '保存课程失败')
  } finally {
    saving.value = false
  }
}

async function confirmDelete(course: Course) {
  try {
    await deleteCourse(course.id)
    message.success('课程已删除')
    await loadCourses()
  } catch (err) {
    message.error(err instanceof Error ? err.message : '删除课程失败')
  }
}

function resetForm() {
  form.courseName = ''
  form.courseCode = ''
  form.major = ''
  form.grade = ''
  form.totalHours = 0
  form.theoryHours = null
  form.experimentHours = null
  form.weeklyHours = null
  form.credit = null
  form.textbook = ''
  form.courseGoal = ''
  form.keyPoint = ''
  form.difficultPoint = ''
  form.assessmentMethod = ''
  form.syllabus = ''
  form.description = ''
  form.status = 'ACTIVE'
}

function buildPayload(): CoursePayload {
  return {
    courseName: form.courseName.trim(),
    courseCode: textOrNull(form.courseCode),
    major: textOrNull(form.major),
    grade: textOrNull(form.grade),
    totalHours: form.totalHours ?? 0,
    theoryHours: form.theoryHours,
    experimentHours: form.experimentHours,
    weeklyHours: form.weeklyHours,
    credit: form.credit,
    textbook: textOrNull(form.textbook),
    courseGoal: textOrNull(form.courseGoal),
    keyPoint: textOrNull(form.keyPoint),
    difficultPoint: textOrNull(form.difficultPoint),
    assessmentMethod: textOrNull(form.assessmentMethod),
    syllabus: textOrNull(form.syllabus),
    description: textOrNull(form.description),
    status: form.status,
  }
}

function textOrNull(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function statusText(status: string) {
  return status === 'ARCHIVED' ? '归档' : '启用'
}
</script>

<template>
  <section class="course-page">
    <div class="summary-grid">
      <article>
        <span>课程总数</span>
        <strong>{{ courses.length }}</strong>
      </article>
      <article>
        <span>启用课程</span>
        <strong>{{ activeCount }}</strong>
      </article>
      <article>
        <span>归档课程</span>
        <strong>{{ archivedCount }}</strong>
      </article>
      <article>
        <span>总学时</span>
        <strong>{{ totalHours }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        placeholder="搜索课程名称或编码"
        @keyup.enter="loadCourses"
      >
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <div class="toolbar-actions">
        <n-button secondary class="icon-button" @click="loadCourses">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          刷新
        </n-button>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          新增课程
        </n-button>
      </div>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadCourses">重试</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="courses.length === 0" class="empty-state">
        <strong>暂无课程</strong>
        <span>创建第一门课程后会显示在这里。</span>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          新增课程
        </n-button>
      </div>

      <div v-else class="course-table-wrap">
        <table class="course-table">
          <thead>
            <tr>
              <th>课程</th>
              <th>专业 / 年级</th>
              <th>学时</th>
              <th>学分</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="course in courses" :key="course.id">
              <td>
                <strong>{{ course.courseName }}</strong>
                <span>{{ course.courseCode || '未设置编码' }}</span>
              </td>
              <td>
                <strong>{{ course.major || '未设置专业' }}</strong>
                <span>{{ course.grade || '未设置年级' }}</span>
              </td>
              <td>
                <strong>{{ course.totalHours ?? 0 }}</strong>
                <span>理论 {{ course.theoryHours ?? '-' }} / 实验 {{ course.experimentHours ?? '-' }}</span>
              </td>
              <td>{{ course.credit ?? '-' }}</td>
              <td>
                <span class="status-pill" :class="{ archived: course.status === 'ARCHIVED' }">
                  {{ statusText(course.status) }}
                </span>
              </td>
              <td>{{ course.updatedTime ? course.updatedTime.slice(0, 10) : '-' }}</td>
              <td>
                <div class="row-actions">
                  <n-button quaternary size="small" @click="openEdit(course)">
                    <template #icon>
                      <n-icon :component="Edit" />
                    </template>
                  </n-button>
                  <n-popconfirm
                    positive-text="删除"
                    negative-text="取消"
                    @positive-click="confirmDelete(course)"
                  >
                    <template #trigger>
                      <n-button quaternary size="small" type="error">
                        <template #icon>
                          <n-icon :component="Trash" />
                        </template>
                      </n-button>
                    </template>
                    删除后课程将从列表中移除。
                  </n-popconfirm>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" class="course-modal">
      <form class="course-form" @submit.prevent="submitCourse">
        <label class="field span-2">
          <span>课程名称</span>
          <n-input v-model:value="form.courseName" placeholder="例如：Java 程序设计" />
        </label>

        <label class="field">
          <span>课程编码</span>
          <n-input v-model:value="form.courseCode" placeholder="例如：CS-JAVA-01" />
        </label>

        <label class="field">
          <span>状态</span>
          <n-select v-model:value="form.status" :options="statusOptions" />
        </label>

        <label class="field">
          <span>专业</span>
          <n-input v-model:value="form.major" placeholder="例如：软件技术" />
        </label>

        <label class="field">
          <span>年级</span>
          <n-input v-model:value="form.grade" placeholder="例如：2026" />
        </label>

        <label class="field">
          <span>总学时</span>
          <n-input-number v-model:value="form.totalHours" :min="0" />
        </label>

        <label class="field">
          <span>理论学时</span>
          <n-input-number v-model:value="form.theoryHours" :min="0" clearable />
        </label>

        <label class="field">
          <span>实验学时</span>
          <n-input-number v-model:value="form.experimentHours" :min="0" clearable />
        </label>

        <label class="field">
          <span>周学时</span>
          <n-input-number v-model:value="form.weeklyHours" :min="0" clearable />
        </label>

        <label class="field">
          <span>学分</span>
          <n-input-number v-model:value="form.credit" :min="0" :precision="1" clearable />
        </label>

        <label class="field span-2">
          <span>教材</span>
          <n-input v-model:value="form.textbook" placeholder="教材名称、版本或出版社" />
        </label>

        <label class="field span-2">
          <span>课程目标</span>
          <n-input v-model:value="form.courseGoal" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <label class="field">
          <span>教学重点</span>
          <n-input v-model:value="form.keyPoint" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <label class="field">
          <span>教学难点</span>
          <n-input v-model:value="form.difficultPoint" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <label class="field span-2">
          <span>考核方式</span>
          <n-input v-model:value="form.assessmentMethod" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <label class="field span-2">
          <span>课程大纲</span>
          <n-input v-model:value="form.syllabus" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
        </label>

        <label class="field span-2">
          <span>课程简介</span>
          <n-input v-model:value="form.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <div class="form-actions span-2">
          <n-button @click="modalVisible = false">取消</n-button>
          <n-button type="primary" attr-type="submit" :loading="saving">保存</n-button>
        </div>
      </form>
    </n-modal>
  </section>
</template>

<style scoped>
.course-page {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr 1fr;
  gap: 10px;
}

.summary-grid article {
  min-height: 86px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
}

.summary-grid span,
.field span {
  color: #647586;
  font-size: 12px;
  font-weight: 800;
}

.summary-grid strong {
  display: block;
  margin-top: 8px;
  color: #111a23;
  font-size: 28px;
  font-weight: 800;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(0, 440px) auto;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 12px;
}

.toolbar-actions,
.row-actions,
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

.table-panel {
  min-height: 360px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
}

.course-table-wrap {
  overflow-x: auto;
}

.course-table {
  width: 100%;
  min-width: 920px;
  border-collapse: collapse;
}

.course-table th {
  border-bottom: 1px solid #d8e0e7;
  background: #f7f9fb;
  color: #5e7181;
  font-size: 12px;
  font-weight: 800;
  text-align: left;
}

.course-table th,
.course-table td {
  padding: 14px 16px;
}

.course-table td {
  border-bottom: 1px solid #edf1f4;
  color: #2b3843;
  font-size: 14px;
  vertical-align: top;
}

.course-table tr:last-child td {
  border-bottom: 0;
}

.course-table td strong {
  display: block;
  color: #111a23;
  font-size: 14px;
  font-weight: 800;
}

.course-table td span {
  display: block;
  margin-top: 4px;
  color: #687a89;
  font-size: 12px;
}

.status-pill {
  display: inline-flex !important;
  align-items: center;
  min-height: 24px;
  border-radius: 999px;
  background: #e5f6ee;
  color: #28734d !important;
  padding: 0 10px;
  font-weight: 800;
}

.status-pill.archived {
  background: #edf1f4;
  color: #667381 !important;
}

.error-state,
.empty-state {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 360px;
  color: #607283;
  text-align: center;
}

.error-state .n-icon {
  color: #b76b2b;
  font-size: 30px;
}

.empty-state strong {
  color: #111a23;
  font-size: 18px;
}

.skeleton-table {
  display: grid;
  gap: 10px;
  padding: 18px;
}

.skeleton-table span {
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(90deg, #eef3f6, #f8fafb, #eef3f6);
  background-size: 200% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}

.course-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: grid;
  gap: 7px;
}

.field :deep(.n-input-number) {
  width: 100%;
}

.span-2 {
  grid-column: 1 / -1;
}

.form-actions {
  padding-top: 8px;
}

:global(.course-modal) {
  max-width: 860px;
  width: calc(100vw - 32px);
  border-radius: 8px;
}

@keyframes shimmer {
  from {
    background-position: 200% 0;
  }

  to {
    background-position: -200% 0;
  }
}

@media (max-width: 860px) {
  .summary-grid,
  .toolbar,
  .course-form {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    justify-content: stretch;
  }

  .toolbar-actions .n-button {
    flex: 1;
  }
}
</style>

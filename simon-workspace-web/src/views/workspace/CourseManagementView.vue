<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NButton,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpin,
  NSwitch,
  useMessage,
} from 'naive-ui'
import { AlertTriangle, Edit, Plus, Refresh, Search, Trash } from '@vicons/tabler'

import {
  createCourse,
  createCourseMaterial,
  deleteCourse,
  deleteCourseMaterial,
  fetchCourses,
  fetchCourseMaterials,
  updateCourse,
  updateCourseMaterial,
  uploadCourseMaterialFile,
  type Course,
  type CourseMaterial,
  type CoursePayload,
} from '../../api/workspace'

const { t } = useI18n()
const message = useMessage()

const courses = ref<Course[]>([])
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const modalVisible = ref(false)
const editingId = ref<string | null>(null)
const selectedCourse = ref<Course | null>(null)
const materials = ref<CourseMaterial[]>([])
const materialsLoading = ref(false)
const materialSaving = ref(false)
const materialError = ref('')
const activeSection = ref('DOCUMENT')
const selectedFile = ref<File | null>(null)
const editingMaterialId = ref<string | null>(null)

const statusOptions = computed(() => [
  { label: t('common.states.active'), value: 'ACTIVE' },
  { label: t('common.states.archived'), value: 'ARCHIVED' },
])

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
  publicVisible: false,
  publicSortOrder: 0,
})

const materialForm = reactive({
  title: '',
  description: '',
  section: 'DOCUMENT',
  materialType: 'FILE',
  externalUrl: '',
  sortOrder: 0,
  status: 'ACTIVE',
})

const activeCount = computed(() => courses.value.filter((course) => course.status === 'ACTIVE').length)
const archivedCount = computed(() => courses.value.filter((course) => course.status === 'ARCHIVED').length)
const totalHours = computed(() => courses.value.reduce((sum, course) => sum + (course.totalHours ?? 0), 0))
const modalTitle = computed(() => (editingId.value ? t('workspace.courses.modal.edit') : t('workspace.courses.modal.create')))
const materialSections = computed(() => [
  { label: t('workspace.courses.materials.sections.documents'), value: 'DOCUMENT' },
  { label: t('workspace.courses.materials.sections.courseware'), value: 'COURSEWARE' },
  { label: t('workspace.courses.materials.sections.resources'), value: 'RESOURCE' },
])
const materialTypeOptions = computed(() => [
  { label: t('workspace.courses.materials.types.file'), value: 'FILE' },
  { label: t('workspace.courses.materials.types.link'), value: 'LINK' },
])
const materialStatusOptions = computed(() => [
  { label: t('workspace.courses.materials.status.active'), value: 'ACTIVE' },
  { label: t('workspace.courses.materials.status.disabled'), value: 'DISABLED' },
])
const filteredMaterials = computed(() => materials.value.filter((item) => item.section === activeSection.value))

onMounted(() => {
  void loadCourses()
})

async function loadCourses() {
  loading.value = true
  error.value = ''
  try {
    courses.value = await fetchCourses(keyword.value.trim())
    if (selectedCourse.value) {
      selectedCourse.value = courses.value.find((course) => course.id === selectedCourse.value?.id) ?? null
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('workspace.courses.messages.loadFailed')
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
  form.publicVisible = Boolean(course.publicVisible)
  form.publicSortOrder = course.publicSortOrder ?? 0
  modalVisible.value = true
}

async function submitCourse() {
  if (!form.courseName.trim()) {
    message.warning(t('workspace.courses.messages.nameRequired'))
    return
  }

  if (form.totalHours < 0) {
    message.warning(t('workspace.courses.messages.invalidTotalHours'))
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updateCourse(editingId.value, payload)
      message.success(t('workspace.courses.messages.updated'))
    } else {
      await createCourse(payload)
      message.success(t('workspace.courses.messages.created'))
    }
    modalVisible.value = false
    await loadCourses()
    if (selectedCourse.value?.id === course.id) {
      selectedCourse.value = null
      materials.value = []
    }
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.courses.messages.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function confirmDelete(course: Course) {
  try {
    await deleteCourse(course.id)
    message.success(t('workspace.courses.messages.deleted'))
    await loadCourses()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.courses.messages.deleteFailed'))
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
  form.publicVisible = false
  form.publicSortOrder = 0
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
    publicVisible: form.publicVisible,
    publicSortOrder: form.publicSortOrder ?? 0,
  }
}

function textOrNull(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function statusText(status: string) {
  return status === 'ARCHIVED' ? t('common.states.archived') : t('common.states.active')
}

function materialStatusText(status: string) {
  return status === 'DISABLED' ? t('workspace.courses.materials.status.disabled') : t('workspace.courses.materials.status.active')
}

function materialMeta(item: CourseMaterial) {
  if (item.materialType === 'LINK') {
    return item.externalUrl || '-'
  }
  return item.originalFilename || item.publicUrl || '-'
}

async function selectCourseMaterials(course: Course) {
  selectedCourse.value = course
  activeSection.value = 'DOCUMENT'
  resetMaterialForm('DOCUMENT')
  await loadMaterials()
}

async function loadMaterials() {
  if (!selectedCourse.value) return
  materialsLoading.value = true
  materialError.value = ''
  try {
    materials.value = await fetchCourseMaterials(selectedCourse.value.id)
  } catch (err) {
    materialError.value = err instanceof Error ? err.message : t('workspace.courses.materials.messages.loadFailed')
    message.error(materialError.value)
  } finally {
    materialsLoading.value = false
  }
}

function setMaterialSection(section: string) {
  activeSection.value = section
  resetMaterialForm(section)
}

function onMaterialFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  selectedFile.value = input.files?.[0] ?? null
}

async function submitMaterial() {
  if (!selectedCourse.value) return
  if (!materialForm.title.trim()) {
    message.warning(t('workspace.courses.materials.messages.titleRequired'))
    return
  }
  const editingItem = editingMaterialId.value
    ? materials.value.find((item) => item.id === editingMaterialId.value)
    : null
  if (materialForm.materialType === 'FILE' && !selectedFile.value && !editingItem?.fileId) {
    message.warning(t('workspace.courses.materials.messages.fileRequired'))
    return
  }
  if (materialForm.materialType === 'LINK' && !materialForm.externalUrl.trim()) {
    message.warning(t('workspace.courses.materials.messages.linkRequired'))
    return
  }

  materialSaving.value = true
  try {
    const file = materialForm.materialType === 'FILE' && selectedFile.value
      ? await uploadCourseMaterialFile(selectedFile.value)
      : null
    const payload = {
      section: materialForm.section,
      materialType: materialForm.materialType,
      fileId: materialForm.materialType === 'FILE' ? (file?.id ?? editingItem?.fileId ?? null) : null,
      externalUrl: materialForm.materialType === 'LINK' ? textOrNull(materialForm.externalUrl) : null,
      title: materialForm.title.trim(),
      description: textOrNull(materialForm.description),
      sortOrder: materialForm.sortOrder ?? 0,
      status: materialForm.status,
    }
    if (editingMaterialId.value) {
      await updateCourseMaterial(selectedCourse.value.id, editingMaterialId.value, payload)
      message.success(t('workspace.courses.materials.messages.updated'))
    } else {
      await createCourseMaterial(selectedCourse.value.id, payload)
      message.success(t('workspace.courses.materials.messages.created'))
    }
    resetMaterialForm(activeSection.value)
    await loadMaterials()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.courses.materials.messages.saveFailed'))
  } finally {
    materialSaving.value = false
  }
}

function openEditMaterial(item: CourseMaterial) {
  editingMaterialId.value = item.id
  activeSection.value = item.section
  materialForm.title = item.title ?? ''
  materialForm.description = item.description ?? ''
  materialForm.section = item.section
  materialForm.materialType = item.materialType
  materialForm.externalUrl = item.externalUrl ?? ''
  materialForm.sortOrder = item.sortOrder ?? 0
  materialForm.status = item.status || 'ACTIVE'
  selectedFile.value = null
}

async function confirmDeleteMaterial(item: CourseMaterial) {
  if (!selectedCourse.value) return
  try {
    await deleteCourseMaterial(selectedCourse.value.id, item.id)
    message.success(t('workspace.courses.materials.messages.deleted'))
    await loadMaterials()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.courses.materials.messages.deleteFailed'))
  }
}

function resetMaterialForm(section = activeSection.value) {
  materialForm.title = ''
  materialForm.description = ''
  materialForm.section = section
  materialForm.materialType = 'FILE'
  materialForm.externalUrl = ''
  materialForm.sortOrder = 0
  materialForm.status = 'ACTIVE'
  selectedFile.value = null
  editingMaterialId.value = null
}
</script>

<template>
  <section class="course-page">
    <div class="summary-grid">
      <article>
        <span>{{ t('workspace.courses.summary.total') }}</span>
        <strong>{{ courses.length }}</strong>
      </article>
      <article>
        <span>{{ t('workspace.courses.summary.active') }}</span>
        <strong>{{ activeCount }}</strong>
      </article>
      <article>
        <span>{{ t('workspace.courses.summary.archived') }}</span>
        <strong>{{ archivedCount }}</strong>
      </article>
      <article>
        <span>{{ t('workspace.courses.summary.totalHours') }}</span>
        <strong>{{ totalHours }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        :placeholder="t('workspace.courses.searchPlaceholder')"
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
          {{ t('common.actions.refresh') }}
        </n-button>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          {{ t('workspace.courses.create') }}
        </n-button>
      </div>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadCourses">{{ t('common.actions.retry') }}</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="courses.length === 0" class="empty-state">
        <strong>{{ t('workspace.courses.emptyTitle') }}</strong>
        <span>{{ t('workspace.courses.emptyText') }}</span>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          {{ t('workspace.courses.create') }}
        </n-button>
      </div>

      <div v-else class="course-table-wrap">
        <table class="course-table">
          <thead>
            <tr>
              <th>{{ t('workspace.courses.table.course') }}</th>
              <th>{{ t('workspace.courses.table.majorGrade') }}</th>
              <th>{{ t('workspace.courses.table.hours') }}</th>
              <th>{{ t('workspace.courses.table.credit') }}</th>
              <th>{{ t('workspace.courses.table.visibility') }}</th>
              <th>{{ t('workspace.courses.table.status') }}</th>
              <th>{{ t('workspace.courses.table.updatedTime') }}</th>
              <th>{{ t('workspace.courses.table.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="course in courses" :key="course.id">
              <td>
                <strong>{{ course.courseName }}</strong>
                <span>{{ course.courseCode || t('workspace.courses.table.unsetCode') }}</span>
              </td>
              <td>
                <strong>{{ course.major || t('workspace.courses.table.unsetMajor') }}</strong>
                <span>{{ course.grade || t('workspace.courses.table.unsetGrade') }}</span>
              </td>
              <td>
                <strong>{{ course.totalHours ?? 0 }}</strong>
                <span>{{ t('workspace.courses.table.theoryExperiment', { theory: course.theoryHours ?? '-', experiment: course.experimentHours ?? '-' }) }}</span>
              </td>
              <td>{{ course.credit ?? '-' }}</td>
              <td>
                <span class="status-pill" :class="{ archived: !course.publicVisible }">
                  {{ course.publicVisible ? t('workspace.courses.table.public') : t('workspace.courses.table.private') }}
                </span>
              </td>
              <td>
                <span class="status-pill" :class="{ archived: course.status === 'ARCHIVED' }">
                  {{ statusText(course.status) }}
                </span>
              </td>
              <td>{{ course.updatedTime ? course.updatedTime.slice(0, 10) : '-' }}</td>
              <td>
                <div class="row-actions">
                  <n-button quaternary size="small" @click="selectCourseMaterials(course)">
                    {{ t('workspace.courses.materials.manage') }}
                  </n-button>
                  <n-button quaternary size="small" @click="openEdit(course)">
                    <template #icon>
                      <n-icon :component="Edit" />
                    </template>
                  </n-button>
                  <n-popconfirm
                    :positive-text="t('common.actions.delete')"
                    :negative-text="t('common.actions.cancel')"
                    @positive-click="confirmDelete(course)"
                  >
                    <template #trigger>
                      <n-button quaternary size="small" type="error">
                        <template #icon>
                          <n-icon :component="Trash" />
                        </template>
                      </n-button>
                    </template>
                    {{ t('workspace.courses.messages.deleteConfirm') }}
                  </n-popconfirm>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section v-if="selectedCourse" class="materials-panel">
      <header class="materials-head">
        <div>
          <span>{{ t('workspace.courses.materials.kicker') }}</span>
          <h2>{{ selectedCourse.courseName }}</h2>
        </div>
        <n-button secondary class="icon-button" @click="loadMaterials">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          {{ t('common.actions.refresh') }}
        </n-button>
      </header>

      <div class="section-tabs">
        <button
          v-for="section in materialSections"
          :key="section.value"
          type="button"
          :class="{ active: activeSection === section.value }"
          @click="setMaterialSection(section.value)"
        >
          {{ section.label }}
        </button>
      </div>

      <form class="material-form" @submit.prevent="submitMaterial">
        <label class="field">
          <span>{{ t('workspace.courses.materials.fields.title') }}</span>
          <n-input v-model:value="materialForm.title" :placeholder="t('workspace.courses.materials.fields.titlePlaceholder')" />
        </label>
        <label class="field">
          <span>{{ t('workspace.courses.materials.fields.type') }}</span>
          <n-select v-model:value="materialForm.materialType" :options="materialTypeOptions" />
        </label>
        <label v-if="materialForm.materialType === 'LINK'" class="field span-2">
          <span>{{ t('workspace.courses.materials.fields.url') }}</span>
          <n-input v-model:value="materialForm.externalUrl" placeholder="https://" />
        </label>
        <label v-else class="field span-2">
          <span>{{ t('workspace.courses.materials.fields.file') }}</span>
          <input class="file-input" type="file" @change="onMaterialFileChange" />
        </label>
        <label class="field">
          <span>{{ t('workspace.courses.materials.fields.sortOrder') }}</span>
          <n-input-number v-model:value="materialForm.sortOrder" :min="0" />
        </label>
        <label class="field">
          <span>{{ t('workspace.courses.materials.fields.status') }}</span>
          <n-select v-model:value="materialForm.status" :options="materialStatusOptions" />
        </label>
        <label class="field">
          <span>{{ t('workspace.courses.materials.fields.description') }}</span>
          <n-input v-model:value="materialForm.description" :placeholder="t('workspace.courses.materials.fields.descriptionPlaceholder')" />
        </label>
        <div class="form-actions span-2">
          <n-button v-if="editingMaterialId" @click="resetMaterialForm(activeSection)">
            {{ t('common.actions.cancel') }}
          </n-button>
          <n-button type="primary" attr-type="submit" :loading="materialSaving">
            {{ editingMaterialId ? t('workspace.courses.materials.actions.update') : t('workspace.courses.materials.actions.add') }}
          </n-button>
        </div>
      </form>

      <div v-if="materialError" class="error-state compact">
        <span>{{ materialError }}</span>
        <n-button size="small" tertiary @click="loadMaterials">{{ t('common.actions.retry') }}</n-button>
      </div>

      <n-spin v-else-if="materialsLoading" :show="materialsLoading">
        <div class="skeleton-table compact">
          <span v-for="index in 3" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="filteredMaterials.length === 0" class="empty-materials">
        {{ t('workspace.courses.materials.empty') }}
      </div>

      <div v-else class="materials-list">
        <article v-for="item in filteredMaterials" :key="item.id" class="material-row">
          <div>
            <strong>{{ item.title }}</strong>
            <span>{{ materialMeta(item) }}</span>
          </div>
          <span class="status-pill" :class="{ archived: item.status !== 'ACTIVE' }">
            {{ materialStatusText(item.status) }}
          </span>
          <n-button quaternary size="small" @click="openEditMaterial(item)">
            <template #icon>
              <n-icon :component="Edit" />
            </template>
          </n-button>
          <n-popconfirm
            :positive-text="t('common.actions.delete')"
            :negative-text="t('common.actions.cancel')"
            @positive-click="confirmDeleteMaterial(item)"
          >
            <template #trigger>
              <n-button quaternary size="small" type="error">
                <template #icon>
                  <n-icon :component="Trash" />
                </template>
              </n-button>
            </template>
            {{ t('workspace.courses.materials.messages.deleteConfirm') }}
          </n-popconfirm>
        </article>
      </div>
    </section>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" class="course-modal">
      <form class="course-form" @submit.prevent="submitCourse">
        <label class="field span-2">
          <span>{{ t('workspace.courses.form.courseName') }}</span>
          <n-input v-model:value="form.courseName" :placeholder="t('workspace.courses.form.courseNamePlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.courseCode') }}</span>
          <n-input v-model:value="form.courseCode" :placeholder="t('workspace.courses.form.courseCodePlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.status') }}</span>
          <n-select v-model:value="form.status" :options="statusOptions" />
        </label>

        <label class="field switch-field">
          <span>{{ t('workspace.courses.form.publicVisible') }}</span>
          <n-switch v-model:value="form.publicVisible" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.publicSortOrder') }}</span>
          <n-input-number v-model:value="form.publicSortOrder" :min="0" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.major') }}</span>
          <n-input v-model:value="form.major" :placeholder="t('workspace.courses.form.majorPlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.grade') }}</span>
          <n-input v-model:value="form.grade" :placeholder="t('workspace.courses.form.gradePlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.totalHours') }}</span>
          <n-input-number v-model:value="form.totalHours" :min="0" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.theoryHours') }}</span>
          <n-input-number v-model:value="form.theoryHours" :min="0" clearable />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.experimentHours') }}</span>
          <n-input-number v-model:value="form.experimentHours" :min="0" clearable />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.weeklyHours') }}</span>
          <n-input-number v-model:value="form.weeklyHours" :min="0" clearable />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.credit') }}</span>
          <n-input-number v-model:value="form.credit" :min="0" :precision="1" clearable />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.courses.form.textbook') }}</span>
          <n-input v-model:value="form.textbook" :placeholder="t('workspace.courses.form.textbookPlaceholder')" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.courses.form.courseGoal') }}</span>
          <n-input v-model:value="form.courseGoal" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.keyPoint') }}</span>
          <n-input v-model:value="form.keyPoint" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <label class="field">
          <span>{{ t('workspace.courses.form.difficultPoint') }}</span>
          <n-input v-model:value="form.difficultPoint" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.courses.form.assessmentMethod') }}</span>
          <n-input v-model:value="form.assessmentMethod" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.courses.form.syllabus') }}</span>
          <n-input v-model:value="form.syllabus" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.courses.form.description') }}</span>
          <n-input v-model:value="form.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
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
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
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
  color: var(--sw-text);
  font-size: 28px;
  font-weight: 800;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(0, 440px) auto;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
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
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  overflow: hidden;
}

.course-table-wrap {
  overflow-x: auto;
}

.course-table {
  width: 100%;
  min-width: 1020px;
  border-collapse: collapse;
}

.course-table th {
  border-bottom: 1px solid var(--sw-border);
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
  color: var(--sw-text);
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
  color: var(--sw-text);
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

.materials-panel {
  display: grid;
  gap: 14px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  padding: 16px;
}

.materials-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.materials-head span {
  color: #16708f;
  font-size: 12px;
  font-weight: 800;
}

.materials-head h2 {
  margin: 4px 0 0;
  color: var(--sw-text);
  font-size: 20px;
}

.section-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.section-tabs button {
  min-height: 34px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: transparent;
  color: var(--sw-muted);
  cursor: pointer;
  padding: 0 14px;
  font-weight: 800;
}

.section-tabs button.active {
  border-color: transparent;
  background: var(--sw-text);
  color: var(--sw-surface-solid);
}

.material-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  border-top: 1px solid var(--sw-border);
  padding-top: 14px;
}

.file-input {
  min-height: 34px;
  border: 1px dashed var(--sw-border);
  border-radius: 8px;
  color: var(--sw-muted);
  padding: 8px;
}

.materials-list {
  display: grid;
  gap: 8px;
}

.material-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto auto;
  align-items: center;
  gap: 10px;
  border-top: 1px solid var(--sw-border);
  padding: 12px 0 4px;
}

.material-row strong,
.material-row span {
  display: block;
}

.material-row strong {
  color: var(--sw-text);
  font-size: 14px;
}

.material-row span {
  margin-top: 4px;
  color: var(--sw-muted);
  font-size: 12px;
  word-break: break-all;
}

.empty-materials {
  border-top: 1px solid var(--sw-border);
  color: var(--sw-muted);
  padding: 18px 0 4px;
  text-align: center;
}

.compact {
  min-height: 120px;
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

.switch-field {
  align-content: center;
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
  .material-form,
  .course-form {
    grid-template-columns: 1fr;
  }

  .materials-head {
    align-items: stretch;
    flex-direction: column;
  }

  .material-row {
    align-items: stretch;
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

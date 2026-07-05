<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NButton,
  NCheckbox,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NSelect,
  NSpin,
  useMessage,
} from 'naive-ui'
import { AlertTriangle, Calendar, CalendarTime, Edit, Plus, Refresh, Search, Wand } from '@vicons/tabler'

import {
  createSemester,
  fetchSemesterCalendar,
  fetchSemesters,
  generateSemesterCalendar,
  updateSemesterCalendar,
  updateSemester,
  type Semester,
  type SemesterCalendar,
  type SemesterCalendarPayload,
  type SemesterPayload,
} from '../../api/workspace'

const { t } = useI18n()
const message = useMessage()

const semesters = ref<Semester[]>([])
const calendarRows = ref<SemesterCalendar[]>([])
const keyword = ref('')
const loading = ref(false)
const calendarLoading = ref(false)
const saving = ref(false)
const error = ref('')
const modalVisible = ref(false)
const calendarEditVisible = ref(false)
const editingId = ref<string | null>(null)
const selectedSemesterId = ref<string | null>(null)
const editingCalendarId = ref<string | null>(null)

const statusOptions = computed(() => [
  { label: t('common.states.planned'), value: 'PLANNED' },
  { label: t('common.states.active'), value: 'ACTIVE' },
  { label: t('common.states.closed'), value: 'CLOSED' },
])

const form = reactive({
  academicYear: '',
  semesterName: '',
  startDate: '',
  endDate: '',
  totalWeeks: 18 as number | null,
  examWeek: null as number | null,
  holidayJson: '',
  adjustmentJson: '',
  remark: '',
  status: 'PLANNED',
})

const calendarForm = reactive({
  weekNo: 0,
  startDate: '',
  endDate: '',
  examWeek: false,
  holiday: false,
  holidayNote: '',
  adjustmentNote: '',
})

const activeSemester = computed(() => semesters.value.find((item) => item.status === 'ACTIVE'))
const plannedCount = computed(() => semesters.value.filter((item) => item.status === 'PLANNED').length)
const closedCount = computed(() => semesters.value.filter((item) => item.status === 'CLOSED').length)
const selectedSemester = computed(() => semesters.value.find((item) => item.id === selectedSemesterId.value))
const modalTitle = computed(() => (editingId.value ? t('workspace.semesters.modal.edit') : t('workspace.semesters.modal.create')))

onMounted(() => {
  void loadSemesters()
})

async function loadSemesters() {
  loading.value = true
  error.value = ''
  try {
    semesters.value = await fetchSemesters(keyword.value.trim())
    if (!selectedSemesterId.value && semesters.value.length > 0) {
      selectedSemesterId.value = semesters.value[0].id
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('workspace.semesters.messages.loadFailed')
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

function openEdit(item: Semester) {
  editingId.value = item.id
  form.academicYear = item.academicYear ?? ''
  form.semesterName = item.semesterName ?? ''
  form.startDate = item.startDate ?? ''
  form.endDate = item.endDate ?? ''
  form.totalWeeks = item.totalWeeks ?? 18
  form.examWeek = item.examWeek ?? null
  form.holidayJson = item.holidayJson ?? ''
  form.adjustmentJson = item.adjustmentJson ?? ''
  form.remark = item.remark ?? ''
  form.status = item.status || 'PLANNED'
  modalVisible.value = true
}

async function submitSemester() {
  if (!form.academicYear.trim() || !form.semesterName.trim()) {
    message.warning(t('workspace.semesters.messages.nameRequired'))
    return
  }

  if (!form.startDate) {
    message.warning(t('workspace.semesters.messages.startDateRequired'))
    return
  }

  if (!form.totalWeeks || form.totalWeeks < 1) {
    message.warning(t('workspace.semesters.messages.invalidTotalWeeks'))
    return
  }

  if (form.endDate && form.endDate < form.startDate) {
    message.warning(t('workspace.semesters.messages.invalidEndDate'))
    return
  }

  if (form.examWeek !== null && (form.examWeek < 1 || form.examWeek > form.totalWeeks)) {
    message.warning(t('workspace.semesters.messages.invalidExamWeek'))
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updateSemester(editingId.value, payload)
      message.success(t('workspace.semesters.messages.updated'))
    } else {
      await createSemester(payload)
      message.success(t('workspace.semesters.messages.created'))
    }
    modalVisible.value = false
    await loadSemesters()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.semesters.messages.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function viewCalendar(item: Semester) {
  selectedSemesterId.value = item.id
  calendarLoading.value = true
  try {
    calendarRows.value = await fetchSemesterCalendar(item.id)
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.semesters.messages.calendarLoadFailed'))
  } finally {
    calendarLoading.value = false
  }
}

async function generateCalendar(item: Semester) {
  selectedSemesterId.value = item.id
  calendarLoading.value = true
  try {
    calendarRows.value = await generateSemesterCalendar(item.id)
    message.success(t('workspace.semesters.messages.calendarGenerated'))
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.semesters.messages.calendarGenerateFailed'))
  } finally {
    calendarLoading.value = false
  }
}

function openCalendarEdit(week: SemesterCalendar) {
  editingCalendarId.value = week.id
  calendarForm.weekNo = week.weekNo
  calendarForm.startDate = week.startDate
  calendarForm.endDate = week.endDate
  calendarForm.examWeek = Boolean(week.examWeek)
  calendarForm.holiday = Boolean(week.holiday)
  calendarForm.holidayNote = week.holidayNote ?? ''
  calendarForm.adjustmentNote = week.adjustmentNote ?? ''
  calendarEditVisible.value = true
}

async function submitCalendarEdit() {
  if (!selectedSemesterId.value || !editingCalendarId.value) {
    return
  }

  if (!calendarForm.startDate || !calendarForm.endDate) {
    message.warning(t('workspace.semesters.messages.calendarDateRequired'))
    return
  }

  if (calendarForm.endDate < calendarForm.startDate) {
    message.warning(t('workspace.semesters.messages.calendarInvalidEndDate'))
    return
  }

  saving.value = true
  try {
    const payload: SemesterCalendarPayload = {
      startDate: calendarForm.startDate,
      endDate: calendarForm.endDate,
      examWeek: calendarForm.examWeek,
      holiday: calendarForm.holiday,
      holidayNote: textOrNull(calendarForm.holidayNote),
      adjustmentNote: textOrNull(calendarForm.adjustmentNote),
    }
    const updated = await updateSemesterCalendar(selectedSemesterId.value, editingCalendarId.value, payload)
    calendarRows.value = calendarRows.value.map((item) => (item.id === updated.id ? updated : item))
    calendarEditVisible.value = false
    message.success(t('workspace.semesters.messages.calendarUpdated'))
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.semesters.messages.calendarSaveFailed'))
  } finally {
    saving.value = false
  }
}

function resetForm() {
  form.academicYear = ''
  form.semesterName = ''
  form.startDate = ''
  form.endDate = ''
  form.totalWeeks = 18
  form.examWeek = null
  form.holidayJson = ''
  form.adjustmentJson = ''
  form.remark = ''
  form.status = 'PLANNED'
}

function buildPayload(): SemesterPayload {
  return {
    academicYear: form.academicYear.trim(),
    semesterName: form.semesterName.trim(),
    startDate: form.startDate,
    endDate: textOrNull(form.endDate),
    totalWeeks: form.totalWeeks ?? 18,
    examWeek: form.examWeek,
    holidayJson: textOrNull(form.holidayJson),
    adjustmentJson: textOrNull(form.adjustmentJson),
    remark: textOrNull(form.remark),
    status: form.status,
  }
}

function textOrNull(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function statusText(status: string) {
  if (status === 'ACTIVE') return t('common.states.active')
  if (status === 'CLOSED') return t('common.states.closed')
  return t('common.states.planned')
}
</script>

<template>
  <section class="semester-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Calendar" />
        <span>{{ t('workspace.semesters.summary.total') }}</span>
        <strong>{{ semesters.length }}</strong>
      </article>
      <article>
        <n-icon :component="CalendarTime" />
        <span>{{ t('workspace.semesters.summary.current') }}</span>
        <strong>{{ activeSemester?.semesterName || '-' }}</strong>
      </article>
      <article>
        <n-icon :component="Calendar" />
        <span>{{ t('workspace.semesters.summary.planned') }}</span>
        <strong>{{ plannedCount }}</strong>
      </article>
      <article>
        <n-icon :component="Calendar" />
        <span>{{ t('workspace.semesters.summary.closed') }}</span>
        <strong>{{ closedCount }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        :placeholder="t('workspace.semesters.searchPlaceholder')"
        @keyup.enter="loadSemesters"
      >
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <div class="toolbar-actions">
        <n-button secondary class="icon-button" @click="loadSemesters">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          {{ t('common.actions.refresh') }}
        </n-button>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          {{ t('workspace.semesters.create') }}
        </n-button>
      </div>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadSemesters">{{ t('common.actions.retry') }}</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="semesters.length === 0" class="empty-state">
        <strong>{{ t('workspace.semesters.emptyTitle') }}</strong>
        <span>{{ t('workspace.semesters.emptyText') }}</span>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          {{ t('workspace.semesters.create') }}
        </n-button>
      </div>

      <div v-else class="semester-table-wrap">
        <table class="semester-table">
          <thead>
            <tr>
              <th>{{ t('workspace.semesters.table.semester') }}</th>
              <th>{{ t('workspace.semesters.table.date') }}</th>
              <th>{{ t('workspace.semesters.table.weeks') }}</th>
              <th>{{ t('workspace.semesters.table.examWeek') }}</th>
              <th>{{ t('workspace.semesters.table.status') }}</th>
              <th>{{ t('workspace.semesters.table.updatedTime') }}</th>
              <th>{{ t('workspace.semesters.table.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in semesters" :key="item.id" :class="{ selected: item.id === selectedSemesterId }">
              <td>
                <strong>{{ item.academicYear }}</strong>
                <span>{{ item.semesterName }}</span>
              </td>
              <td>
                <strong>{{ item.startDate }}</strong>
                <span>{{ item.endDate || t('workspace.semesters.table.unsetEndDate') }}</span>
              </td>
              <td>{{ item.totalWeeks }}</td>
              <td>{{ item.examWeek ?? '-' }}</td>
              <td>
                <span class="status-pill" :class="item.status.toLowerCase()">
                  {{ statusText(item.status) }}
                </span>
              </td>
              <td>{{ item.updatedTime ? item.updatedTime.slice(0, 10) : '-' }}</td>
              <td>
                <div class="row-actions">
                  <n-button quaternary size="small" @click="openEdit(item)">
                    <template #icon>
                      <n-icon :component="Edit" />
                    </template>
                  </n-button>
                  <n-button quaternary size="small" @click="viewCalendar(item)">
                    <template #icon>
                      <n-icon :component="CalendarTime" />
                    </template>
                  </n-button>
                  <n-button quaternary size="small" type="primary" @click="generateCalendar(item)">
                    <template #icon>
                      <n-icon :component="Wand" />
                    </template>
                  </n-button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="calendar-panel">
      <header>
        <div>
          <span>{{ t('workspace.semesters.calendar.title') }}</span>
          <h2>{{ selectedSemester ? `${selectedSemester.academicYear} ${selectedSemester.semesterName}` : t('workspace.semesters.calendar.unselected') }}</h2>
        </div>
        <n-button v-if="selectedSemester" secondary class="icon-button" :loading="calendarLoading" @click="viewCalendar(selectedSemester)">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          {{ t('workspace.semesters.calendar.refresh') }}
        </n-button>
      </header>

      <n-spin v-if="calendarLoading" :show="calendarLoading">
        <div class="skeleton-calendar">
          <span v-for="index in 8" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="calendarRows.length === 0" class="calendar-empty">
        <strong>{{ t('workspace.semesters.calendar.emptyTitle') }}</strong>
        <span>{{ t('workspace.semesters.calendar.emptyText') }}</span>
      </div>

      <div v-else class="week-grid">
        <article v-for="week in calendarRows" :key="week.id" :class="{ exam: week.examWeek }">
          <div class="week-card-heading">
            <strong>{{ t('workspace.semesters.calendar.week', { week: week.weekNo }) }}</strong>
            <n-button quaternary size="tiny" @click="openCalendarEdit(week)">
              <template #icon>
                <n-icon :component="Edit" />
              </template>
            </n-button>
          </div>
          <span>{{ week.startDate }} - {{ week.endDate }}</span>
          <em v-if="week.examWeek">{{ t('workspace.semesters.calendar.examWeek') }}</em>
          <em v-if="week.holiday">{{ t('workspace.semesters.calendar.holiday') }}</em>
          <p v-if="week.holidayNote || week.adjustmentNote">
            {{ week.holidayNote || week.adjustmentNote }}
          </p>
        </article>
      </div>
    </section>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" class="semester-modal">
      <form class="semester-form" @submit.prevent="submitSemester">
        <label class="field">
          <span>{{ t('workspace.semesters.form.academicYear') }}</span>
          <n-input v-model:value="form.academicYear" :placeholder="t('workspace.semesters.form.academicYearPlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.semesterName') }}</span>
          <n-input v-model:value="form.semesterName" :placeholder="t('workspace.semesters.form.semesterNamePlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.startDate') }}</span>
          <input v-model="form.startDate" class="date-input" type="date" />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.endDate') }}</span>
          <input v-model="form.endDate" class="date-input" type="date" />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.totalWeeks') }}</span>
          <n-input-number v-model:value="form.totalWeeks" :min="1" />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.examWeek') }}</span>
          <n-input-number v-model:value="form.examWeek" :min="1" clearable />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.status') }}</span>
          <n-select v-model:value="form.status" :options="statusOptions" />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.remark') }}</span>
          <n-input v-model:value="form.remark" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.semesters.form.holidayJson') }}</span>
          <n-input v-model:value="form.holidayJson" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.semesters.form.adjustmentJson') }}</span>
          <n-input v-model:value="form.adjustmentJson" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" />
        </label>

        <div class="form-actions span-2">
          <n-button @click="modalVisible = false">{{ t('common.actions.cancel') }}</n-button>
          <n-button type="primary" attr-type="submit" :loading="saving">{{ t('common.actions.save') }}</n-button>
        </div>
      </form>
    </n-modal>

    <n-modal v-model:show="calendarEditVisible" preset="card" :title="t('workspace.semesters.modal.calendarEdit')" class="semester-modal">
      <form class="semester-form" @submit.prevent="submitCalendarEdit">
        <label class="field">
          <span>{{ t('workspace.semesters.form.weekNo') }}</span>
          <n-input-number v-model:value="calendarForm.weekNo" disabled />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.weekStartDate') }}</span>
          <input v-model="calendarForm.startDate" class="date-input" type="date" />
        </label>

        <label class="field">
          <span>{{ t('workspace.semesters.form.weekEndDate') }}</span>
          <input v-model="calendarForm.endDate" class="date-input" type="date" />
        </label>

        <label class="check-field">
          <n-checkbox v-model:checked="calendarForm.examWeek">{{ t('workspace.semesters.calendar.examWeek') }}</n-checkbox>
        </label>

        <label class="check-field">
          <n-checkbox v-model:checked="calendarForm.holiday">{{ t('workspace.semesters.calendar.holiday') }}</n-checkbox>
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.semesters.form.holidayNote') }}</span>
          <n-input v-model:value="calendarForm.holidayNote" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.semesters.form.adjustmentNote') }}</span>
          <n-input v-model:value="calendarForm.adjustmentNote" />
        </label>

        <div class="form-actions span-2">
          <n-button @click="calendarEditVisible = false">{{ t('common.actions.cancel') }}</n-button>
          <n-button type="primary" attr-type="submit" :loading="saving">{{ t('common.actions.save') }}</n-button>
        </div>
      </form>
    </n-modal>
  </section>
</template>

<style scoped>
.semester-page {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1.4fr 1fr 1fr;
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
  background: #e7f5fb;
  color: #1688b9;
  font-size: 22px;
}

.summary-grid span,
.calendar-panel header span,
.field span {
  color: #647586;
  font-size: 12px;
  font-weight: 800;
}

.summary-grid strong {
  overflow: hidden;
  color: var(--sw-text);
  font-size: 24px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.table-panel,
.calendar-panel {
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  overflow: hidden;
}

.table-panel {
  min-height: 340px;
}

.semester-table-wrap {
  overflow-x: auto;
}

.semester-table {
  width: 100%;
  min-width: 920px;
  border-collapse: collapse;
}

.semester-table th {
  border-bottom: 1px solid var(--sw-border);
  background: #f7f9fb;
  color: #5e7181;
  font-size: 12px;
  font-weight: 800;
  text-align: left;
}

.semester-table th,
.semester-table td {
  padding: 14px 16px;
}

.semester-table td {
  border-bottom: 1px solid #edf1f4;
  color: #2b3843;
  font-size: 14px;
  vertical-align: top;
}

.semester-table tr:last-child td {
  border-bottom: 0;
}

.semester-table tr.selected td {
  background: #f5fbfe;
}

.semester-table td strong {
  display: block;
  color: var(--sw-text);
  font-weight: 800;
}

.semester-table td span {
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
  background: #eef6ff;
  color: #246b9f !important;
  padding: 0 10px;
  font-weight: 800;
}

.status-pill.active {
  background: #e5f6ee;
  color: #28734d !important;
}

.status-pill.closed {
  background: #edf1f4;
  color: #667381 !important;
}

.calendar-panel {
  padding: 18px;
}

.calendar-panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.calendar-panel h2 {
  margin: 5px 0 0;
  color: var(--sw-text);
  font-size: 18px;
  font-weight: 800;
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.week-grid article {
  min-height: 86px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  padding: 14px;
}

.week-grid article.exam {
  border-color: #79b8d2;
  background: #f1f9fc;
}

.week-card-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.week-grid strong,
.week-grid span,
.week-grid em,
.week-grid p {
  display: block;
}

.week-grid strong {
  color: var(--sw-text);
  font-size: 15px;
  font-weight: 800;
}

.week-grid span {
  margin-top: 7px;
  color: #647586;
  font-size: 12px;
}

.week-grid em {
  margin-top: 8px;
  color: #1688b9;
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.week-grid p {
  margin: 8px 0 0;
  color: #4f6374;
  font-size: 12px;
  line-height: 1.5;
}

.error-state,
.empty-state,
.calendar-empty {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 300px;
  color: #607283;
  text-align: center;
}

.error-state .n-icon {
  color: #b76b2b;
  font-size: 30px;
}

.empty-state strong,
.calendar-empty strong {
  color: var(--sw-text);
  font-size: 18px;
}

.skeleton-table,
.skeleton-calendar {
  display: grid;
  gap: 10px;
  padding: 18px;
}

.skeleton-table span,
.skeleton-calendar span {
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(90deg, #eef3f6, #f8fafb, #eef3f6);
  background-size: 200% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}

.semester-form {
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

.check-field {
  display: flex;
  align-items: end;
  min-height: 34px;
}

.date-input {
  width: 100%;
  height: 34px;
  border: 1px solid var(--sw-border);
  border-radius: 6px;
  background: var(--sw-surface-solid);
  color: var(--sw-text);
  padding: 0 12px;
  outline: none;
}

.date-input:focus {
  border-color: #1688b9;
}

.span-2 {
  grid-column: 1 / -1;
}

.form-actions {
  padding-top: 8px;
}

:global(.semester-modal) {
  max-width: 780px;
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

@media (max-width: 980px) {
  .summary-grid,
  .toolbar,
  .semester-form {
    grid-template-columns: 1fr;
  }

  .week-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar-actions {
    justify-content: stretch;
  }

  .toolbar-actions .n-button {
    flex: 1;
  }
}

@media (max-width: 560px) {
  .calendar-panel header {
    align-items: flex-start;
    flex-direction: column;
  }

  .week-grid {
    grid-template-columns: 1fr;
  }
}
</style>

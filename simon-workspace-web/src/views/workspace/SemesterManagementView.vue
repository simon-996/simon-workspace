<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  NButton,
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
  updateSemester,
  type Semester,
  type SemesterCalendar,
  type SemesterPayload,
} from '../../api/workspace'

const message = useMessage()

const semesters = ref<Semester[]>([])
const calendarRows = ref<SemesterCalendar[]>([])
const keyword = ref('')
const loading = ref(false)
const calendarLoading = ref(false)
const saving = ref(false)
const error = ref('')
const modalVisible = ref(false)
const editingId = ref<string | null>(null)
const selectedSemesterId = ref<string | null>(null)

const statusOptions = [
  { label: '计划中', value: 'PLANNED' },
  { label: '进行中', value: 'ACTIVE' },
  { label: '已结束', value: 'CLOSED' },
]

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

const activeSemester = computed(() => semesters.value.find((item) => item.status === 'ACTIVE'))
const plannedCount = computed(() => semesters.value.filter((item) => item.status === 'PLANNED').length)
const closedCount = computed(() => semesters.value.filter((item) => item.status === 'CLOSED').length)
const selectedSemester = computed(() => semesters.value.find((item) => item.id === selectedSemesterId.value))
const modalTitle = computed(() => (editingId.value ? '编辑学期' : '新增学期'))

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
    error.value = err instanceof Error ? err.message : '学期列表加载失败'
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
    message.warning('请输入学年和学期名称')
    return
  }

  if (!form.startDate) {
    message.warning('请选择开学日期')
    return
  }

  if (!form.totalWeeks || form.totalWeeks < 1) {
    message.warning('总周数至少为 1')
    return
  }

  if (form.endDate && form.endDate < form.startDate) {
    message.warning('结束日期不能早于开学日期')
    return
  }

  if (form.examWeek !== null && (form.examWeek < 1 || form.examWeek > form.totalWeeks)) {
    message.warning('考试周必须在总周数范围内')
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updateSemester(editingId.value, payload)
      message.success('学期已更新')
    } else {
      await createSemester(payload)
      message.success('学期已新增')
    }
    modalVisible.value = false
    await loadSemesters()
  } catch (err) {
    message.error(err instanceof Error ? err.message : '保存学期失败')
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
    message.error(err instanceof Error ? err.message : '读取周历失败')
  } finally {
    calendarLoading.value = false
  }
}

async function generateCalendar(item: Semester) {
  selectedSemesterId.value = item.id
  calendarLoading.value = true
  try {
    calendarRows.value = await generateSemesterCalendar(item.id)
    message.success('周历已生成')
  } catch (err) {
    message.error(err instanceof Error ? err.message : '生成周历失败')
  } finally {
    calendarLoading.value = false
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
  if (status === 'ACTIVE') return '进行中'
  if (status === 'CLOSED') return '已结束'
  return '计划中'
}
</script>

<template>
  <section class="semester-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Calendar" />
        <span>学期总数</span>
        <strong>{{ semesters.length }}</strong>
      </article>
      <article>
        <n-icon :component="CalendarTime" />
        <span>当前学期</span>
        <strong>{{ activeSemester?.semesterName || '-' }}</strong>
      </article>
      <article>
        <n-icon :component="Calendar" />
        <span>计划中</span>
        <strong>{{ plannedCount }}</strong>
      </article>
      <article>
        <n-icon :component="Calendar" />
        <span>已结束</span>
        <strong>{{ closedCount }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        placeholder="搜索学年或学期名称"
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
          刷新
        </n-button>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          新增学期
        </n-button>
      </div>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadSemesters">重试</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="semesters.length === 0" class="empty-state">
        <strong>暂无学期</strong>
        <span>新增学期后可以生成周历。</span>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          新增学期
        </n-button>
      </div>

      <div v-else class="semester-table-wrap">
        <table class="semester-table">
          <thead>
            <tr>
              <th>学期</th>
              <th>日期</th>
              <th>周数</th>
              <th>考试周</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
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
                <span>{{ item.endDate || '未设置结束日期' }}</span>
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
          <span>周历</span>
          <h2>{{ selectedSemester ? `${selectedSemester.academicYear} ${selectedSemester.semesterName}` : '未选择学期' }}</h2>
        </div>
        <n-button v-if="selectedSemester" secondary class="icon-button" :loading="calendarLoading" @click="viewCalendar(selectedSemester)">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          刷新周历
        </n-button>
      </header>

      <n-spin v-if="calendarLoading" :show="calendarLoading">
        <div class="skeleton-calendar">
          <span v-for="index in 8" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="calendarRows.length === 0" class="calendar-empty">
        <strong>暂无周历</strong>
        <span>点击表格中的生成按钮创建周历。</span>
      </div>

      <div v-else class="week-grid">
        <article v-for="week in calendarRows" :key="week.id" :class="{ exam: week.examWeek }">
          <strong>第 {{ week.weekNo }} 周</strong>
          <span>{{ week.startDate }} - {{ week.endDate }}</span>
          <em v-if="week.examWeek">考试周</em>
        </article>
      </div>
    </section>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" class="semester-modal">
      <form class="semester-form" @submit.prevent="submitSemester">
        <label class="field">
          <span>学年</span>
          <n-input v-model:value="form.academicYear" placeholder="例如：2026-2027" />
        </label>

        <label class="field">
          <span>学期名称</span>
          <n-input v-model:value="form.semesterName" placeholder="例如：第一学期" />
        </label>

        <label class="field">
          <span>开学日期</span>
          <input v-model="form.startDate" class="date-input" type="date" />
        </label>

        <label class="field">
          <span>结束日期</span>
          <input v-model="form.endDate" class="date-input" type="date" />
        </label>

        <label class="field">
          <span>总周数</span>
          <n-input-number v-model:value="form.totalWeeks" :min="1" />
        </label>

        <label class="field">
          <span>考试周</span>
          <n-input-number v-model:value="form.examWeek" :min="1" clearable />
        </label>

        <label class="field">
          <span>状态</span>
          <n-select v-model:value="form.status" :options="statusOptions" />
        </label>

        <label class="field">
          <span>备注</span>
          <n-input v-model:value="form.remark" />
        </label>

        <label class="field span-2">
          <span>节假日 JSON</span>
          <n-input v-model:value="form.holidayJson" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" />
        </label>

        <label class="field span-2">
          <span>调课 JSON</span>
          <n-input v-model:value="form.adjustmentJson" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" />
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
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
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
  color: #111a23;
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

.table-panel,
.calendar-panel {
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
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
  border-bottom: 1px solid #d8e0e7;
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
  color: #111a23;
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
  color: #111a23;
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
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  padding: 14px;
}

.week-grid article.exam {
  border-color: #79b8d2;
  background: #f1f9fc;
}

.week-grid strong,
.week-grid span,
.week-grid em {
  display: block;
}

.week-grid strong {
  color: #111a23;
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
  color: #111a23;
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

.date-input {
  width: 100%;
  height: 34px;
  border: 1px solid #d5dde5;
  border-radius: 6px;
  background: #ffffff;
  color: #111a23;
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

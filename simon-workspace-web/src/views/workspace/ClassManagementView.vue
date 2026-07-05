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
  NSpin,
  useMessage,
} from 'naive-ui'
import { AlertTriangle, Edit, Plus, Refresh, School, Search, Trash, Users } from '@vicons/tabler'

import {
  createClassInfo,
  deleteClassInfo,
  fetchClasses,
  updateClassInfo,
  type ClassInfo,
  type ClassInfoPayload,
} from '../../api/workspace'

const { t } = useI18n()
const message = useMessage()

const classes = ref<ClassInfo[]>([])
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const modalVisible = ref(false)
const editingId = ref<string | null>(null)

const form = reactive({
  className: '',
  major: '',
  grade: '',
  studentCount: null as number | null,
  counselor: '',
  remark: '',
})

const totalStudents = computed(() =>
  classes.value.reduce((sum, item) => sum + (item.studentCount ?? 0), 0),
)
const majorCount = computed(() => new Set(classes.value.map((item) => item.major).filter(Boolean)).size)
const gradeCount = computed(() => new Set(classes.value.map((item) => item.grade).filter(Boolean)).size)
const modalTitle = computed(() => (editingId.value ? t('workspace.classes.modal.edit') : t('workspace.classes.modal.create')))

onMounted(() => {
  void loadClasses()
})

async function loadClasses() {
  loading.value = true
  error.value = ''
  try {
    classes.value = await fetchClasses(keyword.value.trim())
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('workspace.classes.messages.loadFailed')
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

function openEdit(item: ClassInfo) {
  editingId.value = item.id
  form.className = item.className ?? ''
  form.major = item.major ?? ''
  form.grade = item.grade ?? ''
  form.studentCount = item.studentCount ?? null
  form.counselor = item.counselor ?? ''
  form.remark = item.remark ?? ''
  modalVisible.value = true
}

async function submitClassInfo() {
  if (!form.className.trim()) {
    message.warning(t('workspace.classes.messages.nameRequired'))
    return
  }

  if (form.studentCount !== null && form.studentCount < 0) {
    message.warning(t('workspace.classes.messages.invalidStudentCount'))
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updateClassInfo(editingId.value, payload)
      message.success(t('workspace.classes.messages.updated'))
    } else {
      await createClassInfo(payload)
      message.success(t('workspace.classes.messages.created'))
    }
    modalVisible.value = false
    await loadClasses()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.classes.messages.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function confirmDelete(item: ClassInfo) {
  try {
    await deleteClassInfo(item.id)
    message.success(t('workspace.classes.messages.deleted'))
    await loadClasses()
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.classes.messages.deleteFailed'))
  }
}

function resetForm() {
  form.className = ''
  form.major = ''
  form.grade = ''
  form.studentCount = null
  form.counselor = ''
  form.remark = ''
}

function buildPayload(): ClassInfoPayload {
  return {
    className: form.className.trim(),
    major: textOrNull(form.major),
    grade: textOrNull(form.grade),
    studentCount: form.studentCount,
    counselor: textOrNull(form.counselor),
    remark: textOrNull(form.remark),
  }
}

function textOrNull(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}
</script>

<template>
  <section class="class-page">
    <div class="summary-strip">
      <article>
        <n-icon :component="Users" />
        <span>{{ t('workspace.classes.summary.total') }}</span>
        <strong>{{ classes.length }}</strong>
      </article>
      <article>
        <n-icon :component="School" />
        <span>{{ t('workspace.classes.summary.majorCoverage') }}</span>
        <strong>{{ majorCount }}</strong>
      </article>
      <article>
        <n-icon :component="School" />
        <span>{{ t('workspace.classes.summary.gradeCoverage') }}</span>
        <strong>{{ gradeCount }}</strong>
      </article>
      <article>
        <n-icon :component="Users" />
        <span>{{ t('workspace.classes.summary.students') }}</span>
        <strong>{{ totalStudents }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        :placeholder="t('workspace.classes.searchPlaceholder')"
        @keyup.enter="loadClasses"
      >
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <div class="toolbar-actions">
        <n-button secondary class="icon-button" @click="loadClasses">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          {{ t('common.actions.refresh') }}
        </n-button>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          {{ t('workspace.classes.create') }}
        </n-button>
      </div>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadClasses">{{ t('common.actions.retry') }}</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="classes.length === 0" class="empty-state">
        <strong>{{ t('workspace.classes.emptyTitle') }}</strong>
        <span>{{ t('workspace.classes.emptyText') }}</span>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          {{ t('workspace.classes.create') }}
        </n-button>
      </div>

      <div v-else class="class-table-wrap">
        <table class="class-table">
          <thead>
            <tr>
              <th>{{ t('workspace.classes.table.className') }}</th>
              <th>{{ t('workspace.classes.table.major') }}</th>
              <th>{{ t('workspace.classes.table.grade') }}</th>
              <th>{{ t('workspace.classes.table.studentCount') }}</th>
              <th>{{ t('workspace.classes.table.counselor') }}</th>
              <th>{{ t('workspace.classes.table.updatedTime') }}</th>
              <th>{{ t('workspace.classes.table.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in classes" :key="item.id">
              <td>
                <strong>{{ item.className }}</strong>
                <span>{{ item.remark || t('workspace.classes.table.noRemark') }}</span>
              </td>
              <td>{{ item.major || '-' }}</td>
              <td>{{ item.grade || '-' }}</td>
              <td>{{ item.studentCount ?? '-' }}</td>
              <td>{{ item.counselor || '-' }}</td>
              <td>{{ item.updatedTime ? item.updatedTime.slice(0, 10) : '-' }}</td>
              <td>
                <div class="row-actions">
                  <n-button quaternary size="small" @click="openEdit(item)">
                    <template #icon>
                      <n-icon :component="Edit" />
                    </template>
                  </n-button>
                  <n-popconfirm
                    :positive-text="t('common.actions.delete')"
                    :negative-text="t('common.actions.cancel')"
                    @positive-click="confirmDelete(item)"
                  >
                    <template #trigger>
                      <n-button quaternary size="small" type="error">
                        <template #icon>
                          <n-icon :component="Trash" />
                        </template>
                      </n-button>
                    </template>
                    {{ t('workspace.classes.messages.deleteConfirm') }}
                  </n-popconfirm>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" class="class-modal">
      <form class="class-form" @submit.prevent="submitClassInfo">
        <label class="field">
          <span>{{ t('workspace.classes.form.className') }}</span>
          <n-input v-model:value="form.className" :placeholder="t('workspace.classes.form.classNamePlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.classes.form.major') }}</span>
          <n-input v-model:value="form.major" :placeholder="t('workspace.classes.form.majorPlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.classes.form.grade') }}</span>
          <n-input v-model:value="form.grade" :placeholder="t('workspace.classes.form.gradePlaceholder')" />
        </label>

        <label class="field">
          <span>{{ t('workspace.classes.form.studentCount') }}</span>
          <n-input-number v-model:value="form.studentCount" :min="0" clearable />
        </label>

        <label class="field">
          <span>{{ t('workspace.classes.form.counselor') }}</span>
          <n-input v-model:value="form.counselor" :placeholder="t('workspace.classes.form.counselorPlaceholder')" />
        </label>

        <label class="field span-2">
          <span>{{ t('workspace.classes.form.remark') }}</span>
          <n-input v-model:value="form.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" />
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
.class-page {
  display: grid;
  gap: 16px;
}

.summary-strip {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr 1fr;
  gap: 10px;
}

.summary-strip article {
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

.summary-strip .n-icon {
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

.summary-strip span,
.field span {
  color: #647586;
  font-size: 12px;
  font-weight: 800;
}

.summary-strip strong {
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
  min-height: 340px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  overflow: hidden;
}

.class-table-wrap {
  overflow-x: auto;
}

.class-table {
  width: 100%;
  min-width: 840px;
  border-collapse: collapse;
}

.class-table th {
  border-bottom: 1px solid var(--sw-border);
  background: #f7f9fb;
  color: #5e7181;
  font-size: 12px;
  font-weight: 800;
  text-align: left;
}

.class-table th,
.class-table td {
  padding: 14px 16px;
}

.class-table td {
  border-bottom: 1px solid #edf1f4;
  color: #2b3843;
  font-size: 14px;
  vertical-align: top;
}

.class-table tr:last-child td {
  border-bottom: 0;
}

.class-table td strong {
  display: block;
  color: var(--sw-text);
  font-weight: 800;
}

.class-table td span {
  display: block;
  margin-top: 4px;
  color: #687a89;
  font-size: 12px;
}

.error-state,
.empty-state {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 340px;
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

.class-form {
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

:global(.class-modal) {
  max-width: 720px;
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
  .summary-strip,
  .toolbar,
  .class-form {
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

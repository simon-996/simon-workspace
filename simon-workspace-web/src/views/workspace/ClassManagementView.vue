<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
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
const modalTitle = computed(() => (editingId.value ? '编辑班级' : '新增班级'))

onMounted(() => {
  void loadClasses()
})

async function loadClasses() {
  loading.value = true
  error.value = ''
  try {
    classes.value = await fetchClasses(keyword.value.trim())
  } catch (err) {
    error.value = err instanceof Error ? err.message : '班级列表加载失败'
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
    message.warning('请输入班级名称')
    return
  }

  if (form.studentCount !== null && form.studentCount < 0) {
    message.warning('学生人数不能小于 0')
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updateClassInfo(editingId.value, payload)
      message.success('班级已更新')
    } else {
      await createClassInfo(payload)
      message.success('班级已新增')
    }
    modalVisible.value = false
    await loadClasses()
  } catch (err) {
    message.error(err instanceof Error ? err.message : '保存班级失败')
  } finally {
    saving.value = false
  }
}

async function confirmDelete(item: ClassInfo) {
  try {
    await deleteClassInfo(item.id)
    message.success('班级已删除')
    await loadClasses()
  } catch (err) {
    message.error(err instanceof Error ? err.message : '删除班级失败')
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
        <span>班级总数</span>
        <strong>{{ classes.length }}</strong>
      </article>
      <article>
        <n-icon :component="School" />
        <span>专业覆盖</span>
        <strong>{{ majorCount }}</strong>
      </article>
      <article>
        <n-icon :component="School" />
        <span>年级覆盖</span>
        <strong>{{ gradeCount }}</strong>
      </article>
      <article>
        <n-icon :component="Users" />
        <span>学生人数</span>
        <strong>{{ totalStudents }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        placeholder="搜索班级、专业或年级"
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
          刷新
        </n-button>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          新增班级
        </n-button>
      </div>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadClasses">重试</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="classes.length === 0" class="empty-state">
        <strong>暂无班级</strong>
        <span>新增班级后可以关联课程、学期和生成任务。</span>
        <n-button type="primary" class="icon-button" @click="openCreate">
          <template #icon>
            <n-icon :component="Plus" />
          </template>
          新增班级
        </n-button>
      </div>

      <div v-else class="class-table-wrap">
        <table class="class-table">
          <thead>
            <tr>
              <th>班级</th>
              <th>专业</th>
              <th>年级</th>
              <th>学生人数</th>
              <th>辅导员</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in classes" :key="item.id">
              <td>
                <strong>{{ item.className }}</strong>
                <span>{{ item.remark || '无备注' }}</span>
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
                    positive-text="删除"
                    negative-text="取消"
                    @positive-click="confirmDelete(item)"
                  >
                    <template #trigger>
                      <n-button quaternary size="small" type="error">
                        <template #icon>
                          <n-icon :component="Trash" />
                        </template>
                      </n-button>
                    </template>
                    删除后班级将从列表中移除。
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
          <span>班级名称</span>
          <n-input v-model:value="form.className" placeholder="例如：软件 2601 班" />
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
          <span>学生人数</span>
          <n-input-number v-model:value="form.studentCount" :min="0" clearable />
        </label>

        <label class="field">
          <span>辅导员</span>
          <n-input v-model:value="form.counselor" placeholder="辅导员姓名" />
        </label>

        <label class="field span-2">
          <span>备注</span>
          <n-input v-model:value="form.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" />
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
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
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
  min-height: 340px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
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
  border-bottom: 1px solid #d8e0e7;
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
  color: #111a23;
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

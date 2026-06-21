<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NCheckbox,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpin,
  useMessage,
} from 'naive-ui'
import {
  AlertTriangle,
  DeviceFloppy,
  Edit,
  ListDetails,
  Plus,
  Refresh,
  Search,
  Template,
  Trash,
  Upload,
} from '@vicons/tabler'

import {
  deleteTemplate,
  fetchTemplateFields,
  fetchTemplates,
  updateTemplate,
  updateTemplateFields,
  uploadTemplate,
  type TemplateField,
  type TemplateFieldPayload,
  type TemplateFile,
  type TemplatePayload,
  type TemplateUploadPayload,
} from '../../api/workspace'

interface FieldDraft {
  fieldKey: string
  fieldLabel: string
  fieldType: string
  required: boolean
  defaultValue: string
  sortOrder: number | null
  remark: string
  status: string
}

const message = useMessage()

const templates = ref<TemplateFile[]>([])
const fields = ref<FieldDraft[]>([])
const keyword = ref('')
const loading = ref(false)
const fieldsLoading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const error = ref('')
const selectedTemplateId = ref<string | null>(null)
const uploadModalVisible = ref(false)
const editModalVisible = ref(false)
const uploadFileRef = ref<File | null>(null)

const typeOptions = [
  { label: 'Word', value: 'WORD' },
  { label: 'Excel', value: 'EXCEL' },
  { label: '其他', value: 'OTHER' },
]

const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '归档', value: 'ARCHIVED' },
]

const fieldTypeOptions = [
  { label: '文本', value: 'TEXT' },
  { label: '数字', value: 'NUMBER' },
  { label: '日期', value: 'DATE' },
  { label: 'JSON', value: 'JSON' },
]

const fieldStatusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'DISABLED' },
]

const uploadForm = reactive({
  templateName: '',
  templateType: '',
  description: '',
  status: 'ACTIVE',
})

const editForm = reactive({
  templateName: '',
  templateType: 'OTHER',
  description: '',
  status: 'ACTIVE',
})

const selectedTemplate = computed(() => templates.value.find((item) => item.id === selectedTemplateId.value))
const activeCount = computed(() => templates.value.filter((item) => item.status === 'ACTIVE').length)
const wordCount = computed(() => templates.value.filter((item) => item.templateType === 'WORD').length)
const excelCount = computed(() => templates.value.filter((item) => item.templateType === 'EXCEL').length)

onMounted(() => {
  void loadTemplates()
})

async function loadTemplates() {
  loading.value = true
  error.value = ''
  try {
    templates.value = await fetchTemplates(keyword.value.trim())
    if (!selectedTemplateId.value && templates.value.length > 0) {
      await selectTemplate(templates.value[0])
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '模板列表加载失败'
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

function openUpload() {
  uploadFileRef.value = null
  uploadForm.templateName = ''
  uploadForm.templateType = ''
  uploadForm.description = ''
  uploadForm.status = 'ACTIVE'
  uploadModalVisible.value = true
}

function openEdit(item: TemplateFile) {
  selectedTemplateId.value = item.id
  editForm.templateName = item.templateName
  editForm.templateType = item.templateType || 'OTHER'
  editForm.description = item.description ?? ''
  editForm.status = item.status || 'ACTIVE'
  editModalVisible.value = true
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  uploadFileRef.value = input.files?.[0] ?? null
}

async function submitUpload() {
  if (!uploadFileRef.value) {
    message.warning('请选择模板文件')
    return
  }

  uploading.value = true
  try {
    const payload: TemplateUploadPayload = {
      templateName: textOrNull(uploadForm.templateName),
      templateType: textOrNull(uploadForm.templateType),
      description: textOrNull(uploadForm.description),
      status: uploadForm.status,
    }
    const created = await uploadTemplate(uploadFileRef.value, payload)
    message.success('模板已上传')
    uploadModalVisible.value = false
    selectedTemplateId.value = created.id
    await loadTemplates()
    await selectTemplate(created)
  } catch (err) {
    message.error(err instanceof Error ? err.message : '上传模板失败')
  } finally {
    uploading.value = false
  }
}

async function submitEdit() {
  if (!selectedTemplateId.value) return
  if (!editForm.templateName.trim()) {
    message.warning('请输入模板名称')
    return
  }

  saving.value = true
  try {
    const payload: TemplatePayload = {
      templateName: editForm.templateName.trim(),
      templateType: editForm.templateType,
      description: textOrNull(editForm.description),
      status: editForm.status,
    }
    await updateTemplate(selectedTemplateId.value, payload)
    message.success('模板已更新')
    editModalVisible.value = false
    await loadTemplates()
  } catch (err) {
    message.error(err instanceof Error ? err.message : '更新模板失败')
  } finally {
    saving.value = false
  }
}

async function selectTemplate(item: TemplateFile) {
  selectedTemplateId.value = item.id
  fieldsLoading.value = true
  try {
    const rows = await fetchTemplateFields(item.id)
    fields.value = rows.map(fieldToDraft)
  } catch (err) {
    message.error(err instanceof Error ? err.message : '读取模板字段失败')
  } finally {
    fieldsLoading.value = false
  }
}

function addField() {
  fields.value.push({
    fieldKey: '',
    fieldLabel: '',
    fieldType: 'TEXT',
    required: false,
    defaultValue: '',
    sortOrder: fields.value.length + 1,
    remark: '',
    status: 'ACTIVE',
  })
}

function removeField(index: number) {
  fields.value.splice(index, 1)
}

async function saveFields() {
  if (!selectedTemplateId.value) return
  const invalid = fields.value.find((field) => !field.fieldKey.trim())
  if (invalid) {
    message.warning('字段键不能为空')
    return
  }

  saving.value = true
  try {
    const payload = fields.value.map(fieldDraftToPayload)
    const saved = await updateTemplateFields(selectedTemplateId.value, payload)
    fields.value = saved.map(fieldToDraft)
    message.success('模板字段已保存')
  } catch (err) {
    message.error(err instanceof Error ? err.message : '保存模板字段失败')
  } finally {
    saving.value = false
  }
}

async function confirmDelete(item: TemplateFile) {
  try {
    await deleteTemplate(item.id)
    message.success('模板已删除')
    if (selectedTemplateId.value === item.id) {
      selectedTemplateId.value = null
      fields.value = []
    }
    await loadTemplates()
  } catch (err) {
    message.error(err instanceof Error ? err.message : '删除模板失败')
  }
}

function fieldToDraft(field: TemplateField): FieldDraft {
  return {
    fieldKey: field.fieldKey,
    fieldLabel: field.fieldLabel ?? '',
    fieldType: field.fieldType || 'TEXT',
    required: Boolean(field.required),
    defaultValue: field.defaultValue ?? '',
    sortOrder: field.sortOrder ?? null,
    remark: field.remark ?? '',
    status: field.status || 'ACTIVE',
  }
}

function fieldDraftToPayload(field: FieldDraft): TemplateFieldPayload {
  return {
    fieldKey: field.fieldKey.trim(),
    fieldLabel: textOrNull(field.fieldLabel),
    fieldType: field.fieldType,
    required: field.required,
    defaultValue: textOrNull(field.defaultValue),
    sortOrder: field.sortOrder,
    remark: textOrNull(field.remark),
    status: field.status,
  }
}

function textOrNull(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function formatSize(size: number) {
  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  if (size >= 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${size} B`
}

function typeText(type: string) {
  if (type === 'WORD') return 'Word'
  if (type === 'EXCEL') return 'Excel'
  return '其他'
}
</script>

<template>
  <section class="template-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Template" />
        <span>模板总数</span>
        <strong>{{ templates.length }}</strong>
      </article>
      <article>
        <n-icon :component="Template" />
        <span>启用模板</span>
        <strong>{{ activeCount }}</strong>
      </article>
      <article>
        <n-icon :component="Template" />
        <span>Word</span>
        <strong>{{ wordCount }}</strong>
      </article>
      <article>
        <n-icon :component="Template" />
        <span>Excel</span>
        <strong>{{ excelCount }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        placeholder="搜索模板名称、文件名或类型"
        @keyup.enter="loadTemplates"
      >
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <div class="toolbar-actions">
        <n-button secondary class="icon-button" @click="loadTemplates">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          刷新
        </n-button>
        <n-button type="primary" class="icon-button" @click="openUpload">
          <template #icon>
            <n-icon :component="Upload" />
          </template>
          上传模板
        </n-button>
      </div>
    </section>

    <div class="template-layout">
      <section class="table-panel">
        <div v-if="error" class="error-state">
          <n-icon :component="AlertTriangle" />
          <span>{{ error }}</span>
          <n-button size="small" tertiary @click="loadTemplates">重试</n-button>
        </div>

        <n-spin v-else-if="loading" :show="loading">
          <div class="skeleton-table">
            <span v-for="index in 6" :key="index" />
          </div>
        </n-spin>

        <div v-else-if="templates.length === 0" class="empty-state">
          <strong>暂无模板</strong>
          <span>上传 Word 或 Excel 模板后维护字段。</span>
          <n-button type="primary" class="icon-button" @click="openUpload">
            <template #icon>
              <n-icon :component="Upload" />
            </template>
            上传模板
          </n-button>
        </div>

        <div v-else class="template-table-wrap">
          <table class="template-table">
            <thead>
              <tr>
                <th>模板</th>
                <th>类型</th>
                <th>大小</th>
                <th>状态</th>
                <th>更新时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in templates" :key="item.id" :class="{ selected: item.id === selectedTemplateId }">
                <td>
                  <strong>{{ item.templateName }}</strong>
                  <span>{{ item.originalFilename }}</span>
                </td>
                <td>{{ typeText(item.templateType) }}</td>
                <td>{{ formatSize(item.fileSize) }}</td>
                <td>
                  <span class="status-pill" :class="{ archived: item.status === 'ARCHIVED' }">
                    {{ item.status === 'ARCHIVED' ? '归档' : '启用' }}
                  </span>
                </td>
                <td>{{ item.updatedTime ? item.updatedTime.slice(0, 10) : '-' }}</td>
                <td>
                  <div class="row-actions">
                    <n-button quaternary size="small" @click="selectTemplate(item)">
                      <template #icon>
                        <n-icon :component="ListDetails" />
                      </template>
                    </n-button>
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
                      删除后模板和字段将从列表中移除。
                    </n-popconfirm>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="field-panel">
        <header>
          <div>
            <span>字段</span>
            <h2>{{ selectedTemplate?.templateName || '未选择模板' }}</h2>
          </div>
          <div class="toolbar-actions">
            <n-button secondary class="icon-button" :disabled="!selectedTemplate" @click="addField">
              <template #icon>
                <n-icon :component="Plus" />
              </template>
              新增字段
            </n-button>
            <n-button type="primary" class="icon-button" :disabled="!selectedTemplate" :loading="saving" @click="saveFields">
              <template #icon>
                <n-icon :component="DeviceFloppy" />
              </template>
              保存字段
            </n-button>
          </div>
        </header>

        <n-spin v-if="fieldsLoading" :show="fieldsLoading">
          <div class="skeleton-table">
            <span v-for="index in 4" :key="index" />
          </div>
        </n-spin>

        <div v-else-if="!selectedTemplate" class="field-empty">
          <strong>选择模板</strong>
          <span>选择左侧模板后维护占位符字段。</span>
        </div>

        <div v-else-if="fields.length === 0" class="field-empty">
          <strong>暂无字段</strong>
          <span>新增字段后保存到模板。</span>
        </div>

        <div v-else class="field-list">
          <article v-for="(field, index) in fields" :key="index" class="field-row">
            <label>
              <span>字段键</span>
              <n-input v-model:value="field.fieldKey" placeholder="courseName" />
            </label>
            <label>
              <span>显示名</span>
              <n-input v-model:value="field.fieldLabel" placeholder="课程名称" />
            </label>
            <label>
              <span>类型</span>
              <n-select v-model:value="field.fieldType" :options="fieldTypeOptions" />
            </label>
            <label>
              <span>排序</span>
              <n-input-number v-model:value="field.sortOrder" :min="0" clearable />
            </label>
            <label>
              <span>默认值</span>
              <n-input v-model:value="field.defaultValue" />
            </label>
            <label>
              <span>状态</span>
              <n-select v-model:value="field.status" :options="fieldStatusOptions" />
            </label>
            <label class="field-check">
              <n-checkbox v-model:checked="field.required">必填</n-checkbox>
            </label>
            <n-button quaternary type="error" size="small" @click="removeField(index)">
              <template #icon>
                <n-icon :component="Trash" />
              </template>
            </n-button>
          </article>
        </div>
      </section>
    </div>

    <n-modal v-model:show="uploadModalVisible" preset="card" title="上传模板" class="template-modal">
      <form class="template-form" @submit.prevent="submitUpload">
        <label class="field span-2">
          <span>模板文件</span>
          <input class="file-input" type="file" accept=".doc,.docx,.xls,.xlsx" @change="onFileChange" />
        </label>

        <label class="field">
          <span>模板名称</span>
          <n-input v-model:value="uploadForm.templateName" placeholder="不填则使用文件名" />
        </label>

        <label class="field">
          <span>模板类型</span>
          <n-select v-model:value="uploadForm.templateType" clearable :options="typeOptions" />
        </label>

        <label class="field">
          <span>状态</span>
          <n-select v-model:value="uploadForm.status" :options="statusOptions" />
        </label>

        <label class="field span-2">
          <span>说明</span>
          <n-input v-model:value="uploadForm.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <div class="form-actions span-2">
          <n-button @click="uploadModalVisible = false">取消</n-button>
          <n-button type="primary" attr-type="submit" :loading="uploading">上传</n-button>
        </div>
      </form>
    </n-modal>

    <n-modal v-model:show="editModalVisible" preset="card" title="编辑模板" class="template-modal">
      <form class="template-form" @submit.prevent="submitEdit">
        <label class="field">
          <span>模板名称</span>
          <n-input v-model:value="editForm.templateName" />
        </label>

        <label class="field">
          <span>模板类型</span>
          <n-select v-model:value="editForm.templateType" :options="typeOptions" />
        </label>

        <label class="field">
          <span>状态</span>
          <n-select v-model:value="editForm.status" :options="statusOptions" />
        </label>

        <label class="field span-2">
          <span>说明</span>
          <n-input v-model:value="editForm.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </label>

        <div class="form-actions span-2">
          <n-button @click="editModalVisible = false">取消</n-button>
          <n-button type="primary" attr-type="submit" :loading="saving">保存</n-button>
        </div>
      </form>
    </n-modal>
  </section>
</template>

<style scoped>
.template-page {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
.field-panel header span,
.field span,
.field-row label span {
  color: #647586;
  font-size: 12px;
  font-weight: 800;
}

.summary-grid strong {
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

.template-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 0.9fr);
  gap: 14px;
  align-items: start;
}

.table-panel,
.field-panel {
  min-height: 420px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
}

.template-table-wrap {
  overflow-x: auto;
}

.template-table {
  width: 100%;
  min-width: 760px;
  border-collapse: collapse;
}

.template-table th {
  border-bottom: 1px solid #d8e0e7;
  background: #f7f9fb;
  color: #5e7181;
  font-size: 12px;
  font-weight: 800;
  text-align: left;
}

.template-table th,
.template-table td {
  padding: 14px 16px;
}

.template-table td {
  border-bottom: 1px solid #edf1f4;
  color: #2b3843;
  font-size: 14px;
  vertical-align: top;
}

.template-table tr:last-child td {
  border-bottom: 0;
}

.template-table tr.selected td {
  background: #f5fbfe;
}

.template-table td strong,
.field-panel h2 {
  display: block;
  color: #111a23;
  font-weight: 800;
}

.template-table td span {
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

.field-panel {
  padding: 18px;
}

.field-panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.field-panel h2 {
  margin: 5px 0 0;
  font-size: 18px;
}

.field-list {
  display: grid;
  gap: 10px;
}

.field-row {
  display: grid;
  grid-template-columns: minmax(120px, 1fr) minmax(110px, 1fr) 108px 90px minmax(100px, 1fr) 96px 64px 38px;
  gap: 8px;
  align-items: end;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  padding: 10px;
}

.field-row label {
  display: grid;
  gap: 6px;
}

.field-row :deep(.n-input-number) {
  width: 100%;
}

.field-check {
  min-height: 34px;
  align-content: center;
}

.error-state,
.empty-state,
.field-empty {
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

.empty-state strong,
.field-empty strong {
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

.template-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: grid;
  gap: 7px;
}

.file-input {
  width: 100%;
  border: 1px solid #d5dde5;
  border-radius: 6px;
  background: #ffffff;
  padding: 8px;
}

.span-2 {
  grid-column: 1 / -1;
}

.form-actions {
  padding-top: 8px;
}

:global(.template-modal) {
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

@media (max-width: 1180px) {
  .template-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .summary-grid,
  .toolbar,
  .template-form {
    grid-template-columns: 1fr;
  }

  .toolbar-actions,
  .field-panel header {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-actions .n-button,
  .field-panel header .n-button {
    width: 100%;
  }

  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>

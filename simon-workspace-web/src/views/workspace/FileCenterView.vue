<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NIcon, NInput, NPopconfirm, NSpin, useMessage } from 'naive-ui'
import { AlertTriangle, Download, FileText, Files, Refresh, Search, Trash, Upload } from '@vicons/tabler'

import {
  deleteFileResource,
  downloadFileResource,
  fetchFiles,
  type FileResource,
} from '../../api/workspace'
import FileUploadDialog from '../../components/FileUploadDialog.vue'
import { formatBinaryFileSize } from '../../utils/fileFormatters'
import { consumeFileUploadAction, shouldOpenFileUpload } from './fileUploadRoute'

const { locale, t } = useI18n()
const message = useMessage()
const route = useRoute()
const router = useRouter()

const files = ref<FileResource[]>([])
const keyword = ref('')
const loading = ref(false)
const downloadingId = ref<string | null>(null)
const error = ref('')
const uploadOpen = ref(false)
let latestLoadRequestId = 0

const totalSize = computed(() => files.value.reduce((sum, item) => sum + (item.fileSize ?? 0), 0))
const generatedCount = computed(() => files.value.filter((item) => item.sourceType === 'GENERATED').length)
const uploadCount = computed(() => files.value.filter((item) => item.sourceType === 'UPLOAD').length)

watch(() => route.query.action, () => {
  if (shouldOpenFileUpload(route.query)) {
    uploadOpen.value = true
    void router
      .replace({ query: consumeFileUploadAction(route.query) })
      .catch(() => undefined)
  }
}, { immediate: true })

onMounted(() => {
  void loadFiles()
})

async function loadFiles() {
  const requestId = ++latestLoadRequestId
  loading.value = true
  error.value = ''
  try {
    const nextFiles = await fetchFiles(keyword.value.trim())
    if (requestId !== latestLoadRequestId) return
    files.value = nextFiles
  } catch (err) {
    if (requestId !== latestLoadRequestId) return
    error.value = resolveErrorMessage(err, t('workspace.files.messages.loadFailed'))
    message.error(error.value)
  } finally {
    if (requestId === latestLoadRequestId) {
      loading.value = false
    }
  }
}

async function downloadFile(item: FileResource) {
  downloadingId.value = item.id
  try {
    const { blob, filename } = await downloadFileResource(item.id)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename || item.originalFilename
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
  } catch (err) {
    message.error(resolveErrorMessage(err, t('workspace.files.messages.downloadFailed')))
  } finally {
    downloadingId.value = null
  }
}

async function confirmDelete(item: FileResource) {
  try {
    await deleteFileResource(item.id)
    message.success(t('workspace.files.messages.deleted'))
    await loadFiles()
  } catch (err) {
    message.error(resolveErrorMessage(err, t('workspace.files.messages.deleteFailed')))
  }
}

async function handleUploaded(resource: FileResource) {
  message.success(t('workspace.files.upload.succeeded', { filename: resource.originalFilename }))
  await loadFiles()
}

function formatSize(size: number) {
  return formatBinaryFileSize(size, locale.value)
}

function resolveErrorMessage(err: unknown, fallback: string): string {
  if (err instanceof Error && err.message.trim()) {
    return err.message
  }

  return fallback
}

function sourceText(sourceType: string) {
  if (sourceType === 'UPLOAD') return t('workspace.files.source.upload')
  if (sourceType === 'TEMPLATE') return t('workspace.files.source.template')
  if (sourceType === 'GENERATED') return t('workspace.files.source.generated')
  return t('workspace.files.source.other')
}

function visibilityText(visibility: string) {
  return visibility === 'PUBLIC' ? t('workspace.files.visibility.public') : t('workspace.files.visibility.private')
}
</script>

<template>
  <section class="file-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Files" />
        <span>{{ t('workspace.files.summary.total') }}</span>
        <strong>{{ files.length }}</strong>
      </article>
      <article>
        <n-icon :component="FileText" />
        <span>{{ t('workspace.files.summary.generated') }}</span>
        <strong>{{ generatedCount }}</strong>
      </article>
      <article>
        <n-icon :component="FileText" />
        <span>{{ t('workspace.files.summary.uploaded') }}</span>
        <strong>{{ uploadCount }}</strong>
      </article>
      <article>
        <n-icon :component="Files" />
        <span>{{ t('workspace.files.summary.size') }}</span>
        <strong>{{ formatSize(totalSize) }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        :placeholder="t('workspace.files.searchPlaceholder')"
        @keyup.enter="loadFiles"
      >
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <div class="toolbar-actions">
        <n-button
          type="primary"
          class="icon-button"
          data-testid="toolbar-upload"
          @click="uploadOpen = true"
        >
          <template #icon>
            <n-icon :component="Upload" />
          </template>
          {{ t('common.actions.upload') }}
        </n-button>
        <n-button secondary class="icon-button" @click="loadFiles">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          {{ t('common.actions.refresh') }}
        </n-button>
      </div>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadFiles">{{ t('common.actions.retry') }}</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="files.length === 0" class="empty-state">
        <strong>{{ t('workspace.files.emptyTitle') }}</strong>
        <span>{{ t('workspace.files.emptyText') }}</span>
        <n-button
          type="primary"
          class="icon-button"
          data-testid="empty-upload"
          @click="uploadOpen = true"
        >
          <template #icon>
            <n-icon :component="Upload" />
          </template>
          {{ t('common.actions.upload') }}
        </n-button>
      </div>

      <div v-else class="file-table-wrap">
        <table class="file-table">
          <thead>
            <tr>
              <th>{{ t('workspace.files.table.file') }}</th>
              <th>{{ t('workspace.files.table.source') }}</th>
              <th>{{ t('workspace.files.table.storage') }}</th>
              <th>{{ t('workspace.files.table.type') }}</th>
              <th>{{ t('workspace.files.table.size') }}</th>
              <th>{{ t('workspace.files.table.visibility') }}</th>
              <th>{{ t('workspace.files.table.createdTime') }}</th>
              <th>{{ t('workspace.files.table.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in files" :key="item.id">
              <td>
                <strong>{{ item.originalFilename }}</strong>
                <span>{{ item.fileExtension || 'unknown' }}</span>
              </td>
              <td>
                <span class="source-pill">{{ sourceText(item.sourceType) }}</span>
              </td>
              <td>
                <span class="storage-pill">{{ item.storageProvider || 'LOCAL' }}</span>
              </td>
              <td>{{ item.contentType || '-' }}</td>
              <td>{{ formatSize(item.fileSize) }}</td>
              <td>{{ visibilityText(item.visibility) }}</td>
              <td>{{ item.createdTime ? item.createdTime.slice(0, 10) : '-' }}</td>
              <td>
                <div class="row-actions">
                  <n-button
                    quaternary
                    size="small"
                    :aria-label="t('common.actions.download')"
                    :loading="downloadingId === item.id"
                    @click="downloadFile(item)"
                  >
                    <template #icon>
                      <n-icon :component="Download" />
                    </template>
                  </n-button>
                  <n-popconfirm
                    :positive-text="t('common.actions.delete')"
                    :negative-text="t('common.actions.cancel')"
                    @positive-click="confirmDelete(item)"
                  >
                    <template #trigger>
                      <n-button
                        quaternary
                        size="small"
                        type="error"
                        :aria-label="t('common.actions.delete')"
                      >
                        <template #icon>
                          <n-icon :component="Trash" />
                        </template>
                      </n-button>
                    </template>
                    {{ t('workspace.files.messages.deleteConfirm') }}
                  </n-popconfirm>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <file-upload-dialog v-model:show="uploadOpen" @uploaded="handleUploaded" />
  </section>
</template>

<style scoped>
.file-page {
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

.summary-grid span {
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
  min-width: 0;
  max-width: 100%;
  grid-template-columns: minmax(0, 440px) auto;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  padding: 12px;
}

.icon-button {
  --n-border-radius: 8px !important;
  font-weight: 700;
}

.toolbar-actions {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.table-panel {
  min-height: 360px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  overflow: hidden;
}

.file-table-wrap {
  overflow-x: auto;
}

.file-table {
  width: 100%;
  min-width: 980px;
  border-collapse: collapse;
}

.file-table th {
  border-bottom: 1px solid var(--sw-border);
  background: #f7f9fb;
  color: #5e7181;
  font-size: 12px;
  font-weight: 800;
  text-align: left;
}

.file-table th,
.file-table td {
  padding: 14px 16px;
}

.file-table td {
  border-bottom: 1px solid #edf1f4;
  color: #2b3843;
  font-size: 14px;
  vertical-align: top;
}

.file-table tr:last-child td {
  border-bottom: 0;
}

.file-table td strong {
  display: block;
  color: var(--sw-text);
  font-weight: 800;
}

.file-table td span {
  display: block;
  margin-top: 4px;
  color: #687a89;
  font-size: 12px;
}

.source-pill {
  display: inline-flex !important;
  align-items: center;
  min-height: 24px;
  border-radius: 999px;
  background: #eef6ff;
  color: #246b9f !important;
  padding: 0 10px;
  font-weight: 800;
}

.storage-pill {
  display: inline-flex !important;
  align-items: center;
  min-height: 24px;
  border-radius: 999px;
  background: #eef8f3;
  color: #22764e !important;
  padding: 0 10px;
  font-weight: 800;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
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
  .toolbar {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: stretch;
  }

  .toolbar-actions :deep(.n-button) {
    min-width: 0;
    flex: 1 1 160px;
  }
}

@media (max-width: 390px) {
  .toolbar-actions :deep(.n-button) {
    max-width: 100%;
    flex-basis: 100%;
  }
}
</style>

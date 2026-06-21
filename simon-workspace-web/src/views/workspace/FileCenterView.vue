<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NButton, NIcon, NInput, NPopconfirm, NSpin, useMessage } from 'naive-ui'
import { AlertTriangle, Download, FileText, Files, Refresh, Search, Trash } from '@vicons/tabler'

import {
  deleteFileResource,
  downloadFileResource,
  fetchFiles,
  type FileResource,
} from '../../api/workspace'

const message = useMessage()

const files = ref<FileResource[]>([])
const keyword = ref('')
const loading = ref(false)
const downloadingId = ref<string | null>(null)
const error = ref('')

const totalSize = computed(() => files.value.reduce((sum, item) => sum + (item.fileSize ?? 0), 0))
const generatedCount = computed(() => files.value.filter((item) => item.sourceType === 'GENERATED').length)
const uploadCount = computed(() => files.value.filter((item) => item.sourceType === 'UPLOAD').length)

onMounted(() => {
  void loadFiles()
})

async function loadFiles() {
  loading.value = true
  error.value = ''
  try {
    files.value = await fetchFiles(keyword.value.trim())
  } catch (err) {
    error.value = err instanceof Error ? err.message : '文件列表加载失败'
    message.error(error.value)
  } finally {
    loading.value = false
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
    message.error(err instanceof Error ? err.message : '文件下载失败')
  } finally {
    downloadingId.value = null
  }
}

async function confirmDelete(item: FileResource) {
  try {
    await deleteFileResource(item.id)
    message.success('文件已删除')
    await loadFiles()
  } catch (err) {
    message.error(err instanceof Error ? err.message : '删除文件失败')
  }
}

function formatSize(size: number) {
  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  if (size >= 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${size} B`
}

function sourceText(sourceType: string) {
  if (sourceType === 'UPLOAD') return '上传'
  if (sourceType === 'TEMPLATE') return '模板'
  if (sourceType === 'GENERATED') return '生成'
  return '其他'
}
</script>

<template>
  <section class="file-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Files" />
        <span>文件总数</span>
        <strong>{{ files.length }}</strong>
      </article>
      <article>
        <n-icon :component="FileText" />
        <span>生成文件</span>
        <strong>{{ generatedCount }}</strong>
      </article>
      <article>
        <n-icon :component="FileText" />
        <span>上传文件</span>
        <strong>{{ uploadCount }}</strong>
      </article>
      <article>
        <n-icon :component="Files" />
        <span>总大小</span>
        <strong>{{ formatSize(totalSize) }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        placeholder="搜索文件名或来源"
        @keyup.enter="loadFiles"
      >
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <n-button secondary class="icon-button" @click="loadFiles">
        <template #icon>
          <n-icon :component="Refresh" />
        </template>
        刷新
      </n-button>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadFiles">重试</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="files.length === 0" class="empty-state">
        <strong>暂无文件</strong>
        <span>生成或上传后的文件会显示在这里。</span>
      </div>

      <div v-else class="file-table-wrap">
        <table class="file-table">
          <thead>
            <tr>
              <th>文件</th>
              <th>来源</th>
              <th>类型</th>
              <th>大小</th>
              <th>创建时间</th>
              <th>操作</th>
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
              <td>{{ item.contentType || '-' }}</td>
              <td>{{ formatSize(item.fileSize) }}</td>
              <td>{{ item.createdTime ? item.createdTime.slice(0, 10) : '-' }}</td>
              <td>
                <div class="row-actions">
                  <n-button
                    quaternary
                    size="small"
                    :loading="downloadingId === item.id"
                    @click="downloadFile(item)"
                  >
                    <template #icon>
                      <n-icon :component="Download" />
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
                    删除后文件将从列表中移除。
                  </n-popconfirm>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
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

.summary-grid span {
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

.file-table-wrap {
  overflow-x: auto;
}

.file-table {
  width: 100%;
  min-width: 820px;
  border-collapse: collapse;
}

.file-table th {
  border-bottom: 1px solid #d8e0e7;
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
  color: #111a23;
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

  .toolbar .n-button {
    width: 100%;
  }
}
</style>

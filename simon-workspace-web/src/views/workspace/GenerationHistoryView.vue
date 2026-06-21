<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NButton, NIcon, NInput, NModal, NSpin, useMessage } from 'naive-ui'
import { AlertTriangle, Clock, Eye, History, Refresh, Search } from '@vicons/tabler'

import {
  fetchGenerationTaskDetail,
  fetchGenerationTasks,
  type GenerationTask,
} from '../../api/workspace'

const message = useMessage()

const tasks = ref<GenerationTask[]>([])
const selectedTask = ref<GenerationTask | null>(null)
const keyword = ref('')
const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const error = ref('')

const activeStatuses = new Set(['PENDING', 'RUNNING', 'PREVIEW_READY', 'FILLING_TEMPLATE'])

const successCount = computed(() => tasks.value.filter((item) => item.status === 'SUCCESS').length)
const failedCount = computed(() => tasks.value.filter((item) => item.status === 'FAILED').length)
const runningCount = computed(() => tasks.value.filter((item) => activeStatuses.has(item.status)).length)

onMounted(() => {
  void loadTasks()
})

async function loadTasks() {
  loading.value = true
  error.value = ''
  try {
    tasks.value = await fetchGenerationTasks(keyword.value.trim())
  } catch (err) {
    error.value = err instanceof Error ? err.message : '生成记录加载失败'
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

async function openDetail(item: GenerationTask) {
  detailVisible.value = true
  detailLoading.value = true
  selectedTask.value = item
  try {
    selectedTask.value = await fetchGenerationTaskDetail(item.id)
  } catch (err) {
    message.error(err instanceof Error ? err.message : '读取生成记录失败')
  } finally {
    detailLoading.value = false
  }
}

function statusText(status: string) {
  if (status === 'PENDING') return '等待预览'
  if (status === 'PREVIEW_READY') return '可预览'
  if (status === 'FILLING_TEMPLATE') return '导出中'
  if (status === 'SUCCESS') return '成功'
  if (status === 'FAILED') return '失败'
  if (status === 'RUNNING') return '进行中'
  if (status === 'CANCELED') return '已取消'
  return '等待中'
}

function typeText(taskType: string) {
  if (taskType === 'TEACHING_PLAN') return '教案'
  if (taskType === 'TEACHING_CALENDAR') return '教学日历'
  return '其他'
}
</script>

<template>
  <section class="history-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="History" />
        <span>任务总数</span>
        <strong>{{ tasks.length }}</strong>
      </article>
      <article>
        <n-icon :component="Clock" />
        <span>处理中</span>
        <strong>{{ runningCount }}</strong>
      </article>
      <article>
        <n-icon :component="History" />
        <span>成功</span>
        <strong>{{ successCount }}</strong>
      </article>
      <article>
        <n-icon :component="AlertTriangle" />
        <span>失败</span>
        <strong>{{ failedCount }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        placeholder="搜索任务名称、类型或状态"
        @keyup.enter="loadTasks"
      >
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <n-button secondary class="icon-button" @click="loadTasks">
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
        <n-button size="small" tertiary @click="loadTasks">重试</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="tasks.length === 0" class="empty-state">
        <strong>暂无生成记录</strong>
        <span>生成任务创建后会显示在这里。</span>
      </div>

      <div v-else class="history-table-wrap">
        <table class="history-table">
          <thead>
            <tr>
              <th>任务</th>
              <th>类型</th>
              <th>状态</th>
              <th>开始</th>
              <th>完成</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in tasks" :key="item.id">
              <td>
                <strong>{{ item.taskName }}</strong>
                <span>{{ item.resultSummary || item.failureReason || '无摘要' }}</span>
              </td>
              <td>{{ typeText(item.taskType) }}</td>
              <td>
                <span class="status-pill" :class="item.status.toLowerCase()">
                  {{ statusText(item.status) }}
                </span>
              </td>
              <td>{{ item.startedTime ? item.startedTime.slice(0, 16).replace('T', ' ') : '-' }}</td>
              <td>{{ item.finishedTime ? item.finishedTime.slice(0, 16).replace('T', ' ') : '-' }}</td>
              <td>{{ item.createdTime ? item.createdTime.slice(0, 10) : '-' }}</td>
              <td>
                <n-button quaternary size="small" @click="openDetail(item)">
                  <template #icon>
                    <n-icon :component="Eye" />
                  </template>
                </n-button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <n-modal v-model:show="detailVisible" preset="card" title="生成记录详情" class="history-modal">
      <n-spin :show="detailLoading">
        <section v-if="selectedTask" class="detail-grid">
          <article>
            <span>任务名称</span>
            <strong>{{ selectedTask.taskName }}</strong>
          </article>
          <article>
            <span>任务类型</span>
            <strong>{{ typeText(selectedTask.taskType) }}</strong>
          </article>
          <article>
            <span>状态</span>
            <strong>{{ statusText(selectedTask.status) }}</strong>
          </article>
          <article>
            <span>课程 / 班级 / 学期</span>
            <strong>{{ selectedTask.courseId || '-' }} / {{ selectedTask.classId || '-' }} / {{ selectedTask.semesterId || '-' }}</strong>
          </article>
          <article class="span-2">
            <span>结果摘要</span>
            <p>{{ selectedTask.resultSummary || '-' }}</p>
          </article>
          <article class="span-2">
            <span>失败原因</span>
            <p>{{ selectedTask.failureReason || '-' }}</p>
          </article>
          <article class="span-2">
            <span>输入快照</span>
            <pre>{{ selectedTask.inputJson || '{}' }}</pre>
          </article>
        </section>
      </n-spin>
    </n-modal>
  </section>
</template>

<style scoped>
.history-page {
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
.detail-grid span {
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

.history-table-wrap {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  min-width: 900px;
  border-collapse: collapse;
}

.history-table th {
  border-bottom: 1px solid #d8e0e7;
  background: #f7f9fb;
  color: #5e7181;
  font-size: 12px;
  font-weight: 800;
  text-align: left;
}

.history-table th,
.history-table td {
  padding: 14px 16px;
}

.history-table td {
  border-bottom: 1px solid #edf1f4;
  color: #2b3843;
  font-size: 14px;
  vertical-align: top;
}

.history-table tr:last-child td {
  border-bottom: 0;
}

.history-table td strong {
  display: block;
  color: #111a23;
  font-weight: 800;
}

.history-table td span {
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

.status-pill.success {
  background: #e5f6ee;
  color: #28734d !important;
}

.status-pill.failed {
  background: #fff1e8;
  color: #a5521f !important;
}

.status-pill.running,
.status-pill.pending,
.status-pill.preview_ready,
.status-pill.filling_template {
  background: #f1f9fc;
  color: #1688b9 !important;
}

.status-pill.canceled {
  background: #eef1f3;
  color: #66727d !important;
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

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-grid article {
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  padding: 14px;
}

.detail-grid strong,
.detail-grid p,
.detail-grid pre {
  display: block;
  margin: 7px 0 0;
  color: #111a23;
}

.detail-grid pre {
  max-height: 240px;
  overflow: auto;
  border-radius: 8px;
  background: #f7f9fb;
  padding: 12px;
  white-space: pre-wrap;
}

.span-2 {
  grid-column: 1 / -1;
}

:global(.history-modal) {
  max-width: 760px;
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
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .toolbar .n-button {
    width: 100%;
  }

  .span-2 {
    grid-column: auto;
  }
}
</style>

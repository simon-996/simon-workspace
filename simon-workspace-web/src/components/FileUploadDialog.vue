<script setup lang="ts">
import { computed, ref, watch, type CSSProperties } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NModal, NProgress, NRadio, NRadioGroup } from 'naive-ui'

import { uploadFileResource, type FileResource } from '../api/workspace'
import { useFileUploadDialog } from './useFileUploadDialog'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
  'uploaded': [resource: FileResource]
}>()

const { t } = useI18n()
const fileInput = ref<HTMLInputElement | null>(null)
const {
  selectedFile,
  visibility,
  uploading,
  uploadProgress,
  uploadError,
  setSelectedFile,
  submit,
  reset,
} = useFileUploadDialog({
  upload: uploadFileResource,
  fileRequired: () => t('workspace.files.upload.fileRequired'),
  failed: () => t('workspace.files.upload.failed'),
})

const modalStyle: CSSProperties = {
  width: 'min(520px, calc(100vw - 32px))',
  borderRadius: '8px',
}

const selectedFileSummary = computed(() => {
  if (!selectedFile.value) return ''
  return `${selectedFile.value.name} · ${formatFileSize(selectedFile.value.size)}`
})

function formatFileSize(size: number) {
  const units = ['B', 'KiB', 'MiB', 'GiB'] as const
  let value = Math.max(0, size)
  let unitIndex = 0

  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex += 1
  }

  const precision = unitIndex === 0 || value >= 10 ? 0 : 1
  return `${value.toFixed(precision)} ${units[unitIndex]}`
}

function onFileChange(event: Event) {
  if (uploading.value) return
  const target = event.target as HTMLInputElement
  setSelectedFile(target.files?.[0] ?? null)
}

function resetDialog() {
  reset()
  if (fileInput.value) fileInput.value.value = ''
}

function closeDialog() {
  if (uploading.value) return
  emit('update:show', false)
  resetDialog()
}

function handleShowUpdate(show: boolean) {
  if (show) return
  closeDialog()
}

async function submitUpload() {
  const resource = await submit()
  if (!resource) return

  emit('uploaded', resource)
  emit('update:show', false)
  resetDialog()
}

watch(
  () => props.show,
  (show) => {
    if (!show && !uploading.value) resetDialog()
  },
)
</script>

<template>
  <n-modal
    :show="show"
    class="file-upload-modal"
    preset="card"
    :title="t('workspace.files.upload.title')"
    :style="modalStyle"
    :mask-closable="!uploading"
    :close-on-esc="!uploading"
    :closable="!uploading"
    @update:show="handleShowUpdate"
  >
    <form class="upload-form" :aria-busy="uploading" @submit.prevent="submitUpload">
      <div class="field-group">
        <span id="upload-file-label" class="field-label">{{ t('workspace.files.upload.fileLabel') }}</span>
        <label class="file-picker" :class="{ disabled: uploading }">
          <input
            ref="fileInput"
            class="native-file-input"
            type="file"
            :disabled="uploading"
            aria-labelledby="upload-file-label"
            @change="onFileChange"
          >
          <span class="file-picker-action">{{ t('workspace.files.upload.chooseFile') }}</span>
        </label>
        <div v-if="selectedFile" class="selected-file">
          <span>{{ t('workspace.files.upload.selectedFile') }}</span>
          <strong>{{ selectedFileSummary }}</strong>
        </div>
      </div>

      <fieldset class="field-group visibility-field" :disabled="uploading">
        <legend class="field-label">{{ t('workspace.files.upload.visibility') }}</legend>
        <n-radio-group v-model:value="visibility" name="file-visibility" :disabled="uploading">
          <n-radio value="PRIVATE" :disabled="uploading">
            {{ t('workspace.files.visibility.private') }}
          </n-radio>
          <n-radio value="PUBLIC" :disabled="uploading">
            {{ t('workspace.files.visibility.public') }}
          </n-radio>
        </n-radio-group>
      </fieldset>

      <div v-if="uploading || uploadProgress > 0" class="upload-progress" role="status">
        <div class="progress-label">
          <span>{{ uploading ? t('workspace.files.upload.uploading') : t('workspace.files.upload.progress') }}</span>
          <strong>{{ uploadProgress }}%</strong>
        </div>
        <n-progress type="line" :percentage="uploadProgress" :height="8" :show-indicator="false" />
      </div>

      <p v-if="uploadError" class="upload-error" role="alert">{{ uploadError }}</p>

      <div class="upload-actions">
        <n-button attr-type="button" :disabled="uploading" @click="closeDialog">
          {{ t('common.actions.cancel') }}
        </n-button>
        <n-button
          type="primary"
          attr-type="submit"
          :loading="uploading"
          :disabled="uploading"
        >
          {{ t('common.actions.upload') }}
        </n-button>
      </div>
    </form>
  </n-modal>
</template>

<style scoped>
.upload-form {
  display: grid;
  gap: 20px;
  min-width: 0;
}

.field-group {
  display: grid;
  gap: 8px;
  min-width: 0;
  margin: 0;
  padding: 0;
  border: 0;
}

.field-label {
  color: var(--sw-text);
  font-size: 0.9rem;
  font-weight: 600;
}

.file-picker {
  position: relative;
  justify-self: start;
  cursor: pointer;
}

.file-picker.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.native-file-input {
  position: absolute;
  inline-size: 1px;
  block-size: 1px;
  margin: -1px;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
}

.file-picker-action {
  display: inline-flex;
  min-height: 34px;
  align-items: center;
  padding: 0 14px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  color: var(--sw-text);
  background: var(--sw-surface-muted);
  transition: transform var(--sw-motion-standard), border-color var(--sw-motion-standard);
}

.native-file-input:focus-visible + .file-picker-action {
  border-color: var(--sw-accent);
  box-shadow: var(--sw-focus-ring);
}

.file-picker:not(.disabled):active .file-picker-action {
  transform: translateY(1px);
}

.selected-file {
  display: grid;
  gap: 3px;
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid var(--sw-border-soft);
  border-radius: 8px;
  background: var(--sw-surface-muted);
}

.selected-file span {
  color: var(--sw-muted);
  font-size: 0.78rem;
}

.selected-file strong {
  min-width: 0;
  overflow-wrap: anywhere;
  color: var(--sw-text);
  font-size: 0.9rem;
  font-weight: 600;
}

.visibility-field :deep(.n-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.upload-progress {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.progress-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: var(--sw-muted);
  font-size: 0.84rem;
}

.progress-label strong {
  color: var(--sw-text);
  font-variant-numeric: tabular-nums;
}

.upload-error {
  margin: 0;
  padding: 10px 12px;
  border: 1px solid var(--sw-danger);
  border-radius: 8px;
  color: var(--sw-danger);
  background: var(--sw-status-danger-bg);
  overflow-wrap: anywhere;
}

.upload-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 390px) {
  .upload-actions :deep(.n-button) {
    flex: 1 1 0;
  }
}
</style>

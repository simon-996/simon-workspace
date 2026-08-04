import { ref } from 'vue'

import type { FileResource, uploadFileResource } from '../api/workspace'

interface FileUploadDialogDependencies {
  upload: typeof uploadFileResource
  fileRequired: () => string
  failed: () => string
}

export function useFileUploadDialog({ upload, fileRequired, failed }: FileUploadDialogDependencies) {
  const selectedFile = ref<File | null>(null)
  const visibility = ref<'PRIVATE' | 'PUBLIC'>('PRIVATE')
  const uploading = ref(false)
  const uploadProgress = ref(0)
  const uploadError = ref('')

  function setSelectedFile(file: File | null) {
    selectedFile.value = file
    uploadError.value = ''
  }

  async function submit(): Promise<FileResource | null> {
    if (uploading.value) return null

    if (!selectedFile.value) {
      uploadError.value = fileRequired()
      return null
    }

    uploadProgress.value = 0
    uploadError.value = ''
    uploading.value = true

    try {
      return await upload(selectedFile.value, visibility.value, (progress) => {
        uploadProgress.value = progress
      })
    } catch (error) {
      uploadError.value = error instanceof Error ? error.message : failed()
      return null
    } finally {
      uploading.value = false
    }
  }

  function reset() {
    selectedFile.value = null
    visibility.value = 'PRIVATE'
    uploading.value = false
    uploadProgress.value = 0
    uploadError.value = ''
  }

  return {
    selectedFile,
    visibility,
    uploading,
    uploadProgress,
    uploadError,
    setSelectedFile,
    submit,
    reset,
  }
}

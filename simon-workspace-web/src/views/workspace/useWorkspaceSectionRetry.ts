import { computed, ref, type Ref } from 'vue'

export function useWorkspaceSectionRetry(error: Ref<string>, load: () => Promise<void>) {
  const running = ref(false)
  const retainedError = ref('')
  const visibleError = computed(() => error.value || retainedError.value)

  async function retry(): Promise<void> {
    if (running.value) {
      return
    }

    retainedError.value = error.value
    running.value = true
    try {
      await load()
    } finally {
      running.value = false
      retainedError.value = ''
    }
  }

  return { retry, running, visibleError }
}

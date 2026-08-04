import { ref } from 'vue'
import { describe, expect, it, vi } from 'vitest'

import { useWorkspaceSectionRetry } from './useWorkspaceSectionRetry'

function deferred() {
  let resolve!: () => void
  const promise = new Promise<void>((resolvePromise) => {
    resolve = resolvePromise
  })
  return { promise, resolve }
}

describe('useWorkspaceSectionRetry', () => {
  it('retains the visible error while one retry is running and ignores overlapping retries', async () => {
    const error = ref('failed')
    const request = deferred()
    const load = vi.fn(async () => {
      error.value = ''
      await request.promise
    })
    const { retry, running, visibleError } = useWorkspaceSectionRetry(error, load)

    const firstRetry = retry()
    expect(running.value).toBe(true)
    expect(visibleError.value).toBe('failed')

    await retry()
    expect(load).toHaveBeenCalledTimes(1)

    request.resolve()
    await firstRetry

    expect(running.value).toBe(false)
    expect(visibleError.value).toBe('')
  })

  it('propagates unexpected load rejections and resets retained retry state', async () => {
    const error = ref('failed')
    const failure = new Error('unexpected')
    const load = vi.fn(async () => {
      error.value = ''
      throw failure
    })
    const { retry, running, visibleError } = useWorkspaceSectionRetry(error, load)

    await expect(retry()).rejects.toBe(failure)

    expect(running.value).toBe(false)
    expect(visibleError.value).toBe('')
  })
})

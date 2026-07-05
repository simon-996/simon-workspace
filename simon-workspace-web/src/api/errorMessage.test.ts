import { describe, expect, it, vi } from 'vitest'

import { AppError } from './errors'

describe('api error message helper', () => {
  it('shows translated app errors through the provided message api', async () => {
    const { notifyAppError } = await import('./errorMessage')
    const message = {
      error: vi.fn(),
    }

    notifyAppError(
      new AppError({ errorCode: 'NETWORK_ERROR', message: 'Network Error' }),
      message,
      (key) => key === 'errors.NETWORK_ERROR' ? 'Network unavailable' : key,
    )

    expect(message.error).toHaveBeenCalledWith('Network unavailable')
  })
})

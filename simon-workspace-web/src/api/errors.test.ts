import { describe, expect, it } from 'vitest'

import { messages } from '../i18n/messages'
import {
  AppError,
  toAppError,
  translateAppError,
  unwrapApiResponse,
  type ApiResponse,
} from './errors'

describe('api errors', () => {
  it('throws AppError from non-success API responses', () => {
    const response: ApiResponse<null> = {
      code: 40101,
      message: 'Unauthorized',
      data: null,
      errorCode: 'AUTH_UNAUTHORIZED',
      traceId: 'trace-1',
      params: {},
      fieldErrors: [],
    }

    expect(() => unwrapApiResponse(response)).toThrow(AppError)

    try {
      unwrapApiResponse(response)
    } catch (error) {
      expect(error).toBeInstanceOf(AppError)
      expect((error as AppError).errorCode).toBe('AUTH_UNAUTHORIZED')
      expect((error as AppError).traceId).toBe('trace-1')
    }
  })

  it('translates AppError by errorCode and traceId', () => {
    const error = new AppError({
      code: 50001,
      message: 'Internal server error',
      errorCode: 'INTERNAL_ERROR',
      traceId: 'trace-2',
      params: {},
      fieldErrors: [],
    })

    const translated = translateAppError(error, (key, named = {}) => {
      const templates: Record<string, string> = {
        'errors.INTERNAL_ERROR': 'Service unavailable. Trace: {traceId}',
        'errors.UNKNOWN_ERROR': 'Something went wrong.',
      }
      return Object.entries(named).reduce(
        (message, [name, value]) => message.replace(`{${name}}`, String(value)),
        templates[key] ?? key,
      )
    })

    expect(translated).toBe('Service unavailable. Trace: trace-2')
  })

  it('normalizes network, timeout, and non-standard server failures', () => {
    expect(toAppError({ isAxiosError: true, message: 'Network Error' }).errorCode).toBe('NETWORK_ERROR')
    expect(toAppError({ isAxiosError: true, code: 'ECONNABORTED', message: 'timeout' }).errorCode)
      .toBe('REQUEST_TIMEOUT')

    const serverError = toAppError({
      isAxiosError: true,
      message: 'Request failed with status code 502',
      response: {
        status: 502,
        data: '<html>Bad Gateway</html>',
      },
    })

    expect(serverError.errorCode).toBe('INTERNAL_ERROR')
    expect(serverError.status).toBe(502)
  })

  it('defines shared error translations for every supported locale', () => {
    const requiredKeys = [
      'AUTH_UNAUTHORIZED',
      'AUTH_BAD_CREDENTIALS',
      'AUTH_ACCOUNT_DISABLED',
      'AUTH_FORBIDDEN',
      'VALIDATION_FAILED',
      'VALIDATION_FIELD_INVALID',
      'BAD_REQUEST',
      'INTERNAL_ERROR',
      'NETWORK_ERROR',
      'REQUEST_TIMEOUT',
      'UNKNOWN_ERROR',
    ]

    for (const localeMessages of Object.values(messages)) {
      const errors = (localeMessages as { errors?: Record<string, string> }).errors
      expect(errors).toBeTruthy()
      for (const key of requiredKeys) {
        expect(errors?.[key], key).toBeTruthy()
      }
    }
  })
})

import { isAxiosError } from 'axios'

import { translate } from '../i18n'

export interface ApiFieldError {
  field: string
  errorCode: string
  message: string
  params?: Record<string, unknown>
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  errorCode?: string | null
  traceId?: string | null
  params?: Record<string, unknown> | null
  fieldErrors?: ApiFieldError[] | null
}

interface AppErrorOptions {
  code?: number
  status?: number
  message?: string
  errorCode?: string | null
  traceId?: string | null
  params?: Record<string, unknown> | null
  fieldErrors?: ApiFieldError[] | null
}

type ErrorTranslator = (key: string, named?: Record<string, string | number>) => string

export class AppError extends Error {
  readonly code: number
  readonly status?: number
  readonly errorCode: string
  readonly traceId?: string
  readonly params: Record<string, unknown>
  readonly fieldErrors: ApiFieldError[]

  constructor(options: AppErrorOptions) {
    const errorCode = options.errorCode || 'UNKNOWN_ERROR'
    super(messageOf({ ...options, errorCode }, translate))
    this.name = 'AppError'
    this.code = options.code ?? 0
    this.status = options.status
    this.errorCode = errorCode
    this.traceId = options.traceId ?? undefined
    this.params = options.params ?? {}
    this.fieldErrors = options.fieldErrors ?? []
  }
}

export function unwrapApiResponse<T>(response: ApiResponse<T>) {
  if (response.code !== 0) {
    throw new AppError({
      code: response.code,
      message: response.message,
      errorCode: response.errorCode,
      traceId: response.traceId,
      params: response.params,
      fieldErrors: response.fieldErrors,
    })
  }
  return response.data
}

export function toAppError(error: unknown): AppError {
  if (error instanceof AppError) {
    return error
  }

  if (isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') {
      return new AppError({
        status: error.response?.status,
        errorCode: 'REQUEST_TIMEOUT',
        message: error.message,
      })
    }

    const data = error.response?.data
    if (isApiErrorResponse(data)) {
      return new AppError({
        code: data.code,
        status: error.response?.status,
        message: data.message,
        errorCode: data.errorCode,
        traceId: data.traceId,
        params: data.params,
        fieldErrors: data.fieldErrors,
      })
    }

    if (!error.response) {
      return new AppError({
        errorCode: 'NETWORK_ERROR',
        message: error.message,
      })
    }
  }

  if (error instanceof Error) {
    return new AppError({
      errorCode: 'UNKNOWN_ERROR',
      message: error.message,
    })
  }

  return new AppError({
    errorCode: 'UNKNOWN_ERROR',
  })
}

export function translateAppError(error: unknown, t: ErrorTranslator = translate) {
  return messageOf(toAppError(error), t)
}

function isApiErrorResponse(value: unknown): value is ApiResponse<unknown> {
  if (!value || typeof value !== 'object') {
    return false
  }
  const response = value as Partial<ApiResponse<unknown>>
  return typeof response.code === 'number' || typeof response.errorCode === 'string'
}

function messageOf(options: AppErrorOptions & { errorCode: string }, t: ErrorTranslator) {
  const key = `errors.${options.errorCode}`
  const translated = t(key, translationParams(options.params, options.traceId))
  if (translated && translated !== key) {
    return translated
  }
  if (options.message) {
    return options.message
  }
  return t('errors.UNKNOWN_ERROR')
}

function translationParams(params?: Record<string, unknown> | null, traceId?: string | null) {
  const named: Record<string, string | number> = {
    traceId: traceId || '-',
  }

  for (const [key, value] of Object.entries(params ?? {})) {
    if (typeof value === 'string' || typeof value === 'number') {
      named[key] = value
    } else if (typeof value === 'boolean') {
      named[key] = String(value)
    } else if (value != null) {
      named[key] = String(value)
    }
  }

  return named
}

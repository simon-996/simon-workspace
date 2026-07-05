import { translate } from '../i18n'
import { translateAppError } from './errors'

interface ErrorMessageApi {
  error: (content: string) => unknown
}

type ErrorTranslator = (key: string, named?: Record<string, string | number>) => string

export function notifyAppError(
  error: unknown,
  message: ErrorMessageApi,
  t: ErrorTranslator = translate,
) {
  const content = translateAppError(error, t)
  message.error(content)
  return content
}

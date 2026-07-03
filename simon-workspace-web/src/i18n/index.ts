import { createI18n } from 'vue-i18n'

import {
  type AppLanguage,
  loadStoredLanguage,
  saveLanguage,
} from './language'
import { messages } from './messages'

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: loadStoredLanguage(),
  fallbackLocale: 'en',
  messages,
})

export function setAppLanguage(language: AppLanguage) {
  i18n.global.locale.value = language
  saveLanguage(language)
}

export function translate(key: string, named?: Record<string, string | number>) {
  return i18n.global.t(key, named ?? {})
}

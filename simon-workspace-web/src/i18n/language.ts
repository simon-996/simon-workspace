export const LANGUAGE_STORAGE_KEY = 'simon-workspace-language'

export const supportedLanguages = ['en', 'zh-CN', 'th-TH'] as const

export type AppLanguage = typeof supportedLanguages[number]

export interface LanguageOption {
  value: AppLanguage
  label: string
  nativeLabel: string
}

export const languageOptions: LanguageOption[] = [
  { value: 'en', label: 'English', nativeLabel: 'English' },
  { value: 'zh-CN', label: 'Chinese', nativeLabel: '中文' },
  { value: 'th-TH', label: 'Thai', nativeLabel: 'ไทย' },
]

export function isAppLanguage(value: string): value is AppLanguage {
  return supportedLanguages.includes(value as AppLanguage)
}

export function normalizeLanguage(value?: string | null): AppLanguage {
  if (!value) {
    return 'en'
  }

  const normalized = value.trim().toLowerCase()

  if (normalized === 'zh' || normalized === 'zh-cn') {
    return 'zh-CN'
  }

  if (normalized === 'th' || normalized === 'th-th') {
    return 'th-TH'
  }

  if (normalized === 'en' || normalized.startsWith('en-')) {
    return 'en'
  }

  return 'en'
}

export function loadStoredLanguage(storage: Storage | undefined = browserStorage()): AppLanguage {
  if (!storage) {
    return 'en'
  }
  return normalizeLanguage(storage.getItem(LANGUAGE_STORAGE_KEY))
}

export function saveLanguage(language: AppLanguage, storage: Storage | undefined = browserStorage()) {
  if (!storage) {
    return
  }
  storage.setItem(LANGUAGE_STORAGE_KEY, language)
}

function browserStorage() {
  return typeof localStorage === 'undefined' ? undefined : localStorage
}

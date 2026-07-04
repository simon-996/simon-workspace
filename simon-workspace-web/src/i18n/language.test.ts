import { describe, expect, it } from 'vitest'
import { createI18n } from 'vue-i18n'

import {
  LANGUAGE_STORAGE_KEY,
  isAppLanguage,
  loadStoredLanguage,
  normalizeLanguage,
  saveLanguage,
} from './language'
import { messages } from './messages'

function createStorage(initial: Record<string, string> = {}): Storage {
  const data = new Map(Object.entries(initial))

  return {
    get length() {
      return data.size
    },
    clear() {
      data.clear()
    },
    getItem(key: string) {
      return data.get(key) ?? null
    },
    key(index: number) {
      return Array.from(data.keys())[index] ?? null
    },
    removeItem(key: string) {
      data.delete(key)
    },
    setItem(key: string, value: string) {
      data.set(key, value)
    },
  }
}

describe('language helpers', () => {
  it('defaults to English when a value is missing or unsupported', () => {
    expect(normalizeLanguage()).toBe('en')
    expect(normalizeLanguage('')).toBe('en')
    expect(normalizeLanguage('fr-FR')).toBe('en')
  })

  it('normalizes supported browser-style locale tags', () => {
    expect(normalizeLanguage('en-US')).toBe('en')
    expect(normalizeLanguage('zh')).toBe('zh-CN')
    expect(normalizeLanguage('zh-cn')).toBe('zh-CN')
    expect(normalizeLanguage('th')).toBe('th-TH')
    expect(normalizeLanguage('th-TH')).toBe('th-TH')
  })

  it('recognizes only supported app languages', () => {
    expect(isAppLanguage('en')).toBe(true)
    expect(isAppLanguage('zh-CN')).toBe(true)
    expect(isAppLanguage('th-TH')).toBe(true)
    expect(isAppLanguage('zh')).toBe(false)
  })

  it('loads and saves the selected language in storage', () => {
    const storage = createStorage()

    expect(loadStoredLanguage(storage)).toBe('en')

    saveLanguage('th-TH', storage)

    expect(storage.getItem(LANGUAGE_STORAGE_KEY)).toBe('th-TH')
    expect(loadStoredLanguage(storage)).toBe('th-TH')
  })

  it('keeps homepage contact email as profile data instead of i18n copy', () => {
    const i18n = createI18n({
      legacy: false,
      locale: 'en',
      fallbackLocale: 'en',
      messages,
    })

    for (const locale of Object.keys(messages)) {
      if (!isAppLanguage(locale)) {
        throw new Error(`Unsupported test locale: ${locale}`)
      }
      const message = messages[locale]
      i18n.global.locale.value = locale
      expect(message.home.intro).not.toHaveProperty('contactEmail')
      expect(i18n.global.t('home.intro.lead')).not.toBe('')
    }
  })
})

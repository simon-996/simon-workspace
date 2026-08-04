import { describe, expect, it } from 'vitest'
import { createI18n } from 'vue-i18n'

import { terminalCommands } from '../components/terminalCommands'
import { buildWorkspaceNavigation } from '../navigation/workspaceNavigation'
import { buildWorkspaceHomeActions } from '../views/workspace/workspaceHomeActions'
import { messages } from './messages'

const navigation = buildWorkspaceNavigation(() => true)
const homeActions = buildWorkspaceHomeActions(() => true)

const requiredWorkspaceUiKeys = Array.from(
  new Set([
    ...navigation.groups.flatMap((group) => [
      group.labelKey,
      ...group.items.map((item) => item.labelKey),
    ]),
    'workspace.nav.more',
    'workspace.home.title',
    'workspace.home.description',
    'workspace.home.noAvailableTasks',
    ...homeActions.flatMap((action) => [action.titleKey, action.descriptionKey]),
    'workspace.home.recent.title',
    'workspace.home.recent.courses',
    'workspace.home.recent.files',
    'workspace.home.recent.drafts',
    'workspace.home.recent.emptyCourses',
    'workspace.home.recent.emptyFiles',
    'workspace.home.recent.emptyDrafts',
    'workspace.home.recent.retry',
    'workspace.home.recent.loadFailed',
    'workspace.files.upload.title',
    'workspace.files.upload.fileLabel',
    'workspace.files.upload.chooseFile',
    'workspace.files.upload.selectedFile',
    'workspace.files.upload.visibility',
    'workspace.files.upload.progress',
    'workspace.files.upload.uploading',
    'workspace.files.upload.fileRequired',
    'workspace.files.upload.failed',
    'workspace.files.upload.succeeded',
    'workspace.files.visibility.private',
    'workspace.files.visibility.public',
    ...terminalCommands.map((command) => command.descriptionKey),
  ]),
)

function getMessageValue(message: unknown, key: string): unknown {
  return key.split('.').reduce<unknown>((current, segment) => {
    if (typeof current !== 'object' || current === null || !(segment in current)) {
      return undefined
    }

    return (current as Record<string, unknown>)[segment]
  }, message)
}

function getPlaceholders(value: string) {
  return Array.from(value.matchAll(/\{([a-zA-Z][\w-]*)\}/g), (match) => match[1]).sort()
}

describe('workspace UI translations', () => {
  it('provides every label used by workspace navigation, home, upload, and terminal help', () => {
    for (const [locale, message] of Object.entries(messages)) {
      for (const key of requiredWorkspaceUiKeys) {
        const value = getMessageValue(message, key)

        expect(value, `${locale}:${key}`).toEqual(expect.any(String))
        expect((value as string).trim(), `${locale}:${key}`).not.toBe('')
      }
    }
  })

  it('keeps named placeholders consistent across locales', () => {
    for (const key of requiredWorkspaceUiKeys) {
      const expected = getPlaceholders(getMessageValue(messages.en, key) as string)

      for (const [locale, message] of Object.entries(messages)) {
        const value = getMessageValue(message, key)
        expect(getPlaceholders(value as string), `${locale}:${key}`).toEqual(expected)
      }
    }
  })

  it('labels the fourth canonical mobile destination as articles in every locale', () => {
    const mobileArticle = navigation.mobileItems[3]

    expect(mobileArticle.key).toBe('blogPosts')
    expect(mobileArticle.labelKey).toBe('workspace.nav.blogPosts')
    expect.soft(getMessageValue(messages['zh-CN'], mobileArticle.labelKey)).toBe('文章')
    expect.soft(getMessageValue(messages.en, mobileArticle.labelKey)).toBe('Articles')
    expect.soft(getMessageValue(messages['th-TH'], mobileArticle.labelKey)).toBe('บทความ')
  })

  it('interpolates the uploaded filename through vue-i18n in every locale', () => {
    const i18n = createI18n({
      legacy: false,
      locale: 'en',
      messages,
    })

    for (const locale of Object.keys(messages)) {
      i18n.global.locale.value = locale as keyof typeof messages
      const translated = i18n.global.t('workspace.files.upload.succeeded', {
        filename: 'lesson-plan.pdf',
      })

      expect(translated, locale).toContain('lesson-plan.pdf')
      expect(translated, locale).not.toContain('{filename}')
    }
  })

  it('does not retain retired workspace home status copy', () => {
    for (const message of Object.values(messages)) {
      expect(message.workspace.home).not.toHaveProperty('apiReady')
      expect(message.workspace.home).not.toHaveProperty('phase')
    }
  })
})

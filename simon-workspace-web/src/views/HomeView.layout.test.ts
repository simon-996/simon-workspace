// @vitest-environment jsdom

import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { ref } from 'vue'
import { createI18n } from 'vue-i18n'
import { createMemoryHistory, createRouter } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import zhCNSource from '../i18n/locales/zh-CN.ts?raw'
import { messages } from '../i18n/messages'
import HomeView from './HomeView.vue'
import homeSource from './HomeView.vue?raw'

vi.mock('../composables/usePublicSiteConfig', () => ({
  usePublicSiteConfig: () => ({
    site: ref({
      ownerName: 'Simon',
      heroSubtitle: 'Build useful tools',
      profileBio: '',
      contactEmail: 'simon@example.com',
      techStack: [],
    }),
    failed: ref(false),
    loadSite: vi.fn().mockResolvedValue(undefined),
  }),
}))

enableAutoUnmount(afterEach)
afterEach(() => {
  vi.restoreAllMocks()
})

describe('HomeView profile layout', () => {
  it('does not render the old Chinese intro kicker copy', () => {
    expect(zhCNSource).not.toContain("kicker: '个人介绍'")
    expect(homeSource).toContain('v-if="introKicker"')
  })

  it('omits the Chinese short intro line when it is empty', () => {
    const removedShortLine = ['喜欢把教学', '和技术想法', '做成可用工具', '的大学教师'].join('')
    expect(zhCNSource).not.toContain(removedShortLine)
    expect(homeSource).toContain('const introShort = computed')
    expect(homeSource).toContain('v-if="introShort"')
    expect(homeSource).toContain('{{ introShort }}')
    expect(homeSource).not.toContain("{{ t('home.intro.shortLine') }}")
  })

  it('positions expanded intro details below the title area', () => {
    expect(homeSource).not.toContain('top: calc(100% - 130px)')
    expect(homeSource).toContain('top: calc(100% + var(--intro-details-gap))')
    expect(homeSource).not.toContain('--intro-details-lift')
  })

  it('centers the contact link in compact layouts', () => {
    expect(homeSource).toContain('@media (max-width: 900px)')
    expect(homeSource).toContain('justify-self: center;')
    expect(homeSource).toContain('text-align: center;')
  })

  it('omits the Chinese contact label when it is empty', () => {
    expect(zhCNSource).not.toContain("contactLabel: '邮箱'")
    expect(homeSource).toContain('const contactLabel = computed')
    expect(homeSource).toContain('v-if="contactLabel"')
  })

  it('renders the contact email as profile data instead of an i18n message', () => {
    expect(homeSource).toContain('const contactEmail = computed')
    expect(homeSource).toContain('site.value?.contactEmail')
    expect(homeSource).toContain(':href="contactHref"')
    expect(homeSource).toContain('{{ contactEmail }}')
    expect(homeSource).not.toContain("t('home.intro.contactEmail')")
  })

  it('keeps hidden profile details from intercepting terminal input', () => {
    expect(homeSource).toContain(":class=\"{ 'details-active': detailsInteractive }\"")
    expect(homeSource).toContain('pointer-events: none;')
    expect(homeSource).toContain('.home-page.details-active .intro-details')
    expect(homeSource).toContain('pointer-events: auto;')
    expect(homeSource).toContain('.home-page.details-active .terminal-stage')
  })

  it('does not pass initial focus intent into the terminal panel', async () => {
    const requestAnimationFrame = vi
      .spyOn(window, 'requestAnimationFrame')
      .mockImplementation((callback) => {
        callback(0)
        return 1
      })
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: { template: '<div />' } },
        { path: '/login', component: { template: '<div />' } },
      ],
    })
    const i18n = createI18n({
      legacy: false,
      locale: 'en',
      messages,
    })
    const wrapper = mount(HomeView, {
      attachTo: document.body,
      global: {
        plugins: [createPinia(), i18n, router],
        stubs: { AppHeader: true },
      },
    })
    await flushPromises()

    const input = wrapper.get<HTMLInputElement>('.terminal input')
    expect(document.activeElement).not.toBe(input.element)
    expect(homeSource).toContain('<TerminalPanel v-if="site" />')
    expect(homeSource).not.toContain('auto-focus')
    expect(requestAnimationFrame).not.toHaveBeenCalled()
  })
})

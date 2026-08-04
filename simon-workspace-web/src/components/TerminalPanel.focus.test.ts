// @vitest-environment jsdom

import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { messages } from '../i18n/messages'
import { useAuthStore } from '../stores/auth'
import TerminalPanel from './TerminalPanel.vue'
import source from './TerminalPanel.vue?raw'

const routerMocks = vi.hoisted(() => ({
  push: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: routerMocks.push }),
}))

enableAutoUnmount(afterEach)
afterEach(() => {
  vi.restoreAllMocks()
})

function mountTerminalPanel() {
  const pinia = createPinia()
  const auth = useAuthStore(pinia)
  vi.spyOn(auth, 'restore').mockResolvedValue(false)
  const login = vi.spyOn(auth, 'login').mockResolvedValue(undefined)
  const i18n = createI18n({
    legacy: false,
    locale: 'en',
    messages,
  })
  const wrapper = mount(TerminalPanel, {
    attachTo: document.body,
    global: {
      plugins: [pinia, i18n],
    },
  })

  return { login, wrapper }
}

describe('TerminalPanel focus behavior', () => {
  beforeEach(() => {
    routerMocks.push.mockReset()
    routerMocks.push.mockResolvedValue(undefined)
    vi.spyOn(window, 'requestAnimationFrame').mockImplementation((callback) => {
      callback(0)
      return 1
    })
  })

  it('does not expose or execute an autofocus prop', () => {
    expect(source).not.toContain('autoFocus?: boolean')
    expect(source).not.toContain('if (props.autoFocus)')
  })

  it('starts with an empty command input instead of prefilling help', () => {
    expect(source).toContain("const prompt = ref('')")
    expect(source).not.toContain("const prompt = ref('help')")
  })

  it('returns pointer interaction on the terminal surface to the command input', async () => {
    const { wrapper } = mountTerminalPanel()
    const input = wrapper.get<HTMLInputElement>('input')

    input.element.blur()
    expect(document.activeElement).not.toBe(input.element)

    await wrapper.get('.terminal').trigger('pointerdown')

    expect(document.activeElement).toBe(input.element)
  })

  it.each(['login', 'login simon', 'login simon secret'])(
    'routes %s without invoking credential login or echoing credentials',
    async (command) => {
      const { login, wrapper } = mountTerminalPanel()
      const input = wrapper.get<HTMLInputElement>('input')

      await input.setValue(command)
      await wrapper.get('form').trigger('submit')
      await flushPromises()

      expect(login).not.toHaveBeenCalled()
      expect(routerMocks.push).toHaveBeenCalledOnce()
      expect(routerMocks.push).toHaveBeenCalledWith('/login')
      expect(wrapper.get('.terminal-output').text()).not.toContain('simon')
      expect(wrapper.get('.terminal-output').text()).not.toContain('secret')
    },
  )

  it('clears rejected login input without retaining credentials in state or output', async () => {
    routerMocks.push.mockRejectedValueOnce(new Error('route unavailable'))
    const { login, wrapper } = mountTerminalPanel()
    const input = wrapper.get<HTMLInputElement>('input')

    await input.setValue('login simon secret')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    const terminalState = wrapper.vm as unknown as Record<string, unknown>
    const retainedCommandState = JSON.stringify(
      ['lines', 'history', 'output', 'result', 'prompt'].map((key) => terminalState[key] ?? null),
    )
    const visibleCommandState = [
      input.element.value,
      wrapper.get('.terminal-output').text(),
    ].join('\n')

    expect(login).not.toHaveBeenCalled()
    expect(routerMocks.push).toHaveBeenCalledOnce()
    expect(input.element.value).toBe('')
    expect(wrapper.get('.terminal-output').text()).toContain('Something went wrong. Please try again.')
    expect(retainedCommandState).not.toContain('simon')
    expect(retainedCommandState).not.toContain('secret')
    expect(visibleCommandState).not.toContain('simon')
    expect(visibleCommandState).not.toContain('secret')
  })

  it('contains no credential-login side effect', () => {
    expect(source).not.toContain('auth.login')
  })
})

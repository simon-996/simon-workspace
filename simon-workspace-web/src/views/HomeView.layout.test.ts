import { describe, expect, it } from 'vitest'

import zhCNSource from '../i18n/locales/zh-CN.ts?raw'
import homeSource from './HomeView.vue?raw'

describe('HomeView profile layout', () => {
  it('does not render the old Chinese intro kicker copy', () => {
    expect(zhCNSource).not.toContain("kicker: '个人介绍'")
    expect(homeSource).toContain('v-if="introKicker"')
  })

  it('positions expanded intro details below the title area', () => {
    expect(homeSource).not.toContain('top: calc(100% - 130px)')
    expect(homeSource).toContain('top: calc(100% + var(--intro-details-gap) - var(--intro-details-lift))')
    expect(homeSource).toContain('--intro-details-lift')
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
})

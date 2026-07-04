import { describe, expect, it } from 'vitest'

import appSource from './App.vue?raw'

describe('App providers', () => {
  it('registers Naive UI providers used by setup components', () => {
    expect(appSource).toContain('NConfigProvider')
    expect(appSource).toContain('NMessageProvider')
    expect(appSource).toContain('<NConfigProvider')
    expect(appSource).toContain('<NMessageProvider')
    expect(appSource).not.toContain('<n-config-provider')
    expect(appSource).not.toContain('<n-message-provider')
  })
})

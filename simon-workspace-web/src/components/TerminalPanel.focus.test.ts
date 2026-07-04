import { describe, expect, it } from 'vitest'

import source from './TerminalPanel.vue?raw'

describe('TerminalPanel focus behavior', () => {
  it('exposes an autofocus prop and focuses the command input after mount', () => {
    expect(source).toContain('autoFocus?: boolean')
    expect(source).toContain('autoFocus: false')
    expect(source).toContain('const commandInput = ref<HTMLInputElement | null>(null)')
    expect(source).toContain('if (props.autoFocus)')
    expect(source).toContain('window.requestAnimationFrame(focusInput)')
  })

  it('returns pointer interaction on the terminal surface to the command input', () => {
    expect(source).toContain('@pointerdown="focusInput"')
    expect(source).toContain('ref="commandInput"')
    expect(source).toContain('commandInput.value?.focus()')
  })
})

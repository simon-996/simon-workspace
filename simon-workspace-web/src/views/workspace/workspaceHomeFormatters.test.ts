import { describe, expect, it, vi } from 'vitest'

import {
  formatWorkspaceCourseStatus,
  formatWorkspaceDate,
  formatWorkspaceFileSize,
} from './workspaceHomeFormatters'

describe('workspace home formatters', () => {
  it('translates only known course statuses and renders unknown values safely', () => {
    const translate = vi.fn((key: string) => key)

    expect(formatWorkspaceCourseStatus('ACTIVE', translate)).toBe('common.states.active')
    expect(formatWorkspaceCourseStatus('ARCHIVED', translate)).toBe('common.states.archived')
    expect(formatWorkspaceCourseStatus('PAUSED', translate)).toBe('—')
    expect(translate).toHaveBeenCalledTimes(2)
  })

  it('uses binary file-size units through GiB', () => {
    expect(formatWorkspaceFileSize(512, 'en-US')).toBe('512 B')
    expect(formatWorkspaceFileSize(1024, 'en-US')).toBe('1 KiB')
    expect(formatWorkspaceFileSize(1024 ** 2, 'en-US')).toBe('1 MiB')
    expect(formatWorkspaceFileSize(1024 ** 3, 'en-US')).toBe('1 GiB')
    expect(formatWorkspaceFileSize(1536, 'de-DE')).toBe('1,5 KiB')
  })

  it('renders missing or invalid dates safely', () => {
    expect(formatWorkspaceDate(undefined, 'en-US')).toBe('—')
    expect(formatWorkspaceDate('not-a-date', 'en-US')).toBe('—')
    expect(formatWorkspaceDate('2026-08-04T00:00:00Z', 'en-US')).toBe('Aug 4, 2026')
  })
})

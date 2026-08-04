import { describe, expect, it } from 'vitest'

import { formatBinaryFileSize } from './fileFormatters'

describe('file formatters', () => {
  it('formats binary sizes with locale-aware decimals', () => {
    expect(formatBinaryFileSize(512, 'en-US')).toBe('512 B')
    expect(formatBinaryFileSize(1536, 'en-US')).toBe('1.5 KiB')
    expect(formatBinaryFileSize(1536, 'de-DE')).toBe('1,5 KiB')
    expect(formatBinaryFileSize(1024 ** 3, 'en-US')).toBe('1 GiB')
  })

  it.each([-1, Number.NaN, Number.POSITIVE_INFINITY])(
    'renders invalid sizes safely (%s)',
    (size) => {
      expect(formatBinaryFileSize(size, 'en-US')).toBe('—')
    },
  )
})

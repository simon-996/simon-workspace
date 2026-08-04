const binaryFileSizeUnits = ['B', 'KiB', 'MiB', 'GiB'] as const

export function formatWorkspaceCourseStatus(
  status: string,
  translate: (key: string) => string,
): string {
  if (status === 'ACTIVE') return translate('common.states.active')
  if (status === 'ARCHIVED') return translate('common.states.archived')
  return '—'
}

export function formatWorkspaceFileSize(size: number, locale: string): string {
  if (!Number.isFinite(size) || size < 0) return '—'

  let value = size
  let unitIndex = 0
  while (value >= 1024 && unitIndex < binaryFileSizeUnits.length - 1) {
    value /= 1024
    unitIndex += 1
  }

  const formattedValue = new Intl.NumberFormat(locale, { maximumFractionDigits: 1 }).format(value)
  return `${formattedValue} ${binaryFileSizeUnits[unitIndex]}`
}

export function formatWorkspaceDate(value: string | null | undefined, locale: string): string {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat(locale, { dateStyle: 'medium' }).format(date)
}

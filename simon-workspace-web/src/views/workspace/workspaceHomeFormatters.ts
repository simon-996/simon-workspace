import { formatBinaryFileSize } from '../../utils/fileFormatters'

export function formatWorkspaceCourseStatus(
  status: string,
  translate: (key: string) => string,
): string {
  if (status === 'ACTIVE') return translate('common.states.active')
  if (status === 'ARCHIVED') return translate('common.states.archived')
  return '—'
}

export function formatWorkspaceFileSize(size: number, locale: string): string {
  return formatBinaryFileSize(size, locale)
}

export function formatWorkspaceDate(value: string | null | undefined, locale: string): string {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat(locale, { dateStyle: 'medium' }).format(date)
}

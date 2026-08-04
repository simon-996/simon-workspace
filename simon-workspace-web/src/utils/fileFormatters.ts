const binaryFileSizeUnits = ['B', 'KiB', 'MiB', 'GiB'] as const

export function formatBinaryFileSize(size: number, locale: string): string {
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

export interface BlogTagLike {
  name: string
}

export interface BlogTagOption {
  label: string
  value: string
}

export function normalizeTagName(value: string) {
  return value.trim().replace(/\s+/g, ' ')
}

export function normalizeTagKey(value: string) {
  return normalizeTagName(value).toLowerCase()
}

export function createTagOption(value: string, existingTags: BlogTagLike[] = []): BlogTagOption {
  const normalizedValue = normalizeTagName(value)
  const key = normalizeTagKey(normalizedValue)
  const existing = existingTags.find((tag) => normalizeTagKey(tag.name) === key)
  const tag = normalizeTagName(existing?.name || normalizedValue)
  return { label: tag, value: tag }
}

export function normalizeSelectedTags(values: string[], limit = 8) {
  const normalized = new Map<string, string>()
  for (const value of values) {
    const tag = normalizeTagName(value)
    if (tag) {
      const key = normalizeTagKey(tag)
      if (!normalized.has(key)) {
        normalized.set(key, tag)
      }
    }
    if (normalized.size >= limit) break
  }
  return Array.from(normalized.values())
}

export function buildTagOptions(remoteTags: BlogTagLike[], selectedTags: string[]) {
  const options = new Map<string, BlogTagOption>()
  for (const item of remoteTags) {
    const tag = normalizeTagName(item.name)
    if (tag) {
      options.set(normalizeTagKey(tag), { label: tag, value: tag })
    }
  }
  for (const value of selectedTags) {
    const tag = normalizeTagName(value)
    if (tag) {
      options.set(normalizeTagKey(tag), { label: tag, value: tag })
    }
  }
  return Array.from(options.values())
}

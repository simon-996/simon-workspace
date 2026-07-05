export function resolveAvatarUrl(
  value?: string | null,
  apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api',
) {
  const url = value?.trim()
  if (!url) {
    return ''
  }

  if (/^(https?:)?\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }

  if (url.startsWith('/api/')) {
    return url
  }

  if (url.startsWith('/')) {
    return joinUrl(apiBaseUrl, url)
  }

  return joinUrl(apiBaseUrl, url)
}

function joinUrl(baseUrl: string, path: string) {
  return `${baseUrl.replace(/\/+$/, '')}/${path.replace(/^\/+/, '')}`
}

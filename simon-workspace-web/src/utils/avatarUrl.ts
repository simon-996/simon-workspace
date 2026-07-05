export function resolveAvatarUrl(
  value?: string | null,
  apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api',
) {
  const url = value?.trim()
  if (!url) {
    logAvatarUrl(value ?? '', '', 'empty')
    return ''
  }

  if (/^(https?:)?\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) {
    logAvatarUrl(url, url, 'absolute')
    return url
  }

  if (url.startsWith('/api/')) {
    logAvatarUrl(url, url, 'api-relative')
    return url
  }

  if (url.startsWith('/')) {
    const output = joinUrl(apiBaseUrl, url)
    logAvatarUrl(url, output, 'root-relative')
    return output
  }

  const output = joinUrl(apiBaseUrl, url)
  logAvatarUrl(url, output, 'relative')
  return output
}

function joinUrl(baseUrl: string, path: string) {
  return `${baseUrl.replace(/\/+$/, '')}/${path.replace(/^\/+/, '')}`
}

function logAvatarUrl(input: string, output: string, reason: string) {
  if (!avatarUrlDebugEnabled()) {
    return
  }

  console.debug('[avatar-url]', { input, output, reason })
}

function avatarUrlDebugEnabled() {
  try {
    return globalThis.localStorage?.getItem('simon-workspace-avatar-debug') === '1'
  } catch {
    return false
  }
}

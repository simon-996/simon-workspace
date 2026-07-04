export const AUTH_SESSION_STORAGE_KEY = 'simon-workspace-session'
const LEGACY_AUTH_TOKEN_STORAGE_KEY = 'simon-workspace-token'

export interface AuthSession {
  accessToken: string
  tokenType: string
  expiresIn: number
  loginTime: number
}

export function buildAuthHeader(session: AuthSession | null) {
  if (!session?.accessToken) {
    return {}
  }

  return {
    Authorization: `${session.tokenType || 'Bearer'} ${session.accessToken}`,
  }
}

export function readStoredSession(): AuthSession | null {
  if (!hasStorage()) {
    return null
  }

  const rawSession = localStorage.getItem(AUTH_SESSION_STORAGE_KEY)
  if (rawSession) {
    try {
      const session = JSON.parse(rawSession) as AuthSession
      return isValidSession(session) ? session : null
    } catch {
      return null
    }
  }

  const legacyToken = localStorage.getItem(LEGACY_AUTH_TOKEN_STORAGE_KEY)
  if (!legacyToken) {
    return null
  }

  return {
    accessToken: legacyToken,
    tokenType: 'Bearer',
    expiresIn: -1,
    loginTime: Date.now(),
  }
}

export function writeStoredSession(session: AuthSession) {
  if (!hasStorage()) {
    return
  }

  localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session))
  localStorage.removeItem(LEGACY_AUTH_TOKEN_STORAGE_KEY)
}

export function clearStoredSession() {
  if (!hasStorage()) {
    return
  }

  localStorage.removeItem(AUTH_SESSION_STORAGE_KEY)
  localStorage.removeItem(LEGACY_AUTH_TOKEN_STORAGE_KEY)
}

export function isSessionExpired(session: AuthSession | null, now = Date.now()) {
  if (!session) {
    return true
  }
  if (session.expiresIn < 0) {
    return false
  }
  return session.loginTime + session.expiresIn * 1000 <= now
}

function isValidSession(session: AuthSession | null): session is AuthSession {
  return Boolean(
    session
      && typeof session.accessToken === 'string'
      && session.accessToken.length > 0
      && typeof session.tokenType === 'string'
      && typeof session.expiresIn === 'number'
      && typeof session.loginTime === 'number',
  )
}

function hasStorage() {
  return typeof localStorage !== 'undefined'
}

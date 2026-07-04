import { describe, expect, it } from 'vitest'

import { buildAuthHeader, type AuthSession } from './authSession'

describe('auth session', () => {
  it('builds a bearer authorization header from the stored session shape', () => {
    const session: AuthSession = {
      accessToken: 'abc123',
      tokenType: 'Bearer',
      expiresIn: 43_200,
      loginTime: 1_783_123_456_000,
    }

    expect(buildAuthHeader(session)).toEqual({
      Authorization: 'Bearer abc123',
    })
  })
})

import { describe, expect, it } from 'vitest'

import workspaceSource from './workspace.ts?raw'

describe('blog workspace api contract', () => {
  it('exposes author user ids on blog post responses for owner actions', () => {
    expect(workspaceSource).toContain('authorUserId: string')
    expect(workspaceSource).toContain('export interface BlogPostDetail extends BlogPostSummary')
  })
})

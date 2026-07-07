import { describe, expect, it } from 'vitest'

import routerSource from './index.ts?raw'

describe('blog routes', () => {
  it('registers an authenticated edit route for blog posts', () => {
    expect(routerSource).toContain("path: '/blog/:id/edit'")
    expect(routerSource).toContain("name: 'blog-edit'")
    expect(routerSource).toContain("permission: 'blog:post:update'")
    expect(routerSource.indexOf("path: '/blog/:id/edit'")).toBeLessThan(
      routerSource.indexOf("path: '/blog/:id'"),
    )
  })
})

import { describe, expect, it } from 'vitest'

import routerSource from './index.ts?raw'

describe('blog routes', () => {
  it('lazy loads every route view', () => {
    expect(routerSource.match(/^import .+View from '\.\.\/views\/.+\.vue'$/gm)).toBeNull()
    expect(routerSource.match(/^const .+View = \(\) => import\('\.\.\/views\/.+\.vue'\)$/gm)).toHaveLength(22)
  })

  it('registers an authenticated edit route for blog posts', () => {
    expect(routerSource).toContain("path: '/blog/:id/edit'")
    expect(routerSource).toContain("name: 'blog-edit'")
    expect(routerSource).toContain("permission: 'blog:post:update'")
    expect(routerSource.indexOf("path: '/blog/:id/edit'")).toBeLessThan(
      routerSource.indexOf("path: '/blog/:id'"),
    )
  })
})

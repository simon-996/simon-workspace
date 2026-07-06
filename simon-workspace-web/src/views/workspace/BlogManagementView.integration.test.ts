import { describe, expect, it } from 'vitest'

import managementSource from './BlogManagementView.vue?raw'

describe('BlogManagementView', () => {
  it('uses the protected category management API and shows post counts', () => {
    expect(managementSource).toContain('fetchBlogManageCategories')
    expect(managementSource).toContain('postCount')
  })

  it('keeps categories with posts from being deleted in the UI', () => {
    expect(managementSource).toContain('canDeleteCategory')
    expect(managementSource).toContain('category.postCount')
  })
})

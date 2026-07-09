import { describe, expect, it } from 'vitest'

import workspaceApiSource from '../../api/workspace.ts?raw'
import routeSource from '../../router/index.ts?raw'
import workspaceSource from '../WorkspaceView.vue?raw'
import homeSource from './WorkspaceHomeView.vue?raw'
import postsSource from './BlogPostManagementView.vue?raw'

describe('BlogPostManagementView', () => {
  it('uses protected management APIs for draft and published post lists', () => {
    expect(workspaceApiSource).toContain('fetchManageBlogPosts')
    expect(workspaceApiSource).toContain('/blog/manage/posts')
    expect(workspaceApiSource).toContain('fetchManageBlogPostDetail')
    expect(workspaceApiSource).toContain('deleteBlogPost')
    expect(postsSource).toContain('fetchManageBlogPosts')
    expect(postsSource).toContain('deleteBlogPost')
  })

  it('is available from workspace routes and navigation with post permission', () => {
    expect(routeSource).toContain("path: 'posts'")
    expect(routeSource).toContain("permission: 'blog:post:create'")
    expect(workspaceSource).toContain('/workspace/posts')
    expect(homeSource).toContain('/workspace/posts')
  })

  it('supports status filtering and draft editing from the workspace', () => {
    expect(postsSource).toContain("value: 'DRAFT'")
    expect(postsSource).toContain("value: 'PUBLISHED'")
    expect(postsSource).toContain('editPost(post)')
    expect(postsSource).toContain("router.push(`/blog/${post.id}/edit`)")
  })
})

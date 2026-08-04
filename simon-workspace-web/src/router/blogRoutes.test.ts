// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'

import { router } from './index'
import routerSource from './index.ts?raw'

describe('blog routes', () => {
  it('lazy loads every route view', () => {
    const components = router.getRoutes().flatMap((route) => Object.values(route.components ?? {}))
    const dynamicViewPaths = Array.from(
      routerSource.matchAll(/\bimport\s*\(\s*['"](\.\.\/views\/[^'"]+\.vue)['"]\s*\)/g),
      (match) => match[1],
    ).sort()

    expect(components).toHaveLength(23)
    expect(components.every((component) => typeof component === 'function')).toBe(true)
    expect(routerSource.match(/import\s+[\w$]+\s+from\s+['"]\.\.\/views\/[^'"]+\.vue['"]/g)).toBeNull()
    expect(dynamicViewPaths).toEqual([
      '../views/HomeView.vue',
      '../views/LoginView.vue',
      '../views/RegisterView.vue',
      '../views/WorkspaceView.vue',
      '../views/blog/BlogDetailView.vue',
      '../views/blog/BlogEditorView.vue',
      '../views/blog/BlogListView.vue',
      '../views/course/PublicCourseDetailView.vue',
      '../views/course/PublicCourseListView.vue',
      '../views/course/PublicMarkdownView.vue',
      '../views/workspace/BlogManagementView.vue',
      '../views/workspace/BlogPostManagementView.vue',
      '../views/workspace/ClassManagementView.vue',
      '../views/workspace/CourseManagementView.vue',
      '../views/workspace/FileCenterView.vue',
      '../views/workspace/GenerationHistoryView.vue',
      '../views/workspace/SecurityManagementView.vue',
      '../views/workspace/SemesterManagementView.vue',
      '../views/workspace/SiteConfigView.vue',
      '../views/workspace/StorageManagementView.vue',
      '../views/workspace/TemplateManagementView.vue',
      '../views/workspace/WorkspaceHomeView.vue',
    ])
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

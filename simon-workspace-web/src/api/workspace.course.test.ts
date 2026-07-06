import { describe, expect, it } from 'vitest'

import source from './workspace.ts?raw'

describe('course material api', () => {
  it('defines public course and material management functions', () => {
    expect(source).toContain('fetchPublicCourses')
    expect(source).toContain('fetchPublicCourseDetail')
    expect(source).toContain('fetchCourseMaterials')
    expect(source).toContain('createCourseMaterial')
    expect(source).toContain('/public/courses')
    expect(source).toContain('/materials')
  })
})

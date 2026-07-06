import { describe, expect, it } from 'vitest'

import source from './CourseManagementView.vue?raw'

describe('course material manager view', () => {
  it('exposes public course controls and material CRUD flows', () => {
    expect(source).toContain('publicVisible')
    expect(source).toContain('publicSortOrder')
    expect(source).toContain('fetchCourseMaterials')
    expect(source).toContain('createCourseMaterial')
    expect(source).toContain('updateCourseMaterial')
    expect(source).toContain('deleteCourseMaterial')
    expect(source).toContain('uploadCourseMaterialFile')
  })

  it('shows the three course material sections', () => {
    expect(source).toContain('DOCUMENT')
    expect(source).toContain('COURSEWARE')
    expect(source).toContain('RESOURCE')
  })
})

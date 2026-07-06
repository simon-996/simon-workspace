import { describe, expect, it } from 'vitest'

import listSource from './PublicCourseListView.vue?raw'
import detailSource from './PublicCourseDetailView.vue?raw'
import markdownSource from './PublicMarkdownView.vue?raw'

describe('public course views', () => {
  it('renders a course list and the three course material sections', () => {
    expect(listSource).toContain('fetchPublicCourses')
    expect(detailSource).toContain("t('courses.sections.documents')")
    expect(detailSource).toContain("t('courses.sections.courseware')")
    expect(detailSource).toContain("t('courses.sections.resources')")
    expect(detailSource).toContain('openMaterial')
  })

  it('uses the public file view endpoint for markdown reading', () => {
    expect(markdownSource).toContain('MdPreview')
    expect(markdownSource).toContain('/files/public/')
    expect(markdownSource).toContain('/view')
  })
})

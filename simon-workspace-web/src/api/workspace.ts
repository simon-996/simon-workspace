import { http } from './http'
import { unwrapApiResponse, type ApiResponse } from './errors'

export interface Course {
  id: string
  courseName: string
  courseCode?: string | null
  major?: string | null
  grade?: string | null
  totalHours: number
  theoryHours?: number | null
  experimentHours?: number | null
  weeklyHours?: number | null
  credit?: number | null
  textbook?: string | null
  courseGoal?: string | null
  keyPoint?: string | null
  difficultPoint?: string | null
  assessmentMethod?: string | null
  syllabus?: string | null
  description?: string | null
  status: 'ACTIVE' | 'ARCHIVED' | string
  publicVisible?: boolean
  publicSortOrder?: number
  createdTime?: string
  updatedTime?: string
}

export interface CoursePayload {
  courseName: string
  courseCode?: string | null
  major?: string | null
  grade?: string | null
  totalHours: number
  theoryHours?: number | null
  experimentHours?: number | null
  weeklyHours?: number | null
  credit?: number | null
  textbook?: string | null
  courseGoal?: string | null
  keyPoint?: string | null
  difficultPoint?: string | null
  assessmentMethod?: string | null
  syllabus?: string | null
  description?: string | null
  status?: string | null
  publicVisible?: boolean | null
  publicSortOrder?: number | null
}

export interface CourseMaterial {
  id: string
  courseId: string
  section: 'DOCUMENT' | 'COURSEWARE' | 'RESOURCE' | string
  materialType: 'FILE' | 'LINK' | string
  fileId?: string | null
  externalUrl?: string | null
  title: string
  description?: string | null
  sortOrder: number
  status: 'ACTIVE' | 'DISABLED' | string
  originalFilename?: string | null
  publicUrl?: string | null
  contentType?: string | null
  fileExtension?: string | null
  fileSize?: number | null
  createdTime?: string | null
  updatedTime?: string | null
}

export interface CourseMaterialPayload {
  section: string
  materialType: string
  fileId?: string | number | null
  externalUrl?: string | null
  title: string
  description?: string | null
  sortOrder?: number | null
  status?: string | null
}

export interface PublicCourseDetail {
  course: Course
  documents: CourseMaterial[]
  courseware: CourseMaterial[]
  resources: CourseMaterial[]
}

export interface ClassInfo {
  id: string
  className: string
  major?: string | null
  grade?: string | null
  studentCount?: number | null
  counselor?: string | null
  remark?: string | null
  createdTime?: string
  updatedTime?: string
}

export interface ClassInfoPayload {
  className: string
  major?: string | null
  grade?: string | null
  studentCount?: number | null
  counselor?: string | null
  remark?: string | null
}

export interface Semester {
  id: string
  academicYear: string
  semesterName: string
  startDate: string
  endDate?: string | null
  totalWeeks: number
  examWeek?: number | null
  holidayJson?: string | null
  adjustmentJson?: string | null
  remark?: string | null
  status: 'PLANNED' | 'ACTIVE' | 'CLOSED' | string
  createdTime?: string
  updatedTime?: string
}

export interface SemesterPayload {
  academicYear: string
  semesterName: string
  startDate: string
  endDate?: string | null
  totalWeeks: number
  examWeek?: number | null
  holidayJson?: string | null
  adjustmentJson?: string | null
  remark?: string | null
  status?: string | null
}

export interface SemesterCalendar {
  id: string
  semesterId: string
  weekNo: number
  startDate: string
  endDate: string
  examWeek: boolean
  holiday: boolean
  holidayNote?: string | null
  adjustmentNote?: string | null
  createdTime?: string
  updatedTime?: string
}

export interface SemesterCalendarPayload {
  startDate: string
  endDate: string
  examWeek?: boolean | null
  holiday?: boolean | null
  holidayNote?: string | null
  adjustmentNote?: string | null
}

export interface TemplateFile {
  id: string
  templateName: string
  templateType: 'WORD' | 'EXCEL' | 'OTHER' | string
  originalFilename: string
  fileSize: number
  contentType?: string | null
  description?: string | null
  status: 'ACTIVE' | 'ARCHIVED' | string
  createdTime?: string
  updatedTime?: string
}

export interface TemplatePayload {
  templateName: string
  templateType?: string | null
  description?: string | null
  status?: string | null
}

export interface TemplateUploadPayload {
  templateName?: string | null
  templateType?: string | null
  description?: string | null
  status?: string | null
}

export interface TemplateField {
  id: string
  templateId: string
  fieldKey: string
  fieldLabel?: string | null
  fieldType: 'TEXT' | 'NUMBER' | 'DATE' | 'JSON' | string
  required: boolean
  defaultValue?: string | null
  sortOrder: number
  remark?: string | null
  status: 'ACTIVE' | 'DISABLED' | string
  createdTime?: string
  updatedTime?: string
}

export interface TemplateFieldPayload {
  fieldKey: string
  fieldLabel?: string | null
  fieldType?: string | null
  required?: boolean | null
  defaultValue?: string | null
  sortOrder?: number | null
  remark?: string | null
  status?: string | null
}

export interface FileResource {
  id: string
  ownerUserId: string
  sourceType: string
  originalFilename: string
  storageProvider: string
  objectKey?: string | null
  visibility: 'PUBLIC' | 'PRIVATE' | string
  publicUrl?: string | null
  fileSize: number
  contentType?: string | null
  fileExtension?: string | null
  status: string
  orphanedTime?: string | null
  createdTime?: string
  updatedTime?: string
}

export interface BlogCategory {
  id: string
  name: string
  slug: string
  description?: string | null
  sortOrder: number
  status: string
  postCount?: number | null
}

export interface BlogTag {
  id: string
  name: string
  slug: string
  usageCount: number
}

export interface BlogPostSummary {
  id: string
  title: string
  summary?: string | null
  slug: string
  status: string
  authorName: string
  authorUserId: string
  category?: BlogCategory | null
  tags: BlogTag[]
  viewCount: number
  commentCount: number
  publishedTime?: string | null
  updatedTime?: string | null
}

export interface BlogPostDetail extends BlogPostSummary {
  contentMd: string
  createdTime?: string | null
}

export interface BlogPostPayload {
  title: string
  summary?: string | null
  slug?: string | null
  categoryId?: number | string | null
  tags: string[]
  contentMd: string
  status: 'DRAFT' | 'PUBLISHED'
}

export interface BlogComment {
  id: string
  postId: string
  parentId?: string | null
  authorName: string
  content: string
  status: string
  createdTime?: string | null
}

export interface StorageProviderState {
  providerCode: string
  providerType: string
  displayName: string
  configured: boolean
  enabled: boolean
  active: boolean
  endpoint?: string | null
  bucket?: string | null
  publicBaseUrl?: string | null
  lastTestStatus?: 'SUCCESS' | 'FAILED' | string | null
  lastTestMessage?: string | null
  lastTestTime?: string | null
}

export interface GenerationTask {
  id: string
  ownerUserId: string
  taskType: string
  taskName: string
  courseId?: string | null
  classId?: string | null
  semesterId?: string | null
  templateId?: string | null
  status: string
  inputJson?: string | null
  resultSummary?: string | null
  failureReason?: string | null
  startedTime?: string | null
  finishedTime?: string | null
  createdTime?: string
  updatedTime?: string
}

export interface ManagedUser {
  id: string
  username: string
  nickname: string
  email?: string | null
  status: string
  lastLoginTime?: string | null
  reviewedTime?: string | null
  reviewRemark?: string | null
  roles: string[]
  createdTime?: string
  updatedTime?: string
}

export interface Permission {
  id: string
  permissionCode: string
  permissionName: string
  resourceType: string
  description?: string | null
}

export interface Role {
  id: string
  roleCode: string
  roleName: string
  description?: string | null
  permissions: Permission[]
}

export interface UpdateUserRolesPayload {
  roleCodes: string[]
}

export async function fetchCourses(keyword?: string) {
  const response = await http.get<ApiResponse<Course[]>>('/courses', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function createCourse(payload: CoursePayload) {
  const response = await http.post<ApiResponse<Course>>('/courses', payload)
  return unwrapApiResponse(response.data)
}

export async function updateCourse(id: string, payload: CoursePayload) {
  const response = await http.put<ApiResponse<Course>>(`/courses/${id}`, payload)
  return unwrapApiResponse(response.data)
}

export async function deleteCourse(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/courses/${id}`)
  return unwrapApiResponse(response.data)
}

export async function fetchCourseMaterials(courseId: string) {
  const response = await http.get<ApiResponse<CourseMaterial[]>>(`/courses/${courseId}/materials`)
  return unwrapApiResponse(response.data)
}

export async function createCourseMaterial(courseId: string, payload: CourseMaterialPayload) {
  const response = await http.post<ApiResponse<CourseMaterial>>(`/courses/${courseId}/materials`, payload)
  return unwrapApiResponse(response.data)
}

export async function updateCourseMaterial(courseId: string, materialId: string, payload: CourseMaterialPayload) {
  const response = await http.put<ApiResponse<CourseMaterial>>(`/courses/${courseId}/materials/${materialId}`, payload)
  return unwrapApiResponse(response.data)
}

export async function deleteCourseMaterial(courseId: string, materialId: string) {
  const response = await http.delete<ApiResponse<null>>(`/courses/${courseId}/materials/${materialId}`)
  return unwrapApiResponse(response.data)
}

export async function fetchPublicCourses() {
  const response = await http.get<ApiResponse<Course[]>>('/public/courses')
  return unwrapApiResponse(response.data)
}

export async function fetchPublicCourseDetail(id: string) {
  const response = await http.get<ApiResponse<PublicCourseDetail>>(`/public/courses/${id}`)
  return unwrapApiResponse(response.data)
}

export async function uploadCourseMaterialFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<FileResource>>('/files', formData, {
    params: {
      sourceType: 'COURSE_MATERIAL',
      visibility: 'PUBLIC',
    },
  })
  return unwrapApiResponse(response.data)
}

export async function fetchClasses(keyword?: string) {
  const response = await http.get<ApiResponse<ClassInfo[]>>('/classes', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function createClassInfo(payload: ClassInfoPayload) {
  const response = await http.post<ApiResponse<ClassInfo>>('/classes', payload)
  return unwrapApiResponse(response.data)
}

export async function updateClassInfo(id: string, payload: ClassInfoPayload) {
  const response = await http.put<ApiResponse<ClassInfo>>(`/classes/${id}`, payload)
  return unwrapApiResponse(response.data)
}

export async function deleteClassInfo(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/classes/${id}`)
  return unwrapApiResponse(response.data)
}

export async function fetchSemesters(keyword?: string) {
  const response = await http.get<ApiResponse<Semester[]>>('/semesters', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function createSemester(payload: SemesterPayload) {
  const response = await http.post<ApiResponse<Semester>>('/semesters', payload)
  return unwrapApiResponse(response.data)
}

export async function updateSemester(id: string, payload: SemesterPayload) {
  const response = await http.put<ApiResponse<Semester>>(`/semesters/${id}`, payload)
  return unwrapApiResponse(response.data)
}

export async function generateSemesterCalendar(id: string) {
  const response = await http.post<ApiResponse<SemesterCalendar[]>>(`/semesters/${id}/calendar/generate`)
  return unwrapApiResponse(response.data)
}

export async function fetchSemesterCalendar(id: string) {
  const response = await http.get<ApiResponse<SemesterCalendar[]>>(`/semesters/${id}/calendar`)
  return unwrapApiResponse(response.data)
}

export async function updateSemesterCalendar(
  semesterId: string,
  calendarId: string,
  payload: SemesterCalendarPayload,
) {
  const response = await http.put<ApiResponse<SemesterCalendar>>(
    `/semesters/${semesterId}/calendar/${calendarId}`,
    payload,
  )
  return unwrapApiResponse(response.data)
}

export async function fetchTemplates(keyword?: string) {
  const response = await http.get<ApiResponse<TemplateFile[]>>('/templates', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function uploadTemplate(file: File, payload: TemplateUploadPayload) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<TemplateFile>>('/templates/upload', formData, {
    params: payload,
  })
  return unwrapApiResponse(response.data)
}

export async function updateTemplate(id: string, payload: TemplatePayload) {
  const response = await http.put<ApiResponse<TemplateFile>>(`/templates/${id}`, payload)
  return unwrapApiResponse(response.data)
}

export async function deleteTemplate(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/templates/${id}`)
  return unwrapApiResponse(response.data)
}

export async function fetchTemplateFields(id: string) {
  const response = await http.get<ApiResponse<TemplateField[]>>(`/templates/${id}/fields`)
  return unwrapApiResponse(response.data)
}

export async function updateTemplateFields(id: string, fields: TemplateFieldPayload[]) {
  const response = await http.put<ApiResponse<TemplateField[]>>(`/templates/${id}/fields`, { fields })
  return unwrapApiResponse(response.data)
}

export async function fetchFiles(keyword?: string) {
  const response = await http.get<ApiResponse<FileResource[]>>('/files', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function fetchFileDetail(id: string) {
  const response = await http.get<ApiResponse<FileResource>>(`/files/${id}`)
  return unwrapApiResponse(response.data)
}

export async function uploadFileResource(
  file: File,
  visibility: 'PRIVATE' | 'PUBLIC' = 'PRIVATE',
  onProgress?: (progress: number) => void,
) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<FileResource>>('/files', formData, {
    params: {
      sourceType: 'UPLOAD',
      visibility,
    },
    onUploadProgress: (event) => {
      if (!Number.isFinite(event.loaded) || !Number.isFinite(event.total) || !event.total || event.total <= 0) return
      const progress = Math.round((event.loaded / event.total) * 100)
      onProgress?.(Math.min(100, Math.max(0, progress)))
    },
  })
  return unwrapApiResponse(response.data)
}

export async function uploadAvatarResource(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<FileResource>>('/files', formData, {
    params: {
      sourceType: 'AVATAR',
      visibility: 'PUBLIC',
    },
  })
  return unwrapApiResponse(response.data)
}

export async function uploadBlogEditorImage(
  file: File,
  sourceType = 'BLOG_EDITOR',
  onProgress?: (progress: number) => void,
) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<FileResource>>('/files', formData, {
    params: {
      sourceType,
      visibility: 'PUBLIC',
    },
    onUploadProgress: (event) => {
      if (!event.total) return
      onProgress?.(Math.min(100, Math.round((event.loaded / event.total) * 100)))
    },
  })
  return unwrapApiResponse(response.data)
}

export async function deleteFileResource(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/files/${id}`)
  return unwrapApiResponse(response.data)
}

export async function downloadFileResource(id: string) {
  const response = await http.get<Blob>(`/files/${id}/download`, {
    responseType: 'blob',
  })
  const disposition = response.headers['content-disposition']
  return {
    blob: response.data,
    filename: filenameFromDisposition(typeof disposition === 'string' ? disposition : undefined) || `file-${id}`,
  }
}

export async function fetchStorageProviders() {
  const response = await http.get<ApiResponse<StorageProviderState[]>>('/storage/providers')
  return unwrapApiResponse(response.data)
}

export async function activateStorageProvider(code: string) {
  const response = await http.put<ApiResponse<StorageProviderState>>(`/storage/providers/${code}/activate`)
  return unwrapApiResponse(response.data)
}

export async function testStorageProvider(code: string) {
  const response = await http.post<ApiResponse<StorageProviderState>>(`/storage/providers/${code}/test`)
  return unwrapApiResponse(response.data)
}

function filenameFromDisposition(disposition?: string) {
  if (!disposition) return ''
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) return decodeURIComponent(utf8Match[1])
  const filenameMatch = disposition.match(/filename="?([^"]+)"?/i)
  return filenameMatch?.[1] ? decodeURIComponent(filenameMatch[1]) : ''
}

export async function fetchGenerationTasks(keyword?: string) {
  const response = await http.get<ApiResponse<GenerationTask[]>>('/generation/tasks', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function fetchGenerationTaskDetail(id: string) {
  const response = await http.get<ApiResponse<GenerationTask>>(`/generation/tasks/${id}`)
  return unwrapApiResponse(response.data)
}

export async function fetchSecurityUsers(keyword?: string) {
  const response = await http.get<ApiResponse<ManagedUser[]>>('/security/users', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function fetchSecurityRoles() {
  const response = await http.get<ApiResponse<Role[]>>('/security/roles')
  return unwrapApiResponse(response.data)
}

export async function updateSecurityUserRoles(id: string, payload: UpdateUserRolesPayload) {
  const response = await http.put<ApiResponse<ManagedUser>>(`/security/users/${id}/roles`, payload)
  return unwrapApiResponse(response.data)
}

export async function approveSecurityUser(id: string, payload: UpdateUserRolesPayload) {
  const response = await http.put<ApiResponse<ManagedUser>>(`/security/users/${id}/approve`, payload)
  return unwrapApiResponse(response.data)
}

export async function rejectSecurityUser(id: string, remark?: string | null) {
  const response = await http.put<ApiResponse<ManagedUser>>(`/security/users/${id}/reject`, { remark: remark || null })
  return unwrapApiResponse(response.data)
}

export async function disableSecurityUser(id: string, remark?: string | null) {
  const response = await http.put<ApiResponse<ManagedUser>>(`/security/users/${id}/disable`, { remark: remark || null })
  return unwrapApiResponse(response.data)
}

export async function fetchBlogCategories() {
  const response = await http.get<ApiResponse<BlogCategory[]>>('/blog/categories')
  return unwrapApiResponse(response.data)
}

export async function fetchBlogManageCategories() {
  const response = await http.get<ApiResponse<BlogCategory[]>>('/blog/manage/categories')
  return unwrapApiResponse(response.data)
}

export async function createBlogCategory(payload: {
  name: string
  slug?: string | null
  description?: string | null
  sortOrder?: number | null
  status?: string | null
}) {
  const response = await http.post<ApiResponse<BlogCategory>>('/blog/categories', payload)
  return unwrapApiResponse(response.data)
}

export async function updateBlogCategory(
  id: string,
  payload: {
    name: string
    slug?: string | null
    description?: string | null
    sortOrder?: number | null
    status?: string | null
  },
) {
  const response = await http.put<ApiResponse<BlogCategory>>(`/blog/categories/${id}`, payload)
  return unwrapApiResponse(response.data)
}

export async function deleteBlogCategory(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/blog/categories/${id}`)
  return unwrapApiResponse(response.data)
}

export async function fetchBlogTags(keyword?: string) {
  const response = await http.get<ApiResponse<BlogTag[]>>('/blog/tags', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function fetchBlogPosts(params?: { keyword?: string; categoryId?: string | number | null; tag?: string }) {
  const response = await http.get<ApiResponse<BlogPostSummary[]>>('/blog/posts', {
    params: {
      keyword: params?.keyword || undefined,
      categoryId: params?.categoryId || undefined,
      tag: params?.tag || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function fetchManageBlogPosts(params?: { keyword?: string; status?: string | null }) {
  const response = await http.get<ApiResponse<BlogPostSummary[]>>('/blog/manage/posts', {
    params: {
      keyword: params?.keyword || undefined,
      status: params?.status || undefined,
    },
  })
  return unwrapApiResponse(response.data)
}

export async function fetchBlogPostDetail(id: string) {
  const response = await http.get<ApiResponse<BlogPostDetail>>(`/blog/posts/${id}`)
  return unwrapApiResponse(response.data)
}

export async function fetchManageBlogPostDetail(id: string) {
  const response = await http.get<ApiResponse<BlogPostDetail>>(`/blog/manage/posts/${id}`)
  return unwrapApiResponse(response.data)
}

export async function createBlogPost(payload: BlogPostPayload) {
  const response = await http.post<ApiResponse<BlogPostDetail>>('/blog/posts', payload)
  return unwrapApiResponse(response.data)
}

export async function updateBlogPost(id: string, payload: BlogPostPayload) {
  const response = await http.put<ApiResponse<BlogPostDetail>>(`/blog/posts/${id}`, payload)
  return unwrapApiResponse(response.data)
}

export async function deleteBlogPost(id: string) {
  const response = await http.delete<ApiResponse<void>>(`/blog/posts/${id}`)
  return unwrapApiResponse(response.data)
}

export async function fetchBlogComments(id: string) {
  const response = await http.get<ApiResponse<BlogComment[]>>(`/blog/posts/${id}/comments`)
  return unwrapApiResponse(response.data)
}

export async function createBlogComment(id: string, content: string, parentId?: string | null) {
  const response = await http.post<ApiResponse<BlogComment>>(`/blog/posts/${id}/comments`, {
    content,
    parentId: parentId || null,
  })
  return unwrapApiResponse(response.data)
}

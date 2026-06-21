import { http } from './http'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

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
  fileSize: number
  contentType?: string | null
  fileExtension?: string | null
  status: string
  createdTime?: string
  updatedTime?: string
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

function unwrap<T>(response: ApiResponse<T>) {
  if (response.code !== 0) {
    throw new Error(response.message || '请求失败')
  }
  return response.data
}

export async function fetchCourses(keyword?: string) {
  const response = await http.get<ApiResponse<Course[]>>('/courses', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrap(response.data)
}

export async function createCourse(payload: CoursePayload) {
  const response = await http.post<ApiResponse<Course>>('/courses', payload)
  return unwrap(response.data)
}

export async function updateCourse(id: string, payload: CoursePayload) {
  const response = await http.put<ApiResponse<Course>>(`/courses/${id}`, payload)
  return unwrap(response.data)
}

export async function deleteCourse(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/courses/${id}`)
  return unwrap(response.data)
}

export async function fetchClasses(keyword?: string) {
  const response = await http.get<ApiResponse<ClassInfo[]>>('/classes', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrap(response.data)
}

export async function createClassInfo(payload: ClassInfoPayload) {
  const response = await http.post<ApiResponse<ClassInfo>>('/classes', payload)
  return unwrap(response.data)
}

export async function updateClassInfo(id: string, payload: ClassInfoPayload) {
  const response = await http.put<ApiResponse<ClassInfo>>(`/classes/${id}`, payload)
  return unwrap(response.data)
}

export async function deleteClassInfo(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/classes/${id}`)
  return unwrap(response.data)
}

export async function fetchSemesters(keyword?: string) {
  const response = await http.get<ApiResponse<Semester[]>>('/semesters', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrap(response.data)
}

export async function createSemester(payload: SemesterPayload) {
  const response = await http.post<ApiResponse<Semester>>('/semesters', payload)
  return unwrap(response.data)
}

export async function updateSemester(id: string, payload: SemesterPayload) {
  const response = await http.put<ApiResponse<Semester>>(`/semesters/${id}`, payload)
  return unwrap(response.data)
}

export async function generateSemesterCalendar(id: string) {
  const response = await http.post<ApiResponse<SemesterCalendar[]>>(`/semesters/${id}/calendar/generate`)
  return unwrap(response.data)
}

export async function fetchSemesterCalendar(id: string) {
  const response = await http.get<ApiResponse<SemesterCalendar[]>>(`/semesters/${id}/calendar`)
  return unwrap(response.data)
}

export async function fetchTemplates(keyword?: string) {
  const response = await http.get<ApiResponse<TemplateFile[]>>('/templates', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrap(response.data)
}

export async function uploadTemplate(file: File, payload: TemplateUploadPayload) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<TemplateFile>>('/templates/upload', formData, {
    params: payload,
  })
  return unwrap(response.data)
}

export async function updateTemplate(id: string, payload: TemplatePayload) {
  const response = await http.put<ApiResponse<TemplateFile>>(`/templates/${id}`, payload)
  return unwrap(response.data)
}

export async function deleteTemplate(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/templates/${id}`)
  return unwrap(response.data)
}

export async function fetchTemplateFields(id: string) {
  const response = await http.get<ApiResponse<TemplateField[]>>(`/templates/${id}/fields`)
  return unwrap(response.data)
}

export async function updateTemplateFields(id: string, fields: TemplateFieldPayload[]) {
  const response = await http.put<ApiResponse<TemplateField[]>>(`/templates/${id}/fields`, { fields })
  return unwrap(response.data)
}

export async function fetchFiles(keyword?: string) {
  const response = await http.get<ApiResponse<FileResource[]>>('/files', {
    params: {
      keyword: keyword || undefined,
    },
  })
  return unwrap(response.data)
}

export async function fetchFileDetail(id: string) {
  const response = await http.get<ApiResponse<FileResource>>(`/files/${id}`)
  return unwrap(response.data)
}

export async function deleteFileResource(id: string) {
  const response = await http.delete<ApiResponse<null>>(`/files/${id}`)
  return unwrap(response.data)
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
  return unwrap(response.data)
}

export async function fetchGenerationTaskDetail(id: string) {
  const response = await http.get<ApiResponse<GenerationTask>>(`/generation/tasks/${id}`)
  return unwrap(response.data)
}

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

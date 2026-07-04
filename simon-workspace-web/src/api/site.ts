import { http } from './http'
import { unwrapApiResponse, type ApiResponse } from './errors'

export interface SiteConfig {
  id: string
  siteTitle: string
  ownerName: string
  heroTitle: string
  heroSubtitle?: string | null
  ownerRole?: string | null
  contactEmail?: string | null
  githubUrl?: string | null
  profileVisible: boolean
  blogVisible: boolean
  projectsVisible: boolean
  workspaceEntryVisible: boolean
  updatedTime?: string
}

export interface SiteConfigPayload {
  siteTitle: string
  ownerName: string
  heroTitle: string
  heroSubtitle?: string | null
  ownerRole?: string | null
  contactEmail?: string | null
  githubUrl?: string | null
  profileVisible: boolean
  blogVisible: boolean
  projectsVisible: boolean
  workspaceEntryVisible: boolean
}

export async function fetchPublicSiteConfig() {
  const response = await http.get<ApiResponse<SiteConfig>>('/public/site')
  return unwrapApiResponse(response.data)
}

export async function fetchAdminSiteConfig() {
  const response = await http.get<ApiResponse<SiteConfig>>('/site/config')
  return unwrapApiResponse(response.data)
}

export async function updateAdminSiteConfig(payload: SiteConfigPayload) {
  const response = await http.put<ApiResponse<SiteConfig>>('/site/config', payload)
  return unwrapApiResponse(response.data)
}

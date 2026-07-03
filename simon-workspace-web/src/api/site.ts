import { http } from './http'
import { translate } from '../i18n'
import type { ApiResponse } from './workspace'

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

function unwrap<T>(response: ApiResponse<T>) {
  if (response.code !== 0) {
    throw new Error(response.message || translate('common.requestFailed'))
  }
  return response.data
}

export async function fetchPublicSiteConfig() {
  const response = await http.get<ApiResponse<SiteConfig>>('/public/site')
  return unwrap(response.data)
}

export async function fetchAdminSiteConfig() {
  const response = await http.get<ApiResponse<SiteConfig>>('/site/config')
  return unwrap(response.data)
}

export async function updateAdminSiteConfig(payload: SiteConfigPayload) {
  const response = await http.put<ApiResponse<SiteConfig>>('/site/config', payload)
  return unwrap(response.data)
}

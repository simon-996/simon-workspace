import type { Component } from 'vue'
import {
  Book,
  Calendar,
  CircleCheck,
  Cloud,
  FileText,
  Files,
  History,
  Notebook,
  Settings,
  Template,
  Users,
} from '@vicons/tabler'

export type WorkspaceNavGroupKey = 'teaching' | 'content' | 'records' | 'system'

export type WorkspaceNavItemKey =
  | 'overview'
  | 'courses'
  | 'classes'
  | 'semesters'
  | 'templates'
  | 'files'
  | 'storage'
  | 'history'
  | 'blogPosts'
  | 'blog'
  | 'security'
  | 'site'

export interface WorkspaceNavItem {
  key: WorkspaceNavItemKey
  to: string
  labelKey: string
  permission: string
  icon: Component
  mobile: boolean
}

export interface WorkspaceNavGroup {
  key: WorkspaceNavGroupKey
  labelKey: string
  items: WorkspaceNavItem[]
}

export interface WorkspaceNavigation {
  groups: WorkspaceNavGroup[]
  mobileItems: WorkspaceNavItem[]
  moreGroups: WorkspaceNavGroup[]
}

const workspaceNavigationGroups: WorkspaceNavGroup[] = [
  {
    key: 'teaching',
    labelKey: 'workspace.navGroups.teaching',
    items: [
      {
        key: 'overview',
        to: '/workspace',
        labelKey: 'workspace.nav.overview',
        permission: 'workspace:view',
        icon: CircleCheck,
        mobile: true,
      },
      {
        key: 'courses',
        to: '/workspace/courses',
        labelKey: 'workspace.nav.courses',
        permission: 'course:manage',
        icon: Book,
        mobile: true,
      },
      {
        key: 'classes',
        to: '/workspace/classes',
        labelKey: 'workspace.nav.classes',
        permission: 'class:manage',
        icon: FileText,
        mobile: false,
      },
      {
        key: 'semesters',
        to: '/workspace/semesters',
        labelKey: 'workspace.nav.semesters',
        permission: 'semester:manage',
        icon: Calendar,
        mobile: false,
      },
    ],
  },
  {
    key: 'content',
    labelKey: 'workspace.navGroups.content',
    items: [
      {
        key: 'templates',
        to: '/workspace/templates',
        labelKey: 'workspace.nav.templates',
        permission: 'template:manage',
        icon: Template,
        mobile: false,
      },
      {
        key: 'files',
        to: '/workspace/files',
        labelKey: 'workspace.nav.files',
        permission: 'file:manage',
        icon: Files,
        mobile: true,
      },
      {
        key: 'storage',
        to: '/workspace/storage',
        labelKey: 'workspace.nav.storage',
        permission: 'file:manage',
        icon: Cloud,
        mobile: false,
      },
      {
        key: 'blogPosts',
        to: '/workspace/posts',
        labelKey: 'workspace.nav.blogPosts',
        permission: 'blog:post:create',
        icon: Notebook,
        mobile: true,
      },
      {
        key: 'blog',
        to: '/workspace/blog',
        labelKey: 'workspace.nav.blog',
        permission: 'blog:category:manage',
        icon: Notebook,
        mobile: false,
      },
    ],
  },
  {
    key: 'records',
    labelKey: 'workspace.navGroups.records',
    items: [
      {
        key: 'history',
        to: '/workspace/history',
        labelKey: 'workspace.nav.history',
        permission: 'generation:history',
        icon: History,
        mobile: false,
      },
    ],
  },
  {
    key: 'system',
    labelKey: 'workspace.navGroups.system',
    items: [
      {
        key: 'security',
        to: '/workspace/security',
        labelKey: 'workspace.nav.security',
        permission: 'user:manage',
        icon: Users,
        mobile: false,
      },
      {
        key: 'site',
        to: '/workspace/site',
        labelKey: 'workspace.nav.site',
        permission: 'site:config',
        icon: Settings,
        mobile: false,
      },
    ],
  },
]

export function buildWorkspaceNavigation(hasPermission: (permission: string) => boolean): WorkspaceNavigation {
  const groups = workspaceNavigationGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => hasPermission(item.permission)),
    }))
    .filter((group) => group.items.length > 0)

  const mobileItems = groups.flatMap((group) => group.items.filter((item) => item.mobile))
  const moreGroups = groups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => !item.mobile),
    }))
    .filter((group) => group.items.length > 0)

  return { groups, mobileItems, moreGroups }
}

import type { Component } from 'vue'
import { Book, FileUpload, Notes } from '@vicons/tabler'

export interface WorkspaceHomeAction {
  readonly key: string
  readonly to: string
  readonly permission: string
  readonly icon: Component
  readonly titleKey: string
  readonly descriptionKey: string
}

const workspaceHomeActionDefinitions = [
  {
    key: 'courses',
    to: '/workspace/courses',
    permission: 'course:manage',
    icon: Book,
    titleKey: 'workspace.home.actions.courses',
    descriptionKey: 'workspace.home.actions.coursesHelp',
  },
  {
    key: 'upload',
    to: '/workspace/files?action=upload',
    permission: 'file:manage',
    icon: FileUpload,
    titleKey: 'workspace.home.actions.upload',
    descriptionKey: 'workspace.home.actions.uploadHelp',
  },
  {
    key: 'write',
    to: '/blog/new',
    permission: 'blog:post:create',
    icon: Notes,
    titleKey: 'workspace.home.actions.write',
    descriptionKey: 'workspace.home.actions.writeHelp',
  },
] as const satisfies readonly WorkspaceHomeAction[]

export function buildWorkspaceHomeActions(
  hasPermission: (permission: string) => boolean,
): readonly WorkspaceHomeAction[] {
  return workspaceHomeActionDefinitions
    .filter((action) => hasPermission(action.permission))
    .map((action) => ({ ...action }))
}

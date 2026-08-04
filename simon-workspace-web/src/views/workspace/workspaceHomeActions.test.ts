import { Book, FileUpload, Notes } from '@vicons/tabler'
import { describe, expect, it } from 'vitest'

import { buildWorkspaceHomeActions } from './workspaceHomeActions'

const expectedActions = [
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
] as const

function allow(...permissions: string[]) {
  return (permission: string) => permissions.includes(permission)
}

describe('buildWorkspaceHomeActions', () => {
  it('returns no actions when the account has none of the approved permissions', () => {
    expect(buildWorkspaceHomeActions(allow())).toEqual([])
  })

  it('returns only upload for file management permission', () => {
    expect(buildWorkspaceHomeActions(allow('file:manage'))).toEqual([expectedActions[1]])
  })

  it('keeps courses and write in definition order for course and blog permissions', () => {
    expect(buildWorkspaceHomeActions(allow('course:manage', 'blog:post:create'))).toEqual([
      expectedActions[0],
      expectedActions[2],
    ])
  })

  it('returns all complete mappings, including icon identity, for all approved permissions', () => {
    expect(buildWorkspaceHomeActions(allow('course:manage', 'file:manage', 'blog:post:create'))).toEqual(
      expectedActions,
    )
  })
})

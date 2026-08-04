import type { LocationQuery, LocationQueryRaw } from 'vue-router'

export function shouldOpenFileUpload(query: LocationQuery): boolean {
  const action = query.action

  return action === 'upload' || (Array.isArray(action) && action.includes('upload'))
}

export function consumeFileUploadAction(query: LocationQuery): LocationQueryRaw {
  const nextQuery = { ...query }
  delete nextQuery.action

  return nextQuery
}

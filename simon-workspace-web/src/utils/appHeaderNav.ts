export type HeaderNavKey = 'home' | 'blog' | 'projects' | 'login' | 'workspace'

export interface HeaderSiteVisibility {
  blogVisible?: boolean
  projectsVisible?: boolean
  workspaceEntryVisible?: boolean
}

export interface BuildHeaderNavItemsInput {
  path: string
  name?: string | symbol | null
  hash?: string
  authenticated: boolean
  site?: HeaderSiteVisibility | null
}

export interface HeaderNavItem {
  key: HeaderNavKey
  to: string
  labelKey: string
  active: boolean
  disabled: boolean
}

export function buildHeaderNavItems(input: BuildHeaderNavItemsInput): HeaderNavItem[] {
  const path = input.path
  const hash = input.hash ?? ''
  const isHome = path === '/' && hash === ''
  const isBlog = path === '/' && hash === '#blog'
  const isProjects = path === '/' && hash === '#projects'
  const isLogin = input.name === 'login' || path === '/login'
  const isWorkspace = path === '/workspace' || path.startsWith('/workspace/')

  const items: Array<HeaderNavItem & { visible: boolean }> = [
    {
      key: 'home',
      to: '/',
      labelKey: 'home.home',
      active: isHome,
      disabled: isHome,
      visible: true,
    },
    {
      key: 'blog',
      to: '/#blog',
      labelKey: 'home.blog',
      active: isBlog,
      disabled: isBlog,
      visible: Boolean(input.site?.blogVisible),
    },
    {
      key: 'projects',
      to: '/#projects',
      labelKey: 'home.projects',
      active: isProjects,
      disabled: isProjects,
      visible: Boolean(input.site?.projectsVisible),
    },
    {
      key: 'login',
      to: '/login',
      labelKey: 'home.login',
      active: isLogin,
      disabled: isLogin,
      visible: !input.authenticated || isLogin,
    },
    {
      key: 'workspace',
      to: '/workspace',
      labelKey: 'workspace.title',
      active: isWorkspace,
      disabled: isWorkspace,
      visible: input.authenticated || Boolean(input.site?.workspaceEntryVisible),
    },
  ]

  return items.filter(({ visible }) => visible).map(({ visible: _visible, ...item }) => item)
}

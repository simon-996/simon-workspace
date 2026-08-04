export type TerminalCommandStatus =
  | 'run'
  | 'help'
  | 'unknown'
  | 'login-required'
  | 'forbidden'
  | 'logout'
  | 'info'
  | 'invalid'
  | 'theme'
  | 'language'
  | 'clear'
export type TerminalCommandCategory = 'nav' | 'workspace' | 'session' | 'system'
export type TerminalThemeMode = 'light' | 'dark' | 'toggle'
export type TerminalLanguage = 'en' | 'zh-CN' | 'th-TH'

export interface TerminalCommand {
  command: string
  descriptionKey: string
  category: TerminalCommandCategory
  aliases?: string[]
  to?: string
  permission?: string
  featured?: boolean
}

export interface TerminalCommandView extends TerminalCommand {
  description: string
}

export interface TerminalCommandContext {
  isAuthenticated: boolean
  username?: string
  hasPermission: (permission: string) => boolean
}

export interface TerminalCommandResult {
  status: TerminalCommandStatus
  command: string
  message: string
  to?: string
  permission?: string
  themeMode?: TerminalThemeMode
  language?: TerminalLanguage
}

export const terminalCommands: TerminalCommand[] = [
  { command: 'help', descriptionKey: 'terminal.commands.help', category: 'system', featured: true },
  { command: 'home', descriptionKey: 'terminal.commands.home', category: 'nav', to: '/', aliases: ['start'] },
  { command: 'about', descriptionKey: 'terminal.commands.about', category: 'nav', to: '#about', featured: true },
  { command: 'blog', descriptionKey: 'terminal.commands.blog', category: 'nav', to: '/blog', featured: true },
  { command: 'courses', descriptionKey: 'terminal.commands.courses', category: 'nav', to: '/courses', featured: true },
  { command: 'projects', descriptionKey: 'terminal.commands.projects', category: 'nav', to: '#projects' },
  { command: 'login', descriptionKey: 'terminal.commands.login', category: 'session', to: '/login' },
  { command: 'logout', descriptionKey: 'terminal.commands.logout', category: 'session' },
  { command: 'whoami', descriptionKey: 'terminal.commands.whoami', category: 'session' },
  { command: 'workspace', descriptionKey: 'terminal.commands.workspace', category: 'workspace', to: '/workspace', permission: 'workspace:view' },
  { command: 'courses-admin', descriptionKey: 'terminal.commands.coursesAdmin', category: 'workspace', to: '/workspace/courses', permission: 'course:manage', aliases: ['course-admin'] },
  { command: 'classes', descriptionKey: 'terminal.commands.classes', category: 'workspace', to: '/workspace/classes', permission: 'class:manage' },
  { command: 'semesters', descriptionKey: 'terminal.commands.semesters', category: 'workspace', to: '/workspace/semesters', permission: 'semester:manage' },
  { command: 'templates', descriptionKey: 'terminal.commands.templates', category: 'workspace', to: '/workspace/templates', permission: 'template:manage' },
  { command: 'files', descriptionKey: 'terminal.commands.files', category: 'workspace', to: '/workspace/files', permission: 'file:manage' },
  { command: 'storage', descriptionKey: 'terminal.commands.storage', category: 'workspace', to: '/workspace/storage', permission: 'file:manage' },
  { command: 'history', descriptionKey: 'terminal.commands.history', category: 'workspace', to: '/workspace/history', permission: 'generation:history' },
  { command: 'blog-admin', descriptionKey: 'terminal.commands.blogAdmin', category: 'workspace', to: '/workspace/blog', permission: 'blog:category:manage' },
  { command: 'site', descriptionKey: 'terminal.commands.site', category: 'workspace', to: '/workspace/site', permission: 'site:config' },
  { command: 'security', descriptionKey: 'terminal.commands.security', category: 'workspace', to: '/workspace/security', permission: 'user:manage' },
]

type TerminalTranslate = (key: string, named?: Record<string, string>) => string

const fallbackMessages: Record<string, string> = {
  'terminal.unknown': 'unknown command: {command}',
  'terminal.loginRequired': 'login required: {permission}',
  'terminal.forbidden': 'permission denied: {permission}',
  'terminal.opening': 'opening {to}',
  'terminal.openUsage': 'usage: open <target>',
  'terminal.logoutReady': 'signing out',
  'terminal.whoami': 'signed in as {username}',
  'terminal.whoamiGuest': 'guest',
  'terminal.themeUsage': 'usage: theme <light|dark|toggle>',
  'terminal.themeChanged': 'theme: {mode}',
  'terminal.languageUsage': 'usage: lang <en|zh-CN|th-TH>',
  'terminal.languageChanged': 'language: {language}',
  'terminal.clearReady': 'cleared',
  'terminal.helpUnknown': 'unknown help topic: {topic}',
  'terminal.commands.help': 'show commands',
  'terminal.commands.home': 'open home',
  'terminal.commands.about': 'open profile',
  'terminal.commands.blog': 'open blog',
  'terminal.commands.courses': 'open courses',
  'terminal.commands.projects': 'open projects',
  'terminal.commands.login': 'sign in',
  'terminal.commands.logout': 'sign out',
  'terminal.commands.whoami': 'show account',
  'terminal.commands.workspace': 'open workspace',
  'terminal.commands.coursesAdmin': 'manage courses',
  'terminal.commands.classes': 'manage classes',
  'terminal.commands.semesters': 'manage semesters',
  'terminal.commands.templates': 'manage templates',
  'terminal.commands.files': 'manage files',
  'terminal.commands.storage': 'manage storage',
  'terminal.commands.history': 'open generation history',
  'terminal.commands.blogAdmin': 'manage blog',
  'terminal.commands.site': 'edit homepage',
  'terminal.commands.security': 'manage roles',
}

const fallbackTranslate: TerminalTranslate = (key, named = {}) => {
  const template = fallbackMessages[key] ?? key
  return Object.entries(named).reduce(
    (message, [name, value]) => message.replace(`{${name}}`, value),
    template,
  )
}

export function getTerminalCommands(t: TerminalTranslate = fallbackTranslate): TerminalCommandView[] {
  return terminalCommands.map((command) => ({
    ...command,
    description: t(command.descriptionKey),
  }))
}

export function evaluateTerminalCommand(
  input: string,
  context: TerminalCommandContext,
  t: TerminalTranslate = fallbackTranslate,
): TerminalCommandResult {
  const parsed = parseTerminalInput(input)
  const commandName = parsed.command
  const trimmedInput = input.trim()

  if (commandName === 'help') {
    return {
      status: 'help',
      command: trimmedInput || commandName,
      message: buildHelpMessage(parsed.args[0], t),
    }
  }

  if (commandName === 'open') {
    return evaluateOpenCommand(parsed.args, context, t, trimmedInput || commandName)
  }

  if (commandName === 'theme') {
    return evaluateThemeCommand(parsed.args, t, trimmedInput || commandName)
  }

  if (commandName === 'lang' || commandName === 'language') {
    return evaluateLanguageCommand(parsed.args, t, trimmedInput || commandName)
  }

  if (commandName === 'clear') {
    return {
      status: 'clear',
      command: commandName,
      message: t('terminal.clearReady'),
    }
  }

  const command = findTerminalCommand(commandName)

  if (!command) {
    return {
      status: 'unknown',
      command: commandName || input,
      message: t('terminal.unknown', { command: commandName || input }),
    }
  }

  if (command.command === 'logout') {
    return {
      status: 'logout',
      command: command.command,
      message: t('terminal.logoutReady'),
    }
  }

  if (command.command === 'whoami') {
    return {
      status: 'info',
      command: command.command,
      message: context.isAuthenticated && context.username
        ? t('terminal.whoami', { username: context.username })
        : t('terminal.whoamiGuest'),
    }
  }

  return runTerminalCommand(command, context, t, commandName)
}

export function parseTerminalInput(input: string) {
  const parts = input.trim().match(/(?:[^\s"]+|"[^"]*")+/g) ?? []
  const [command = '', ...args] = parts.map((part) => {
    if (part.startsWith('"') && part.endsWith('"')) {
      return part.slice(1, -1)
    }
    return part
  })
  const normalizedCommand = command.toLowerCase()

  return {
    command: normalizedCommand,
    args: normalizedCommand === 'login' ? [] : args,
  }
}

function evaluateOpenCommand(
  args: string[],
  context: TerminalCommandContext,
  t: TerminalTranslate,
  input: string,
): TerminalCommandResult {
  const target = normalizeTarget(args.join('-'))
  if (!target) {
    return {
      status: 'invalid',
      command: input,
      message: t('terminal.openUsage'),
    }
  }

  const command = findTerminalCommand(target)
  if (!command?.to) {
    return {
      status: 'unknown',
      command: input,
      message: t('terminal.unknown', { command: target }),
    }
  }

  return runTerminalCommand(command, context, t, input)
}

function evaluateThemeCommand(
  args: string[],
  t: TerminalTranslate,
  input: string,
): TerminalCommandResult {
  const mode = args[0]?.toLowerCase()
  if (mode !== 'light' && mode !== 'dark' && mode !== 'toggle') {
    return {
      status: 'invalid',
      command: input,
      message: t('terminal.themeUsage'),
    }
  }

  return {
    status: 'theme',
    command: input,
    themeMode: mode,
    message: t('terminal.themeChanged', { mode }),
  }
}

function evaluateLanguageCommand(
  args: string[],
  t: TerminalTranslate,
  input: string,
): TerminalCommandResult {
  const language = normalizeTerminalLanguage(args[0])
  if (!language) {
    return {
      status: 'invalid',
      command: input,
      message: t('terminal.languageUsage'),
    }
  }

  return {
    status: 'language',
    command: input,
    language,
    message: t('terminal.languageChanged', { language }),
  }
}

function runTerminalCommand(
  command: TerminalCommand,
  context: TerminalCommandContext,
  t: TerminalTranslate,
  inputCommand: string,
): TerminalCommandResult {
  if (command.permission && !context.isAuthenticated) {
    return {
      status: 'login-required',
      command: inputCommand,
      permission: command.permission,
      message: t('terminal.loginRequired', { permission: command.permission }),
    }
  }

  if (command.permission && !context.hasPermission(command.permission)) {
    return {
      status: 'forbidden',
      command: inputCommand,
      permission: command.permission,
      message: t('terminal.forbidden', { permission: command.permission }),
    }
  }

  return {
    status: 'run',
    command: inputCommand,
    to: command.to,
    permission: command.permission,
    message: command.to ? t('terminal.opening', { to: command.to }) : t(command.descriptionKey),
  }
}

function buildHelpMessage(topic: string | undefined, t: TerminalTranslate) {
  const normalizedTopic = normalizeTarget(topic || '')

  if (!normalizedTopic) {
    return [
      'help nav - public pages',
      'help workspace - protected tools',
      'help session - account commands',
      'help system - display and language',
      'open <target> - open any page',
      'theme <light|dark|toggle>',
      'lang <en|zh-CN|th-TH>',
    ].join('\n')
  }

  if (normalizedTopic === 'nav') {
    return describeCommands('nav', t)
  }

  if (normalizedTopic === 'workspace') {
    return describeCommands('workspace', t)
  }

  if (normalizedTopic === 'session') {
    return [
      'login',
      'logout',
      'whoami',
    ].join('\n')
  }

  if (normalizedTopic === 'system') {
    return [
      'theme <light|dark|toggle>',
      'lang <en|zh-CN|th-TH>',
      'clear',
    ].join('\n')
  }

  const command = findTerminalCommand(normalizedTopic)
  if (command) {
    return describeCommand(command, t)
  }

  return t('terminal.helpUnknown', { topic: normalizedTopic })
}

function describeCommands(category: TerminalCommandCategory, t: TerminalTranslate) {
  return terminalCommands
    .filter((command) => command.category === category && command.to)
    .map((command) => describeCommand(command, t))
    .join('\n')
}

function describeCommand(command: TerminalCommand, t: TerminalTranslate) {
  const permission = command.permission ? ` [${command.permission}]` : ''
  return `open ${command.command} - ${t(command.descriptionKey)}${permission}`
}

function findTerminalCommand(value: string) {
  const target = normalizeTarget(value)
  return terminalCommands.find((item) => item.command === target || item.aliases?.includes(target))
}

function normalizeTarget(value: string) {
  return value.trim().toLowerCase().replace(/\s+/g, '-')
}

function normalizeTerminalLanguage(value: string | undefined): TerminalLanguage | null {
  const normalized = value?.trim().toLowerCase()
  if (!normalized) return null
  if (normalized === 'en' || normalized === 'english') return 'en'
  if (normalized === 'zh' || normalized === 'zh-cn' || normalized === 'cn' || normalized === 'chinese') return 'zh-CN'
  if (normalized === 'th' || normalized === 'th-th' || normalized === 'thai') return 'th-TH'
  return null
}

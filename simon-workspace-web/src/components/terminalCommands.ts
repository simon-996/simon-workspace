export type TerminalCommandStatus = 'run' | 'help' | 'unknown' | 'login-required' | 'forbidden'

export interface TerminalCommand {
  command: string
  descriptionKey: string
  to?: string
  permission?: string
}

export interface TerminalCommandView extends TerminalCommand {
  description: string
}

export interface TerminalCommandContext {
  isAuthenticated: boolean
  hasPermission: (permission: string) => boolean
}

export interface TerminalCommandResult {
  status: TerminalCommandStatus
  command: string
  message: string
  to?: string
  permission?: string
}

export const terminalCommands: TerminalCommand[] = [
  { command: 'help', descriptionKey: 'terminal.commands.help' },
  { command: 'about', descriptionKey: 'terminal.commands.about', to: '#about' },
  { command: 'blog', descriptionKey: 'terminal.commands.blog', to: '#blog' },
  { command: 'projects', descriptionKey: 'terminal.commands.projects', to: '#projects' },
  { command: 'login', descriptionKey: 'terminal.commands.login', to: '/login' },
  { command: 'workspace', descriptionKey: 'terminal.commands.workspace', to: '/workspace', permission: 'workspace:view' },
  { command: 'courses', descriptionKey: 'terminal.commands.courses', to: '/workspace/courses', permission: 'course:manage' },
  { command: 'site', descriptionKey: 'terminal.commands.site', to: '/workspace/site', permission: 'site:config' },
  { command: 'security', descriptionKey: 'terminal.commands.security', to: '/workspace/security', permission: 'user:manage' },
]

type TerminalTranslate = (key: string, named?: Record<string, string>) => string

const fallbackMessages: Record<string, string> = {
  'terminal.unknown': 'unknown command: {command}',
  'terminal.loginRequired': 'login required: {permission}',
  'terminal.forbidden': 'permission denied: {permission}',
  'terminal.opening': 'opening {to}',
  'terminal.commands.help': 'show commands',
  'terminal.commands.about': 'open profile',
  'terminal.commands.blog': 'open blog',
  'terminal.commands.projects': 'open projects',
  'terminal.commands.login': 'sign in',
  'terminal.commands.workspace': 'open workspace',
  'terminal.commands.courses': 'manage courses',
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
  const commandName = input.trim().toLowerCase()
  const command = terminalCommands.find((item) => item.command === commandName)

  if (!command) {
    return {
      status: 'unknown',
      command: commandName || input,
      message: t('terminal.unknown', { command: commandName || input }),
    }
  }

  if (command.command === 'help') {
    return {
      status: 'help',
      command: command.command,
      message: terminalCommands.map((item) => item.command).join('  '),
    }
  }

  if (command.permission && !context.isAuthenticated) {
    return {
      status: 'login-required',
      command: command.command,
      permission: command.permission,
      message: t('terminal.loginRequired', { permission: command.permission }),
    }
  }

  if (command.permission && !context.hasPermission(command.permission)) {
    return {
      status: 'forbidden',
      command: command.command,
      permission: command.permission,
      message: t('terminal.forbidden', { permission: command.permission }),
    }
  }

  return {
    status: 'run',
    command: command.command,
    to: command.to,
    permission: command.permission,
    message: command.to ? t('terminal.opening', { to: command.to }) : t(command.descriptionKey),
  }
}

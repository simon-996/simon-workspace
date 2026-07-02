export type TerminalCommandStatus = 'run' | 'help' | 'unknown' | 'login-required' | 'forbidden'

export interface TerminalCommand {
  command: string
  description: string
  to?: string
  permission?: string
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
  { command: 'help', description: 'show commands' },
  { command: 'about', description: 'open profile', to: '#about' },
  { command: 'blog', description: 'open blog', to: '#blog' },
  { command: 'projects', description: 'open projects', to: '#projects' },
  { command: 'login', description: 'sign in', to: '/login' },
  { command: 'workspace', description: 'open workspace', to: '/workspace', permission: 'workspace:view' },
  { command: 'courses', description: 'manage courses', to: '/workspace/courses', permission: 'course:manage' },
  { command: 'site', description: 'edit homepage', to: '/workspace/site', permission: 'site:config' },
  { command: 'security', description: 'manage roles', to: '/workspace/security', permission: 'user:manage' },
]

export function evaluateTerminalCommand(input: string, context: TerminalCommandContext): TerminalCommandResult {
  const commandName = input.trim().toLowerCase()
  const command = terminalCommands.find((item) => item.command === commandName)

  if (!command) {
    return {
      status: 'unknown',
      command: commandName || input,
      message: `unknown command: ${commandName || input}`,
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
      message: `login required: ${command.permission}`,
    }
  }

  if (command.permission && !context.hasPermission(command.permission)) {
    return {
      status: 'forbidden',
      command: command.command,
      permission: command.permission,
      message: `permission denied: ${command.permission}`,
    }
  }

  return {
    status: 'run',
    command: command.command,
    to: command.to,
    permission: command.permission,
    message: command.to ? `opening ${command.to}` : command.description,
  }
}

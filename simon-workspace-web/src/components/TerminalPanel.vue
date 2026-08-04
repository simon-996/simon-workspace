<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { useAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'
import { setAppLanguage } from '../i18n'
import { translateAppError } from '../api/errors'
import {
  evaluateTerminalCommand,
  getTerminalCommands,
  type TerminalCommand,
  type TerminalCommandResult,
} from './terminalCommands'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const theme = useThemeStore()
const prompt = ref('')
const commandInput = ref<HTMLInputElement | null>(null)
const initialLine = computed(() => t('terminal.initialLine'))
const lines = ref<string[]>([initialLine.value])

const commandContext = computed(() => ({
  isAuthenticated: auth.isAuthenticated && Boolean(auth.user),
  username: auth.user?.username || auth.user?.nickname || '',
  hasPermission: (permission: string) => auth.hasPermission(permission),
}))

const commandRows = computed(() =>
  getTerminalCommands(t)
    .filter((command) => command.featured)
    .map((command) => ({
      ...command,
      state: commandState(command),
    })),
)

watch(initialLine, (value, oldValue) => {
  if (lines.value.length === 1 && lines.value[0] === oldValue) {
    lines.value = [value]
  }
})

onMounted(() => {
  void auth.restore()
})

async function execute(command = prompt.value) {
  const result = evaluateTerminalCommand(command, commandContext.value, t)

  try {
    if (result.status === 'logout') {
      await auth.logout()
      writeResult({ ...result, message: t('terminal.logoutSuccess') })
      return
    }

    if (result.status === 'theme') {
      if (result.themeMode === 'toggle') {
        theme.toggleTheme()
      } else if (result.themeMode) {
        theme.setTheme(result.themeMode)
      }
      writeResult({ ...result, message: t('terminal.themeChanged', { mode: theme.mode }) })
      return
    }

    if (result.status === 'language' && result.language) {
      setAppLanguage(result.language)
      writeResult({ ...result, message: t('terminal.languageChanged', { language: result.language }) })
      return
    }

    if (result.status === 'clear') {
      lines.value = [initialLine.value]
      prompt.value = ''
      window.requestAnimationFrame(focusInput)
      return
    }

    writeResult(result)

    if (result.status === 'run' && result.to) {
      await navigate(result.to)
    }
  } catch (error) {
    writeResult({
      status: 'unknown',
      command: result.command,
      message: translateAppError(error, t),
    })
  }
}

function writeResult(result: TerminalCommandResult) {
  const outputLines = result.message.split('\n').filter(Boolean)
  lines.value = [
    ...lines.value.slice(-8),
    `$ ${result.command}`,
    ...outputLines,
  ].slice(-12)
  prompt.value = ''
  window.requestAnimationFrame(focusInput)
}

async function navigate(to: string) {
  if (to.startsWith('#')) {
    window.location.hash = to
    return
  }
  await router.push(to)
}

function commandState(command: TerminalCommand) {
  if (!command.permission) {
    return 'public'
  }
  if (!commandContext.value.isAuthenticated) {
    return 'login'
  }
  if (!commandContext.value.hasPermission(command.permission)) {
    return 'locked'
  }
  return 'ready'
}

function focusInput() {
  commandInput.value?.focus()
}
</script>

<template>
  <section class="terminal" :aria-label="t('terminal.aria')" @pointerdown="focusInput">
    <div class="terminal-bar">
      <strong>simon.dev</strong>
      <span>{{ commandContext.isAuthenticated ? t('terminal.authenticated') : t('terminal.guest') }}</span>
    </div>

    <div class="terminal-body">
      <div class="terminal-output" aria-live="polite">
        <p v-for="(line, index) in lines" :key="`${line}-${index}`">
          {{ line }}
        </p>
      </div>

      <div class="command-list" :aria-label="t('terminal.quickCommandsAria')">
        <button
          v-for="item in commandRows"
          :key="item.command"
          type="button"
          :class="item.state"
          :aria-label="item.description"
          :title="item.description"
          @click="execute(item.command)"
        >
          <span>{{ item.command }}</span>
        </button>
      </div>

      <form class="prompt-line" @submit.prevent="execute()">
        <span>simon@home:~$</span>
        <input
          ref="commandInput"
          v-model="prompt"
          :aria-label="t('terminal.inputAria')"
          autocomplete="off"
          spellcheck="false"
        >
      </form>
    </div>
  </section>
</template>

<style scoped>
.terminal {
  overflow: hidden;
  --terminal-bg: rgba(255, 255, 255, 0.82);
  --terminal-bg-strong: rgba(255, 255, 255, 0.95);
  --terminal-border: rgba(32, 53, 66, 0.12);
  --terminal-border-strong: rgba(22, 112, 143, 0.18);
  --terminal-text: #17212b;
  --terminal-muted: #627481;
  --terminal-faint: #8a9aa4;
  --terminal-accent: #16708f;
  --terminal-accent-soft: rgba(22, 112, 143, 0.08);
  --terminal-line-bg: rgba(247, 250, 251, 0.76);
  --terminal-shadow: 0 24px 64px rgba(35, 55, 68, 0.13);
  border: 1px solid var(--terminal-border);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.62), rgba(245, 249, 250, 0.84)),
    var(--terminal-bg);
  box-shadow: var(--terminal-shadow);
  backdrop-filter: blur(18px);
  transition:
    border-color 240ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 240ms cubic-bezier(0.16, 1, 0.3, 1),
    box-shadow 240ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 240ms cubic-bezier(0.16, 1, 0.3, 1);
}

:global(:root[data-theme="dark"] .terminal) {
  --terminal-bg: rgba(13, 21, 30, 0.86);
  --terminal-bg-strong: rgba(16, 25, 35, 0.96);
  --terminal-border: rgba(222, 231, 237, 0.1);
  --terminal-border-strong: rgba(106, 190, 216, 0.24);
  --terminal-text: #e9f2f6;
  --terminal-muted: #9aaeb9;
  --terminal-faint: #6f8593;
  --terminal-accent: #6abed8;
  --terminal-accent-soft: rgba(106, 190, 216, 0.14);
  --terminal-line-bg: rgba(8, 13, 19, 0.42);
  --terminal-shadow: 0 26px 72px rgba(0, 0, 0, 0.34);
  background:
    linear-gradient(180deg, rgba(18, 27, 36, 0.92), rgba(12, 19, 27, 0.96)),
    var(--terminal-bg);
}

.terminal-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--terminal-border);
  padding: 13px 16px;
}

.terminal-bar strong,
.terminal-bar span,
.terminal-body,
.prompt-line input {
  font-family: "JetBrains Mono", Consolas, monospace;
}

.terminal-bar strong {
  color: var(--terminal-text);
  font-size: 13px;
  font-weight: 800;
}

.terminal-bar span {
  color: var(--terminal-faint);
  font-size: 12px;
}

.terminal-body {
  display: grid;
  gap: 14px;
  min-height: 420px;
  padding: 16px;
  color: var(--terminal-text);
  font-size: 13px;
  line-height: 1.55;
}

.terminal-output {
  display: grid;
  align-content: end;
  gap: 3px;
  min-height: 170px;
  border: 1px solid var(--terminal-border);
  border-radius: 8px;
  background: var(--terminal-line-bg);
  padding: 14px;
}

.terminal-output p {
  margin: 0;
  color: var(--terminal-muted);
  word-break: break-word;
}

.terminal-output p:nth-last-child(2) {
  color: var(--terminal-accent);
}

.terminal-output p:last-child {
  color: var(--terminal-text);
}

.command-list {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.command-list button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 31px;
  border: 1px solid var(--terminal-border);
  border-radius: 8px;
  background: var(--terminal-bg-strong);
  color: var(--terminal-muted);
  cursor: pointer;
  padding: 0 11px;
  text-align: left;
  transition:
    border-color 180ms cubic-bezier(0.16, 1, 0.3, 1),
    background 180ms cubic-bezier(0.16, 1, 0.3, 1),
    color 180ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 180ms cubic-bezier(0.16, 1, 0.3, 1);
}

.command-list button:hover {
  border-color: var(--terminal-border-strong);
  background: var(--terminal-accent-soft);
  color: var(--terminal-accent);
  transform: translate3d(0, -1px, 0);
}

.command-list button:active {
  transform: translate3d(0, 1px, 0);
}

.command-list button.login,
.command-list button.locked {
  color: #8999a4;
}

.command-list button.ready span,
.command-list button.public span {
  color: currentColor;
}

.command-list span {
  overflow: hidden;
  font-size: 12px;
  font-weight: 800;
  text-overflow: ellipsis;
}

.prompt-line {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  margin: 0;
  border: 1px solid var(--terminal-border);
  border-radius: 8px;
  background: var(--terminal-bg-strong);
  padding: 10px 12px;
}

.prompt-line span {
  color: var(--terminal-accent);
  font-weight: 800;
}

.prompt-line input {
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--terminal-text);
  font-size: 13px;
}

@media (max-width: 760px) {
  .terminal-bar {
    padding: 13px 14px;
  }

  .terminal-body {
    min-height: 320px;
    gap: 14px;
    padding: 14px;
    font-size: 12px;
  }

  .terminal-output {
    min-height: 132px;
    padding: 12px;
  }

  .command-list {
    gap: 6px;
  }

  .command-list button {
    min-height: 30px;
    padding: 0 10px;
  }
}
</style>

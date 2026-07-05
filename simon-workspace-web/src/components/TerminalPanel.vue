<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { useAuthStore } from '../stores/auth'
import {
  evaluateTerminalCommand,
  getTerminalCommands,
  type TerminalCommand,
  type TerminalCommandResult,
} from './terminalCommands'

interface TerminalPanelProps {
  autoFocus?: boolean
}

const props = withDefaults(defineProps<TerminalPanelProps>(), {
  autoFocus: false,
})
const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
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
  getTerminalCommands(t).map((command) => ({
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
  if (props.autoFocus) {
    window.requestAnimationFrame(focusInput)
  }
})

async function execute(command = prompt.value) {
  const result = evaluateTerminalCommand(command, commandContext.value, t)

  try {
    if (result.status === 'login') {
      await auth.login(result.args?.[0] || '', result.args?.[1] || '')
      writeResult({ ...result, message: t('terminal.loginSuccess', { username: auth.user?.username || result.args?.[0] || '' }) })
      return
    }

    if (result.status === 'logout') {
      await auth.logout()
      writeResult({ ...result, message: t('terminal.logoutSuccess') })
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
      message: error instanceof Error ? error.message : t('errors.UNKNOWN_ERROR'),
    })
  }
}

function writeResult(result: TerminalCommandResult) {
  lines.value = [
    ...lines.value.slice(-4),
    `$ ${result.command}`,
    result.message,
  ]
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

      <div class="command-list">
        <button
          v-for="item in commandRows"
          :key="item.command"
          type="button"
          :class="item.state"
          @click="execute(item.command)"
        >
          <span>{{ item.command }}</span>
          <small>{{ item.description }}</small>
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
  border: 1px solid rgba(43, 60, 73, 0.18);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(18, 27, 36, 0.96), rgba(12, 19, 27, 0.98)),
    #111923;
  box-shadow: 0 24px 54px rgba(26, 43, 56, 0.22);
}

.terminal-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(222, 231, 237, 0.1);
  padding: 15px 18px;
}

.terminal-bar strong,
.terminal-bar span,
.terminal-body,
.prompt-line input {
  font-family: "JetBrains Mono", Consolas, monospace;
}

.terminal-bar strong {
  color: #d9eef7;
  font-size: 13px;
  font-weight: 800;
}

.terminal-bar span {
  color: #86a0af;
  font-size: 12px;
}

.terminal-body {
  display: grid;
  gap: 20px;
  min-height: 420px;
  padding: 20px;
  color: #dce6ec;
  font-size: 13px;
  line-height: 1.55;
}

.terminal-output {
  display: grid;
  align-content: end;
  min-height: 118px;
  border-bottom: 1px solid rgba(222, 231, 237, 0.08);
  padding-bottom: 18px;
}

.terminal-output p {
  margin: 0;
  color: #aab8c2;
}

.terminal-output p:nth-last-child(2) {
  color: #59b6d7;
}

.terminal-output p:last-child {
  color: #e9f2f6;
}

.command-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.command-list button {
  display: grid;
  gap: 3px;
  min-width: 0;
  border: 1px solid rgba(222, 231, 237, 0.1);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.03);
  color: #dce6ec;
  cursor: pointer;
  padding: 10px;
  text-align: left;
  transition: border-color 180ms ease, background 180ms ease, transform 180ms ease;
}

.command-list button:hover {
  border-color: rgba(89, 182, 215, 0.45);
  background: rgba(89, 182, 215, 0.08);
}

.command-list button:active {
  transform: translateY(1px);
}

.command-list button.login,
.command-list button.locked {
  color: #8999a4;
}

.command-list button.ready span,
.command-list button.public span {
  color: #59b6d7;
}

.command-list span {
  overflow: hidden;
  font-size: 13px;
  font-weight: 800;
  text-overflow: ellipsis;
}

.command-list small {
  overflow: hidden;
  color: #81919b;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.prompt-line {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  margin: 0;
}

.prompt-line span {
  color: #59b6d7;
  font-weight: 800;
}

.prompt-line input {
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #edf6fa;
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
    min-height: 86px;
    padding-bottom: 12px;
  }

  .command-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .command-list button {
    padding: 9px;
  }
}
</style>

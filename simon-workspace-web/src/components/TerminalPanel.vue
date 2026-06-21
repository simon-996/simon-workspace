<script setup lang="ts">
const commands = [
  { command: 'help', description: 'Show available commands' },
  { command: 'courses', description: 'List and manage courses' },
  { command: 'semester', description: 'View or create semester calendars' },
  { command: 'templates', description: 'Browse and edit templates' },
  { command: 'generate', description: 'Generate lesson plan or resource' },
  { command: 'files', description: 'View generated files' },
  { command: 'tasks', description: 'Show task history' },
  { command: 'exit', description: 'Leave terminal' },
]
</script>

<template>
  <section class="terminal" aria-label="Terminal command preview">
    <div class="terminal-bar">
      <strong>Terminal Observatory</strong>
      <span class="status-dot" aria-hidden="true" />
    </div>

    <div class="terminal-body">
      <p class="terminal-line"><span>simon@workspace:~$</span> help</p>
      <p class="terminal-title">Simon Workspace - AI Teaching Workspace</p>
      <div class="command-list">
        <p v-for="item in commands" :key="item.command">
          <span>{{ item.command }}</span>
          <span>{{ item.description }}</span>
        </p>
      </div>
      <p class="terminal-line prompt-line"><span>simon@workspace:~$</span><i /></p>
    </div>
  </section>
</template>

<style scoped>
.terminal {
  overflow: hidden;
  border: 1px solid rgba(88, 167, 217, 0.32);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(10, 23, 35, 0.94), rgba(5, 13, 22, 0.96)),
    #07111b;
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.38);
}

.terminal-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(88, 167, 217, 0.22);
  padding: 17px 22px;
}

.terminal-bar strong {
  color: #39bdf2;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #75d46c;
  box-shadow: 0 0 0 4px rgba(117, 212, 108, 0.09);
}

.terminal-body {
  min-height: 398px;
  padding: 26px 22px 28px;
  color: #d8e0e8;
  font-family: "JetBrains Mono", "Fira Code", Consolas, monospace;
  font-size: 14px;
  line-height: 1.65;
}

.terminal-body p {
  margin: 0;
}

.terminal-line span,
.command-list p span:first-child {
  color: #3fc6ff;
  font-weight: 600;
}

.terminal-title {
  margin-top: 18px !important;
  color: #edf7ff;
}

.command-list {
  display: grid;
  gap: 9px;
  margin-top: 24px;
}

.command-list p {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 22px;
  color: #9daab8;
}

.prompt-line {
  align-items: center;
  display: flex;
  gap: 10px;
  margin-top: 34px !important;
}

.prompt-line i {
  display: inline-block;
  width: 9px;
  height: 18px;
  background: #56c6ff;
  box-shadow: 0 0 18px rgba(86, 198, 255, 0.45);
  animation: cursor-blink 1s steps(2, start) infinite;
}

@keyframes cursor-blink {
  50% {
    opacity: 0.25;
  }
}

@media (max-width: 760px) {
  .terminal-bar {
    padding: 13px 14px;
  }

  .terminal-body {
    min-height: 224px;
    padding: 14px;
    font-size: 11px;
    line-height: 1.5;
  }

  .terminal-title {
    margin-top: 12px !important;
  }

  .command-list {
    gap: 4px;
    margin-top: 12px;
  }

  .command-list p {
    grid-template-columns: 72px minmax(0, 1fr);
    gap: 12px;
  }

  .prompt-line {
    margin-top: 16px !important;
  }
}
</style>

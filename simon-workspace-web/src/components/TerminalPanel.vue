<template>
  <section class="terminal" aria-label="Terminal command preview">
    <div class="terminal-bar">
      <span />
      <span />
      <span />
      <strong>simon@workspace</strong>
    </div>
    <div class="terminal-body">
      <p><span class="prompt">$</span> help</p>
      <p class="muted">Available commands:</p>
      <p>blog latest</p>
      <p>workspace</p>
      <p>generate lesson --course "Java Web" --week 3</p>
      <p class="success">[NAV] Opened lesson generation form</p>
      <p class="info">[AI] Waiting for confirmation</p>
    </div>
  </section>
</template>

<style scoped>
.terminal {
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.78);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.35);
}

.terminal-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
  padding: 14px 16px;
}

.terminal-bar span {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #38bdf8;
}

.terminal-bar span:first-child {
  background: #f87171;
}

.terminal-bar span:nth-child(2) {
  background: #fbbf24;
}

.terminal-bar strong {
  margin-left: 8px;
  color: #cbd5e1;
  font-size: 13px;
}

.terminal-body {
  min-height: 360px;
  padding: 22px;
  color: #e2e8f0;
  font-family: "JetBrains Mono", "Fira Code", Consolas, monospace;
  font-size: 14px;
  line-height: 1.8;
}

.terminal-body p {
  margin: 0;
}

.prompt {
  color: #22d3ee;
}

.muted {
  color: #94a3b8;
}

.success {
  color: #4ade80;
}

.info {
  color: #67e8f9;
}
</style>

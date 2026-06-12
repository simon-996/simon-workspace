<script setup lang="ts">
import { NButton } from 'naive-ui'

import TerminalPanel from '../components/TerminalPanel.vue'
</script>

<template>
  <main class="home-page">
    <nav class="top-nav">
      <a class="brand" href="/">Simon Workspace</a>
      <div class="nav-links">
        <a href="/#blog">Blog</a>
        <a href="/#projects">Projects</a>
        <router-link to="/workspace">Workspace</router-link>
      </div>
    </nav>

    <section class="hero-section">
      <div class="hero-copy">
        <p class="eyebrow">Personal site and AI teaching workspace</p>
        <h1>Simon Workspace</h1>
        <p class="summary">
          A minimal geek-style personal website with a graphical teaching workspace,
          terminal shortcuts, and AI-assisted document generation.
        </p>
        <div class="actions">
          <n-button type="primary" size="large" tag="router-link" to="/workspace">
            Enter Workspace
          </n-button>
          <n-button ghost size="large">Read Blog</n-button>
        </div>
      </div>

      <TerminalPanel />
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  background:
    linear-gradient(180deg, rgba(37, 99, 235, 0.14), transparent 42%),
    #020617;
}

.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
  padding: 24px 0;
}

.brand {
  font-size: 18px;
  font-weight: 700;
}

.nav-links {
  display: flex;
  gap: 22px;
  color: #cbd5e1;
  font-size: 14px;
}

.hero-section {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(420px, 1.1fr);
  align-items: center;
  gap: 48px;
  width: min(1180px, calc(100% - 40px));
  min-height: calc(100vh - 92px);
  margin: 0 auto;
  padding: 40px 0 88px;
}

.hero-copy {
  max-width: 610px;
}

.eyebrow {
  margin: 0 0 18px;
  color: #67e8f9;
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: #f8fafc;
  font-size: clamp(48px, 7vw, 86px);
  line-height: 0.98;
}

.summary {
  margin: 24px 0 0;
  color: #cbd5e1;
  font-size: 18px;
  line-height: 1.7;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 34px;
}

@media (max-width: 900px) {
  .top-nav {
    align-items: flex-start;
    flex-direction: column;
    gap: 16px;
  }

  .hero-section {
    grid-template-columns: 1fr;
    min-height: auto;
    padding-top: 24px;
  }
}
</style>

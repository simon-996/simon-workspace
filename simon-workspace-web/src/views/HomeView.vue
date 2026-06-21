<script setup lang="ts">
import { ref } from 'vue'
import { NButton, NIcon } from 'naive-ui'
import {
  Book,
  Calendar,
  ChevronRight,
  Download,
  FileText,
  Files,
  Menu2,
  Wand,
} from '@vicons/tabler'

import TerminalPanel from '../components/TerminalPanel.vue'

const navOpen = ref(false)

const workflowSteps = [
  {
    icon: Book,
    number: '01',
    title: 'Course',
    summary: 'Create and manage your courses.',
  },
  {
    icon: Calendar,
    number: '02',
    title: 'Semester',
    summary: 'Build and maintain semester calendars.',
  },
  {
    icon: FileText,
    number: '03',
    title: 'Template',
    summary: 'Use or edit templates for any topic.',
  },
  {
    icon: Wand,
    number: '04',
    title: 'Generate',
    summary: 'Generate lesson plans, resources, and more.',
  },
  {
    icon: Download,
    number: '05',
    title: 'Download',
    summary: 'Export Word, Excel, or PDF files.',
  },
]

const quickActions = [
  { icon: Book, title: 'Generate lesson plan', detail: 'Create with AI' },
  { icon: Book, title: 'View my courses', detail: 'Manage all courses' },
  { icon: FileText, title: 'Browse templates', detail: 'Use or edit templates' },
  { icon: Files, title: 'My files', detail: 'Generated documents' },
]
</script>

<template>
  <main class="home-page">
    <nav class="top-nav">
      <a class="brand" href="/"><span>&gt;_</span> Simon Workspace</a>
      <button class="menu-button" type="button" aria-label="Toggle navigation" @click="navOpen = !navOpen">
        <n-icon :component="Menu2" />
      </button>
      <div class="nav-links" :class="{ open: navOpen }">
        <router-link to="/workspace">Workspace</router-link>
        <a href="/#blog">Blog</a>
        <a href="/#help">Help</a>
      </div>
    </nav>

    <section class="hero-section">
      <div class="hero-copy">
        <p class="eyebrow">// Personal site and AI teaching workspace</p>
        <h1>Teach better. Automate the rest.</h1>
        <p class="summary">
          Maintain courses, semester calendars, templates, and teaching materials.
          Generate lesson plans, track tasks, and keep everything in sync.
        </p>
        <div class="actions">
          <n-button class="primary-action" type="primary" size="large" tag="router-link" to="/workspace">
            <span>&gt;_</span> Enter Workspace
          </n-button>
          <n-button class="ghost-action" ghost size="large">
            <n-icon :component="Book" /> Read Blog
          </n-button>
        </div>
        <p class="ready-state"><span /> AI tools ready</p>
      </div>

      <TerminalPanel />
    </section>

    <section class="workflow-section" aria-label="Workspace workflow">
      <p class="section-kicker">Workflow</p>
      <div class="workflow-rail">
        <article v-for="(step, index) in workflowSteps" :key="step.title" class="workflow-step">
          <div class="step-heading">
            <n-icon :component="step.icon" />
            <span>{{ step.number }}</span>
          </div>
          <h2>{{ step.title }}</h2>
          <p>{{ step.summary }}</p>
          <n-icon v-if="index < workflowSteps.length - 1" class="step-arrow" :component="ChevronRight" />
        </article>
      </div>
    </section>

    <section class="overview-section" aria-label="Workspace overview">
      <div>
        <p class="section-kicker">Overview</p>
        <h2>Everything in one place.</h2>
        <p>Organized. Searchable. AI-assisted.</p>
      </div>
      <div class="overview-list">
        <article>
          <n-icon :component="Files" />
          <div>
            <h3>Courses & Calendars</h3>
            <p>All courses and semester schedules at a glance.</p>
          </div>
        </article>
        <article>
          <n-icon :component="FileText" />
          <div>
            <h3>Templates</h3>
            <p>Reusable teaching documents for lessons and activities.</p>
          </div>
        </article>
        <article>
          <n-icon :component="Wand" />
          <div>
            <h3>AI Assistance</h3>
            <p>Generate structured content faster, then review it yourself.</p>
          </div>
        </article>
      </div>
    </section>

    <section class="mobile-actions" aria-label="Quick actions">
      <p class="section-kicker">Quick Actions</p>
      <a v-for="action in quickActions" :key="action.title" href="/workspace">
        <n-icon :component="action.icon" />
        <span>
          <strong>{{ action.title }}</strong>
          <small>{{ action.detail }}</small>
        </span>
        <n-icon :component="ChevronRight" />
      </a>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: 100dvh;
  overflow-x: hidden;
  background:
    linear-gradient(90deg, rgba(62, 198, 255, 0.08) 1px, transparent 1px),
    linear-gradient(180deg, rgba(62, 198, 255, 0.04) 1px, transparent 1px),
    radial-gradient(circle at 72% 12%, rgba(62, 198, 255, 0.08), transparent 32%),
    #061018;
  background-size: 120px 120px, 120px 120px, auto, auto;
  color: #f6fbff;
}

.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  width: min(1260px, calc(100% - 48px));
  margin: 0 auto;
  padding: 34px 0 20px;
}

.brand {
  align-items: center;
  display: inline-flex;
  gap: 12px;
  color: #f5fbff;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand span {
  color: #3fc6ff;
}

.nav-links {
  display: flex;
  gap: 32px;
  color: #91a2b5;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.nav-links a {
  padding-bottom: 8px;
  transition: color 180ms ease, border-color 180ms ease;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  border-bottom: 2px solid #3fc6ff;
  color: #3fc6ff;
}

.menu-button {
  display: none;
  border: 1px solid rgba(91, 147, 180, 0.35);
  border-radius: 8px;
  background: rgba(7, 18, 29, 0.68);
  color: #d8e9f5;
  cursor: pointer;
  font-size: 24px;
  padding: 8px 10px;
}

.hero-section {
  display: grid;
  grid-template-columns: minmax(320px, 0.82fr) minmax(560px, 1.18fr);
  align-items: center;
  gap: 88px;
  width: min(1260px, calc(100% - 48px));
  min-height: calc(100dvh - 96px);
  margin: 0 auto;
  padding: 44px 0 42px;
}

.hero-copy {
  max-width: 420px;
}

.eyebrow {
  margin: 0 0 24px;
  color: #3fc6ff;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0;
}

h1 {
  margin: 0;
  color: #f9fcff;
  font-size: clamp(42px, 5.2vw, 76px);
  font-weight: 500;
  letter-spacing: 0;
  line-height: 1.08;
}

.summary {
  margin: 26px 0 0;
  color: #aab5c2;
  font-size: 18px;
  line-height: 1.55;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 40px;
}

.actions :deep(.n-button) {
  --n-border-radius: 6px !important;
  min-width: 220px;
  height: 54px;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.primary-action span {
  color: #062033;
  margin-right: 8px;
}

.ghost-action :deep(.n-button__content) {
  gap: 10px;
}

.ready-state {
  align-items: center;
  display: flex;
  gap: 12px;
  margin: 22px 0 0;
  color: #81d778;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
}

.ready-state span {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #75d46c;
}

.workflow-section,
.overview-section {
  width: min(1260px, calc(100% - 48px));
  margin: 0 auto;
  border-top: 1px solid rgba(83, 137, 169, 0.22);
}

.workflow-section {
  padding: 22px 0 44px;
}

.section-kicker {
  margin: 0 0 28px;
  color: #3fc6ff;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.workflow-rail {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.workflow-step {
  position: relative;
  min-height: 126px;
  border-left: 1px solid rgba(83, 137, 169, 0.2);
  padding: 0 28px 0 20px;
}

.step-heading {
  align-items: center;
  display: flex;
  gap: 15px;
  color: #3fc6ff;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-weight: 700;
}

.step-heading .n-icon {
  font-size: 28px;
}

.workflow-step h2 {
  margin: 20px 0 0;
  color: #ffffff;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 15px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.workflow-step p {
  margin: 9px 0 0;
  color: #95a4b3;
  font-size: 14px;
  line-height: 1.45;
}

.step-arrow {
  position: absolute;
  top: 42px;
  right: 22px;
  color: #aab5c2;
  font-size: 24px;
}

.overview-section {
  display: grid;
  grid-template-columns: minmax(240px, 0.75fr) minmax(0, 1.25fr);
  gap: 60px;
  padding: 30px 0 70px;
}

.overview-section h2 {
  margin: 0;
  color: #f8fbff;
  font-size: 28px;
  font-weight: 500;
}

.overview-section p {
  margin: 10px 0 0;
  color: #9daab8;
  font-size: 18px;
}

.overview-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 40px;
}

.overview-list article {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 16px;
}

.overview-list .n-icon {
  color: #3fc6ff;
  font-size: 28px;
}

.overview-list h3 {
  margin: 0;
  color: #f4f8fb;
  font-size: 15px;
  font-weight: 600;
}

.overview-list p {
  margin: 12px 0 0;
  color: #9aa8b5;
  font-size: 14px;
  line-height: 1.55;
}

.mobile-actions {
  display: none;
}

@media (max-width: 900px) {
  .top-nav {
    width: min(100% - 32px, 520px);
    padding-top: 28px;
  }

  .menu-button {
    align-items: center;
    display: inline-flex;
  }

  .nav-links {
    display: none;
    position: absolute;
    top: 78px;
    right: 0;
    left: 0;
    z-index: 2;
    border: 1px solid rgba(83, 137, 169, 0.26);
    border-radius: 8px;
    background: rgba(7, 18, 29, 0.96);
    padding: 18px;
  }

  .nav-links.open {
    display: grid;
    gap: 16px;
  }

  .hero-section {
    grid-template-columns: 1fr;
    gap: 32px;
    width: min(100% - 32px, 520px);
    min-height: auto;
    padding: 42px 0 32px;
  }

  .hero-copy {
    max-width: none;
  }

  .eyebrow {
    margin-bottom: 14px;
    font-size: 11px;
  }

  h1 {
    font-size: clamp(32px, 10vw, 42px);
  }

  .summary {
    font-size: 16px;
  }

  .actions {
    display: grid;
    gap: 12px;
    margin-top: 28px;
  }

  .actions :deep(.n-button) {
    width: 100%;
    min-width: 0;
  }

  .workflow-section,
  .overview-section,
  .mobile-actions {
    width: min(100% - 32px, 520px);
  }

  .workflow-rail {
    display: grid;
    gap: 0;
    grid-template-columns: 1fr;
  }

  .workflow-step {
    display: none;
  }

  .workflow-step:nth-child(-n + 3) {
    display: block;
    min-height: auto;
    border-top: 1px solid rgba(83, 137, 169, 0.18);
    padding: 18px 0;
  }

  .workflow-step:first-child {
    border-top: 0;
  }

  .step-arrow {
    display: none;
  }

  .overview-section {
    grid-template-columns: 1fr;
    gap: 28px;
    padding-bottom: 32px;
  }

  .overview-list {
    grid-template-columns: 1fr;
    gap: 22px;
  }

  .mobile-actions {
    display: block;
    margin: 0 auto;
    padding: 0 0 54px;
  }

  .mobile-actions a {
    align-items: center;
    display: grid;
    grid-template-columns: auto 1fr auto;
    gap: 14px;
    border: 1px solid rgba(83, 137, 169, 0.24);
    border-radius: 6px;
    color: #dce8f2;
    margin-top: 10px;
    padding: 14px;
  }

  .mobile-actions a > .n-icon:first-child {
    color: #3fc6ff;
    font-size: 24px;
  }

  .mobile-actions strong,
  .mobile-actions small {
    display: block;
  }

  .mobile-actions strong {
    font-size: 14px;
    font-weight: 600;
  }

  .mobile-actions small {
    color: #92a1af;
    margin-top: 2px;
  }

  .mobile-actions a > .n-icon:last-child {
    color: #8da0b0;
  }
}
</style>

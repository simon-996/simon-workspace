<script setup lang="ts">
import { NButton, NIcon } from 'naive-ui'
import {
  Book,
  Calendar,
  CircleCheck,
  Clock,
  Download,
  FileText,
  Files,
  History,
  Template,
  Wand,
} from '@vicons/tabler'

const modules = [
  {
    icon: Book,
    title: 'Course Management',
    detail: 'Maintain course records, outlines, and teaching notes.',
    status: 'Ready',
  },
  {
    icon: Calendar,
    title: 'Semester Calendar',
    detail: 'Generate weekly calendars and adjust class rhythm.',
    status: 'Planned',
  },
  {
    icon: Template,
    title: 'Template Library',
    detail: 'Store Word and Excel templates for repeatable output.',
    status: 'Ready',
  },
  {
    icon: Wand,
    title: 'Lesson Generation',
    detail: 'Open a guided flow for lesson plans and resources.',
    status: 'Next',
  },
  {
    icon: Files,
    title: 'File Center',
    detail: 'Collect generated documents and download history.',
    status: 'Queued',
  },
  {
    icon: History,
    title: 'Task History',
    detail: 'Trace generation tasks, failures, and confirmations.',
    status: 'Queued',
  },
]

const activity = [
  { icon: CircleCheck, title: 'API health check', detail: 'Service reports UP' },
  { icon: Clock, title: 'Roadmap tracking', detail: 'Phase todo lists are synced' },
  { icon: Download, title: 'Next export target', detail: 'Word lesson plan flow' },
]
</script>

<template>
  <main class="workspace-page">
    <aside class="workspace-sider">
      <div class="side-brand"><span>&gt;_</span> Simon</div>
      <nav class="side-nav">
        <router-link to="/workspace">Overview</router-link>
        <a>Courses</a>
        <a>Semesters</a>
        <a>Templates</a>
        <a>Files</a>
        <a>History</a>
      </nav>
    </aside>

    <section class="workspace-main">
      <header class="workspace-header">
        <div>
          <p class="section-label">// Workspace</p>
          <h1>Teaching document cockpit</h1>
          <p>Courses, templates, generation tasks, and files stay visible without turning the workspace into a control room.</p>
        </div>
        <n-button class="generate-button" type="primary"><n-icon :component="Wand" /> Generate Lesson</n-button>
      </header>

      <div class="workspace-grid">
        <article v-for="module in modules" :key="module.title" class="module-row">
          <n-icon :component="module.icon" />
          <div>
            <span>{{ module.status }}</span>
            <h2>{{ module.title }}</h2>
            <p>{{ module.detail }}</p>
          </div>
        </article>
      </div>

      <section class="activity-panel" aria-label="Workspace activity">
        <p class="section-label">Recent signals</p>
        <article v-for="item in activity" :key="item.title">
          <n-icon :component="item.icon" />
          <div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.detail }}</p>
          </div>
        </article>
      </section>
    </section>
  </main>
</template>

<style scoped>
.workspace-page {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  min-height: 100dvh;
  background: #f4f7fa;
  color: #0c1722;
}

.workspace-sider {
  border-right: 1px solid #d9e2ea;
  background:
    linear-gradient(180deg, rgba(63, 198, 255, 0.08), transparent 38%),
    #07111b;
  color: #d7e7f2;
}

.side-brand {
  align-items: center;
  display: flex;
  gap: 10px;
  padding: 30px 24px;
  color: #f7fbff;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.side-brand span {
  color: #3fc6ff;
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 16px;
}

.side-nav a {
  border-radius: 6px;
  color: #91a5b6;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.06em;
  padding: 12px 14px;
  text-transform: uppercase;
}

.side-nav a.router-link-active,
.side-nav a:hover {
  background: rgba(63, 198, 255, 0.1);
  color: #45c8ff;
}

.workspace-main {
  display: grid;
  gap: 26px;
  grid-template-columns: minmax(0, 1fr) 320px;
  padding: 42px;
}

.workspace-header {
  align-items: end;
  display: grid;
  grid-column: 1 / -1;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 28px;
  border-bottom: 1px solid #d9e2ea;
  padding-bottom: 28px;
}

.section-label {
  margin: 0 0 8px;
  color: #0b87bd;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: #0b1520;
  font-size: clamp(34px, 4vw, 54px);
  font-weight: 600;
  line-height: 1.2;
}

.workspace-header p:not(.section-label) {
  max-width: 700px;
  margin: 16px 0 0;
  color: #536271;
  font-size: 17px;
  line-height: 1.55;
}

.generate-button {
  --n-border-radius: 6px !important;
  height: 46px;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.generate-button :deep(.n-button__content) {
  gap: 8px;
}

.workspace-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border: 1px solid #d9e2ea;
  border-radius: 8px;
  background: #ffffff;
}

.module-row {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 18px;
  min-height: 168px;
  border-right: 1px solid #d9e2ea;
  border-bottom: 1px solid #d9e2ea;
  padding: 24px;
}

.module-row:nth-child(2n) {
  border-right: 0;
}

.module-row:nth-last-child(-n + 2) {
  border-bottom: 0;
}

.module-row > .n-icon {
  color: #0b96d1;
  font-size: 30px;
}

.module-row span {
  color: #647587;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.module-row h2 {
  margin: 10px 0 0;
  color: #0c1722;
  font-size: 20px;
  font-weight: 700;
}

.module-row p {
  margin: 9px 0 0;
  color: #5f6f7e;
  font-size: 15px;
  line-height: 1.5;
}

.activity-panel {
  border: 1px solid #d9e2ea;
  border-radius: 8px;
  background: #ffffff;
  padding: 22px;
}

.activity-panel article {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 14px;
  border-top: 1px solid #e3eaf0;
  padding: 18px 0;
}

.activity-panel article:last-child {
  padding-bottom: 0;
}

.activity-panel .n-icon {
  color: #0b96d1;
  font-size: 24px;
}

.activity-panel h3 {
  margin: 0;
  color: #0c1722;
  font-size: 15px;
  font-weight: 700;
}

.activity-panel p {
  margin: 4px 0 0;
  color: #697887;
  font-size: 14px;
}

@media (max-width: 760px) {
  .workspace-page {
    grid-template-columns: 1fr;
  }

  .workspace-sider {
    border-right: 0;
    border-bottom: 1px solid rgba(63, 198, 255, 0.18);
  }

  .side-brand {
    padding: 22px 18px 14px;
  }

  .side-nav {
    display: grid;
    grid-auto-columns: max-content;
    grid-auto-flow: column;
    overflow-x: auto;
    padding: 0 18px 18px;
  }

  .workspace-main {
    grid-template-columns: 1fr;
    padding: 24px 18px 36px;
  }

  .workspace-header {
    align-items: start;
    grid-template-columns: 1fr;
  }

  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .module-row,
  .module-row:nth-child(2n),
  .module-row:nth-last-child(-n + 2) {
    border-right: 0;
    border-bottom: 1px solid #d9e2ea;
  }

  .module-row:last-child {
    border-bottom: 0;
  }

  .module-row {
    min-height: auto;
    padding: 20px;
  }
}
</style>

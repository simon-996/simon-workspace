<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { NIcon } from 'naive-ui'
import { Book, Calendar, CircleCheck, Files, History, Template } from '@vicons/tabler'

const { t } = useI18n()

const modules = [
  { labelKey: 'workspace.home.modules.courses', valueKey: 'workspace.home.apiReady', icon: Book, to: '/workspace/courses' },
  { labelKey: 'workspace.home.modules.classes', valueKey: 'workspace.home.apiReady', icon: CircleCheck, to: '/workspace/classes' },
  { labelKey: 'workspace.home.modules.semesters', valueKey: 'workspace.home.apiReady', icon: Calendar, to: '/workspace/semesters' },
  { labelKey: 'workspace.home.modules.templates', valueKey: 'workspace.home.apiReady', icon: Template, to: '/workspace/templates' },
  { labelKey: 'workspace.home.modules.files', valueKey: 'workspace.home.apiReady', icon: Files, to: '/workspace/files' },
  { labelKey: 'workspace.home.modules.history', valueKey: 'workspace.home.apiReady', icon: History, to: '/workspace/history' },
]
</script>

<template>
  <section class="workspace-home">
    <div class="status-grid">
      <router-link v-for="module in modules" :key="module.labelKey" :to="module.to" class="status-card">
        <n-icon :component="module.icon" />
        <div>
          <span>{{ t(module.valueKey) }}</span>
          <h2>{{ t(module.labelKey) }}</h2>
        </div>
      </router-link>
    </div>

    <section class="home-band">
      <div>
        <span>{{ t('workspace.home.phase') }}</span>
        <h2>{{ t('workspace.home.title') }}</h2>
      </div>
      <p>{{ t('workspace.home.description') }}</p>
    </section>
  </section>
</template>

<style scoped>
.workspace-home {
  display: grid;
  gap: 16px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 1px;
  overflow: hidden;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-border-soft);
  box-shadow: var(--sw-shadow-soft);
}

.status-card {
  display: grid;
  align-content: space-between;
  gap: 22px;
  min-height: 132px;
  border: 0;
  border-radius: 0;
  background: var(--sw-panel-bg-strong);
  padding: 18px;
  transition:
    background-color 260ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 260ms cubic-bezier(0.16, 1, 0.3, 1);
}

.status-card:hover {
  background: var(--sw-panel-bg-hover);
  transform: translate3d(0, -2px, 0);
}

.status-card .n-icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  font-size: 22px;
}

.status-card span,
.home-band span {
  color: var(--sw-muted);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.status-card h2 {
  margin: 6px 0 0;
  color: var(--sw-text);
  font-size: 18px;
  font-weight: 700;
}

.home-band {
  display: grid;
  grid-template-columns: minmax(0, 280px) minmax(0, 1fr);
  gap: 22px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  box-shadow: var(--sw-shadow-soft);
  padding: 22px;
}

.home-band h2 {
  margin: 6px 0 0;
  color: var(--sw-text);
  font-size: 22px;
  font-weight: 700;
}

.home-band p {
  margin: 0;
  color: var(--sw-muted);
  font-size: 15px;
  line-height: 1.7;
}

@media (max-width: 1180px) {
  .status-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .status-grid,
  .home-band {
    grid-template-columns: 1fr;
  }

  .status-card {
    min-height: 104px;
  }
}
</style>

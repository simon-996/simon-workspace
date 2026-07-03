<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { NIcon } from 'naive-ui'
import { Book, Calendar, CircleCheck, Files, History, Template } from '@vicons/tabler'

const { t } = useI18n()

const modules = [
  { labelKey: 'workspace.home.modules.courses', valueKey: 'workspace.home.apiReady', icon: Book },
  { labelKey: 'workspace.home.modules.classes', valueKey: 'workspace.home.apiReady', icon: CircleCheck },
  { labelKey: 'workspace.home.modules.semesters', valueKey: 'workspace.home.apiReady', icon: Calendar },
  { labelKey: 'workspace.home.modules.templates', valueKey: 'workspace.home.apiReady', icon: Template },
  { labelKey: 'workspace.home.modules.files', valueKey: 'workspace.home.apiReady', icon: Files },
  { labelKey: 'workspace.home.modules.history', valueKey: 'workspace.home.apiReady', icon: History },
]
</script>

<template>
  <section class="workspace-home">
    <div class="status-grid">
      <article v-for="module in modules" :key="module.labelKey" class="status-card">
        <n-icon :component="module.icon" />
        <div>
          <span>{{ t(module.valueKey) }}</span>
          <h2>{{ t(module.labelKey) }}</h2>
        </div>
      </article>
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
  gap: 18px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.status-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
  min-height: 104px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px;
}

.status-card .n-icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: #e7f5fb;
  color: #1688b9;
  font-size: 23px;
}

.status-card span,
.home-band span {
  color: #668093;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.status-card h2 {
  margin: 6px 0 0;
  color: #14202b;
  font-size: 18px;
  font-weight: 800;
}

.home-band {
  display: grid;
  grid-template-columns: minmax(0, 280px) minmax(0, 1fr);
  gap: 22px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 22px;
}

.home-band h2 {
  margin: 6px 0 0;
  color: #14202b;
  font-size: 22px;
  font-weight: 800;
}

.home-band p {
  margin: 0;
  color: #536675;
  font-size: 15px;
  line-height: 1.7;
}

@media (max-width: 860px) {
  .status-grid {
    grid-template-columns: 1fr;
  }

  .home-band {
    grid-template-columns: 1fr;
  }
}
</style>

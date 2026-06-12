<script setup lang="ts">
import { NButton, NCard, NGrid, NGridItem, NLayout, NLayoutContent, NLayoutSider } from 'naive-ui'

const modules = [
  'Course Management',
  'Semester Management',
  'Template Library',
  'Lesson Generation',
  'Teaching Calendar',
  'File Center',
]
</script>

<template>
  <n-layout class="workspace-page" has-sider>
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="72" :width="240">
      <div class="side-brand">Simon</div>
      <nav class="side-nav">
        <router-link to="/workspace">Overview</router-link>
        <a>Courses</a>
        <a>Templates</a>
        <a>Files</a>
        <a>History</a>
      </nav>
    </n-layout-sider>

    <n-layout-content class="workspace-main">
      <header class="workspace-header">
        <div>
          <p class="section-label">Workspace</p>
          <h1>Teaching document cockpit</h1>
        </div>
        <n-button type="primary">Generate Lesson</n-button>
      </header>

      <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen">
        <n-grid-item v-for="module in modules" :key="module">
          <n-card :title="module" size="small">
            Ready for the first MVP workflow.
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-layout-content>
  </n-layout>
</template>

<style scoped>
.workspace-page {
  min-height: 100vh;
  background: #f8fafc;
}

.side-brand {
  padding: 24px;
  color: #0f172a;
  font-size: 20px;
  font-weight: 800;
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 14px;
}

.side-nav a {
  border-radius: 6px;
  color: #475569;
  padding: 10px 12px;
}

.side-nav a.router-link-active,
.side-nav a:hover {
  background: #eff6ff;
  color: #2563eb;
}

.workspace-main {
  padding: 32px;
}

.workspace-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.section-label {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: #0f172a;
  font-size: 30px;
  line-height: 1.2;
}

@media (max-width: 760px) {
  .workspace-page {
    display: block;
  }

  .workspace-main {
    padding: 20px;
  }

  .workspace-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

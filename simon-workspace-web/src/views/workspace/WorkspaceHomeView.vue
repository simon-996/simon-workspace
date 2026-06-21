<script setup lang="ts">
import { NIcon } from 'naive-ui'
import { Book, Calendar, CircleCheck, Files, History, Template } from '@vicons/tabler'

const modules = [
  { label: '课程管理', value: 'API Ready', icon: Book },
  { label: '班级管理', value: 'API Ready', icon: CircleCheck },
  { label: '学期周历', value: 'API Ready', icon: Calendar },
  { label: '模板字段', value: 'API Ready', icon: Template },
  { label: '文件中心', value: 'API Ready', icon: Files },
  { label: '生成记录', value: 'API Ready', icon: History },
]
</script>

<template>
  <section class="workspace-home">
    <div class="status-grid">
      <article v-for="module in modules" :key="module.label" class="status-card">
        <n-icon :component="module.icon" />
        <div>
          <span>{{ module.value }}</span>
          <h2>{{ module.label }}</h2>
        </div>
      </article>
    </div>

    <section class="home-band">
      <div>
        <span>Phase 03</span>
        <h2>基础资料与生成资源入口</h2>
      </div>
      <p>后端核心接口已经分模块落地，下一步进入课程、班级、学期、模板、文件和记录的前端维护页。</p>
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

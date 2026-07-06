<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import {
  Book,
  Calendar,
  CircleCheck,
  Cloud,
  FileText,
  Files,
  History,
  Notebook,
  Settings,
  Template,
  Users,
} from '@vicons/tabler'

import AppHeader from '../components/AppHeader.vue'
import { useAuthStore } from '../stores/auth'

const { t } = useI18n()
const route = useRoute()
const auth = useAuthStore()

const navItems = [
  { to: '/workspace', labelKey: 'workspace.nav.overview', icon: CircleCheck, permission: 'workspace:view' },
  { to: '/workspace/courses', labelKey: 'workspace.nav.courses', icon: Book, permission: 'course:manage' },
  { to: '/workspace/classes', labelKey: 'workspace.nav.classes', icon: FileText, permission: 'class:manage' },
  { to: '/workspace/semesters', labelKey: 'workspace.nav.semesters', icon: Calendar, permission: 'semester:manage' },
  { to: '/workspace/templates', labelKey: 'workspace.nav.templates', icon: Template, permission: 'template:manage' },
  { to: '/workspace/files', labelKey: 'workspace.nav.files', icon: Files, permission: 'file:manage' },
  { to: '/workspace/storage', labelKey: 'workspace.nav.storage', icon: Cloud, permission: 'file:manage' },
  { to: '/workspace/history', labelKey: 'workspace.nav.history', icon: History, permission: 'generation:history' },
  { to: '/workspace/blog', labelKey: 'workspace.nav.blog', icon: Notebook, permission: 'blog:category:manage' },
  { to: '/workspace/security', labelKey: 'workspace.nav.security', icon: Users, permission: 'user:manage' },
  { to: '/workspace/site', labelKey: 'workspace.nav.site', icon: Settings, permission: 'site:config' },
]

const visibleNavItems = computed(() => navItems.filter((item) => auth.hasPermission(item.permission)))
const pageTitle = computed(() => {
  const titleKey = typeof route.meta.titleKey === 'string' ? route.meta.titleKey : 'workspace.title'
  return t(titleKey)
})
</script>

<template>
  <main class="workspace-shell">
    <AppHeader />

    <section class="workspace-body">
      <aside class="workspace-sider" :aria-label="t('workspace.aria')">
        <nav class="side-nav">
          <RouterLink v-for="item in visibleNavItems" :key="item.to" :to="item.to" class="nav-link">
            <n-icon :component="item.icon" />
            <span>{{ t(item.labelKey) }}</span>
          </RouterLink>
        </nav>
      </aside>

      <section class="workspace-main">
        <header class="workspace-header">
          <div>
            <h1>{{ pageTitle }}</h1>
          </div>
        </header>

        <RouterView />
      </section>
    </section>

    <nav class="mobile-tabs" :aria-label="t('workspace.mobileAria')">
      <RouterLink v-for="item in visibleNavItems" :key="item.to" :to="item.to">
        <n-icon :component="item.icon" />
        <span>{{ t(item.labelKey) }}</span>
      </RouterLink>
    </nav>
  </main>
</template>

<style scoped>
.workspace-shell {
  min-height: 100dvh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.workspace-body {
  display: grid;
  grid-template-columns: 236px minmax(0, 1fr);
  min-height: calc(100dvh - 74px);
}

.workspace-sider {
  position: sticky;
  top: 74px;
  align-self: start;
  display: grid;
  min-height: calc(100dvh - 74px);
  border-right: 1px solid var(--sw-border);
  background: var(--sw-panel-bg);
  color: var(--sw-text);
  backdrop-filter: blur(24px);
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 18px 10px;
}

.nav-link {
  display: grid;
  grid-template-columns: 24px 1fr;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  border-radius: 8px;
  padding: 0 12px;
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 700;
  transition:
    background-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 220ms cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-link .n-icon {
  font-size: 20px;
}

.nav-link.router-link-exact-active,
.nav-link:hover {
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  transform: translate3d(2px, 0, 0);
}

.workspace-main {
  min-width: 0;
  padding: 26px clamp(18px, 3vw, 34px) 34px;
}

.workspace-header {
  position: sticky;
  top: 86px;
  z-index: 8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 64px;
  margin: -10px 0 22px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  box-shadow: var(--sw-shadow-soft);
  padding: 12px 14px 12px 18px;
  backdrop-filter: blur(20px);
}

h1 {
  margin: 0;
  color: var(--sw-text);
  font-size: clamp(22px, 2.2vw, 30px);
  font-weight: 700;
  line-height: 1.2;
}

.mobile-tabs {
  display: none;
}

@media (max-width: 860px) {
  .workspace-shell {
    padding-bottom: 70px;
  }

  .workspace-body {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .workspace-sider {
    display: none;
  }

  .workspace-main {
    padding: 14px 12px 22px;
  }

  .workspace-header {
    align-items: flex-start;
    flex-direction: column;
    top: 76px;
    margin-top: 0;
    padding: 14px;
  }

  h1 {
    font-size: 24px;
  }

  .mobile-tabs {
    position: fixed;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 20;
    display: flex;
    min-height: 64px;
    border-top: 1px solid var(--sw-border);
    background: var(--sw-panel-bg-strong);
    backdrop-filter: blur(16px);
    overflow-x: auto;
  }

  .mobile-tabs a {
    display: grid;
    flex: 0 0 58px;
    place-items: center;
    align-content: center;
    gap: 3px;
    min-width: 0;
    color: var(--sw-muted);
    font-size: 11px;
    font-weight: 700;
  }

  .mobile-tabs .n-icon {
    font-size: 20px;
  }

  .mobile-tabs a.router-link-exact-active {
    color: var(--sw-accent);
  }
}
</style>
